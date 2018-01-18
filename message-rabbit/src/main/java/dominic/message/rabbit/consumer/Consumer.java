package dominic.message.rabbit.consumer;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import dominic.message.rabbit.constant.RabbitConstants;
import dominic.message.rabbit.properties.ConsumeProperties;
import dominic.message.rabbit.properties.RabbitMessageConsumerProperties;
import dominic.message.rabbit.properties.consume.ConsumeBasicQos;
import dominic.message.rabbit.properties.consume.ConsumePackProperties;
import dominic.message.rabbit.properties.consume.ExchangeDeclareProperties;
import dominic.message.rabbit.properties.consume.QueueDeclareProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * Created by Administrator:herongxing on 2017/12/13 21:55.
 */
@Slf4j
@Service("rabbitConsumer")
public class Consumer {

    private static Connection connection;

    private List<Channel> channelList = new Vector<>();

    @Autowired(required = false)
    private List<RabbitMessageConsumer> consumers = Lists.newArrayList();


    public Consumer(Connection rabbitMessageConnection) throws IOException {
        connection = rabbitMessageConnection;
    }

    public static Channel newChannel() {
        try {
            return connection.createChannel();
        } catch (IOException e) {
            log.error("newChannel exception: ", e);
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeResource));

        if (CollectionUtils.isEmpty(consumers)) {
            log.warn("rabbitMessageConsumers is zero!");
            return;
        }
        assembleConsumer();
    }

    private void closeResource() {
        log.debug("关闭rabbit连接..........");

        channelList.parallelStream().forEach(channel -> {
            try {
                channel.close();
            } catch (Exception e) {
                log.error("channel关闭异常：{}", channel, e);
            }
        });

        try {
            connection.close();
        } catch (IOException e) {
            log.error("rabbit connection关闭异常", e);
        }
    }

    private void assembleConsumer() {
        log.debug("开始装配 rabbit-message consumer..........");
        consumers.forEach(consumer -> {
            Class<? extends RabbitMessageConsumer> aClass = consumer.getClass();
            java.lang.reflect.Method[] declaredMethods = aClass.getDeclaredMethods();
            Type type = null;
            for (int i=declaredMethods.length-1; i>=0; --i) {
                java.lang.reflect.Method method = declaredMethods[i];
                Parameter[] parameters = method.getParameters();
                if (RabbitConstants.CONSUME_METHOD_NAME.equals(method.getName()) && 2 == parameters.length
                        && parameters[1].getType() == RabbitConstants.CONSUMER_PROPERTIES_CLASS) {
                    type = method.getGenericParameterTypes()[0];
                    if (!Objects.equals(type.getTypeName(), "java.lang.Object")) {
                        break;
                    }
                }
            }

            if (type == null) {
                throw new RuntimeException(String.format("没有拿到消费者:%s的consume方法", aClass.getName()));
            }
            Gson gson = new Gson();


            ConsumeProperties properties = consumer.consumerProperties();
            String exchange = properties.getExchange();
            if (null == exchange) {
                exchange = RabbitConstants.DEFAULT_EXCHANGE;
            }
            List<String> routingKeys = properties.getRoutingKeys();
            if (CollectionUtils.isEmpty(routingKeys))
                routingKeys = Lists.newArrayList(RabbitConstants.DEFAULT_ROUTING_KEY);
            ConsumePackProperties consumePackProperties = properties.getConsumePackProperties();
            if (null == consumePackProperties) consumePackProperties = ConsumePackProperties.basic();
            ExchangeDeclareProperties exchangeProperties = properties.getExchangeProperties();
            if (null == exchangeProperties) exchangeProperties = ExchangeDeclareProperties.basic();
            QueueDeclareProperties queueProperties = properties.getQueueProperties();
            if (null == queueProperties) queueProperties = QueueDeclareProperties.basic();
            ConsumeBasicQos basicQos = properties.getBasicQos();

            String queue = properties.getQueue();
            if (null == queue && routingKeys.size() > 1) {
                String str = String.format("消费者%s有多个routingKey, 请给请给消费队列起个名字（多个routingKey时queue不能空）", consumer.getClass().getName());
                throw new RuntimeException(str);
            }
            if (null == queue) {
                queue = exchange + "_" + routingKeys.get(0);//queue为空时,默认值为exchangeName+"_"+routingKey
                if (RabbitConstants.DEFAULT_EXCHANGE.equals(exchange)) {
                    queue = routingKeys.get(0);
                }
            }

            Channel channel = newChannel();
            channelList.add(channel);
            try {
                //declare exchange
                if (!RabbitConstants.DEFAULT_EXCHANGE.equals(exchange)) {
                    channel.exchangeDeclare(exchange, exchangeProperties.getType(), exchangeProperties.isDurable(),
                            exchangeProperties.isAutoDelete(), exchangeProperties.isInternal(), exchangeProperties.getArguments());
                }
                //declare queue
                if (!queue.startsWith("amq.")) {
                    channel.queueDeclare(queue, queueProperties.isDurable(),
                            queueProperties.isExclusive(), queueProperties.isAutoDelete(), queueProperties.getArguments());
                }
                //binding queue: exchange, routingKeys
                if (!RabbitConstants.DEFAULT_EXCHANGE.equals(exchange)) {
                    for (String routingKey : routingKeys) {
                        channel.queueBind(queue, exchange, routingKey);
                    }
                }
                //setting Qos
                if (null != basicQos) {
                    channel.basicQos(basicQos.getPrefetchSize(), basicQos.getPrefetchCount(), basicQos.isGlobal());
                }
                //assemble consumer
                boolean isNeedAck = !consumePackProperties.isAutoAck();
                String consumerClassName = consumer.getClass().getName();
                Type finalType = type;
                String typeName = finalType.getTypeName();
                DefaultConsumer paramConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String messageJson = new String(body, RabbitConstants.DEFAULT_ENCODING);
                        try {
                            log.debug("consumer:{}接收到消息, message：{}", consumerClassName, messageJson);
                            Object message = messageJson;
                            if (!Objects.equals(typeName, "java.lang.Object") && !Objects.equals(typeName, "java.lang.String")) {
                                message = gson.fromJson(messageJson, finalType);
                            }
                            RabbitMessageConsumerProperties rabbitMessageConsumerProperties = RabbitMessageConsumerProperties.builder()
                                    .channel(channel).consumerTag(consumerTag).envelope(envelope).properties(properties).build();
                            consumer.consume(message, rabbitMessageConsumerProperties);
                            if (isNeedAck) {
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            }
                        } catch (Exception e) {
                            log.error("consumer:{}消费消息时发生异常, message:{}", consumerClassName, messageJson, e);
                            if (isNeedAck) {
                                log.error("consumer:{}消费异常，消息已经重新入队列，message:{}", consumerClassName, messageJson, e);
                                channel.basicReject(envelope.getDeliveryTag(), true);
                            }
                        }
                    }
                };
                //channel.basicConsume(...)
                channel.basicConsume(queue, /*consumePackProperties.isAutoAck()*/false, consumePackProperties.getConsumerTag(),
                        consumePackProperties.isNoLocal(), consumePackProperties.isExclusive(), consumePackProperties.getArguments(),
                        paramConsumer);
            } catch (IOException e) {
                log.error("装配rabbitmq消费者出错：", e);
                throw new RuntimeException(e);
            }
        });
    }
}
