package com.nowcoder.wenda.service;

import com.nowcoder.wenda.dao.LoginTicketDAO;
import com.nowcoder.wenda.dao.UserDAO;
import com.nowcoder.wenda.model.LoginTicket;
import com.nowcoder.wenda.model.User;
import com.nowcoder.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    public User selectByName(String name) {
        return userDAO.selectByName(name);
    }


    //注册
    public Map<String, String> register(String username, String password){
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(username)){
            map.put("msg", "用户名不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)){
            map.put("msg", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);
        if (user != null){
            map.put("msg", "该用户名已经被注册");
            return map;
        }

        //注册没有问题可以添加账号进库
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 10));   //通用唯一标示码.前缀跟时间有关,每秒都会不同
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);

        //自动登录
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);

        return map;

    }
//    public Map<String, String> register(String username, String password) {
//        Map<String, String> map = new HashMap<String, String>();
//        if (StringUtils.isBlank(username)) {
//            map.put("msg", "用户名不能为空");
//            return map;
//        }
//
//        if (StringUtils.isBlank(password)) {
//            map.put("msg", "密码不能为空");
//            return map;
//        }
//
//        User user = userDAO.selectByName(username);
//
//        if (user != null) {
//            map.put("msg", "用户名已经被注册");
//            return map;
//        }
//
//        // 密码强度
//        user = new User();
//        user.setName(username);
//        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
//        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
//        user.setHeadUrl(head);
//        user.setPassword(WendaUtil.MD5(password + user.getSalt()));
//        userDAO.addUser(user);
//
//        // 登陆
//        String ticket = addLoginTicket(user.getId());
//        map.put("ticket", ticket);
//        return map;
//    }

    //登录
    public Map<String, String> login(String username, String password) {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);

        if (user == null) {
            map.put("msg", "用户名不存在");
            return map;
        }

        if (!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())) {
            map.put("msg", "密码不正确");
            return map;
        }

        //第一次
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }

    //根据用户id添加匹配的ticket
    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000*3600*24);    //date的gettime()为对应1970.0.0的毫秒时间差,这里默认有效期为1天
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }



    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);  //状态为1即无效
    }


    public User getUser(int id) {
        return userDAO.selectById(id);
    }

}
