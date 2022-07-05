package com.example.emos.wx.db.service.contollerService.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbHolidays;
import com.example.emos.wx.db.pojo.TbWorkday;
import com.example.emos.wx.db.service.contollerService.CheckinService;
import com.example.emos.wx.db.service.sqlService.TbCheckinService;
import com.example.emos.wx.db.service.sqlService.TbHolidaysService;
import com.example.emos.wx.db.service.sqlService.TbWorkdayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author 2657944563
 */
@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {
    @Resource
    SystemConstants systemConstants;
    @Resource
    TbHolidaysService tbHolidaysService;
    @Resource
    TbCheckinService tbCheckinService;
    @Resource
    TbWorkdayService tbWorkdayService;

    /**
     * 查询是否能够考勤
     *
     * @param userId 用户id
     * @param data   考勤日期
     * @return 考勤消息：能够考勤，不能考勤，节假日不需要考勤....
     */
    @Override
    public String validCanCheckIn(int userId, String data) {
        boolean b_1 = tbHolidaysService.getOne(new QueryWrapper<TbHolidays>().eq("date", LocalDate.now())) != null;
        boolean b_2 = tbWorkdayService.getOne(new QueryWrapper<TbWorkday>().eq("date", LocalDate.now())) != null;
        System.out.println("是否是节假日：" + b_1);
        System.out.println("是否是工作日：" + b_2);
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (b_1) {
            type = "节假日";
        } else if (b_2) {
            type = "工作日";
        }
        if ("节假日".equals(type)) {
            return "今天是节假日,不需要考勤";
        } else {
            LocalDateTime now = LocalDateTime.now();
            String start = LocalDate.now() + "T" + systemConstants.getAttendanceStartTime();
            String end = LocalDate.now() + "T" + systemConstants.getAttendanceEndTime();
            System.out.println(now);
            if (now.isBefore(LocalDateTime.parse(start))) {
                return "没有到上班考勤时间";
            } else if (now.isAfter(LocalDateTime.parse(end))) {
                return "已经过了上班考勤时间";
            } else {
                TbCheckin one = tbCheckinService.getOne(new QueryWrapper<TbCheckin>()
                        .eq("id", userId)
                        .eq("date", LocalDate.now())
                        .ge("create_time", LocalDateTime.parse(start))
                        .le("create_time", LocalDateTime.parse(end)));
                return one == null ? "可以考勤" : "今日已经考勤,不用重复考勤";
            }
        }

    }

}
