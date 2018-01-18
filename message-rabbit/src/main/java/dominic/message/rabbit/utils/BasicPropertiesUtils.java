package dominic.message.rabbit.utils;

import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * Created by Administrator:herongxing on 2018/1/18 15:41.
 */
public class BasicPropertiesUtils {

    public static AMQP.BasicProperties addHeadersInNew(AMQP.BasicProperties props, String key, Object value) {
        Map<String, Object> headers = Maps.newHashMap();
        if (MapUtils.isNotEmpty(props.getHeaders())) {
            headers.putAll(props.getHeaders());
        }
        headers.put(key, value);
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
                .contentType(props.getContentType())
                .contentEncoding(props.getContentEncoding())
                .headers(headers)
                .deliveryMode(props.getDeliveryMode())
                .priority(props.getPriority())
                .correlationId(props.getCorrelationId())
                .replyTo(props.getReplyTo())
                .expiration(props.getExpiration())
                .messageId(props.getMessageId())
                .timestamp(props.getTimestamp())
                .type(props.getType())
                .userId(props.getUserId())
                .appId(props.getAppId())
                .clusterId(props.getClusterId());
        props = builder.build();
        return props;
    }
}
