package dominic.mq.consumer.rabbit;

import dominic.dto.InvoiceConsumerDTO;
import dominic.message.rabbit.consumer.RabbitMessageConsumer;
import dominic.message.rabbit.properties.ConsumeProperties;
import dominic.message.rabbit.properties.RabbitMessageConsumerProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RabbitInvoiceConsumer implements RabbitMessageConsumer<InvoiceConsumerDTO> {

    @Override
    public ConsumeProperties consumerProperties() {
        return ConsumeProperties.durable("message.rabbit.invoice", "message.rabbit", "message.rabbit.invoice", false);
    }

    @Override
    public void consume(InvoiceConsumerDTO message, RabbitMessageConsumerProperties properties) throws IOException {
        System.out.println("消费rabbit message:===============================");
        System.out.println(String.format("invoiceId=%d", message.getInvoiceId()));
        System.out.println(message);
    }
}
