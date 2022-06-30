package com.example.emos.wx;

import com.example.emos.wx.db.mapper.TbRoleMapper;
import com.example.emos.wx.db.service.TbRoleService;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    RedisTemplate<String,String> redisTemplate;
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
    TbRoleService tbRoleService;
    @Autowired
    TbRoleMapper tbRoleMapper;
    @Test
    void mybatisplusTest() {
        System.out.println(tbRoleMapper.selectAllByIdTbRole());
    }
}
