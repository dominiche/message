package dominic.message.rabbit.properties.consume;

import com.rabbitmq.client.BuiltinExchangeType;
import dominic.message.rabbit.constant.ExchangeType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Created by Administrator:herongxing on 2017/12/14 13:53.
 */
@Data
@Builder
public class ExchangeDeclareProperties {
    /**
     * the exchange type
     */
    private BuiltinExchangeType type;
    /**
     * true if we are declaring a durable exchange (the exchange will survive a server restart)
     */
    private boolean durable;
    /**
     * true if the server should delete the exchange when it is no longer in use
     */
    private boolean autoDelete;
    /**
     * true if the exchange is internal, i.e. can't be directly published to by a client.
     */
    private boolean internal;
    /**
     * other properties (construction arguments) for the exchange
     */
    private Map<String, Object> arguments;

    public static ExchangeDeclareProperties basic() {
        return ExchangeDeclareProperties.builder().type(BuiltinExchangeType.DIRECT)
                .durable(false).autoDelete(true).internal(false).build();
    }

    public static ExchangeDeclareProperties durable() {
        return ExchangeDeclareProperties.builder().type(BuiltinExchangeType.DIRECT)
                .durable(true).autoDelete(false).internal(false).build();
    }
}
