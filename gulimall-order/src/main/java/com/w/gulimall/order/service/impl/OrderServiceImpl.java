package com.w.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.w.common.expection.NoStockException;
import com.w.common.to.OrderTo;
import com.w.common.to.SkuHasStockVo;
import com.w.common.to.mq.SeckillOrderTo;
import com.w.common.utils.R;
import com.w.common.vo.MemberResponseVo;
import com.w.gulimall.order.constant.OrderConstant;
import com.w.gulimall.order.controller.OrderController;
import com.w.gulimall.order.dao.OrderItemDao;
import com.w.gulimall.order.entity.OrderItemEntity;
import com.w.gulimall.order.enume.OrderStatusEnum;
import com.w.gulimall.order.feign.CartFeignService;
import com.w.gulimall.order.feign.MemberFeignService;
import com.w.gulimall.order.feign.ProductFeignService;
import com.w.gulimall.order.feign.WmsFeignService;
import com.w.gulimall.order.interceptor.LoginUserInterceptor;
import com.w.gulimall.order.service.OrderItemService;
import com.w.gulimall.order.to.OrderCreateTo;
import com.w.gulimall.order.to.SpuInfoVo;
import com.w.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.order.dao.OrderDao;
import com.w.gulimall.order.entity.OrderEntity;
import com.w.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
//      取出主线程里的数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            //        查询所有收货地址列表
//            放入新的线程中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            orderConfirmVo.setMemberAddressVos(address);
        }, executor);


        CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
//        查询购物车中所有选中的购物项
            //            放入新的线程中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
            orderConfirmVo.setItems(currentCartItems);
//            feign在远程调用之前要构造请求，调用很多的拦截器
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(item -> {
                return item.getSkuId();
            }).collect(Collectors.toList());

            R skuHasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = skuHasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });

            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }
        }, executor);


//        查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

//        其余自动计算
//        TODO 幂等性令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);

        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1).get();

        return orderConfirmVo;
    }


//  TODO 分布式事务

    //    @GlobalTransactional    //分布式seata事务
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();

//        下单：创建订单，校验令牌价格锁库
        //获取当前用户登录的信息
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        responseVo.setCode(0);

        //1、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                orderToken);

        if (result == 0l) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证成功
            //1、创建订单、订单项等信息
            OrderCreateTo order = createOrder();
            //2、验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比
                //TODO 3、保存订单
                saveOrder(order);

                //4、库存锁定,只要有异常，回滚订单数据
                //订单号、所有订单项信息(skuId,skuNum,skuName)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                //获取出要锁定的商品数据信息
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);

                //TODO 调用远程锁定库存的方法
                //出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
                //为了保证高并发，不推荐使用seata，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //锁定成功
                    responseVo.setOrder(order.getOrder());
//                     int i = 10/0;
//
//                    //TODO 订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
//
//                    //删除购物车里的数据
//                    redisTemplate.delete(CART_PREFIX + memberResponseVo.getId());
                    return responseVo;
                } else {
                    //锁定失败
//                    String msg = (String) r.get("msg");
//                    throw new NoStockException(msg);
                    responseVo.setCode(3);
                    return responseVo;
                }

            } else {
                responseVo.setCode(2);
                return responseVo;
            }

        }

    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
//        查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
//            关单
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            new SeckillOrderTo();
//            发给MQ
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期
     * 导致卡顿的订单，永远不能解锁库存
     *
     * @param orderTo
     */
    @Override
    public void closeOrder(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
//        查询一下最新的状态，防止重复解锁库存

    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderInfo = this.getOrderByOrderSn(orderSn);

        //保留两位小数点，向上取值
        BigDecimal payAmount = orderInfo.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payAmount.toString());
        payVo.setOut_trade_no(orderInfo.getOrderSn());

        //查询订单项的数据
        List<OrderItemEntity> orderItemInfo = orderItemService.list(
                new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = orderItemInfo.get(0);
        payVo.setBody(orderItemEntity.getSkuAttrsVals());

        payVo.setSubject(orderItemEntity.getSkuName());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberResponseVo.getId()).orderByDesc("create_time")
        );

        //遍历所有订单集合
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //根据订单号查询订单项里的数据
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntityList(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * 创建秒杀订单
     * @param orderTo
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = orderTo.getSeckillPrice().multiply(BigDecimal.valueOf(orderTo.getNum()));
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(orderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);

        orderItem.setSkuQuantity(orderTo.getNum());

        //保存商品的spu信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(orderTo.getSkuId());
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfoData.getId());
        orderItem.setSpuName(spuInfoData.getSpuName());
        orderItem.setSpuBrand(spuInfoData.getBrandName());
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //保存订单项数据
        orderItemService.save(orderItem);
    }

    /**
     * 保存
     *
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);

    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = builderOrder(orderSn);

        //2、获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);

        //3、验价(计算价格、积分等信息)
        computePrice(orderEntity, orderItemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);

        return orderCreateTo;
    }

    /**
     * 计算价格
     *
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> builderOrderItems(String orderSn) {

        List<OrderItemEntity> orderItemEntityList = new ArrayList<>();

        //最后确定每个购物项的价格
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if (currentCartItems != null && currentCartItems.size() > 0) {
            orderItemEntityList = currentCartItems.stream().map((items) -> {
                //构建订单项数据
                OrderItemEntity orderItemEntity = builderOrderItem(items);
                orderItemEntity.setOrderSn(orderSn);

                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntityList;
        }
        return null;
    }

    /**
     * 构建某一个订单项的数据
     *
     * @param items
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVo items) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = items.getSkuId();
        //获取spu的信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoData.getBrandName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(items.getTitle());
        orderItemEntity.setSkuPic(items.getImage());
        orderItemEntity.setSkuPrice(items.getPrice());
        orderItemEntity.setSkuQuantity(items.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(items.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、商品的优惠信息

        //4、商品的积分信息
        orderItemEntity.setGiftGrowth(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        orderItemEntity.setGiftIntegration(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;

    }

    /**
     * 构建订单数据
     *
     * @param orderSn
     * @return
     */
    private OrderEntity builderOrder(String orderSn) {

        //获取当前用户登录信息
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberUsername(memberResponseVo.getUsername());

        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        R fareAddressVo = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fareAddressVo.getData("data", new TypeReference<FareVo>() {
        });

        //获取到运费信息
        BigDecimal fare = fareResp.getFare();
        orderEntity.setFreightAmount(fare);

        //获取到收货地址信息
        MemberAddressVo address = fareResp.getAddress();

        //设置收货人信息
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);
        return orderEntity;

    }
}