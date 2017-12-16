package dominic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dominic.message.rabbit.properties.consume.ConsumeBasicQos;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Administrator:herongxing on 2017/12/13 20:05.
 */
public class SimpleTest {

    @Test
    public void testMap() {
        HashMap<String, ConsumeBasicQos> map = Maps.newHashMap();
        map.put("sb", ConsumeBasicQos.builder().global(false).prefetchCount(1).prefetchSize(3).build());
        String jsonString = JSON.toJSONString(map);
        System.out.println(jsonString);
        Map hashMap = JSON.parseObject(jsonString, Map.class);
        System.out.println(hashMap);
    }

    @Test
    public void testList() {
        ConsumeBasicQos build = ConsumeBasicQos.builder().global(false).prefetchCount(1).prefetchSize(3).build();
        ConsumeBasicQos build2 = ConsumeBasicQos.builder().global(false).prefetchCount(22).prefetchSize(44).build();
        List<ConsumeBasicQos> listOrigin = Lists.newArrayList(build,build2);
        String jsonString = JSON.toJSONString(listOrigin);
        System.out.println(jsonString);
        List list = JSON.parseObject(jsonString, List.class); //JSONObject
        System.out.println(list);
    }

    @Test
    public void testArray() {
        ConsumeBasicQos[] arr = {ConsumeBasicQos.builder().global(false).prefetchCount(1).prefetchSize(3).build(),
                ConsumeBasicQos.builder().global(false).prefetchCount(22).prefetchSize(44).build()};
        String jsonString = JSON.toJSONString(arr);
        System.out.println(jsonString);
//        ConsumeBasicQos[] strings = JSON.parseObject(jsonString, ConsumeBasicQos[].class);
        Object strings = JSON.parseObject(jsonString, ConsumeBasicQos[].class);
        ConsumeBasicQos[] strings1 = (ConsumeBasicQos[]) strings;
        System.out.println(strings);
    }

    @Test
    public void test() {
        Object strings = Lists.newArrayList("123", "134");
        LinkedList<String> strings1 = (LinkedList<String>) strings;
        System.out.println(strings1.get(0));
    }

    @Test
    public void testPatten() {
        String listPatten = "^java.util.[A-Za-z]*List.*$";
        boolean matches = Pattern.matches(listPatten, "java.util.List<java.util.String>");
        System.out.println(matches);
    }

    @Test
    public void testJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("123", "string");
        jsonObject.put("456", "Int");
        String string = jsonObject.toString();
        System.out.println(string);
    }
}
