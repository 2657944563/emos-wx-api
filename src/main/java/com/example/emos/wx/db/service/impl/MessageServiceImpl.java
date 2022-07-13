package com.example.emos.wx.db.service.impl;

import com.example.emos.wx.db.mapper.MessageDao;
import com.example.emos.wx.db.mapper.MessageRefDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.db.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    MessageDao messageDao;
    @Resource
    MessageRefDao messageRefDao;

    @Override
    public String insertRef(MessageRefEntity messageRefEntity) {
        return messageRefDao.insert(messageRefEntity);
    }

    @Override
    public long searchUnreadCount(int userId) {
        return messageRefDao.searchUnreadCount(userId);
    }

    @Override
    public long searchLastCount(int userId) {
        return messageRefDao.searchLastCount(userId);
    }

    @Override
    public long updateUnreadMessage(String messageId) {
        return messageRefDao.updateUnreadMessage(messageId);
    }

    @Override
    public long deleteMessageRefById(String messageId) {
        return messageRefDao.deleteMessageRefById(messageId);
    }

    @Override
    public long deleteUserMessageRef(int userId) {
        return messageRefDao.deleteUserMessageRef(userId);
    }

    @Override
    public String insertMessage(MessageEntity entity) {
        return messageDao.insert(entity);
    }

    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        return messageDao.searchMessageByPage(userId, start, length);
    }

    @Override
    public HashMap searchMessageById(String id) {
        return messageDao.searchMessageById(id);
    }
}
