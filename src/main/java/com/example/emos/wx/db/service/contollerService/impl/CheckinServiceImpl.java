package com.example.emos.wx.db.service.contollerService.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.pojo.*;
import com.example.emos.wx.db.service.contollerService.CheckinService;
import com.example.emos.wx.db.service.sqlService.*;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;

/**
 * @author 2657944563
 */
@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {
    @Resource
    SystemConstants systemConstants;
    @Resource
    TbHolidaysService tbHolidaysService;
    @Resource
    TbCheckinService tbCheckinService;
    @Resource
    TbWorkdayService tbWorkdayService;
    @Resource
    TbCityService tbCityService;
    //    人脸数据存储库
    @Resource
    TbFaceModelService tbFaceModelService;

    //    人脸数据模型创建接口地址
    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;
    //    人脸验证接口地址
    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;
    @Value("${emos.face.saveFaceModelUrl}")
    private String saveFaceModelUrl;
    @Value("${emos.face.deletFaceModelUrl}")
    private String deletFaceModelUrl;

    @Value("${emos.face.key}")
    private String faceKey;
    @Value("${emos.face.secret}")
    private String faceSecret;
    @Value("${emos.face.faceLevel}")
    private Float faceLevel;

    @Value("${emos.face.outher_id}")
    private String outherId;

    @Value("${emos.email.hr}")
    private String hrEmail;
    @Resource
    private EmailTask emailTask;

    /**
     * 查询是否能够考勤
     *
     * @param userId 用户id
     * @param data   考勤日期
     * @return 考勤消息：能够考勤，不能考勤，节假日不需要考勤....
     */
    @Override
    public String validCanCheckIn(int userId, String data) {

        boolean b_1 = tbHolidaysService.getOne(new QueryWrapper<TbHolidays>().eq("date", LocalDate.now())) != null;
        boolean b_2 = tbWorkdayService.getOne(new QueryWrapper<TbWorkday>().eq("date", LocalDate.now())) != null;
//        System.out.println("是否是节假日：" + b_1);
//        System.out.println("是否是工作日：" + b_2);
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (b_1) {
            type = "节假日";
        } else if (b_2) {
            type = "工作日";
        }
        if ("节假日".equals(type)) {
            return "节假日不需考勤";
        } else {
            LocalDateTime now = LocalDateTime.now();
            String start = LocalDate.now() + "T" + systemConstants.getAttendanceStartTime();
            String end = LocalDate.now() + "T" + systemConstants.getAttendanceEndTime();
//            System.out.println(now);
            if (now.isBefore(LocalDateTime.parse(start))) {
                return "未到考勤时间";
            } else if (now.isAfter(LocalDateTime.parse(end))) {
                return "超过考勤时间";
            } else {
                TbCheckin one = tbCheckinService.getOne(new QueryWrapper<TbCheckin>()
                        .eq("id", userId)
                        .eq("date", LocalDate.now())
                        .ge("create_time", LocalDateTime.parse(start))
                        .le("create_time", LocalDateTime.parse(end)));
                return one == null ? "可以考勤" : "今日已考勤";
            }
        }

    }

    /**
     * 人脸签到，验证签到是否忙满足时间，验证是否拥有人脸模型，验证是否通过人脸签到验证
     *
     * @param params 前端获得的地址信息，临时存储的需要验证的图片位置，userId
     */
    @Override
    public void chikin(HashMap params) {
//        默认考勤状态 1：正常  2：迟到
        int status = 1;
        LocalTime date = LocalTime.now();

        if (date.isBefore(LocalTime.parse(systemConstants.getAttendanceTime()))
                && date.isAfter(LocalTime.parse(systemConstants.getAttendanceStartTime()))) {
            status = 1;
            //考勤开始之前

        }
        if (date.isAfter(LocalTime.parse(systemConstants.getAttendanceTime()))
                && date.isBefore(LocalTime.parse(systemConstants.getAttendanceEndTime()))) {
            status = 2;
            //考勤结束之后
        }
        Integer userId = (Integer) params.get("userId");
//        获取数据库中的人脸模型
        TbFaceModel userFace = tbFaceModelService.getOne(new QueryWrapper<TbFaceModel>().eq("user_id", userId));
        if (userFace == null) {
            throw new EmosException("不存在人脸模型");
        } else {
//            请求人脸识别接口，判断人脸数据 path1:本地存储的人脸模型 path2:前端传入验证的人脸模型
            String path = (String) params.get("path");
            HttpRequest request = getFaceRequest(checkinUrl);
            request.form("image_file1", FileUtil.file(path));
            request.form("face_token2", userFace.getFaceModel());
            HttpResponse response = request.execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body());
            //相似度
//            BigDecimal bigDecimal = (BigDecimal) jsonObject.get("confidence");
//            Float confidence = Float.parseFloat(bigDecimal.toString());
            Float confidence = jsonObject.getFloat("confidence");
//            System.out.println("人脸匹配度：" + confidence);
//            System.out.println(jsonObject);

            //接口传入的错误数据
            if (response.getStatus() != 200 && jsonObject.get("error_message") != null) {
                log.warn(this.getClass().getName() + "人脸数据接口异常" + jsonObject.get("error_message"));
                throw new EmosException("人脸数据接口异常");
            }
//            System.out.println("status:" + response.getStatus());
            if (response.getStatus() != 200) {
                log.warn(this.getClass().getName() + "人脸图片异常");
                log.warn((String) jsonObject.get("error_message"));
                throw new EmosException("人脸图片异常");
            } else if (jsonObject.getJSONArray("faces1").size() > 1) {
                throw new EmosException("检测到多个人脸");
            } else if (confidence == null || jsonObject.getJSONArray("faces1").size() == 0) {
                throw new EmosException("未检测到人脸");
            } else if (confidence < faceLevel) {
//                如果人脸匹配数据相似度不足
                throw new EmosException("人脸匹配不达标");
            } else {
//                System.out.println("人脸匹配度：" + confidence);
                // resk 疫情等级 1：低风险 2：中风险 3：高风险
                int resk = 1;
                if (StrUtil.isBlank((String) params.get("city")) || StrUtil.isBlank((String) params.get("district"))) {
                    throw new EmosException("位置信息错误");
                }
                // 城市编码
                String cityCode = tbCityService.getOne(new QueryWrapper<TbCity>().eq("city", params.get("city"))).getCode();
                // 获取区级
                String district = (String) params.get("district");
                //请求疫情解析页面
                String responseBody = request("http://m." + cityCode + ".bendibao.com/news/yqdengji/?qu=" + district, "get");
                //疫情等级
                String yqLevel = null;
                try {
                    yqLevel = Jsoup.parse(responseBody).getElementsByClass("list-content").get(0).select("p").last().text();
                } catch (Exception e) {
                    log.error("疫情信息获取失败： " + this.getClass().getName());
                    throw new EmosException("疫情信息获取失败");
                }
                System.out.println("疫情等级：" + yqLevel);
//                yqLevel = "高风险";
                if ("高风险".equals(yqLevel)) {
                    resk = 3;
                    // TODO 发邮箱给管理员警告
                    SimpleMailMessage emailMessage = new SimpleMailMessage();
                    emailMessage.setTo(hrEmail);
                    emailMessage.setSubject("员工风险警报");
                    emailMessage.setText("高风险：" + userId);
                    emailTask.sendAsync(emailMessage);

                } else if ("中风险".equals(yqLevel)) {
                    resk = 2;
                    // TODO 发邮箱给管理员警告
                } else if ("低风险".equals(yqLevel)) {
                    resk = 1;
                } else {
                    throw new EmosException("疫情风险等级错误");
                }
                //  保存签到记录
                TbCheckin tbCheckin = new TbCheckin();
                tbCheckin.setAddress((String) params.get("address"));
                tbCheckin.setCity((String) params.get("city"));
                tbCheckin.setProvince((String) params.get("province"));
                tbCheckin.setCountry((String) params.get("country"));
                tbCheckin.setDistrict((String) params.get("district"));
                tbCheckin.setUserId(userId);
                tbCheckin.setRisk(resk);
                tbCheckin.setStatus(status);
                tbCheckin.setDate(new Date());
                tbCheckin.setCreateTime(new DateTime(date));
                //重复签到前置处理，删除今天的签到
                tbCheckinService.remove(new QueryWrapper<TbCheckin>().eq("user_id", userId).eq("date", LocalDate.now()));
                if (!tbCheckinService.save(tbCheckin)) {
                    throw new EmosException("签到失败");
                }

            }
        }

    }

    /**
     * 通过path路径的图片创建用户面部数据模型
     *
     * @param userId 用户id
     * @param path   用户上传的面部图片
     */
    @Override
    public void createFaceModel(int userId, String path) {
//        获取人脸模型数据
        HttpRequest request = getFaceRequest(createFaceModelUrl);
        request.form("image_file", FileUtil.file(path));
        request.form("return_landmark", 2);
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            log.error("人脸服务端错误");
            throw new EmosException("人脸服务器错误");
        }
        JSONObject jsonObject = JSONObject.parseObject(response.body());
        if (jsonObject.getString("request_id") == null) {
            log.error("格式转换错误");
            throw new EmosException("格式转换错误");
        } else if (jsonObject.getInteger("face_num") != 1) {
            if (jsonObject.getInteger("face_num") == 0) {
                throw new EmosException("未检测到人脸");
            }
            throw new EmosException("检测到多个人脸");
        } else {
            JSONArray faces = jsonObject.getJSONArray("faces");
//            人脸模型数据
            String faceToken = faces.getJSONObject(0).getString("face_token");
            System.out.println("人脸token：" + faceToken);
//            删除人脸模型服务器旧数据
            deleteFaceModel(userId);
//            人脸模型数据服务端持久化
            HttpRequest saveFaceRequest = getFaceRequest(saveFaceModelUrl);
            saveFaceRequest.form("outer_id", outherId);
            saveFaceRequest.form("face_tokens", faceToken);
            HttpResponse saveFaceResponse = saveFaceRequest.execute();
            if (saveFaceResponse.getStatus() != 200) {
                log.error("人脸数据端持久化错误");
                throw new EmosException("人脸持久化错误");
            }
//            持久化
            TbFaceModel faceModel = new TbFaceModel();
            faceModel.setFaceModel(faceToken);
            faceModel.setUserId(userId);
            if (!tbFaceModelService.save(faceModel)) {
                log.error("人脸数据库持久化错误");
                throw new EmosException("人脸持久化错误");
            }
        }
    }

    /**
     * 删除人脸模型 数据库，人脸模型服务端
     *
     * @param userId 用户id
     */
    @Override
    public void deleteFaceModel(int userId) {
        TbFaceModel user = tbFaceModelService.getOne(new QueryWrapper<TbFaceModel>().eq("user_id", userId));
        String userFaceModel = user.getFaceModel();
        HttpRequest faceRequest = getFaceRequest(deletFaceModelUrl);
        faceRequest.form("outer_id", outherId);
        faceRequest.form("face_tokens", userFaceModel);
        HttpResponse execute = faceRequest.execute();
        if (execute.getStatus() != 200 && !tbFaceModelService.remove(new QueryWrapper<TbFaceModel>().eq("user_id", userId))) {
            log.error("删除人脸数据服务异常");
            throw new EmosException("删除人脸数据服务异常");
        }
    }


    /**
     * 封装请求方法，请求url，url会自动解析中文
     *
     * @param url    请求的地址(可包含中文)
     * @param method 请求的方法
     * @return 返回请求后的body
     */
    private String request(String url, String method) {
        url = HttpUtil.encodeParams(url, StandardCharsets.UTF_8);
        HttpRequest httpRequest;
        if ("get".equals(method)) {
            httpRequest = HttpUtil.createGet(url);
        } else if ("post".equals(method)) {
            httpRequest = HttpUtil.createPost(url);
        } else {
            throw new EmosException("疫情数据获取错误");
        }
        httpRequest.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.66 Safari/537.36 Edg/103.0.1264.44");
        return httpRequest.execute().body();
    }

    /**
     * 返回一个配置好key和secret的face验证接口
     *
     * @param url 调用的请求接口
     * @return 一个Post请求
     */
    private HttpRequest getFaceRequest(String url) {
        HttpRequest request = HttpUtil.createPost(url);
        request.form("api_key", faceKey);
        request.form("api_secret", faceSecret);
        return request;
    }
}
