package com.example.emos.wx.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.db.pojo.SysConfig;
import com.example.emos.wx.db.pojo.TbUser;

import java.util.List;
import java.util.Set;

/**
 * @author 2657944563
 * @description 针对表【tb_user(用户表)】的数据库操作Service
 * @createDate 2022-06-29 16:33:12
 */
public interface TbUserService extends IService<TbUser> {
    Integer registerUser(String registerCode, String code, String name, String imgUrl);

    Set<String> searchUserPermissions(Integer userid);

    Integer login(String code);

    List<SysConfig> allCheckTime();

    TbUser selectByUserId(Integer userId);
}
