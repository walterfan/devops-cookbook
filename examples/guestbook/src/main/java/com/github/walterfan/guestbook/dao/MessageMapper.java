package com.github.walterfan.guestbook.dao;

/**
 * Created by walter on 06/11/2016.
 */


import com.github.walterfan.guestbook.domain.GenericQuery;
import com.github.walterfan.guestbook.domain.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MessageMapper extends MessageDao {

    //#{author.id}
    @Insert("INSERT into message(id,title,content,tags, createTime) " +
            "VALUES(#{id}, #{title}, #{content}, #{tags}, #{createTime})")
    void createMessage(Message message);

    @Select("SELECT * FROM message WHERE id = #{id}")
    Message retrieveMessage(String id);

    @Update("UPDATE message SET title=#{title}, content =#{content}, tags=#{tags} , " +
            " WHERE id =#{id}")
    void updateMessage(Message message);

    @Delete("DELETE FROM message WHERE id =#{id}")
    void deleteMessage(String id);

    @Select("SELECT * FROM message ")
    List<Message> queryMessage(GenericQuery query);

}
