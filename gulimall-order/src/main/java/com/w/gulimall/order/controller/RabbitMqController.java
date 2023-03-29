package com.w.gulimall.order.controller;

import com.w.gulimall.order.entity.OrderItemEntity;
import com.w.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xin
 * @date 2023-02-11-20:24
 */
@RestController
public class RabbitMqController {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/testRabbitMq")
    public String testRabbitMq(){
        for (int i = 0; i <10; i++) {
            if (i % 2==0){
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1l);
                orderReturnReasonEntity.setName("username" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity);
            }else {

                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setId(2l);
                orderItemEntity.setSkuName("username"+ i);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderItemEntity);

            }
        }
        return "ok";
    }
}
