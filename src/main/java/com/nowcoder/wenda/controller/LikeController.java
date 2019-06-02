package com.nowcoder.wenda.controller;

//import com.nowcoder.wenda.async.EventModel;
//import com.nowcoder.async.EventProducer;
//import com.nowcoder.async.EventType;
//import com.nowcoder.wenda.model.Comment;
import com.nowcoder.wenda.async.EventModel;
import com.nowcoder.wenda.async.EventProducer;
import com.nowcoder.wenda.async.EventType;
import com.nowcoder.wenda.model.Comment;
import com.nowcoder.wenda.model.EntityType;
import com.nowcoder.wenda.model.HostHolder;
import com.nowcoder.wenda.service.CommentService;
import com.nowcoder.wenda.service.LikeService;
import com.nowcoder.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//点赞业务(这里是存评论的)
@Controller
public class LikeController {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody   //json的所以直接返回串内容
    public String like(@RequestParam("commentId") int commentId) {
        //先登录
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Comment comment = commentService.getCommentById(commentId);  //从DAO到service都要加上根据id找评论.

        //异步的点赞链式调用,返回的是true,然后事件提交之后就不用管了,Like这个请求就结束啦,不需要处理站内信的消息.这个事件进入getEventQueueKey里面,就是在队列中啦,
        //###这里是异步的关键,开始放队列
        eventProducer.fireEvent(new EventModel(EventType.LIKE)  //设置好参数model.进行添加事件
                .setActorId(hostHolder.getUser().getId()).setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT).setEntityOwnerId(comment.getUserId())  //评论的人
                .setExt("questionId", String.valueOf(comment.getEntityId())));  //最后的questionId要设置,从likehandler里定义.
        //这边是要给一条评论点赞,这条评论属于一条问题下面,根据评论的id,找到评论,再根据评论找到对应的entityid(就是问题的id).对应handler新定义的questionid

        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));   //code:0代表可以生成json,msg就是后面的这个字符串.这里只是文字,不是对象.就不需要序列化和反序列化
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }
}
