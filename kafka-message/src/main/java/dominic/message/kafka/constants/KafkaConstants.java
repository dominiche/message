package dominic.message.kafka.constants;

/**
 * Created by Administrator:herongxing on 2017-12-28 14:22:12.
 */
public interface KafkaConstants {

    /**
     * producer:default keySerializer
     */
    String DEFAULT_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    /**
     * producer:default valueSerializer
     */
    String DEFAULT_VALUE_SERIALIZER = DEFAULT_KEY_SERIALIZER;

    /**
     * consumer:default keyDeserializer
     */
    String DEFAULT_KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    /**
     * consumer:default valueDeserializer
     */
    String DEFAULT_VALUE_DESERIALIZER = DEFAULT_KEY_DESERIALIZER;


    /**
     * consume method name
     */
    String CONSUME_METHOD_NAME = "consume";
}
