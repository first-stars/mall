package com.w.gulimall.seckill.service;

import com.w.common.utils.R;
import com.w.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author xin
 * @date 2023-02-20-15:05
 */
public interface SeckillService {
    void uploadSeckillSkulates3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    String kill(String killId, String key, Integer num) throws InterruptedException;

    SeckillSkuRedisTo getSkuSeckilInfo(Long skuId);

    R testSentinel();
}
