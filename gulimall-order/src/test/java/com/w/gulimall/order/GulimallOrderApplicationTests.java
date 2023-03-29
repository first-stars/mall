package com.w.gulimall.order;

import com.w.gulimall.order.entity.OrderEntity;
import com.w.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {


    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() {
        DirectExchange exchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(exchange);
        log.info("{}创建成功","hello-java-exchange");
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("{}创建成功","hello-java-exchange");
    }


    @Test
    public void createBind() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("{}创建成功","hello-java-binding");
    }

    @Test
    public void sendMessageTest() {
        rabbitTemplate.convertAndSend("order-event-exchange","hello.java","helle java test");
        log.info("普通消息{}发送成功","hello.java");


//        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
//        orderReturnReasonEntity.setId(1l);
//        orderReturnReasonEntity.setName("哈哈");
//        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity);
//        log.info("对象消息{}发送成功",orderReturnReasonEntity);
    }


}
