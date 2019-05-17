package com.nowcoder.wenda.controller;

import com.nowcoder.wenda.model.User;
import com.nowcoder.wenda.service.WendaService;
import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller  //入口层
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    WendaService wendaService;


    //Springboot4.3之后这边用@GetMapping
    @RequestMapping(path = {"/", "/index"})  //指定首页路径
    @ResponseBody  //返回的是字符串,而不是模板,如果是模板不用ResponseBody.注释掉则去templates中调用模板
    public String index(HttpSession httpSession) {  //处理的函数
        logger.info("VISIT HOME");
        return wendaService.getMessage(2) + "Hellohhhhhbbbbb NowCoder" + httpSession.getAttribute("msg");
    }

    //这边有devtools依赖,在进行热部署的时候可以实时更新,比如更换路径.在idea中还要进行同步设置,下面会自动重新编译运行,可能会有点慢
    @RequestMapping(path = {"/profile/{groupId}/{userId}"}, method = {RequestMethod.GET})  //变量ID在路径里面,所以下面用路径变量PathVariable,写入数据用post,防止垃圾数据的写入.读取用get.
    @ResponseBody
    public String profile(@PathVariable("userId") int userId,  //可以通过{}将ID自动解析到上面,路径参数
                          @PathVariable("groupId") String groupId,
                          @RequestParam(value = "type", defaultValue = "81") int type,  //请求参数,可以用在类似于讨论区这样的有默认页面的地方
                          @RequestParam(value = "key", defaultValue = "gs", required = false) String key) {   //不是要求的即便没有默认值不会报错,会自动为null,如果是true,却在路径请求参数中不给key值,会报错.
        return String.format("Profile Page of %s / %d, t:%d k: %s", groupId, userId, type, key); //d是十进制整数,s是字符串
    }


    @RequestMapping(path = {"/vm"}, method = {RequestMethod.GET})
    public String template(Model model) {     //model是给模板的变量进行定义.
        model.addAttribute("value1", "vvvvv1");  //然后把这个value1变量在模板中显示出来.
        List<String> colors = Arrays.asList(new String[]{"RED", "GREEN", "BLUE"});
        model.addAttribute("colors", colors);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 4; ++i) {
            map.put(String.valueOf(i), String.valueOf(i * i));
        }
        model.addAttribute("map", map);
        model.addAttribute("user", new User("Lee"));  //把新建的User,赋值给user.
        return "home";  //配置中是过滤掉html.
    }

    @RequestMapping(path = {"/request"}, method = {RequestMethod.GET})   //看request请求的
    @ResponseBody
    public String request(Model model, HttpServletResponse response,
                          HttpServletRequest request, HttpSession httpSession,
                          @CookieValue("JSESSIONID") String sessionId) {  //model不是必须的,是为了传送模板,这边可以通过注解直接把cookie放到参数进行解析
        StringBuilder sb = new StringBuilder();
        sb.append("COOKIEVALUE:" + sessionId);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name + ":" + request.getHeader(name) + "<br>");
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                sb.append("Cookie:" + cookie.getName() + " value:" + cookie.getValue());
            }
        }
        sb.append(request.getMethod() + "<br>");
        sb.append(request.getQueryString() + "<br>");
        sb.append(request.getPathInfo() + "<br>");
        sb.append(request.getRequestURI() + "<br>");

        response.addHeader("nowcoderId", "hellohhhh");
        response.addCookie(new Cookie("username", "nowcoderaaaaaa"));  //可以用在登录注册上面
        return sb.toString();
    }

    @RequestMapping(path = {"/redirect/{code}"}, method = {RequestMethod.GET})
    public RedirectView redirect(@PathVariable("code") int code, HttpSession httpSession) {
        httpSession.setAttribute("msg", "jump from redirect");
        RedirectView red = new RedirectView("/", true);
        if (code == 301) {  //301是强制跳转,永久性跳转
            red.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        }
        return red;
    }

    @RequestMapping(path = {"/admin"}, method = {RequestMethod.GET})
    @ResponseBody
    public String admin(@RequestParam("key") String key) {
        if ("admin".equals(key)) {   //这边key是请求的参数变量,必须在路径后加上?key=admin才正常输出,如果key不为admin则报异常,如果没有key则直接报错.无论是错误还是异常都是在下面的error展现出来.
            return "hello admin";
        }
        throw  new IllegalArgumentException("参数不对");
    }

    @ExceptionHandler()  //这个就是报错的error界面.就是把这个indexcontroller注释掉,缺失的error页面.
    @ResponseBody
    public String error(Exception e) {
        return "error:" + e.getMessage();
    }
}





