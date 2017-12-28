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
}
