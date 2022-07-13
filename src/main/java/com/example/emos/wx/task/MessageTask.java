package com.example.emos.wx.task;


import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.db.service.MessageService;
import com.example.emos.wx.exception.EmosException;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
//@Scope("prototype")
@Slf4j
public class MessageTask {
    @Resource
    ConnectionFactory connectionFactory;
    @Resource
    MessageService messageService;

    /**
     * 同步写入消息到RabbitMQ，Mongodb
     *
     * @param topic  消息主题
     * @param entity 消息体
     */
    public void send(String topic, MessageEntity entity) {
        String messageId = messageService.insertMessage(entity);
//        获得连接对象
        try (Connection connection = connectionFactory.newConnection()) {
//            打开连接通道
            Channel channel = connection.createChannel();
//            通道连接队列，不存在就创建 队列名字 持久化 排他(加锁) 自动删除队列
            channel.queueDeclare(topic, true, false, false, null);
            HashMap map = new HashMap();
            map.put("messageId", messageId);
//            channel.basicAck(1,true);
//            创建AMQP的请求参数,使用开启消息持久化的参数构建，并且传递userid
            AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder().headers(map).build();
//            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
//            发布消息 指定交换机名 路由规则 消息参数 消息正文
            channel.basicPublish("", topic, properties, entity.getMsg().getBytes(StandardCharsets.UTF_8));
            log.debug("消息发送成功" + entity.getMsg());
        } catch (Exception e) {
            log.warn("执行异常", e);
            throw new EmosException("消息发送失败");
        }
    }

    /**
     * 异步调用向MQ发送消息
     *
     * @param topic  消息主题
     * @param entity 消息体
     */
    @Async
    public void sendAsync(String topic, MessageEntity entity) {
        send(topic, entity);
    }

    /**
     * 同步读取用户的消息，存储mongodb中
     *
     * @param topic 用户id作为消息主题
     * @return 返回读取了多少条消息
     */
    public int receive(String topic) {
//        统计读取了几条记录
        int i = 0;
        try (Connection connection = connectionFactory.newConnection()) {
//            打开连接通道
            Channel channel = connection.createChannel();
//            通道连接队列，不存在就创建 队列名字 持久化 排他(加锁) 自动删除队列
            channel.queueDeclare(topic, true, false, false, null);
            while (true) {
//                轮询接收队列里的消息 取消自动应答
                GetResponse response = channel.basicGet(topic, false);
                if (response != null) {
                    AMQP.BasicProperties properties = response.getProps();
                    Map<String, Object> headers = properties.getHeaders();
//                    获得之前绑定的消息Id
                    String messageId = headers.get("messageId").toString();
//                    获得传入消息的正文
                    String mesageBody = new String(response.getBody());
                    log.debug("接收到：" + mesageBody);

                    MessageRefEntity messageRef = new MessageRefEntity();
                    messageRef.setMessageId(messageId);
                    messageRef.setReceiverId(Integer.parseInt(topic));
                    messageRef.setReadFlag(false);
                    messageRef.setLastFlag(true);
                    messageService.insertRef(messageRef);
//                    获得这条消息的信封，然后获得消息标签
                    final long deliveryTag = response.getEnvelope().getDeliveryTag();
//                    应答收到了这条消息，false：不应答所有消息，只应答传递的这条指定消息确认
                    channel.basicAck(deliveryTag, false);
                    ++i;
                } else {


                    break;
                }
            }
        } catch (Exception e) {
            log.warn("执行异常", e);
            throw new EmosException("消息接收失败");
        }
        return i;
    }

    /**
     * 异步读取用户的消息
     *
     * @param topic 用户消息队列主题 userId
     * @return 返回读取了多少消息
     */
    @Async
    public int receiveAsync(String topic) {
        return receive(topic);
    }


    /**
     * 删除rabbitMQ中的用户消息队列
     *
     * @param topic 用户消息队列主题 userId
     */
    public void deleteQueue(String topic) {
//        获得连接对象
        try (Connection connection = connectionFactory.newConnection()) {
//            打开连接通道
            Channel channel = connection.createChannel();
            channel.queueDelete(topic);
            log.debug("删除队列成功");
        } catch (Exception e) {
            log.warn("删除队列异常", e);
            throw new EmosException("队列移除异常");
        }
    }

    @Async
    public void deleteQueueAsync(String topic) {
        deleteQueue(topic);
    }
}
