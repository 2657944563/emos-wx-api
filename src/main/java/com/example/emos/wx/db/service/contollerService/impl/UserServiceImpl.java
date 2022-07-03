package com.example.emos.wx.db.service.contollerService.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.contollerService.UserService;
import com.example.emos.wx.db.service.sqlService.TbUserService;
import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @author 2657944563
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Value("${emos.wx.secret}")
    private String secret;
    @Value("${emos.wx.appId}")
    private String appId;
    @Value("${emos.wx.registerCode}")
    private String registerCode;
    @Resource
    TbUserService tbUserService;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap<String, Object> headers = new HashMap<>(8);
        headers.put("appid", appId);
        headers.put("secret", secret);
        headers.put("js_code", code);
        headers.put("grant_type", "authorization_code");
        String post = HttpUtil.post(url, headers);
        JSONObject jsonObject = JSONUtil.parseObj(post);
        String openid = (String) jsonObject.get("openid");
        if (StrUtil.isBlank(openid)) {
            throw new RuntimeException("微信登录凭证异常");
        }
        return openid;
    }


    /**
     * 注册账号：获取用户唯一id，判断是否在数据库，向数据库注册账号或者验证账号
     *
     * @param wxRegisterCode 微信注册码
     * @param code           用户临时code
     * @param name           用户名
     * @param imgUrl         用户头衔地址
     * @return 返回用户在数据库中的id
     */
    @Override
    public Integer registerUser(String wxRegisterCode, String code, String name, String imgUrl) {

        String openId = getOpenId(code);
        System.out.println("openId:" + openId);
        if (registerCode.equals(wxRegisterCode)) {
            TbUserMapper baseMapper = (TbUserMapper) tbUserService.getBaseMapper();
            if (baseMapper.haveRootUser()) {
                throw new EmosException("管理员冲突,无法绑定超级管理员账号");
            } else {
                TbUser admin = new TbUser();
                admin.setName(name);
                admin.setPhoto(imgUrl);
                admin.setOpenId(openId);
                admin.setRole("[0]");
                admin.setStatus(1);
                admin.setCreateTime(new Date());
                admin.setRoot(1);
                tbUserService.save(admin);
                //查看是否返回id
                return admin.getId();
            }
        } else {
            //TODO 普通员工注册
            System.out.println("普通员工注册");
            System.out.println("--------------");
            System.out.println(registerCode);
            System.out.println(wxRegisterCode);
            System.out.println("--------------");
        }
        return 1;
    }

    @Override
    public Set<String> searchUserPermissions(Integer userId) {
        TbUserMapper baseMapper = (TbUserMapper) tbUserService.getBaseMapper();
        return baseMapper.searchUserPermissions(3);
    }
}
