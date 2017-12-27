package dominic.message.kafka.producer;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Created by Administrator:herongxing on 2017/12/27 16:38.
 */
@Slf4j
@Service
public class Producer {

    private Properties producerProperties;

    private static org.apache.kafka.clients.producer.Producer<String, String> kafkaProducer;

    public Producer(Properties kafkaProducerProperties) {
        try {
            producerProperties = new Properties();
            kafkaProducerProperties.entrySet().forEach(entry -> {
                String key = entry.getKey().toString();
                key = key.replace("-", ".");
                producerProperties.put(key, entry.getValue().toString());
            });
            Object keySerializer = producerProperties.getOrDefault(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            Object valueSerializer = producerProperties.getOrDefault(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
            kafkaProducer = new KafkaProducer<>(producerProperties);
        } catch (Exception e) {
            log.error("配置kafka producer出错：", e);
            throw new RuntimeException("配置kafka producer出错", e);
        }
    }

    public static void send(String topic, Object message) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic is empty!");
        Preconditions.checkNotNull(message, "message can not be null!");

        String messageJson = JSON.toJSONString(message);
        log.debug("Producer开始发送消息, message type:{}, message：{}", message.getClass().getName(), messageJson);
        kafkaProducer.send(new ProducerRecord<>(topic, messageJson));
        log.debug("kafka Producer发送消息-完成");
    }
}
