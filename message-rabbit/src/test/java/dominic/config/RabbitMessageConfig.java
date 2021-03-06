package dominic.config;

import com.rabbitmq.client.Connection;
import dominic.message.rabbit.properties.config.ConnectionFactoryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator:herongxing on 2017/12/13 10:47.
 */
@Configuration
@ComponentScan("dominic.message.rabbit")
public class RabbitMessageConfig {

    @Bean
    @ConfigurationProperties(prefix="spring.rabbitmq")
    public ConnectionFactoryConfig rabbitMessageConnectionFactoryConfig() {
        return new ConnectionFactoryConfig();
    }

    @Bean
    public Connection rabbitMessageConnection(ConnectionFactoryConfig rabbitMessageConnectionFactoryConfig) {
        return ConnectionFactoryConfig.newConnection(rabbitMessageConnectionFactoryConfig);
    }
}
