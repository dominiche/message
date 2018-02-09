package dominic.test;

import com.google.common.collect.Lists;
import dominic.dto.InvoiceConsumerDTO;
import dominic.dto.InvoiceConsumerSubItemDTO;
import dominic.message.rabbit.producer.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMessageTest {

    @Test
    public void send() {
        String message = "test rabbit message ========================";
        for (int i=1; i<100; ++i)
        Producer.send("message.rabbit", "message.rabbit.test", message);
    }

    @Test
    public void send2() {
        InvoiceConsumerSubItemDTO subItemDTO1 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-789").itemCash(BigDecimal.valueOf(100.12)).build();
        InvoiceConsumerSubItemDTO subItemDTO2 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-321").itemCash(BigDecimal.valueOf(12.11)).build();
        InvoiceConsumerDTO invoiceConsumerDTO = InvoiceConsumerDTO.builder()
                .invoiceId(789L)
                .subItemList(Lists.newArrayList(subItemDTO1,subItemDTO2))
                .build();
//        for (int i=1; i<100; ++i)
        Producer.send("message.rabbit", "message.rabbit.invoice", invoiceConsumerDTO);
    }
}
