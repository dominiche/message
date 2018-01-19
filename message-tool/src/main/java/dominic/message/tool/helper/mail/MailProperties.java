package dominic.message.tool.helper.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator:herongxing on 2018/1/19 9:51.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailProperties {

    /**
     * 邮件接收者邮箱，多个用逗号分隔
     */
    private String receivers;


    /**
     * 发件人的邮箱的 SMTP 服务器地址
     */
    private String senderHost;
    /**
     * 发件人的邮箱账号
     */
    private String senderMail;
    /**
     * 发件人的邮箱账号密码
     */
    private String senderMailPassword;
}
