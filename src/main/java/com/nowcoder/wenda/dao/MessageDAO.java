package com.nowcoder.wenda.dao;

import com.nowcoder.wenda.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/9.
 */
@Mapper
public interface MessageDAO {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, has_read, conversation_id, created_date ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{fromId},#{toId},#{content},#{hasRead},#{conversationId},#{createdDate})"})
    int addMessage(Message message);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where conversation_id=#{conversationId} order by id desc limit #{offset}, #{limit}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset, @Param("limit") int limit);

    @Select({"select count(id) from ", TABLE_NAME, " where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int getConvesationUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

        @Select({"select ", INSERT_FIELDS, " ,count(id) as id from ( select * from ", TABLE_NAME, " where from_id=#{userId} or to_id=#{userId} order by id desc) tt group by conversation_id  order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                      @Param("offset") int offset, @Param("limit") int limit);
////cnt无法映射到message model中，所以用id(id在list中没用)
////    @Select({"select from_id, to_id, content, created_date, has_read, message.conversation_id, tt.id from message,(select conversation_id,count(id) as id from message group by conversation_id) tt WHERE created_date in (select max(created_date) from message group by conversation_id) and message.conversation_id=tt.conversation_id and (from_id= or to_id=) order by created_date desc limit ,"})
////    List<Message> getConversationList(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
//
//    @Select({"select from_id,to_id,conversation_id,has_read,content ,b.id as id,b.date as created_date from message,( select max(created_date) as date ,count(id) as id from message where from_id = or to_id = group by conversation_id) as b where message.created_date = b.date order by created_date desc limit 0,10 ,"})
//    List<Message> getConversationList(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
//    //select from_id,to_id,conversation_id,has_read,content ,b.id as id,b.date as created_date from message,( select max(created_date) as date ,count(id) as id from message where from_id = or to_id = group by conversation_id) as b where message.created_date = b.date limit 0, 10;
}
