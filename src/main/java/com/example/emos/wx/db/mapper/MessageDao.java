package com.example.emos.wx.db.mapper;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * mongodb消息发送服务
 *
 * @author 2657944563
 */
@Repository
public class MessageDao {
    @Resource
    MongoTemplate mongoTemplate;

    /**
     * 插入一条消息
     *
     * @param entity 消息pojo
     * @return 返回消息的id
     */
    public String insert(MessageEntity entity) {
        Date sendTime = entity.getSendTime();
//        系统存放的时间时区不同，适配时区(格林尼治时间)
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        entity.setSendTime(sendTime);
        return mongoTemplate.insert(entity).get_id();
    }

    /**
     * 联合查询某个用户的消息，分页
     *
     * @param userId 用户id
     * @param start  分页跳过多少条记录
     * @param length 分页每页限制显示多少条记录
     * @return 查询到的消息记录列表
     */
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        JSONObject json = new JSONObject();
        json.set("$toString", "$_id");
        Aggregation aggregation = Aggregation.newAggregation(
//                额外引入自定义字段,字段名 id , 字段来源:上面json对应的表达方式:将_id转换成string格式
                Aggregation.addFields().addField("id").withValue(json).build(),
//                联合集合查询, message_ref集合中的id == messageId
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),
//                条件,引用数据的 receiverId == userId
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
//                降序,按照某个字段降序
                Aggregation.sort(Sort.Direction.DESC, "sendTime"),
//                跳过多少,限制显示多少
                Aggregation.skip(start),
                Aggregation.limit(length)
        );
        AggregationResults<HashMap> maps = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = maps.getMappedResults();
        list.forEach(map -> {
            List<MessageRefEntity> ref = (List<MessageRefEntity>) map.get("ref");
            MessageRefEntity messageRefEntity = ref.get(0);
            Boolean readFlag = messageRefEntity.getReadFlag();
            String id = messageRefEntity.get_id();
//            传回的map中简化,将引用消息提取出来,然后删除引用消息
            map.put("readFlag", readFlag);
            map.put("refId", id);
//            用户引用消息的id
            map.remove("ref");
//            消息主体id
            map.remove("_id");
            Date sendTime = (Date) map.get("sendTime");
//            偏移时区(格林尼治时间)
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);

//            显示优化,当日的消息显示时间,往日的消息显示日期
            String today = DateUtil.today();
            if (today.equals(DateUtil.date(sendTime).toDateStr())) {
                map.put("sendTime", DateUtil.format(sendTime, "HH:mm"));
            } else {
                map.put("sendTime", DateUtil.format(sendTime, "yyyy/MM/dd"));
            }

        });
        return list;
    }

    public HashMap searchMessageById(String id) {
//        messageService.findOne(Example.of(messageEntity));
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");
        Date sendTime = (Date) map.get("sendTime");
//        修改时间(格林尼治时间)
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        map.replace("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm"));

        return map;
    }

}
