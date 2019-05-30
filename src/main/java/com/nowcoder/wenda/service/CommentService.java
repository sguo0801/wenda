package com.nowcoder.wenda.service;

import com.nowcoder.wenda.dao.CommentDAO;
import com.nowcoder.wenda.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {  //对上是对业务,对下是数据库

    //首先把下一层 的DAO拿过来
    @Autowired
    CommentDAO commentDAO;

    @Autowired
    SensitiveService sensitiveService;

    //查找所有的评论
    public List<Comment> getCommentsByEntity(int entityId, int entityType){
        return commentDAO.selectCommentByEntity(entityId, entityType);
    }

    //添加评论,这里是service往数据库里添加.也就是默认的comment为空,现在添加内容
    public int addComment(Comment comment) {
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));   //html标签都转义.
        comment.setContent(sensitiveService.filter(comment.getContent()));  //添加的内容需要过滤
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    //得到评论数量
    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }

    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }

    public void deleteComment(int entityId, int entityType) {
        commentDAO.updateStatus(entityId, entityType, 1);
    }

    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }
}
