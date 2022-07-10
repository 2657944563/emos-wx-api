package com.example.emos.wx.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author 2657944563
 * @description 针对表【tb_checkin(签到表)】的数据库操作Mapper
 * @createDate 2022-06-29 16:33:12
 * @Entity com.example.emos.wx.db.pojo.TbCheckin
 */
@Mapper
public interface TbCheckinMapper extends BaseMapper<TbCheckin> {

    /**
     * 查询用户信息,用于展示用户界面
     *
     * @param map 用户id,需要查询的指定日期（yyyy-MM-mm）
     * @return 返回用户查询数据 name photo deptName address status risk checkinTime date
     */
    HashMap searchTodayCheckin(HashMap map);

    /**
     * 查询用户总考勤次数
     *
     * @param userId 用户id
     * @return 返回用户总考勤次数
     */
    Long searchCheckinDays(Integer userId);

    /**
     * 查询用户 userId 指定时间端startTime - endTime 的考勤次数
     *
     * @param map startTime:开始时间  endTime:截至时间
     * @return 返回开始(包括)以及直到截至(包括)时间考勤的记录 date status
     */
    ArrayList<HashMap> searchWeekCheckin(HashMap map);
}




