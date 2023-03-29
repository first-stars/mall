package com.w.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.w.common.expection.NoStockException;
import com.w.common.to.OrderTo;
import com.w.common.to.mq.StockDetailTo;
import com.w.common.to.mq.StockLockedTo;
import com.w.common.utils.R;
import com.w.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.w.gulimall.ware.entity.WareOrderTaskEntity;
import com.w.gulimall.ware.feign.OrderFeignService;
import com.w.gulimall.ware.feign.ProductFeignService;
import com.w.gulimall.ware.service.WareOrderTaskDetailService;
import com.w.gulimall.ware.service.WareOrderTaskService;
import com.w.gulimall.ware.vo.*;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.ware.dao.WareSkuDao;
import com.w.gulimall.ware.entity.WareSkuEntity;
import com.w.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

//@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private OrderFeignService orderFeignService;


    /**
     * TODO mq消息中间件解锁库存
     * @param to
     * @param message
     * @param channel
     * @throws IOException
     */
//    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId!=null){
//            解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
//            根据订单查询订单号的状态
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode()==0){
//                订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data==null||data.getStatus()==4){
//                 订单不催在
//                 订单已经被取消，才能解锁库存
                    unLockStock(detailTo.getSkuId(),detailTo.getWareId(),detailTo.getSkuNum(),detailId);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//                  解锁成功修改锁定状态
                    if (byId.getLockStatus()==1){
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                        wareOrderTaskDetailEntity.setId(byId.getId());
                        wareOrderTaskDetailEntity.setLockStatus(new Integer(2));
                        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
                    }

                }
            }else {
//                消息拒绝以后重新放到队列，让别人继续消费解锁
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                throw new RuntimeException("远程服务失败");
            }
        }else {
//            无需解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,skuNum);
        //更新工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(detailId);
        //变为已解锁
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }



    @Override
    public void addStock(Long skuId, Integer skuNum, Long wareId) {
        //1、判断如果没有库存记录，新增
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities.size()==0||wareSkuEntities==null){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //远程查询sku的名字,如果失败，事务无需回归
            //TODO 出现异常不回滚，高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode()==0){
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> skuHasStockVoList = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

            Long count = baseMapper.getSkuStock(skuId);

            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count==null?false:count>0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVoList;
    }

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存工作单的详情
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //找到每个商品在那个仓库
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪个仓库有库存
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);

            return stock;
        }).collect(Collectors.toList());

        //2、锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();

            if (wareIds==null||wareIds.size()==0){
                //没有任何仓库有这个商品
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,skuWareHasStock.getNum());

                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(skuWareHasStock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    //TODO 告诉MQ库存锁定成功
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    lockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
            }
            if (skuStocked == false) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        //库存工作单的id
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();

        /**
         * 解锁
         * 1、查询数据库关于这个订单锁定库存信息
         *   有：证明库存锁定成功了
         *      解锁：订单状况
         *          1、没有这个订单，必须解锁库存
         *          2、有这个订单，不一定解锁库存
         *              订单状态：已取消：解锁库存
         *                      已支付：不能解锁库存
         */
        WareOrderTaskDetailEntity taskDetailInfo = wareOrderTaskDetailService.getById(detailId);
        if (taskDetailInfo != null) {
            //查出wms_ware_order_task工作单的信息
            Long id = to.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(id);
            //获取订单号查询订单状态
            String orderSn = orderTaskInfo.getOrderSn();
            //远程查询订单信息
            R orderData = orderFeignService.getOrderStatus(orderSn);
            if (orderData.getCode() == 0) {
                //订单数据返回成功
                OrderVo orderInfo = orderData.getData("data", new TypeReference<OrderVo>() {});

                //判断订单状态是否已取消或者支付或者订单不存在
                if (orderInfo == null || orderInfo.getStatus() == 4) {
                    //订单已被取消，才能解锁库存
                    if (taskDetailInfo.getLockStatus() == 1) {
                        //当前库存工作单详情状态1，已锁定，但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            } else {
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        } else {
            //无需解锁
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存优先到期，查订单状态新建，什么都不处理
     * 导致卡顿的订单，永远都不能解锁库存
     * @param orderTo
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        //按照工作单的id找到所有 没有解锁的库存，进行解锁
        Long id = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status", 1));

        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}