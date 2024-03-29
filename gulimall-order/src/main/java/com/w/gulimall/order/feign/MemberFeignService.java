package com.w.gulimall.order.feign;

import com.w.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author xin
 * @date 2023-02-12-16:33
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 查询当前用户的全部收货地址
     * @param memberId
     * @return
     */
    @GetMapping(value = "/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
