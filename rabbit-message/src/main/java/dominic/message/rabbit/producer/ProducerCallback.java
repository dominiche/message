package dominic.message.rabbit.producer;

import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.ReturnCallback;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Administrator:herongxing on 2017/12/13 21:37.
 */
@Data
@Builder
public class ProducerCallback {
    /**
     * 确认消息到达exchange后返回ack
     */
    private ConfirmCallback ackCallback;
    /**
     * 消息没有到达指定exchange，broker返回nack
     */
    private ConfirmCallback nackCallback;
    /**
     * mandatory为true时，消息到达exchange后无法把消息路由到符合条件的queue时，broker直接把消息返还给生产者
     */
    private ReturnCallback returnCallback;
}
