package com.example.emos.wx;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.controller.from.SearchMonthCheckinForm;
import com.example.emos.wx.db.mapper.MessageDao;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbRole;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.MessageService;
import com.example.emos.wx.db.service.TbCheckinService;
import com.example.emos.wx.db.service.TbRoleService;
import com.example.emos.wx.db.service.TbUserService;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.task.MessageTask;
import com.mongodb.client.MongoClient;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
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
//        map.put("startDate", DateUtil.parse("2022-06-01"));
//        map.put("endDate", DateUtil.parse("2022-07-30"));
//        map.put("userId", 9);
//        ArrayList<HashMap> list = checkinService.searchWeekCheckin(map);
//        for (HashMap hashMap : list) {
//            System.out.println(hashMap.toString());
//        }
//        HashMap m = new HashMap();
//        m.put("userId", 9);
//        m.put("date", "2022-07-08");
//        final HashMap map1 = tbCheckinService.searchTodayCheckin(m);
//        System.out.println(map1);
        SearchMonthCheckinForm from = new SearchMonthCheckinForm();
        from.setMonth(7);
        from.setYear(2022);
        TbUser user = tbUserService.getOne(new QueryWrapper<TbUser>().select("hiredate").eq("id", 9));
        Date hiredate = user.getHiredate();
        String month = from.getMonth() < 10 ? "0" + from.getMonth() : from.getMonth() + "";
        StringBuilder stringBuilder = new StringBuilder().append(from.getYear()).append("-").append(month).append("-");
        System.out.println(stringBuilder);
        DateTime startDate = DateUtil.parse(stringBuilder.append("01"));
        if (startDate.before(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("仅能查询入职后的考勤记录");
        }
        if (startDate.before(hiredate)) {
            startDate = DateUtil.date(hiredate);
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap map = new HashMap();
        map.put("userId", 9);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        System.out.println(R.ok().put("result", tbCheckinService.searchMonthCheckin(map)));

    }

    @Autowired
    MessageDao messageServiceImpl;

    @Test
    void mongodbTempTest() {
        System.out.println(messageServiceImpl.searchMessageById("600bea9ab5bafb311f147506"));
    }

    @Resource
    MessageTask messageTask;
    @Resource
    ConnectionFactory connectionFactory;
    @Resource
    MessageService messageService;

    @Test
    void rebbitmqTest() {
        try (Connection connection = connectionFactory.newConnection()) {
            //   打开连接通道
            Channel channel = connection.createChannel();
            for (int i = 0; i < 1; i++) {
                MessageEntity message = new MessageEntity();
                message.setSenderId(0);
                message.setPhoto("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJ2ibFME7vKuDXrxfpF6yQoCrfulRibMTiac5OTnRuZYPib5b1iaLIUqQrFof5ddiam88znTs4mnVMdqqHw/132");
                message.setSenderName("测试消息");
                message.setUuid(IdUtil.simpleUUID());
                message.setMsg("欢迎您注册称为超级管理员，请及时更新你的员工个人信息");
                message.setSendTime(new Date());
                String messageId = messageService.insertMessage(message);
                //   通道连接队列，不存在就创建 队列名字 持久化 排他(加锁) 自动删除队列
                channel.queueDeclare("9", true, false, false, null);
                HashMap map = new HashMap();
                map.put("messageId", messageId);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            channel.basicAck(1,true);
//            创建AMQP的请求参数,使用开启消息持久化的参数构建，并且传递userid
                AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder().headers(map).build();
//            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
//            发布消息 指定交换机名 路由规则 消息参数 消息正文
                channel.basicPublish("", "9", properties, message.getMsg().getBytes(StandardCharsets.UTF_8));
                log.debug("消息发送成功" + message.getMsg());
//                messageTask.sendAsync("9", message);
            }
        } catch (Exception e) {
            log.warn("执行异常", e);
            throw new EmosException("消息发送失败");
        }

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
