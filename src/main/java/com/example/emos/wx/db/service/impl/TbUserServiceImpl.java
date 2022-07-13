package com.example.emos.wx.db.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.SysConfig;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.SysConfigService;
import com.example.emos.wx.db.service.TbUserService;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 2657944563
 * @description 针对表【tb_user(用户表)】的数据库操作Service实现
 * @createDate 2022-06-29 16:33:12
 */
@Service
@Slf4j
@Scope("prototype")
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser>
        implements TbUserService {
    @Value("${emos.wx.secret}")
    private String secret;
    @Value("${emos.wx.appId}")
    private String appId;
    @Value("${emos.wx.rootRegisterCode}")
    private String rootRegisterCode;
    @Resource
    @Lazy
    TbUserService tbUserService;
    @Resource
    SysConfigService sysConfigService;
    @Resource
    MessageTask messageTask;

    @Resource
    RedisTemplate redisTemplate;

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
     * @param nickName       用户名
     * @param imgUrl         用户头衔地址
     * @return 返回用户在数据库中的id
     */
    @Override
    public Integer registerUser(String wxRegisterCode, String code, String nickName, String imgUrl) {

        String openId = getOpenId(code);
//        System.out.println("openId:" + openId);
        if (rootRegisterCode.equals(wxRegisterCode)) {
            TbUserMapper baseMapper = (TbUserMapper) tbUserService.getBaseMapper();
            if (baseMapper.haveRootUser()) {
                throw new EmosException("管理员冲突,无法绑定超级管理员账号");
            } else {
                TbUser admin = new TbUser();
                admin.setNickname(nickName);
                admin.setPhoto(imgUrl);
                admin.setOpenId(openId);
                admin.setRole("[0]");
                admin.setStatus(1);
                admin.setCreateTime(new Date());
                admin.setRoot(1);
                tbUserService.save(admin);
                //向消息队列推送注册消息
                MessageEntity message = new MessageEntity();
                message.setSenderId(0);
                message.setSenderName("系统消息");
                message.setUuid(IdUtil.simpleUUID());
                message.setMsg("欢迎您注册称为超级管理员，请及时更新你的员工个人信息");
                message.setSendTime(new Date());
                messageTask.sendAsync(admin.getId() + "", message);
                return admin.getId();
            }
        } else {
            //TODO 普通员工注册
            System.out.println("普通员工注册");
            System.out.println("--------------");
            System.out.println(rootRegisterCode);
            System.out.println(wxRegisterCode);
            System.out.println(code);
            System.out.println(nickName);
            System.out.println(imgUrl);
            System.out.println("--------------");
        }
        return 7;
    }

    /**
     * 查询用户权限，先看redis有没有用户权限列表，没有再去数据库获取并且存储
     *
     * @param userId
     * @return
     */
    @Override
    public Set<String> searchUserPermissions(Integer userId) {
        Set<String> per = (Set<String>) getRedisValue(userId + ":permission");
        if (per != null) {
//            log.error("redis中有用户权限数据");
            return per;
        }
//        log.error("redis中没有用户权限数据");
        TbUserMapper baseMapper = (TbUserMapper) tbUserService.getBaseMapper();
        Set<String> permissions = baseMapper.searchUserPermissions(userId);
        setRedisValue(userId + ":permission", permissions, 5, TimeUnit.DAYS);
        return permissions;
    }


    @Override
    public Integer login(String code) {
        TbUser open_id = tbUserService.getOne(new QueryWrapper<TbUser>().eq("open_id", getOpenId(code)));
        if (open_id == null) {
            throw new EmosException("账户不存在");
        }
//
//        System.out.println(messageTask.receiveAsync(Integer.toString(open_id.getId())));
//        TODO 用户登录消息处理
        return open_id.getId();
    }

    /**
     * 获得考勤打卡时间段落，上班下班时间
     *
     * @return
     */
    @Override
    public List<SysConfig> allCheckTime() {
        return sysConfigService.list(new QueryWrapper<SysConfig>().eq("status", 1));
    }

    @Override
    public TbUser selectByUserId(Integer userId) {
        return tbUserService.getById(userId);
    }

    private Object getRedisValue(String key) {
        if (redisTemplate.hasKey(key)) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    private void setRedisValue(String key, Object value, Integer expire, TimeUnit timeType) {
        redisTemplate.opsForValue().set(key, value, expire, timeType);
    }
}




