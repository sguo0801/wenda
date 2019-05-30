package com.nowcoder.wenda.controller;

import com.nowcoder.wenda.model.Comment;
import com.nowcoder.wenda.model.EntityType;
import com.nowcoder.wenda.model.HostHolder;
import com.nowcoder.wenda.service.CommentService;
import com.nowcoder.wenda.service.QuestionService;
import com.nowcoder.wenda.service.SensitiveService;
import com.nowcoder.wenda.service.UserService;
import com.nowcoder.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;

@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    SensitiveService sensitiveService;

    //写评论(其实是写问题的评论,所以后面entityID设置股为questionId),首先在controller建立评论对象,然后设置 评论表中字段对应的内容
    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})   //增加评论,写入数据都是post
    public String addComment(@RequestParam("questionId") int questionId,   //这里是往问题下面进行评论添加
                             @RequestParam("content") String content) {   //@RequestParam在indexcontroller时遇到过,是请求参数,可以用在类似于讨论区这样的有默认页面的地方
        try {
            content = HtmlUtils.htmlEscape(content);
            content = sensitiveService.filter(content);
            // 过滤content
            //开始建立评论,添加内容,包括发表评论的人,可以看拦截器的hostholder,没有就设置为匿名(id为3对应user2),也可以进行跳转登录
            Comment comment = new Comment();
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
                //return "redirect:/reglogin";  //这里是跳转登录
            }
            comment.setContent(content);  //内容(上面已经过滤)
            comment.setEntityId(questionId);    //这里方法是在 问题 下添加评论,所以用questionId,同questioncomment中的qid
            comment.setEntityType(EntityType.ENTITY_QUESTION);  //设置评论位置的类型,这里是问题.在model中有相关静态常量.
            comment.setCreatedDate(new Date());
            comment.setStatus(0);

            commentService.addComment(comment);
            // 更新题目里的评论数量,这边是在redis中不用这么写在一起进行更新,涉及到事务.就是可以分开写,评论的数量可以在滞后(异步)进行更新,不用一起.
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);  //对应的是主页面题目上关注题目右边的评论数量
            // 怎么异步化
        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;   //这个questionid其实就是questioncontroller中的url:{qid}
    }
}
