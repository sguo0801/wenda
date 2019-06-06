package com.nowcoder.wenda.controller;

import com.nowcoder.wenda.async.EventModel;
import com.nowcoder.wenda.async.EventProducer;
import com.nowcoder.wenda.async.EventType;
import com.nowcoder.wenda.model.*;
import com.nowcoder.wenda.service.CommentService;
import com.nowcoder.wenda.service.LikeService;
import com.nowcoder.wenda.service.QuestionService;
import com.nowcoder.wenda.service.UserService;
import com.nowcoder.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/{qid}", method = {RequestMethod.GET})
    public String questionDetail(Model model, @PathVariable("qid") int qid) {
        Question question = questionService.getById(qid);
        model.addAttribute("question", question);
        //model.addAttribute("user", userService.getUser(question.getUserId()));

        //开始在页面上添加评论的内容
        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);  //首先拿出数据库中的所有评论信息
        List<ViewObject> vos = new ArrayList<>();   //最后添加到model模板中的列表
        for (Comment comment : commentList) {   //把数据库中的评论信息分条 添加到vo这种数据结构中
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);   //##这边comment不包括user吗,跟前端有关,这里填进去的是什么,内容吗????????


            //加入点赞点踩的信息.
            //先是看当前用户的喜欢状态,没有登录自然就是不赞不踩
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            //赞踩的数量
            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));


            //在页面评论中添加 用户名信息
            vo.set("user", userService.getUser(comment.getUserId()));
            vos.add(vo);
        }
        model.addAttribute("comments", vos);

        return "detail";
    }

    //添加问题
    @RequestMapping(value = "/question/add", method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title, @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setTitle(title);
            //question.setCommentCount(0);
            if (hostHolder.getUser() == null) {
                //question.setUserId(WendaUtil.ANONYMOUS_USERID);    //发布问题如果没有User变量(使用postman)发布则使用匿名其实只要用postman都是匿名,如果wendautil中匿名值为3,则对应user的id为3,即是User2进行发表,也可以走下面
              return WendaUtil.getJSONString(999);  //就是强制登录才能进行发表问题,这时候不能从postman里面进行添加问题.
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            if (questionService.addQuestion(question) > 0) {  //添加问题成功,则返回code=0;说明可以进行json串的直接解析.如果questionservice失败,则此时int为0,这里不符合条件,则code为1,返回msg
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId())
                        .setExt("title", question.getTitle()).setExt("content", question.getContent()));
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }
        return WendaUtil.getJSONString(1, "失败");  //问题发表失败则,code为1,返回的消息为msg内容.不会解析输入的内容
    }

}
