package com.nowcoder.wenda.service;

import org.springframework.stereotype.Service;

@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hessage : " + String.valueOf(userId);
    }

}
