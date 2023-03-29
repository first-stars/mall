package com.w.gulimall.member.feign;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xin
 * @date 2022-12-28-14:19
 */

@FeignClient("gulimall-coupon")     //这是一个声明试的远程接口调用
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupon();
}
