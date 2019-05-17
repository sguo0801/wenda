package com.nowcoder.wenda.controller;

import com.nowcoder.wenda.aspect.LogAspect;
import com.nowcoder.wenda.service.WendaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


@Controller
public class SettingController {
    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);  //注意logger是org.slf4j下面的Logger类.
    @Autowired
    WendaService wendaService;

    @RequestMapping(path = {"/setting"}, method = {RequestMethod.GET})
    @ResponseBody
    public String setting(HttpSession httpSession) {
        logger.info("setting log");
        return "Setting OK. " + wendaService.getMessage(1);
    }
}