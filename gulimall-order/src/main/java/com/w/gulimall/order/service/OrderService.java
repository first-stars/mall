package com.w.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.to.OrderTo;
import com.w.common.to.mq.SeckillOrderTo;
import com.w.common.utils.PageUtils;
import com.w.gulimall.order.entity.OrderEntity;
import com.w.gulimall.order.vo.OrderConfirmVo;
import com.w.gulimall.order.vo.OrderSubmitVo;
import com.w.gulimall.order.vo.PayVo;
import com.w.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:58:40
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    void closeOrder(OrderTo orderTo);

    /**
     * 获取等前订单的支付信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    /**
     * 查询当前用户所有订单数据
     * @param params
     * @return
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    void createSeckillOrder(SeckillOrderTo orderTo);
}

