package com.nowcoder.wenda.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
//@Aspect     //切面编程
//@Component  //不知道是不是service的时候,可以用component直接以组件的方式依赖注入出来.
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);  //建立log,后面参数是切面本类
//    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
//
    @Before("execution(* com.nowcoder.wenda.controller.*Controller.*(..))")  //切面不能自动编译,要自己编译.
    public void beforeMethod(JoinPoint joinPoint){
        StringBuilder sb = new StringBuilder();
        for (Object arg : joinPoint.getArgs()){
            sb.append("   args : " + arg.toString() + " | ");
        }
        logger.info("before method : " + new Date() + sb.toString());

    }

    @After("execution(* com.nowcoder.wenda.controller.*Controller.*(..))")
    public void afterMethod(){
        logger.info("after method : " + new Date());
    }
//    @Before("execution(* com.nowcoder.wenda.controller.*Controller.*(..))")
//    public void beforeMethod(JoinPoint joinPoint) {
//        StringBuilder sb = new StringBuilder();
//        for (Object arg : joinPoint.getArgs()) {
//            if (arg != null) {
//                sb.append("arg:" + arg.toString() + "|");
//            }
//        }
//        logger.info("before method:" + sb.toString());
//    }
//
//    @After("execution(* com.nowcoder.wenda.controller.IndexController.*(..))")
//    public void afterMethod() {
//        logger.info("after method" + new Date());
    }
//}
