package com.w.gulimall.auth.feign;

import com.w.common.utils.R;
import com.w.gulimall.auth.vo.UserLoginVo;
import com.w.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author xin
 * @date 2023-02-08-16:24
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);


}
