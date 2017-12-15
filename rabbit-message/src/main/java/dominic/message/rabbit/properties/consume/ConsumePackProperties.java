package dominic.message.rabbit.properties.consume;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Created by Administrator:herongxing on 2017/12/14 11:54.
 */
@Builder
@Data
public class ConsumePackProperties {
    /**
     * true if the server should consider messages
     */
    private boolean autoAck;
    /**
     * a client-generated consumer tag to establish context
     */
    private String consumerTag;
    /**
     * True if the server should not deliver to this consumer
     */
    private boolean noLocal;
    /**
     * true if this is an exclusive consumer
     */
    private boolean exclusive;
    /**
     * a set of arguments for the consume
     */
    private Map<String, Object> arguments;

    public static ConsumePackProperties basic() {
        return ConsumePackProperties.builder().autoAck(true).consumerTag("").noLocal(false).exclusive(false).build();
    }

    public static ConsumePackProperties basic(boolean autoAck) {
        return ConsumePackProperties.builder().autoAck(autoAck).consumerTag("").noLocal(false).exclusive(false).build();
    }
}
