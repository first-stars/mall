package com.w.gulimall.seckill.scheduled;

import com.w.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author xin
 * @date 2023-02-20-16:37
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){

        log.info("上架秒杀的商品.......");
//        分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkulates3Days();
        } finally {
            lock.unlock();
        }
    }
}
