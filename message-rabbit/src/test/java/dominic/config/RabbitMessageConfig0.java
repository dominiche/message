package dominic.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator:herongxing on 2017/12/13 10:47.
 */
@Configuration
@ComponentScan("dominic.message.rabbit")
public class RabbitMessageConfig0 {

    @Bean
    @ConfigurationProperties(prefix="spring.rabbitmq")
    public ConnectionFactory rabbitMessageConnectionFactory() {
        return new ConnectionFactory();
    }

    @Bean
    public Connection rabbitMessageConnection(ConnectionFactory rabbitMessageConnectionFactory) throws IOException, TimeoutException {
        return rabbitMessageConnectionFactory.newConnection();
    }
}
