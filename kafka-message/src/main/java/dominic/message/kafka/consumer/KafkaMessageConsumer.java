package dominic.message.kafka.consumer;

import java.util.Collection;

/**
 * Created by Administrator:herongxing on 2017-12-28 14:09:39.
 */
public interface KafkaMessageConsumer<T> {

    /**
     * 消息消费topics(可以消费多个topic)
     */
    Collection<String> topics();

    /**
     * 消息到来时执行该方法
     */
    void consume(T message);
}
