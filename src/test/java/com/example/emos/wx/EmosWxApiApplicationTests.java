package com.example.emos.wx;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.TbRole;
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

}
