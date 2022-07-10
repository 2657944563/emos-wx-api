package com.example.emos.wx.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.db.pojo.TbCheckin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author 2657944563
 * @description 针对表【tb_checkin(签到表)】的数据库操作Service
 * @createDate 2022-06-29 16:33:12
 */
public interface TbCheckinService extends IService<TbCheckin> {

    String validCanCheckIn(int userId, String data1);

    void chikin(HashMap params);

    void createFaceModel(int userId, String path);

    void deleteFaceModel(int userId);

    HashMap searchTodayCheckin(HashMap map);

    long searchCheckinDays(Integer userId);

    ArrayList<HashMap> searchWeekCheckin(HashMap params);

    ArrayList<HashMap> searchMonthCheckin(HashMap params);
}
