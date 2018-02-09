# message-kafka

## 介绍
对kafka的java客户端做了封装，让其与spring的集成和使用更简单

## 配置

1. 依赖
```xml
<dependency>
    <groupId>dominic</groupId>
    <artifactId>message.kafka</artifactId>
    <version>0.4.0-SNAPSHOT</version> <!-- 版本有改动请自行改用最新版本 -->
</dependency>
```
2. 配置
```java
@Configuration
@ComponentScan("dominic.message.kafka")
public class KafkaMessageConfig {
    /**
     * 一些配置是有"."作为两个单词之间的间隔，yml不支持"."间隔，请用"-"作为代替
     * 如:"bootstrap.servers" -> "bootstrap-servers"
     */
    @Bean
    @ConfigurationProperties(prefix="message.kafka.producer")
    public Properties kafkaProducerProperties() {
        return new Properties();
    }
    @Bean
    @ConfigurationProperties(prefix="message.kafka.consumer")
    public Properties kafkaConsumerProperties() {
        return new Properties();
    }
}
```
以上是springBoot方式的配置。用传统spring xml方式的配置也是可以的：
需要spring扫描"dominic.message.kafka"，并分别配置两个Properties类型的bean：kafkaProducerProperties 和 kafkaConsumerProperties
配置项内容可看kafka客户端源码中的3个config文件，kafka消费者和生产者的所有配置都有，而且还有相应的解释:
`CommonClientConfigs`、`ProducerConfig`、`ConsumerConfig`

properties配置：
```yml
message:
    kafka:
        producer:
            bootstrap-servers: #你的kafka服务端地址，多个地址间用英文逗号分隔
            acks: 1
            retries: 0

        consumer:
            bootstrap-servers: #你的kafka服务端地址，多个地址间用英文逗号分隔
            group-id: message-demo   #group-id是当前应用的消费group的名字，防止同一个应用重复消费消息
```
以上是yml文件的配置方式，也可以用properties文件方式的配置：
```propterties
message.kafka.producer.bootstrap-servers = 你的kafka服务端地址，多个地址间用英文逗号分隔
message.kafka.producer.acks = 1
message.kafka.producer.retries = 0

message.kafka.consumer.bootstrap-servers = 你的kafka服务端地址，多个地址间用英文逗号分隔
message.kafka.consumer.group-id = message-demo
```
3. 使用
* 消费者
实现KafkaMessageConsumer接口即可，message-kafka接到消息后会自动解析json成消费者的泛型类型，如消息体是String类型的：
```java
@Service
public class KafkaTestConsumer implements KafkaMessageConsumer<String> {
    /**
     * 消息消费topics(可以消费多个topic)
     */
    @Override
    public Collection<String> topics() {
        return Lists.newArrayList("message.kafka.test");
    }
    /**
     * 消息到来时执行该方法
     *
     * @param message
     */
    @Override
    public void consume(String message) {
        System.out.println("消费message:===============================");
        System.out.println(message);
    }
}
```
* 生产者（发送kafka消息）
```java
Producer.send("message.kafka.test", message);
```
"message.kafka.test"是topic，message是要发送的消息

## 使用举例
1. 消费者 <br/>
消息体类型是InvoiceConsumerDTO：
```java
@Service
public class KafkaInvoiceConsumer implements KafkaMessageConsumer<InvoiceConsumerDTO> {
    @Override
    public Collection<String> topics() {
        return Lists.newArrayList("message.kafka.invoice");
    }

    @Override
    public void consume(InvoiceConsumerDTO message) {
        System.out.println("消费message:===============================");
        System.out.println(String.format("invoiceId=%d", message.getInvoiceId()));
        System.out.println(message);
    }
}
```
2. 发送消息
```java
InvoiceConsumerSubItemDTO subItemDTO1 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-123").itemCash(BigDecimal.valueOf(100.12)).build();
InvoiceConsumerSubItemDTO subItemDTO2 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-456").itemCash(BigDecimal.valueOf(12.11)).build();
InvoiceConsumerDTO invoiceConsumerDTO = InvoiceConsumerDTO.builder().invoiceId(123L).subItemList(Lists.newArrayList(subItemDTO1,subItemDTO2)).build();
Producer.send("message.kafka.invoice", invoiceConsumerDTO);
```

3. 日志打印 <br/>
发送消息的日志
```
2018-02-09 11:44:17.480 [main] DEBUG d.message.kafka.producer.Producer - Producer开始发送消息, message type:dominic.dto.InvoiceConsumerDTO, message：{"invoiceId":123,"subItemList":[{"itemCash":100.12,"subNumber":"IV-123"},{"itemCash":12.11,"subNumber":"IV-456"}]}
2018-02-09 11:44:17.498 [main] DEBUG d.message.kafka.producer.Producer - kafka Producer发送消息-完成
```
消费者接收消息的日志：
```
2018-02-09 11:44:17.532 [pool-1-thread-1] DEBUG d.message.kafka.consumer.Consumer - consumer:dominic.mq.consumer.kafka.KafkaInvoiceConsumer接收到消息, message：{"invoiceId":123,"subItemList":[{"itemCash":100.12,"subNumber":"IV-123"},{"itemCash":12.11,"subNumber":"IV-456"}]}
消费message:===============================
invoiceId=123
InvoiceConsumerDTO(invoiceId=123, subItemList=[InvoiceConsumerSubItemDTO(subNumber=IV-123, itemCash=100.12), InvoiceConsumerSubItemDTO(subNumber=IV-456, itemCash=12.11)])
```

相关源码在message-demo项目，直通车：
- [KafkaInvoiceConsumer](https://github.com/dominiche/message/blob/master/message-demo/src/main/java/dominic/mq/consumer/kafka/KafkaInvoiceConsumer.java)
- [KafkaMessageTest](https://github.com/dominiche/message/blob/master/message-demo/src/test/java/dominic/test/KafkaMessageTest.java)

## 注意事项
* message-kafka的消息体都会序列化成json，消费端会根据KafkaMessageConsumer的consume方法参数泛型，
来将json反序列化成相应的java类型，这样方便消费端直接使用。
* KafkaMessageConsumer的consume方法上不能加事务注解`@Transactional`，
因为spring事务的实现方式是通过动态生成一个子类，在该方法前后加上事务的处理来达成的，
这个生成的子类的泛型信息不会被保留，直接是Object类型了，所以无法将json序列化成原来的泛型类型。
如果是必须加事务的话，可以把consume方法的参数类型设置为String或Object类型，然后在方法里自己解析即可；
对于String或Object类型的泛型参数，message-kafka会保留原来的json格式，让用户自己反序列化。
遇到message-kafka无法识别的复杂类型也可以通过把类型设为String或Object，自己解析json的方式来解决。
