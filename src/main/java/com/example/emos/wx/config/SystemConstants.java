package com.example.emos.wx.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author 2657944563
 * 存放打卡时间
 */
@Data
@Component
public class SystemConstants {
    private String attendanceStartTime;
    private String attendanceTime;
    private String attendanceEndTime;
    private String closingStartTime;
    private String closingTime;
    private String closingEndTime;
}
