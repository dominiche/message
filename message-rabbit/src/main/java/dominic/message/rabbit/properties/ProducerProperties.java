package dominic.message.rabbit.properties;

import com.rabbitmq.client.AMQP;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Administrator:herongxing on 2017/12/13 19:51.
 */
@Data
@Builder
public class ProducerProperties {
    /**
     * 当mandatory标志位设置为true时，如果exchange根据自身类型和消息routeKey无法找到一个符合条件的queue（即消息到达exchange后无法把消息路由到符合条件的queue），
     * 那么会调用basic.return方法将消息返回给生产者（Basic.Return + Content-Header + Content-Body）；
     * 当mandatory设置为false时，出现上述情形broker会直接将消息扔掉。
     * 注意：与immediate标志位的区别：概括来说，mandatory标志告诉服务器至少将该消息route到一个队列中，否则将消息返还给生产者；
     * immediate标志告诉服务器如果该消息关联的queue上有消费者，则马上将消息投递给它，如果所有queue都没有消费者，直接把消息返还给生产者，不用将消息入队列等待消费者了。
     */
    private boolean mandatory;
    /**
     * 当immediate标志位设置为true时，即希望队列马上消费消息，如果exchange在将消息路由到queue(s)时发现对应的queue上没有消费者在线，
     * 那么这条消息不会放入队列中等待消费者上线，而是会通过basic.return方法返把消息还给生产者。
     * 注意：RabbitMQ3.0以后的版本里，去掉了immediate参数的支持，发送带immediate标记的publish会返回如下错误：
     * “{amqp_error,not_implemented,”immediate=true”,’basic.publish’}”
     */
    private boolean immediate;
    /**
     * rabbitmq发送消息的properties
     */
    private AMQP.BasicProperties basicProperties;
}
