package dominic.message.rabbit.constant;

import lombok.Getter;

/**
 * Created by Administrator:herongxing on 2017/12/14 14:02.
 */
public enum ExchangeType {
    FANOUT("fanout"),
    DIRECT("direct"),
    TOPIC("topic"),
    HEADERS("headers");

    @Getter
    private String type;

    ExchangeType(String type) {
        this.type = type;
    }
}
