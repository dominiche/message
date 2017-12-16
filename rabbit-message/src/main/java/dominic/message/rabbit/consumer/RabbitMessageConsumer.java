package dominic.message.rabbit.consumer;

import dominic.message.rabbit.properties.ConsumeProperties;
import dominic.message.rabbit.properties.RabbitMessageConsumerProperties;

import java.io.IOException;

/**
 * Created by Administrator:herongxing on 2017/12/14 10:48.
 */
public interface RabbitMessageConsumer<T> {

    /**
     * 消息消费配置
     */
    ConsumeProperties consumerProperties();

    /**
     * 消息到来时执行该方法
     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
     * @param properties packaging data for consume properties
     * @param message the message object
     * @throws IOException if the consumer encounters an I/O error while processing the message
     */
    void consume(T message, RabbitMessageConsumerProperties properties) throws IOException;
}
