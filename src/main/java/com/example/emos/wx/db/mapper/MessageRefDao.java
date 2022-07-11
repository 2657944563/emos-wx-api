package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MessageRefDao {
    @Resource
    MongoTemplate mongoTemplate;

    /**
     * 插入一条消息引用
     *
     * @param messageRefEntity 消息引用主体
     * @return 返回插入后的id
     */
    public String insert(MessageRefEntity messageRefEntity) {
        return mongoTemplate.save(messageRefEntity).get_id();
    }

    /**
     * 查找某个用户的的未读消息条数
     *
     * @param userId 用户id
     * @return 返回未读消息数
     */
    public long searchUnreadCount(int userId) {
//        构造查询对象,readFlag == false and receiverId == userId
        Query query = new Query().addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId));
        return mongoTemplate.count(query, MessageRefEntity.class);

    }

    /**
     * 查找用户的新消息
     *
     * @param userId 用户id
     * @return 返回拥有多少新消息
     */
    public long searchLastCount(int userId) {
//        构造查询对象,lastFlag == true and receiverId == userId
        Query query = new Query().addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId));
        Update update = new Update();
//        设置更新字段
        update.set("lastFlag", false);
//        调用更新
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, "message_ref");
//        返回更新的条目数
        return updateResult.getModifiedCount();
    }

    /**
     * 将消息设置为已读状态
     *
     * @param messageId 设置的消息id
     * @return 返回修改消息的条数
     */
    public long updateUnreadMessage(String messageId) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(messageId));
        Update update = new Update();
        update.set("readFlag", true);
        UpdateResult message_ref = mongoTemplate.updateFirst(query, update, "message_ref");
        return message_ref.getModifiedCount();
    }

    /**
     * 删除用户的消息引用
     *
     * @param messageId 消息id
     * @return 删除了多少条
     */
    public long deleteMessageRefById(String messageId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(messageId));
//        删除并且返回删除的条数
        return mongoTemplate.remove(query, "message_ref").getDeletedCount();
    }

    /**
     * 清空用户的消息引用
     *
     * @param userId 用户的id
     * @return 返回删除了多少个引用消息
     */
    public long deleteUserMessageRef(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId));
//        删除并且返回删除了多少消息
        return mongoTemplate.remove(query, "message_ref").getDeletedCount();
    }
}
