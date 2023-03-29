package com.w.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.w.common.constant.AuthServerConstant;
import com.w.common.expection.BizCodeEnume;
import com.w.common.utils.R;
import com.w.common.vo.MemberResponseVo;
import com.w.gulimall.auth.feign.MemberFeignService;
import com.w.gulimall.auth.feign.ThirdPartyFeignService;
import com.w.gulimall.auth.vo.UserLoginVo;
import com.w.gulimall.auth.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.w.common.constant.AuthServerConstant.LOGIN_USER;

/**
 * @author xin
 * @date 2023-02-07-19:43
 */
@Controller
public class LoginController {


    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 邮箱验证
     *
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);

            if (System.currentTimeMillis() - 1 < 60000) {
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();

        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        R s = thirdPartyFeignService.sendSimpleEmail(phone, code.split("_")[0]);
        return s;
    }

    //RedirectAttributes redirectAttributes 重定向携带数据
    @PostMapping("/register")
    public String regist(UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));
            redirectAttributes.addFlashAttribute("errors",errors);
//            校验出错，转发到注册页面
            return "forward:http://auth.gulimall.com/reg.html";
        }
//        注册，调用远程服务
//        1、校验验证码
        String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(code)){
            if (vo.getCode().equals(code.split("_")[0])){
//                删除验证吗
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
//                验证通过，调用远程方法注册
                R regist = memberFeignService.regist(vo);
                if (regist.getCode()==0){
//                    成功
                    return "forward:http://auth.gulimall.com/login.html";
                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",regist.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
//            校验出错，转发到注册页面
                    return "forward:http://auth.gulimall.com/reg.html";
                }


            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证吗错误");
                redirectAttributes.addFlashAttribute("errors",errors);
//            校验出错，转发到注册页面
                return "forward:http://auth.gulimall.com/reg.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证吗错误");
            redirectAttributes.addFlashAttribute("errors",errors);
//            校验出错，转发到注册页面
            return "forward:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo,RedirectAttributes attributes, HttpSession session){
        //远程登录
        R login = memberFeignService.login(userLoginVo);

        if (login.getCode() == 0) {
            MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(LOGIN_USER);
        if (attribute==null){
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }
}
