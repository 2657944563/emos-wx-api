package com.example.emos.wx.db.service.contollerService;

import java.util.HashMap;

public interface CheckinService {
    public String validCanCheckIn(int userId, String data1);

    public void chikin(HashMap params);

    public void createFaceModel(int userId, String path);

    public void deleteFaceModel(int userId);
}
