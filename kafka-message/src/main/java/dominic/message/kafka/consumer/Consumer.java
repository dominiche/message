package dominic.message.kafka.consumer;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dominic.message.kafka.constants.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by Administrator:herongxing on 2017/12/13 21:55.
 */
@Slf4j
@Service("kafkaConsumer")
public class Consumer {

    private ExecutorService executorService = null;

    private Properties consumerProperties;

    private long pollTimeout = 1000; //1000ms

    private List<KafkaConsumer<String, String>> kafkaConsumerList = new Vector<>();

    @Autowired(required = false)
    private List<KafkaMessageConsumer> consumers = Lists.newArrayList();


    public Consumer(Properties kafkaConsumerProperties) throws IOException {
        consumerProperties = new Properties();
        kafkaConsumerProperties.entrySet().forEach(entry -> {
            String key = entry.getKey().toString();
            key = key.replace("-", ".");
            consumerProperties.put(key, entry.getValue().toString());
        });
        Object keyDeserializer = consumerProperties.getOrDefault(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaConstants.DEFAULT_KEY_DESERIALIZER);
        Object valueDeserializer = consumerProperties.getOrDefault(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaConstants.DEFAULT_VALUE_DESERIALIZER);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
    }

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeResource));

        if (CollectionUtils.isEmpty(consumers)) {
            log.warn("rabbitMessageConsumers is zero!");
            return;
        }
        assembleConsumer();
    }

    private void closeResource() {
        log.debug("关闭kafka consumer连接..........");

        kafkaConsumerList.parallelStream().forEach(kafkaConsumer -> {
            try {
                kafkaConsumer.close();
            } catch (Exception e) {
                log.error("kafka consumer关闭异常：{}", kafkaConsumer, e);
            }
        });

        try {
            if (null != executorService) {
                executorService.shutdown();
            }
        } catch (Exception e) {
            log.error("线程池关闭异常");
        }
    }

//    @SuppressWarnings("unchecked")
    private void assembleConsumer() {
        log.debug("开始装配 kafka-message consumer..........");

        int size = consumers.size();
        int maxExecutorThreads = Runtime.getRuntime().availableProcessors() * 2;
        if (size > maxExecutorThreads) {
            size = maxExecutorThreads;
        }
        executorService = Executors.newFixedThreadPool(size);

        consumers.forEach(consumer -> {
            Class clazz;
            boolean isList = false;
            boolean isSet = false;
            boolean isMap = false;
            Class<? extends KafkaMessageConsumer> aClass = consumer.getClass();
            Method[] declaredMethods = aClass.getDeclaredMethods();
            Method consumeMethod = null;
            for (int i=declaredMethods.length-1; i>=0; --i) {
                Method method = declaredMethods[i];
                Parameter[] parameters = method.getParameters();
                if (KafkaConstants.CONSUME_METHOD_NAME.equals(method.getName()) && 1 == parameters.length) {
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
                throw new RuntimeException(consumer.getClass().getName()+"consume方法消息体参数：不支持的类型"+type.getTypeName());
            }


            try {
                //assemble consumer
                boolean finalIsList = isList;
                boolean finalIsSet = isSet;
                boolean finalIsMap = isMap;
                String consumerClassName = consumer.getClass().getName();
                KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProperties);
                Collection<String> topics = consumer.topics();
                checkTopics(consumerClassName, topics);
                kafkaConsumer.subscribe(topics);
                kafkaConsumerList.add(kafkaConsumer);
                executorService.execute(() -> {
                    while (true) {
                        ConsumerRecords<String, String> records = kafkaConsumer.poll(pollTimeout);
                        for (ConsumerRecord<String, String> record : records) {
                            String messageJson = record.value();
                            try {
                                log.debug("consumer:{}接收到消息, message：{}", consumerClassName, messageJson);
                                Object message = convertMessage(messageJson, finalIsList, clazz, finalIsSet, finalIsMap);
                                consumer.consume(message);
                                //todo 手工确认??
                            } catch (Exception e) {
                                log.error("consumer:{}消费消息时发生异常, message:{}", consumerClassName, messageJson, e);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                log.error("装配kafka.message消费者出错：", e);
                throw new RuntimeException(e);
            }
        });
    }

    private void checkTopics(String consumerClassName, Collection<String> topics) {
        if (topics.isEmpty()) {
            throw new RuntimeException(String.format("消费者：%s的消费topics不能为空！", consumerClassName));
        }
        for (String topic : topics) {
            if (StringUtils.isBlank(topic)) {
                throw new RuntimeException(String.format("kafka.message消费者：%s的消费topics不能包含null或空串！", consumerClassName));
            }
        }
    }

    private Object convertMessage(String messageJson, boolean finalIsList, Class clazz, boolean finalIsSet, boolean finalIsMap) {
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
