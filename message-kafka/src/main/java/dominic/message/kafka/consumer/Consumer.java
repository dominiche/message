package dominic.message.kafka.consumer;

import com.google.common.collect.Lists;
import com.google.gson.*;
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
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        Object maxPollRecords = consumerProperties.getOrDefault(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, KafkaConstants.MAX_POLL_RECORDS);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
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
            Class<? extends KafkaMessageConsumer> aClass = consumer.getClass();
            java.lang.reflect.Method[] declaredMethods = aClass.getDeclaredMethods();
            Type type = null;
            for (int i=declaredMethods.length-1; i>=0; --i) {
                java.lang.reflect.Method method = declaredMethods[i];
                if (KafkaConstants.CONSUME_METHOD_NAME.equals(method.getName()) &&1 == method.getParameters().length) {
                    type = method.getGenericParameterTypes()[0];
                    if (!Objects.equals(type.getTypeName(), "java.lang.Object")) {
                        break;
                    }
                }
            }

            if (type == null) {
                throw new RuntimeException(String.format("没有拿到消费者:%s的consume方法", aClass.getName()));
            }

            GsonBuilder builder = new GsonBuilder();
            // Register an adapter to manage the date types as long values
            builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            });
            Gson gson = builder.create();

            try {
                //assemble consumer
                String consumerClassName = consumer.getClass().getName();
                KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProperties);
                Collection<String> topics = consumer.topics();
                checkTopics(consumerClassName, topics);
                kafkaConsumer.subscribe(topics);
                kafkaConsumerList.add(kafkaConsumer);

                Type finalType = type;
                String typeName = finalType.getTypeName();
                executorService.execute(() -> {
                    while (true) {
                        ConsumerRecords<String, String> records = kafkaConsumer.poll(pollTimeout);
                        for (ConsumerRecord<String, String> record : records) {
                            String messageJson = record.value();
                            try {
                                log.debug("consumer:{}接收到消息, message：{}", consumerClassName, messageJson);
                                Object message = messageJson;
                                if (!Objects.equals(typeName, "java.lang.Object") && !Objects.equals(typeName, "java.lang.String")) {
                                    message = gson.fromJson(messageJson, finalType);
                                }
                                consumer.consume(message);
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
}
