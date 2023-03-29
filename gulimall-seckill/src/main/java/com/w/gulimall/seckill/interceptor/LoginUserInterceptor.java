package com.w.gulimall.seckill.interceptor;

import com.w.common.constant.AuthServerConstant;
import com.w.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xin
 * @date 2023-02-12-15:36
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", uri);
        if (match) {

            Object attribute = request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null) {

                MemberResponseVo memberResponseVo = (MemberResponseVo) attribute;
                loginUser.set(memberResponseVo);

                return true;
            } else {
                //没有登录
                request.getSession().setAttribute("msg", "请先进行登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
