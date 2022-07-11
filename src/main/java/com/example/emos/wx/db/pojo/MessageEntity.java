package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * mongodb存储的消息主体
 *
 * @author 2657944563
 */
@Data
@Document("message")
public class MessageEntity implements Serializable {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private Integer senderId;
    private String photo = "https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJ2ibFME7vKuDXrxfpF6yQoCrfulRibMTiac5OTnRuZYPib5b1iaLIUqQrFof5ddiam88znTs4mnVMdqqHw/132";
    @Indexed
    private Date sendTime;
    private String senderName;
    private String msg;
}
