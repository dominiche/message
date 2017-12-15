package dominic.message.rabbit.properties;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Administrator:herongxing on 2017/12/14 11:04.
 */
@Data
@Builder
public class RabbitMessageConsumerProperties {
    /**
     * channel for this consumer
     */
    private Channel channel;
    /**
     * the <i>consumer tag</i> associated with the consumer
     */
    private String consumerTag;
    /**
     * packaging data for the message
     */
    private Envelope envelope;
    /**
     * content header data for the message
     */
    private AMQP.BasicProperties properties;
}
