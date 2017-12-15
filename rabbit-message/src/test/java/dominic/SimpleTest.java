package dominic;

import com.alibaba.fastjson.JSON;
import dominic.message.rabbit.properties.ProducerProperties;
import org.junit.Test;

/**
 * Created by Administrator:herongxing on 2017/12/13 20:05.
 */
public class SimpleTest {

    @Test
    public void test() {
        String jsonString = JSON.toJSONString("test string type", true);
        System.out.println(jsonString);
        System.out.println(JSON.parseObject("test string type", String.class));
    }
}
