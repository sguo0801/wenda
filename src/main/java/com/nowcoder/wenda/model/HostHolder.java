package com.nowcoder.wenda.model;

import org.springframework.stereotype.Component;

//专门放根据ticket取出来的用户
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();  //线程本地变量,看起来是一个变量,其实每个线程都有拷贝.不能用User user这样多线程也只能表示一个用户

    public User getUser() {
        return users.get();
    }   //返回当前线程的user

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
