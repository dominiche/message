package dominic.message.rabbit.consumer;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
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
import java.util.List;

/**
 * Created by Administrator:herongxing on 2017/12/13 21:55.
 */
@Slf4j
@Service("rabbitConsumer")
public class Consumer {

    private static Connection connection;

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
        if (CollectionUtils.isEmpty(consumers)) {
            log.warn("rabbitMessageConsumers is zero!");
            return;
        }

        consumers.forEach(consumer -> {
            ConsumeProperties properties = consumer.consumerProperties();
            String exchange = properties.getExchange();
            if (null == exchange) {
                exchange = RabbitConstants.DEFAULT_EXCHANGE;
            }
            List<String> routingKeys = properties.getRoutingKeys();
            if (CollectionUtils.isEmpty(routingKeys)) routingKeys = Lists.newArrayList(RabbitConstants.DEFAULT_ROUTING_KEY);
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
                queue = exchange+ "_" + routingKeys.get(0);//queue为空时,默认值为exchangeName+"_"+routingKey，是否重复交给rabbit服务器判断
                if (RabbitConstants.DEFAULT_EXCHANGE.equals(exchange)) {
                    queue = routingKeys.get(0);
                }
            }

            Channel channel = newChannel(); //todo close channel
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
                DefaultConsumer paramConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String messageJson = new String(body, RabbitConstants.DEFAULT_ENCODING);
                        try {
                            //todo time log
                            //todo assemble RabbitMessageConsumer to paramConsumer
                            Object message = messageJson;
                            Class clazz = consumer.getClazz();//Class<T>???
                            if (!"java.lang.String".equals(clazz.getName())) {
                                message = JSON.parseObject(messageJson, clazz);//test???
                            }
                            RabbitMessageConsumerProperties rabbitMessageConsumerProperties = RabbitMessageConsumerProperties.builder()
                                    .channel(channel).consumerTag(consumerTag).envelope(envelope).properties(properties).build();
                            consumer.consume(message, rabbitMessageConsumerProperties);
                        } catch (Exception e) {
                            log.error("consumer:{}消费消息时发生异常, message:{}", consumer.getClass().getName(), messageJson, e);
                        }
                    }
                };
                //channel.basicConsume(...)
                channel.basicConsume(queue, consumePackProperties.isAutoAck(), consumePackProperties.getConsumerTag(),
                        consumePackProperties.isNoLocal(), consumePackProperties.isExclusive(), consumePackProperties.getArguments(),
                        paramConsumer);
            } catch (IOException e) {
                log.error("装配rabbitmq消费者出错：", e);
                throw new RuntimeException(e);
            }
        });
    }
}
