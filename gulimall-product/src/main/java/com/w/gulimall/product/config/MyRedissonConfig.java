package com.w.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xin
 * @date 2023-02-02-21:25
 */
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://114.55.9.31:6379").setPassword("redis123");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
