package com.example.emos.wx.db.service.contollerService;

import java.util.Set;

public interface UserService {
    Integer registerUser(String registerCode, String code, String name, String imgUrl);

    Set<String> searchUserPermissions(Integer userid);

    public Integer login(String code);
}
