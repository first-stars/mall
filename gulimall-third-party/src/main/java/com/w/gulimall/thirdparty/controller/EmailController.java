package com.w.gulimall.thirdparty.controller;

import com.w.common.utils.R;
import com.w.gulimall.thirdparty.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xin
 * @date 2022-12-07-10:41
 */
@RestController
@RequestMapping("/sms")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @GetMapping("/sendCode")
    public R sendSimpleEmail(@RequestParam("email") String email, @RequestParam("code") String code) {
        System.out.println(code);
//        R ret = emailService.sendSimpleEmail(email,"【验证码】标题",code);
        return R.ok();
    }

}
