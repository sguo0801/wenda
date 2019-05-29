package com.nowcoder.wenda.configuration;

import com.nowcoder.wenda.interceptor.LoginRequiredInterceptor;
import com.nowcoder.wenda.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//配置拦截器,就是扩展WebMvcConfigurerAdapter某个接口来实现
@Component
public class WendaWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    //spring在初始化时可以在这个回调的地方注册我们自己的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor);  //系统初始化先添加拦截器,使其注册在链路上.写在前面,因为先要定义hostholder
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/user/*");  //写在hostholder拦截器之后,并且访问/user/* 这个页面的时候如果没有用户才需要访问拦截器进行登录
        super.addInterceptors(registry);
    }
}
