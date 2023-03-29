package com.w.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.w.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.order.dao.OrderItemDao;
import com.w.gulimall.order.entity.OrderItemEntity;
import com.w.gulimall.order.service.OrderItemService;

//@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

//    @RabbitListener(queues = {"hello-java-queue"})
//    @RabbitHandler
    public void recieve1Message(Message message, OrderReturnReasonEntity entity,Channel channel){
        System.out.println("收到的消息内容{"+entity+"}");
        long tag = message.getMessageProperties().getDeliveryTag();
        System.out.println("***************************"+tag);
        try {
            channel.basicNack(tag,false,true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
//    @RabbitHandler
    public void recieve2Message(Message message, OrderItemEntity entity, Channel channel){

        System.out.println("收到的消息内容{"+entity+"}");
        long tag = message.getMessageProperties().getDeliveryTag();
        System.out.println("***************************"+tag);
        try {
            channel.basicAck(tag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}