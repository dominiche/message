package dominic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created by Administrator:herongxing on 2017/12/13 10:47.
 */
@Configuration
@ComponentScan("dominic.message.kafka")
public class KafkaMessageConfig {
    /**
     * 一些配置是有"."作为两个单词之间的间隔，yml不支持"."间隔，请用"-"作为代替
     * 如:"bootstrap.servers" -> "bootstrap-servers"
     */

    @Bean
    @ConfigurationProperties(prefix="message.kafka.producer")
    public Properties kafkaProducerProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix="message.kafka.consumer")
    public Properties kafkaConsumerProperties() {
        return new Properties();
    }
}
