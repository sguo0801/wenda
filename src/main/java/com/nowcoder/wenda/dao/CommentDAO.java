package com.nowcoder.wenda.dao;

import com.nowcoder.wenda.model.Comment;
import com.nowcoder.wenda.model.Question;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface CommentDAO {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status ";  //字段名,对应数据库的字段
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //添加评论的内容
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})"})  //values里面是内容,对应model的变量
    int addComment(Comment comment);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Comment getCommentById(int id);

    //做的是删除评论功能
    @Update({"update ", TABLE_NAME, " set status=#{status} where entity_id=#{entityId} and entity_type=#{entityType}"})
    void updateStatus(@Param("entityId") int entityId, @Param("entityType") int entityType, @Param("status") int status);

//    @Update({"update ", TABLE_NAME, "set status=#{status} where id=#{id}"})
//    int updateStatus(@Param("id") int id, @Param("status") int status);

    //根据评论位置的类型(包括id)来选择查找所有的评论.
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where entity_id=#{entityId} and entity_type=#{entityType} order by created_date desc"})   //根据时间来排序
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId, @Param("entityType") int entityType);  //成对出现,service在用这个方法的时候要求这两个参数
//    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
//            " where entity_id=#{entityId} and entity_type=#{entityType} order by created_date desc"})
//    List<Comment> selectCommentByEntity(@Param("entityId") int entityId, @Param("entityType") int entityType);

    //当a是表的列名时，count(a)为该表中a列的值不等于null的行的总数,这边的疑问是删除评论,那评论数??
    @Select({"select count(id) from ", TABLE_NAME, "  where entity_id=#{entityId} and entity_type=#{entityType}"})   //count(id)是计算id的数量,哪怕中间状态置1(删除).
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    int getUserCommentCount(int userId);

}
