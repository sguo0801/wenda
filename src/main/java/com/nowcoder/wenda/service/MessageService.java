package com.nowcoder.wenda.service;

import com.nowcoder.wenda.dao.MessageDAO;
import com.nowcoder.wenda.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    //加入消息入库
    public int addMessage(Message message) {
        message.setContent(sensitiveService.filter(message.getContent()));  //用户添加站内信内容的时候,先把内容过滤在放进数据库中
        return messageDAO.addMessage(message) > 0 ? message.getId() : 0;    //这里的message已经是过滤过的内容
    }

    //查找消息内容,分页
    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageDAO.getConversationDetail(conversationId, offset, limit);
    }


    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConvesationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConvesationUnreadCount(userId, conversationId);
    }
}
