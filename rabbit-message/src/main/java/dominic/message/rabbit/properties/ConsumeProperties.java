package dominic.message.rabbit.properties;

import com.google.common.collect.Lists;
import dominic.message.rabbit.constant.RabbitConstants;
import dominic.message.rabbit.properties.consume.ConsumeBasicQos;
import dominic.message.rabbit.properties.consume.ConsumePackProperties;
import dominic.message.rabbit.properties.consume.ExchangeDeclareProperties;
import dominic.message.rabbit.properties.consume.QueueDeclareProperties;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by Administrator:herongxing on 2017/12/14 11:15.
 */
@Data
@Builder
public class ConsumeProperties {
    /**
     * consume exchange
     */
    private String exchange;
    /**
     * consume routingKey
     */
    private List<String> routingKeys;
    /**
     * consume queue
     * 注意：
     * （1）多个routingKey时queue不能空
     * （2）单个routingKey时，queue为空，则默认值为exchangeName+"_"+routingKey
     */
    private String queue;

    /**
     * basic consume pack properties, arguments for channel.basicConsume(...)
     */
    private ConsumePackProperties consumePackProperties;

    /**
     * declare a exchange with the properties, arguments for channel.exchangeDeclare(...)
     */
    private ExchangeDeclareProperties exchangeProperties;
    /**
     * declare a queue with the properties, arguments for channel.queueDeclare(...)
     */
    private QueueDeclareProperties queueProperties;
    /**
     * Request specific "quality of service" settings, arguments for channel.basicQos(...)
     */
    private ConsumeBasicQos basicQos;

    //queueBind properties?


    public static ConsumeProperties basic(String routingKey) {
        return basic(RabbitConstants.DEFAULT_EXCHANGE, routingKey);
    }

    public static ConsumeProperties basic(String exchange, String routingKey) {
        if (null == routingKey) {
            routingKey = RabbitConstants.DEFAULT_ROUTING_KEY;
        }
        return basic(null, exchange, Lists.newArrayList(routingKey));
    }

    public static ConsumeProperties basic(List<String> routingKeys) {
        return basic(null, routingKeys);
    }
    public static ConsumeProperties basic(String queue, List<String> routingKeys) {
        return basic(queue, RabbitConstants.DEFAULT_EXCHANGE, routingKeys);
    }

    public static ConsumeProperties basic(String queue, String exchange, List<String> routingKeys) {
        return basic(queue, exchange, routingKeys, true);
    }

    public static ConsumeProperties basic(String exchange, String routingKey, boolean autoAck) {
        return basic(null, exchange, Lists.newArrayList(routingKey), autoAck);
    }
    public static ConsumeProperties basic(String queue, String exchange, String routingKey, boolean autoAck) {
        return basic(queue, exchange, Lists.newArrayList(routingKey), autoAck);
    }

    public static ConsumeProperties basic(String queue, String exchange, List<String> routingKeys, boolean autoAck) {
        if (null == exchange) {
            exchange = RabbitConstants.DEFAULT_EXCHANGE;
        }
        if (CollectionUtils.isEmpty(routingKeys)) {
            routingKeys = Lists.newArrayList(RabbitConstants.DEFAULT_ROUTING_KEY);
        }
        return ConsumeProperties.builder()
                .queue(queue)
                .exchange(exchange)
                .routingKeys(routingKeys)
                .consumePackProperties(ConsumePackProperties.basic(autoAck))
                .exchangeProperties(ExchangeDeclareProperties.basic())
                .queueProperties(QueueDeclareProperties.basic())
                .build();
    }
}
