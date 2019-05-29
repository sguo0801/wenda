package com.nowcoder.wenda.dao;

import com.nowcoder.wenda.model.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserDAO {   //操作数据库的接口,下面直接连Model与数据库字段相对应,跟数据库做交互
    String TABLE_NAME = " user ";
    String INSERT_FIELDS = " name, password, salt, head_url ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;
    //简单的sql语句通过注解实现,复杂的可以通过xml
    @Insert({" insert into " , TABLE_NAME, " ( ", INSERT_FIELDS, " ) values (#{name}, #{password}, #{salt}, #{headUrl}) "})  //里面values后面读取的是User里面的字段
    int addUser(User user);
//    @Insert({" insert into ", TABLE_NAME, " (", INSERT_FIELDS, ") values(#{name}, #{password}, #{salt}, #{headUrl})"})
//    int addUser(User user);

    @Select({" select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id} "})
    User selectById(int id);

    @Select({" select ", SELECT_FIELDS, " from ", TABLE_NAME, " where name=#{name} "})    //登录注册需要根据名字查找数据库内的账号
    User selectByName(String name);

    @Update({" update ", TABLE_NAME, " set password=#{password} where id=#{id} "})
    void updatePassword(User user);

    @Delete({" delete from ", TABLE_NAME, " where id=#{id} "})
    void deleteById(int id);
}
