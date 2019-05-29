package com.nowcoder.wenda.interceptor;

import com.nowcoder.wenda.model.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//在访问一个页面时必须登录,登录后直接跳转,在wendawebconfiguration上进行配置
@Component
public class LoginRequredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if(hostHolder.getUser() == null){  //判断页面用户有没有
            httpServletResponse.sendRedirect("/reglogin?next=" + httpServletRequest.getRequestURL());  //把登录页面的网址作为参数保存在跳转链接上
        }
//        if (hostHolder.getUser() == null) {
//            httpServletResponse.sendRedirect("/reglogin?next=" + httpServletRequest.getRequestURI());
//        }
        return true;   //注意是true才能继续往后走
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
