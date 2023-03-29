package com.w.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.to.OrderTo;
import com.w.common.to.mq.StockLockedTo;
import com.w.common.utils.PageUtils;
import com.w.gulimall.ware.entity.WareSkuEntity;
import com.w.gulimall.ware.vo.LockStockResultVo;
import com.w.gulimall.ware.vo.SkuHasStockVo;
import com.w.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 12:07:52
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Integer skuNum, Long wareId);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo orderTo);
}

