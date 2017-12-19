package dominic.message.rabbit.properties.consume;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Created by Administrator:herongxing on 2017/12/14 14:14.
 */
@Data
@Builder
public class QueueDeclareProperties {
    /**
     * true if we are declaring a durable queue (the queue will survive a server restart)
     */
    private boolean durable;
    /**
     * true if we are declaring an exclusive queue (restricted to this connection)
     */
    private boolean exclusive;
    /**
     * true if we are declaring an autodelete queue (server will delete it when no longer in use)
     */
    private boolean autoDelete;
    /**
     * other properties (construction arguments) for the queue
     */
    private Map<String, Object> arguments;

    public static QueueDeclareProperties basic() {
        return QueueDeclareProperties.builder().durable(false)
                .exclusive(false).autoDelete(true).build();
    }
    public static QueueDeclareProperties durable() {
        return QueueDeclareProperties.builder().durable(true)
                .exclusive(false).autoDelete(false).build();
    }
}
