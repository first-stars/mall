package com.w.gulimall.cart.interceptor;

import com.w.common.constant.AuthServerConstant;
import com.w.common.constant.CartConstant;
import com.w.common.vo.MemberResponseVo;
import com.w.gulimall.cart.to.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author xin
 * @date 2023-02-10-16:52
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> toThreadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (attribute!=null){
//            用户登陆了
            userInfoTo.setUserId(attribute.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    //标记为已是临时用户
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        toThreadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后，分配临时用户来浏览器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //获取当前用户的值
        UserInfoTo userInfoTo = toThreadLocal.get();
        //如果没有临时用户一定保存一个临时用户
        if (!userInfoTo.getTempUser()){
//创建一个cookie
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
//            扩大作用域
            cookie.setDomain("gulimall.com");
            //设置过期时间
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
