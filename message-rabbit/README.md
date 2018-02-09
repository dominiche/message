# message-rabbit

## 介绍
对RabbitMQ的java客户端做了封装，让其与spring的集成和使用更简单

## 配置

1. 依赖
```xml
<dependency>
    <groupId>dominic</groupId>
    <artifactId>message.rabbit</artifactId>
    <version>0.6.0_retry_limit-SNAPSHOT</version> <!-- 版本有改动请自行改用最新版本 -->
</dependency>
```
2. 配置
```java
@Configuration
@ComponentScan("dominic.message.rabbit")
public class RabbitMessageConfig {
    @Bean
    @ConfigurationProperties(prefix="spring.rabbitmq")
    public ConnectionFactoryConfig rabbitMessageConnectionFactoryConfig() {
        return new ConnectionFactoryConfig();
    }

    @Bean
    public Connection rabbitMessageConnection(ConnectionFactoryConfig rabbitMessageConnectionFactoryConfig) {
        return ConnectionFactoryConfig.newConnection(rabbitMessageConnectionFactoryConfig);
    }
}
```
以上是springBoot方式的配置。用传统spring xml方式的配置也是可以的：
需要spring扫描"dominic.message.rabbit"包，并分别配置两个bean：rabbitMessageConnectionFactoryConfig 和 rabbitMessageConnection

properties配置：
```yml
spring:
    rabbitmq:
        addresses: 192.168.50.25:5672
        username:   #用户名
        password:   #密码
        error-policy:
            retry: 10               #最大重试次数。小于0的整数：无限重试；0：不重试；大于0的整数n:重试n次
            remind: true            #超过最大重试次数后是否需要邮件提醒
            mail:
                receivers:          #需要接收消息消费异常邮件提醒的邮箱账号
                sender-host:        #邮件发送者账号的smtp服务器地址
                sender-mail:        #需要邮件提醒的话可以加上发送者账号和密码，否则不会发送邮件提醒
                sender-mail-password:
```
以上是yml文件的配置方式，也可以用properties文件方式的配置：
```propterties
#spring.rabbitmq.host = localhost
#spring.rabbitmq.port = 5672
spring.rabbitmq.addresses = localhost:5672
spring.rabbitmq.username =
spring.rabbitmq.password =
spring.rabbitmq.error-policy.retry = 10
spring.rabbitmq.error-policy.remind = true
spring.rabbitmq.error-policy.mail.receivers =
spring.rabbitmq.error-policy.mail.sender-host =
spring.rabbitmq.error-policy.mail.sender-mail =
spring.rabbitmq.error-policy.mail.sender-mail-password =
```
3. 使用
* 消费者
实现RabbitMessageConsumer接口即可，message-rabbit接到消息后会自动解析json成消费者的泛型类型，如消息体是String类型的：
```java
@Service
public class RabbitTestConsumer implements RabbitMessageConsumer<String> {

    @Override
    public ConsumeProperties consumerProperties() {
        return ConsumeProperties.durable("message.rabbit.test", "message.rabbit", "message.rabbit.test", false);
    }

    @Override
    public void consume(String message, RabbitMessageConsumerProperties properties) throws IOException {
        System.out.println("消费rabbit message:===============================");
        System.out.println(message);
    }
}
```
`consumerProperties`方法体里，`ConsumeProperties.durable`表示消息会被持久化，四个参数的含义分别是：消息的队列名、消息的交换器、消息的路由key、不自动确认消息

* 生产者（发送kafka消息）
```java
Producer.send("message.rabbit", "message.rabbit.test", message);
```
"message.rabbit"是消息的交换器，"message.rabbit.test"是要发送的消息的routingKey，message是要发送的消息

## 使用举例
1. 消费者
消息体类型是InvoiceConsumerDTO：
```java
@Service
public class RabbitInvoiceConsumer implements RabbitMessageConsumer<InvoiceConsumerDTO> {
    @Override
    public ConsumeProperties consumerProperties() {
        return ConsumeProperties.durable("message.rabbit.invoice", "message.rabbit", "message.rabbit.invoice", false);
    }

    @Override
    public void consume(InvoiceConsumerDTO message, RabbitMessageConsumerProperties properties) throws IOException {
        System.out.println("消费rabbit message:===============================");
        System.out.println(String.format("invoiceId=%d", message.getInvoiceId()));
        System.out.println(message);
    }
}
```
2. 发送消息
```java
InvoiceConsumerSubItemDTO subItemDTO1 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-789").itemCash(BigDecimal.valueOf(100.12)).build();
InvoiceConsumerSubItemDTO subItemDTO2 = InvoiceConsumerSubItemDTO.builder().subNumber("IV-321").itemCash(BigDecimal.valueOf(12.11)).build();
InvoiceConsumerDTO invoiceConsumerDTO = InvoiceConsumerDTO.builder().invoiceId(789L).subItemList(Lists.newArrayList(subItemDTO1,subItemDTO2)).build();
Producer.send("message.rabbit", "message.rabbit.invoice", invoiceConsumerDTO);
```

3. 日志打印
发送消息的日志
```
2018-02-09 15:00:49.343 [main] DEBUG d.message.rabbit.producer.Producer - Producer开始发送消息, message type:dominic.dto.InvoiceConsumerDTO, message：{"invoiceId":789,"subItemList":[{"itemCash":100.12,"subNumber":"IV-789"},{"itemCash":12.11,"subNumber":"IV-321"}]}
2018-02-09 15:00:49.344 [main] DEBUG d.message.rabbit.producer.Producer - rabbit Producer发送消息-完成
```
消费者接收消息的日志：
```
2018-02-09 15:00:49.347 [pool-1-thread-8] DEBUG d.message.rabbit.consumer.Consumer - consumer:dominic.mq.consumer.rabbit.RabbitInvoiceConsumer接收到消息, message：{"invoiceId":789,"subItemList":[{"itemCash":100.12,"subNumber":"IV-789"},{"itemCash":12.11,"subNumber":"IV-321"}]}
消费rabbit message:===============================
invoiceId=789
InvoiceConsumerDTO(invoiceId=789, subItemList=[InvoiceConsumerSubItemDTO(subNumber=IV-789, itemCash=100.12), InvoiceConsumerSubItemDTO(subNumber=IV-321, itemCash=12.11)])
```


相关源码在message-demo项目，直通车：
- [RabbitInvoiceConsumer](https://github.com/dominiche/message/blob/master/message-demo/src/main/java/dominic/mq/consumer/rabbit/RabbitInvoiceConsumer.java)
- [RabbitMessageTest](https://github.com/dominiche/message/blob/master/message-demo/src/test/java/dominic/test/RabbitMessageTest.java)

**message-rabbit可以设置消息消费出错时的处理策略，包括重试次数和邮件提醒，在配置文件中设置**

## 注意事项
* message-rabbit的消息体都会序列化成json，消费端会根据RabbitMessageConsumer的consume方法参数泛型，
来将json反序列化成相应的java类型，这样方便消费端直接使用。
* RabbitMessageConsumer的consume方法上不能加事务注解`@Transactional`，
因为spring事务的实现方式是通过动态生成一个子类，在该方法前后加上事务的处理来达成的，
这个生成的子类的泛型信息不会被保留，直接是Object类型了，所以无法将json序列化成原来的泛型类型。
如果是必须加事务的话，可以把consume方法的参数类型设置为String或Object类型，然后在方法里自己解析即可；
对于String或Object类型的泛型参数，message-rabbit会保留原来的json格式，让用户自己反序列化。
遇到message-rabbit无法识别的复杂类型也可以通过把类型设为String或Object，自己解析json的方式来解决。
