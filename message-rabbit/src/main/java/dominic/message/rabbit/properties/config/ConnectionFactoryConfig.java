package dominic.message.rabbit.properties.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by Administrator:herongxing on 2017/12/30 10:18.
 */
@Data
@Slf4j
public class ConnectionFactoryConfig {
    /** Default user name */
    public static final String DEFAULT_USER = "guest";
    /** Default password */
    public static final String DEFAULT_PASS = "guest";
    /** Default virtual host */
    public static final String DEFAULT_VHOST = "/";
    /** Default maximum channel number;
     *  zero for unlimited */
    public static final int    DEFAULT_CHANNEL_MAX = 0;
    /** Default maximum frame size;
     *  zero means no limit */
    public static final int    DEFAULT_FRAME_MAX = 0;
    /** Default heart-beat interval;
     *  60 seconds */
    public static final int    DEFAULT_HEARTBEAT = 60;
    /** The default host */
    public static final String DEFAULT_HOST = "localhost";
    /** 'Use the default port' port */
    public static final int    USE_DEFAULT_PORT = -1;
    /** The default non-ssl port */
    public static final int    DEFAULT_AMQP_PORT = AMQP.PROTOCOL.PORT;
    /** The default ssl port */
    public static final int    DEFAULT_AMQP_OVER_SSL_PORT = 5671;
    /** The default TCP connection timeout: 60 seconds */
    public static final int    DEFAULT_CONNECTION_TIMEOUT = 60000;
    /**
     * The default AMQP 0-9-1 connection handshake timeout. See DEFAULT_CONNECTION_TIMEOUT
     * for TCP (socket) connection timeout.
     */
    public static final int    DEFAULT_HANDSHAKE_TIMEOUT = 10000;
    /** The default shutdown timeout;
     *  zero means wait indefinitely */
    public static final int    DEFAULT_SHUTDOWN_TIMEOUT = 10000;

    /** The default continuation timeout for RPC calls in channels: 10 minutes */
    public static final int    DEFAULT_CHANNEL_RPC_TIMEOUT = (int) MINUTES.toMillis(10);

    private static final String PREFERRED_TLS_PROTOCOL = "TLSv1.2";

    private static final String FALLBACK_TLS_PROTOCOL = "TLSv1";

    private String username                       = DEFAULT_USER;
    private String password                       = DEFAULT_PASS;
    private String virtualHost                    = DEFAULT_VHOST;
    private String host                           = DEFAULT_HOST;
    private int port                              = USE_DEFAULT_PORT;
    private int requestedChannelMax               = DEFAULT_CHANNEL_MAX;
    private int requestedFrameMax                 = DEFAULT_FRAME_MAX;
    private int requestedHeartbeat                = DEFAULT_HEARTBEAT;
    private int connectionTimeout                 = DEFAULT_CONNECTION_TIMEOUT;
    private int handshakeTimeout                  = DEFAULT_HANDSHAKE_TIMEOUT;
    private int shutdownTimeout                   = DEFAULT_SHUTDOWN_TIMEOUT;


    private String addresses;

    public static ConnectionFactory getConnectionFactory(ConnectionFactoryConfig config) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(config.username);
        connectionFactory.setPassword(config.password);
        connectionFactory.setVirtualHost(config.virtualHost);
        connectionFactory.setHost(config.host);
        connectionFactory.setPort(config.port);
        connectionFactory.setRequestedChannelMax(config.requestedChannelMax);
        connectionFactory.setRequestedFrameMax(config.requestedFrameMax);
        connectionFactory.setRequestedHeartbeat(config.requestedHeartbeat);
        connectionFactory.setConnectionTimeout(config.connectionTimeout);
        connectionFactory.setHandshakeTimeout(config.handshakeTimeout);
        connectionFactory.setShutdownTimeout(config.shutdownTimeout);

        return connectionFactory;
    }

    public static Connection newConnection(ConnectionFactoryConfig config) {
        ConnectionFactory connectionFactory = ConnectionFactoryConfig.getConnectionFactory(config);

        try {
            String addresses = config.getAddresses();
            if (StringUtils.isNotBlank(addresses)) {
                Address[] address = Address.parseAddresses(addresses);
                return connectionFactory.newConnection(address);
            } else {
                return connectionFactory.newConnection();
            }
        } catch (Exception e) {
            log.error("message.rabbit: 获取connection异常：", e);
            throw new RuntimeException(e);
        }
    }
}
