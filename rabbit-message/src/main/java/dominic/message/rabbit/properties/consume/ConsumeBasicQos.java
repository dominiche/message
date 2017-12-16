package dominic.message.rabbit.properties.consume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request specific "quality of service" settings.
 *
 * These settings impose limits on the amount of data the server
 * will deliver to consumers before requiring acknowledgements.
 * Thus they provide a means of consumer-initiated flow control.
 */
@Data
@Builder
public class ConsumeBasicQos {
    /**
     * maximum amount of content (measured in
     * octets) that the server will deliver, 0 if unlimited
     */
    private int prefetchSize;
    /**
     * maximum number of messages that the server
     * will deliver, 0 if unlimited
     */
    private int prefetchCount;
    /**
     * true if the settings should be applied to the
     * entire channel rather than each consumer
     */
    private boolean global;


    public static ConsumeBasicQos basicQos(int prefetchCount) {
        return ConsumeBasicQos.builder().prefetchSize(0).prefetchCount(prefetchCount).global(false).build();
    }
}
