package dominic.mq.consumer.kafka;

import com.google.common.collect.Lists;
import dominic.dto.InvoiceConsumerDTO;
import dominic.message.kafka.consumer.KafkaMessageConsumer;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class KafkaInvoiceConsumer implements KafkaMessageConsumer<InvoiceConsumerDTO> {

    /**
     * 消息消费topics(可以消费多个topic)
     */
    @Override
    public Collection<String> topics() {
        return Lists.newArrayList("message.kafka.invoice");
    }

    /**
     * 消息到来时执行该方法
     *
     * @param message
     */
    @Override
    public void consume(InvoiceConsumerDTO message) {
        System.out.println("消费message:===============================");
        System.out.println(String.format("invoiceId=%d", message.getInvoiceId()));
        System.out.println(message);
    }
}
