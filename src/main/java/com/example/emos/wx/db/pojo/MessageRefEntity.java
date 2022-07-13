package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("message_ref")
public class MessageRefEntity implements Serializable {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String messageId;
    @Indexed
    private Integer receiverId;
    @Indexed
    private Boolean lastFlag;
    @Indexed
    private Boolean readFlag;
}
