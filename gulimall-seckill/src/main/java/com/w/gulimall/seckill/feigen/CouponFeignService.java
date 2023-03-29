package com.w.gulimall.seckill.feigen;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author xin
 * @date 2023-02-20-14:58
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lates3DaySession")
    R getLates3DaySession();
}
