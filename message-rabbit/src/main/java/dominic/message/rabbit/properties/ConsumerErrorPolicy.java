package dominic.message.rabbit.properties;

import dominic.message.tool.helper.mail.MailProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator:herongxing on 2018/1/18 17:01.
 * 消费消息出错时的处理策略，注意：前提是consumer手动ack（及autoAck=false）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerErrorPolicy {

    /**
     * 消费消息出错时的重试次数：
     * 小于0的整数 表示无限重试（通过channel.basicAck(envelope.getDeliveryTag(), true)实现）
     * 0 表示不重试，跟autoAck=false效果一致
     * 大于0的整数n 重试n次（不重入队列reject掉消息后，重新发消息）
     */
    private Integer retry;


    /**
     * 是否需要邮件
     */
    private Boolean remind;
    /**
     * 邮件提醒配置
     */
    private MailProperties mail;
}
