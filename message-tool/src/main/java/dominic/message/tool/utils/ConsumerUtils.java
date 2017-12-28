package dominic.message.tool.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple4;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Administrator:herongxing on 2017/12/28 16:21.
 */
public interface ConsumerUtils {

    static Tuple4<Class<?>, Boolean, Boolean, Boolean> consumeParameterType(Class<?> aClass, Predicate<Method> predicate) {
        Class clazz;
        boolean isList = false;
        boolean isSet = false;
        boolean isMap = false;
        Method[] declaredMethods = aClass.getDeclaredMethods();
        Method consumeMethod = null;
        for (int i=declaredMethods.length-1; i>=0; --i) {
            Method method = declaredMethods[i];
            if (predicate.test(method)) {
                consumeMethod = method;
                break;
            }
        }

        if (consumeMethod == null) {
            throw new RuntimeException(String.format("没有拿到消费者:%s的consume方法", aClass.getName()));
        }
        Type type = consumeMethod.getGenericParameterTypes()[0];
        if (type instanceof ParameterizedType) { //参数化类型
            String typeName = type.getTypeName();
            isList = Pattern.matches("^java\\.util\\.[A-Za-z]*List.*$", typeName);
            isSet = Pattern.matches("^java\\.util\\.[A-Za-z]*Set.*$", typeName);
            isMap = Pattern.matches("^java\\.util\\.[A-Za-z]*Map.*$", typeName);
            clazz = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
        } else if (type instanceof GenericArrayType) { //数组
            clazz = (Class) type;
        } else if (type instanceof Class) {
            clazz = (Class) type;
        } else {
            throw new RuntimeException(aClass.getName()+"consume方法消息体参数：不支持的类型"+type.getTypeName());
        }

        return Tuple.of(clazz, isList, isSet, isMap);
    }


    static Object convertMessage(String messageJson, boolean finalIsList, Class clazz, boolean finalIsSet, boolean finalIsMap) {
        Object message = messageJson;
        if (finalIsList) {
            message = JSON.parseArray(messageJson, clazz);
        } else if (finalIsSet) {
            List list = JSON.parseArray(messageJson, clazz);
            HashSet<Object> hashSet = Sets.newHashSet();
            hashSet.addAll(list);
            message = hashSet;
        } else if (finalIsMap) {
            message = JSON.parseObject(messageJson, Map.class);
        } else {
            if (!"java.lang.String".equals(clazz.getName())) {
                message = JSON.parseObject(messageJson, clazz);
            }
        }
        return message;
    }
}
