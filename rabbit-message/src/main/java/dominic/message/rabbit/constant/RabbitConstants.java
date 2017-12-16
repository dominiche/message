package dominic.message.rabbit.constant;

import dominic.message.rabbit.consumer.RabbitMessageConsumer;

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
     * consumer interface class name
     */
    String CONSUMER_INTERFACE_NAME = RabbitMessageConsumer.class.getName();
}
