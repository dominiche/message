package dominic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dominic.message.rabbit.properties.consume.ConsumeBasicQos;
import org.junit.Test;

import java.lang.reflect.Type;
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

    @Test
    public void testArrayAdd() {
        Vector<Integer> list = new Vector<>();

        new Thread(() -> {
            for (int i=1; i<=10; ++i) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.add(i);
            }
        }).start();
        new Thread(() -> {
            for (int i=11; i<=20; ++i) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.add(i);
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(list);
        System.out.println(list);
    }

    @Test
    public void testGSON(){
        GsonBuilder builder = new GsonBuilder();
        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
                return new JsonPrimitive(src.getTime());
            }
        });
        Gson gson = builder.create();

//        Gson gson = new Gson();


        String json = "[{\"key\":\"key1\",\"value\":\"1514514945000\"},{\"key\":\"key2\",\"value\":\"1514514945000\"}]";
        Type listType = new TypeToken<LinkedList<A>>(){}.getType();
        LinkedList<A> la = gson.fromJson(json, listType);
        System.out.println(la.size());
        System.out.println("=========:" + gson.toJson(la));
    }

    class A {
        private String key;
        private Date value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Date getValue() {
            return value;
        }

        public void setValue(Date value) {
            this.value = value;
        }
    }
}
