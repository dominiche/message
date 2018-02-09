package dominic.mq.consumer.rabbit;

import dominic.message.rabbit.consumer.RabbitMessageConsumer;
import dominic.message.rabbit.properties.ConsumeProperties;
import dominic.message.rabbit.properties.RabbitMessageConsumerProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RabbitTestConsumer implements RabbitMessageConsumer<String> {

    @Override
    public ConsumeProperties consumerProperties() {
        return ConsumeProperties.durable("message.rabbit.test", "message.rabbit", "message.rabbit.test", false);
    }

    @Override
    public void consume(String message, RabbitMessageConsumerProperties properties) throws IOException {
        System.out.println("消费rabbit message:===============================");
        System.out.println(message);
    }
}
