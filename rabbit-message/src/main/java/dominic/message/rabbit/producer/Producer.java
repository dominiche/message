package dominic.message.rabbit.producer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import dominic.message.rabbit.constant.RabbitConstants;
import dominic.message.rabbit.properties.ProducerProperties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Administrator:herongxing on 2017/12/13 17:57.
 */
@Slf4j
@Service("rabbitProducer")
public class Producer {

    private static Connection connection;

    private static Channel defaultChannel;


    public Producer(Connection rabbitMessageConnection) throws IOException {
        connection = rabbitMessageConnection;
        defaultChannel = connection.createChannel();
    }

    public static Channel newChannel() {
        try {
            return connection.createChannel();
        } catch (IOException e) {
            log.error("newChannel exception: ", e);
            throw new RuntimeException(e);
        }
    }

    public static void send(String routingKey, Object object) {
        basicPublish(defaultChannel, RabbitConstants.DEFAULT_EXCHANGE, routingKey, false, false, null, object);
    }

    public static void send(String exchange, String routingKey, Object object) {
        basicPublish(defaultChannel, exchange, routingKey, false, false, null, object);
    }

    public static void send(String exchange, String routingKey, AMQP.BasicProperties basicProperties, Object object) {
        basicPublish(defaultChannel, exchange, routingKey, false, false, basicProperties, object);
    }

    public static void send(String exchange, String routingKey, ProducerProperties properties, Object object) {
        if (null == properties) {
            properties = ProducerProperties.builder().build();
        }
        basicPublish(defaultChannel, exchange, routingKey, properties.isMandatory(), properties.isImmediate(), properties.getBasicProperties(), object);
    }

    private static void basicPublish(Channel channel, String exchange, String routingKey,
                                     boolean mandatory, boolean immediate, AMQP.BasicProperties props, @NonNull Object object) {
        if (null == exchange) {
            exchange = RabbitConstants.DEFAULT_EXCHANGE;
        }
        if (null == routingKey) {
            routingKey = RabbitConstants.DEFAULT_ROUTING_KEY;
        }
        try {
            log.debug("Producer发送消息, {}, message type:{}, message：{}", DateTime.now().toString("yyyy-MM-dd HH:mm:ss.sss"),
                    object.getClass().getName(), JSON.toJSONString(object));
            byte[] body = JSON.toJSONString(object).getBytes(RabbitConstants.DEFAULT_ENCODING);
            channel.basicPublish(exchange, routingKey, mandatory, immediate, props, body);
        } catch (IOException e) {
            log.error("send message exception!exchange={}, routingKey={}, mandatory={}, BasicProperties={}, message:{};",
                    exchange, routingKey, mandatory, props, JSON.toJSONString(object), e);
            throw new RuntimeException(e);
        }
    }
}
