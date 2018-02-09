package dominic.config.mq;

import com.rabbitmq.client.Connection;
import dominic.message.rabbit.properties.config.ConnectionFactoryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
