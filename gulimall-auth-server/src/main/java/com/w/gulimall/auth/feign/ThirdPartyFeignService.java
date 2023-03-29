package com.w.gulimall.auth.feign;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xin
 * @date 2023-02-07-21:36
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
    R sendSimpleEmail(@RequestParam("email") String email, @RequestParam("code") String code);
}
