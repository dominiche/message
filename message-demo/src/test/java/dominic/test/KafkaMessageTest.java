package dominic.test;

import com.google.common.collect.Lists;
import dominic.dto.InvoiceConsumerDTO;
import dominic.dto.InvoiceConsumerSubItemDTO;
import dominic.message.kafka.producer.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaMessageTest {

    @Test
    public void send() {
        String message = "test kafka message ========================";
        for (int i=1; i<100; ++i)
        Producer.send("message.kafka.test", message);
    }

    @Test
    public void send2() {
        InvoiceConsumerSubItemDTO subItemDTO1 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-123").itemCash(BigDecimal.valueOf(100.12)).build();
        InvoiceConsumerSubItemDTO subItemDTO2 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-456").itemCash(BigDecimal.valueOf(12.11)).build();
        InvoiceConsumerDTO invoiceConsumerDTO = InvoiceConsumerDTO.builder()
                .invoiceId(123L)
                .subItemList(Lists.newArrayList(subItemDTO1,subItemDTO2))
                .build();
//        for (int i=1; i<100; ++i)
        Producer.send("message.kafka.invoice", invoiceConsumerDTO);
    }
}
