package dominic.message.rabbit.constant;

import dominic.message.rabbit.properties.RabbitMessageConsumerProperties;

/**
 * Created by Administrator:herongxing on 2017/12/14 14:22.
 */
public interface RabbitConstants {
    /**
     * default exchange
     */
    String DEFAULT_EXCHANGE = "";
    /**
     * default routingKey
     */
    String DEFAULT_ROUTING_KEY = "";

    /**
     * default encoding
     */
    String DEFAULT_ENCODING = "UTF-8";

    /**
     * consume method name
     */
    String CONSUME_METHOD_NAME = "consume";

    /**
     * consumerPropertiesClass
     */
    Class<RabbitMessageConsumerProperties> CONSUMER_PROPERTIES_CLASS = RabbitMessageConsumerProperties.class;

    /**
     * 消费消息时出现错误时，重新入队列的最大次数-key
     */
    String ERROR_RETRY_TIMES = "ERROR_RETRY_TIMES";
    /**
     * 消费消息时出现错误时，重新入队列的最大次数-key
     */
    int ERROR_RETRY_TIMES_MAX = 10;
    /**
     * 消费消息时出现错误时，无限次数重试
     */
    int ERROR_RETRY_TIMES_LIMITLESS = -1;
}
