package com.example.emos.wx;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.TbRole;
import com.example.emos.wx.db.service.TbCheckinService;
import com.example.emos.wx.db.service.TbRoleService;
import com.example.emos.wx.db.service.TbUserService;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
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
    TbUserService service;

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
    TbCheckinService checkinService;

    @Test
    void checkTest() {
        System.out.println(checkinService.validCanCheckIn(1, "1"));

    }

    @Test
    void str() {
        String s1 = new StringBuilder("zhang").append("san").toString();
        System.out.println(s1.intern() == s1);

    }

    @Resource
    TbCheckinService tbCheckinService;

    @Test
    void searchWeekCheckinTest() {
//        HashMap map = new HashMap();
//        map.put("startDate", DateUtil.parse("2022-07-05"));
//        map.put("endDate", DateUtil.parse("2022-07-10"));
//        map.put("userId", 9);
//        ArrayList<HashMap> list = checkinService.searchWeekCheckin(map);
//        for (HashMap hashMap : list) {
//            System.out.println(hashMap.toString());
//        }
        HashMap m = new HashMap();
        m.put("userId", 9);
        m.put("date", "2022-07-08");
        final HashMap map1 = tbCheckinService.searchTodayCheckin(m);
        System.out.println(map1);

    }

    public static void main(String[] args) {
//        HttpRequest get = HttpUtil.createGet("http://m.suining.bendibao.com/news/yqdengji/?qu=%E5%AE%89%E5%B1%85%E5%8C%BA");
//        get.header("User-Agetn", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.66 Safari/537.36 Edg/103.0.1264.44");
//        HttpResponse execute = get.execute();
////        //document.querySelector(".list-content").lastElementChild.innerHTML
////        String s = HttpUtil.get("http://m.suining.bendibao.com/news/yqdengji/?qu=%E5%AE%89%E5%B1%85%E5%8C%BA");
//        System.out.println(Jsoup.parse(execute.body()).getElementsByClass("list-content").get(0).select("p").last().text());
//        System.out.println(HttpUtil.encodeParams("http://m.suining.bendibao.com/news/yqdengji/?qu=安居区", StandardCharsets.UTF_8));
//        try {
//            System.out.println(URLEncoder.encode("http://m.suining.bendibao.com/news/yqdengji/?qu=安居区", "UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.out.println(LocalDate.now());
//        System.out.println(LocalTime.now());
//        System.out.println(LocalDateTime.now());
//        System.out.println(new Date());
//        System.out.println(new DateTime());
//        System.out.println(DateUtil.today());
//        System.out.println(new DateTime(LocalDateTime.now()));
//        System.out.println((Date)new DateTime());

//        HttpRequest request = HttpUtil.createPost("https://api-cn.faceplusplus.com/facepp/v3/compare");
//        request.form("image_file1", FileUtil.file("D:/TEMP/1.jpg"));
//        request.form("image_file2", FileUtil.file("D:/TEMP/2.jpg"));
//        request.form("api_key", "LaZ4iBEuh2f55A_O7AhJd1T-3cTuwb7p");
//        request.form("api_secret", "ZJn4TjjnAL5JGDr4TBfPk05KsEhue2dH");
//        HttpResponse response = request.execute();
//        JSONObject jsonObject = JSONObject.parseObject(response.body());
//        JSONObject jsonObject = JSONObject.parseObject("{\"thresholds\":{\"1e-5\":73.975,\"1e-4\":69.101,\"1e-3\":62.327},\"confidence\":97.389,\"faces1\":[{\"face_token\":\"bfd3a2d64530dbc72b9cf7fc0dc60c70\",\"face_rectangle\":{\"top\":956,\"left\":160,\"width\":732,\"height\":732}}],\"request_id\":\"1657205701,c0263e89-25b0-4da4-9b6e-7dcdf6d77224\",\"image_id2\":\"/0hdrVdnbWUIHmHSO4O1IA==\",\"time_used\":841,\"faces2\":[{\"face_token\":\"e83fdf9fad0d5d90c85dccc8ce6d5c91\",\"face_rectangle\":{\"top\":956,\"left\":160,\"width\":732,\"height\":732}}],\"image_id1\":\"/0hdrVdnbWUIHmHSO4O1IA==\"}\n");
//        BigDecimal xs = (BigDecimal) jsonObject.get("confidence"); //相似度
//        System.out.println(jsonObject.getString("confidence"));
//        Float confidence = Float.parseFloat(xs);
//        System.out.println(jsonObject);
//        System.out.println(Float.parseFloat(xs.toString()) > 90);
//
//        System.out.println("-----------------------");
//        System.out.println(response.body());
//        System.out.println(jsonObject.get("time_used"));
//        System.out.println("-----------------------");


    }

}
