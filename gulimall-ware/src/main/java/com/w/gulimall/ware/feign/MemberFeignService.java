package com.w.gulimall.ware.feign;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xin
 * @date 2023-02-14-9:28
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 信息
     */
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
