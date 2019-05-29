package com.nowcoder.wenda.interceptor;

import com.nowcoder.wenda.dao.LoginTicketDAO;
import com.nowcoder.wenda.dao.UserDAO;
import com.nowcoder.wenda.model.HostHolder;
import com.nowcoder.wenda.model.LoginTicket;
import com.nowcoder.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

//拦截器的目的就是制作随处可用的hostholder,可以在controller和service中使用
@Component  //拦截器依赖注入
public class PassportInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HostHolder hostHolder;
    //处在所有http请求的最前面,判断用户是谁,然后找到该用户放到本地线程中,保证以后的所有服务(后台可以直接访问)都能找到该用户
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //先要找到ticket,从链路请求过来的cookie中
        String ticket = null;
        if(httpServletRequest.getCookies() != null){
            for (Cookie cookie : httpServletRequest.getCookies()){
                if (cookie.getName().equals("ticket")){
                    ticket = cookie.getValue();   //cookie是个键值对,值为我们生成的ticket票
                    break;
                }
            }
        }

        //找到了ticket的值,来对应数据库中的ticket票信息,为了找到userid
        if (ticket != null){
            //先判断ticket是否真实有效
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0){
                return true; //此时为无效的ticket,则返回true,虽然继续进行但是没有把找到该用户(status=1),后面渲染就无法出现该用户因为没有走到User哪里.
                // 如果返回false则不会继续进行拦截器的功能,那么controller也进不去,直接空白页面.说明没有传进来ticket
            }

            //开始取userid
            User user = userDAO.selectById(loginTicket.getUserId());
            //把取出来的user放在本地线程副本中
            hostHolder.setUser(user);
        }


        return true;   //注意不会有false,除非直接返回空白
    }

    //渲染之前,可以做或者不做
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null){
            modelAndView.addObject("user", hostHolder.getUser());  //这样直接在modelAndView添加变量user,就可以直接在模板中使用该变,所有controller在渲染前会在velocity页面中上下文添加了该user
        }


    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();  //结束后把用户清除掉,这样不会使用户越来越多

    }



//    @Override
//    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
//        String ticket = null;
//        if (httpServletRequest.getCookies() != null) {
//            for (Cookie cookie : httpServletRequest.getCookies()) {
//                if (cookie.getName().equals("ticket")) {
//                    ticket = cookie.getValue();
//                    break;
//                }
//            }
//        }
//
//        if (ticket != null) {
//            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
//            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
//                return true;
//            }
//
//            User user = userDAO.selectById(loginTicket.getUserId());
//            hostHolder.setUser(user);
//        }
//        return true;
//    }
//
//    @Override
//    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
//        if (modelAndView != null && hostHolder.getUser() != null) {
//            modelAndView.addObject("user", hostHolder.getUser());
//        }
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
//        hostHolder.clear();
//    }
}
