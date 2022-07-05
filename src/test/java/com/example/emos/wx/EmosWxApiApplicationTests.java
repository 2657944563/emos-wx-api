package com.example.emos.wx;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.TbRole;
import com.example.emos.wx.db.service.contollerService.CheckinService;
import com.example.emos.wx.db.service.contollerService.UserService;
import com.example.emos.wx.db.service.sqlService.TbRoleService;
import com.example.emos.wx.db.service.sqlService.TbUserService;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EmosWxApiApplicationTests {


    /**
     * 测试数据库连接状态
     */
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void jdbcTest() {
        System.out.println(jdbcTemplate.queryForList("select * from emos.tb_action"));
    }


    /**
     * 测试redis数据库连接状态
     */
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    void redisTest() {
        redisTemplate.opsForValue().set("test", "test", 100, TimeUnit.SECONDS);
        System.out.println(redisTemplate.getConnectionFactory());
        System.out.println(redisTemplate);
    }

    /**
     * 测试mongodb数据库的连接状态
     */
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    MongoClient mongoClient;

    @Test
    void mongoTest() {
        System.out.println(mongoTemplate);
    }

    /**
     * 测试mybatisplus是否可用
     */
    @Autowired
    TbUserService tbUserService;
    @Autowired
    TbRoleService tbRoleService;

    @Test
    void mybatisplusTest() {
        TbUserMapper baseMapper = (TbUserMapper) tbUserService.getBaseMapper();
        System.out.println(baseMapper.haveRootUser());
        System.out.println(tbRoleService.getOne(new QueryWrapper<TbRole>().eq("id", 0)));
    }

    @Autowired
    JwtUtil jwtUtil;
    @Value("${emos.jwt.cache-expire}")
    String cacheExpire;

    @Test
    void jwtTest() {
        System.out.println(jwtUtil.createToken(123));
        System.out.println(cacheExpire);
    }

    /**
     * 测试权限返回
     */
    @Autowired
    UserService service;

    @Test
    void testuser() {
        System.out.println(service.searchUserPermissions(3));
    }

    /**
     * 测试打卡时间获取
     */
    @Resource
    SystemConstants systemConstants;

    @Test
    void testCheckTime() {
        System.out.println(service.allCheckTime());
        System.out.println(systemConstants.toString());
    }

    /**
     * 测试
     */
    @Resource
    CheckinService checkinService;

    @Test
    void checkTest() {
        System.out.println(checkinService.validCanCheckIn(1, "1"));

    }

    @Test
    void str() {
        String s1 = new StringBuilder("zhang").append("san").toString();
        System.out.println(s1.intern() == s1);
    }

    public static void main(String[] args) {
        String s1 = new StringBuilder("z").append("a").toString();

        System.out.println(s1.intern() == s1);
        String s2 = new StringBuilder("ja").append("va").toString();
        System.out.println(s2.getClass());
        System.out.println(s2.intern().getClass());
        System.out.println(s2.intern() == s2);
        String s3 = new String("niubi");
        System.out.println(s3.intern() == s3.intern());
        final Runtime runtime = Runtime.getRuntime();
        System.out.println(System.getProperty("java.home"));
        System.out.println(new JwtUtil().createToken(1));
        System.out.println("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTc0NjYwMzYsInVzZXJJZCI6MX0.K-R40QBq0dz24IBz8lN5NpYvnJk0J3rBwpGVpxktALo\n");
    }

}
