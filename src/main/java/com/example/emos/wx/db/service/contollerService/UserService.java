package com.example.emos.wx.db.service.contollerService;

import com.example.emos.wx.db.pojo.SysConfig;
import com.example.emos.wx.db.pojo.TbUser;

import java.util.List;
import java.util.Set;

public interface UserService {
    Integer registerUser(String registerCode, String code, String name, String imgUrl);

    Set<String> searchUserPermissions(Integer userid);

    Integer login(String code);

    List<SysConfig> allCheckTime();

    TbUser selectByUserId(Integer userId);
}
