package com.example.emos.wx.db.service;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    public String insert(MessageRefEntity messageRefEntity);

    public long searchUnreadCount(int userId);

    public long searchLastCount(int userId);

    public long updateUnreadMessage(String messageId);

    public long deleteMessageRefById(String messageId);

    public long deleteUserMessageRef(int userId);

    public String insert(MessageEntity entity);

    public List<HashMap> searchMessageByPage(int userId, long start, int length);

    public HashMap searchMessageById(String id);
}
