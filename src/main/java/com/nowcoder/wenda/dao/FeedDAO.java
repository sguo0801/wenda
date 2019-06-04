package com.nowcoder.wenda.dao;

import com.nowcoder.wenda.model.Feed;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface FeedDAO {
    String TABLE_NAME = " feed ";
    String INSERT_FIELDS = " user_id, data, created_date, type ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{data},#{createdDate},#{type})"})
    int addFeed(Feed feed);

    //推,根据id直接取数据.就是不需要拉,数据就显示出来,被推到页面.
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Feed getFeedById(int id);

    //拉,xml方式执行,因为有判断是否登录,并且需要循环得到关注列表里的user的信息
    //登录则是关注人的新鲜事,未登录则是所有人的最新的新鲜事
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
                               @Param("userIds") List<Integer> userIds,  //登录才会用,看关注人的新鲜事,关注人的列表,feed流来源
                               @Param("count") int count);  //分页
}
