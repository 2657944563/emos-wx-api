package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.controller.from.CheckinForm;
import com.example.emos.wx.controller.from.SearchMonthCheckinForm;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.TbCheckinService;
import com.example.emos.wx.db.service.TbUserService;
import com.example.emos.wx.exception.EmosException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author 2657944563
 */
@RequestMapping("/checkin")
@RestController
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {
    @Value("${emos.face.image-folder}")
    private String imageFolder;
    @Resource
    TbCheckinService tbCheckinService;
    @Resource
    TbUserService tbUserService;
    @Resource
    SystemConstants systemConstants;

    @Resource
    JwtUtil jwtUtil;

    /**
     * 根据用户id和用户照片创建人脸数据模型
     *
     * @param file  用户人脸照片
     * @param token 用户token
     * @return 返回创建状态
     */
    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);
        if (file == null) {
            throw new EmosException("未上传人脸照片");
        }
        if (!file.getOriginalFilename().endsWith("jpg")) {
            throw new EmosException("请上传jpg文件");
        }
        String path = imageFolder + "/" + file.getOriginalFilename().toLowerCase();
        try {
            file.transferTo(Paths.get(path));
            tbCheckinService.createFaceModel(userId, path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new EmosException("照片解析失败");
        } finally {
            FileUtil.del(path);
        }
        return R.ok("人脸数据创建成功");
    }

    /**
     * 签到接口
     *
     * @param checkinForm 签到表单数据
     * @param file        用户人脸认证照片
     * @param token       用户请求携带token
     * @return 返回状态信息
     */
    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm checkinForm, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {

        if (file == null) {
            return R.error("没有上传文件");
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        String filePath = imageFolder + "/" + fileName;
        if (!fileName.endsWith("jpg")) {
            return R.error("需要提交jpg格式图片");
        } else {
            try {
                file.transferTo(Paths.get(filePath));
                HashMap param = new HashMap();
                param.put("userId", jwtUtil.getUserId(token));
                param.put("address", checkinForm.getAddress());
                param.put("city", checkinForm.getCity());
                param.put("country", checkinForm.getCountry());
                param.put("district", checkinForm.getDistrict());
                param.put("province", checkinForm.getProvince());
                param.put("path", filePath);
                //执行人脸签到流程
                tbCheckinService.chikin(param);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("照片存取失败");
            } finally {
//                删除本地临时存储的人脸图片
                FileUtil.del(filePath);
            }
        }

        return R.ok("签到成功");
    }

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看是否可签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
//        System.out.println("validaCanCheckIn方法调用 token : " + token);
        String s = tbCheckinService.validCanCheckIn(jwtUtil.getUserId(token), DateUtil.now());
        return R.ok(s);
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当前周签到日期")
    public R checkinWeek(@RequestHeader("token") String token) {
        HashMap map = new HashMap();
        map.put("attendanceTime", systemConstants.getAttendanceTime());
        map.put("closingTime", systemConstants.getClosingTime());
//        map.put("date", "2022-07-08");
        map.put("date", DateUtil.today());
        map.put("userId", jwtUtil.getUserId(token));
        HashMap todayCheckin = tbCheckinService.searchTodayCheckin(map);
//        如果今天没有签到,那么获取上次最近日子的签到信息
        if (todayCheckin == null || todayCheckin.isEmpty()) {
            TbCheckin one = tbCheckinService.getOne(new QueryWrapper<TbCheckin>().eq("user_id", map.get("userId")).orderByDesc("create_time"));
//            用户一次都没有签到
            if (one == null) {
//                System.out.println("用户一次都没有签到");
            } else {
                map.put("date", DateUtil.date(one.getDate()).toDateStr());
                map.putAll(tbCheckinService.searchTodayCheckin(map));
//                System.out.println("今天没有签到");
//                System.out.println(todayCheckin);
            }
        } else {
//            System.out.println(todayCheckin);
            map.putAll(tbCheckinService.searchTodayCheckin((map)));
        }
        map.put("checkinDays", tbCheckinService.searchCheckinDays((Integer) map.get("userId")));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        TbUser user = tbUserService.getOne(new QueryWrapper<TbUser>().select("hiredate").eq("id", map.get("userId")));
        Date hiredDate = user.getHiredate();
        if (startDate.isBefore(hiredDate)) {
            startDate = DateUtil.date(hiredDate);
        }
        map.put("startDate", startDate.toDateStr());
        map.put("endDate", endDate.toDateStr());
        ArrayList<HashMap> list = tbCheckinService.searchWeekCheckin(map);
        map.put("weekCheckin", list);
//        System.out.println(JSONObject.toJSONString(map));
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户指定月的签到日期")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm from, @RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);
        TbUser user = tbUserService.getOne(new QueryWrapper<TbUser>().select("hiredate").eq("id", userId));
        Date hiredate = user.getHiredate();
        String month = from.getMonth() < 10 ? "0" + from.getMonth() : from.getMonth() + "";
        StringBuilder stringBuilder = new StringBuilder().append(from.getYear()).append("-").append(month).append("-");
        DateTime startDate = DateUtil.parse(stringBuilder.append("01"));
        if (startDate.before(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("仅能查询入职后的考勤记录");
        }
        if (startDate.before(hiredate)) {
            startDate = DateUtil.date(hiredate);
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        ArrayList<HashMap> list = tbCheckinService.searchMonthCheckin(map);
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap hashMap : list) {
            if ("正常".equals(hashMap.get("status"))) {
                sum_1++;
            }
            if ("迟到".equals(hashMap.get("status"))) {
                sum_2++;
            }
            if ("缺勤".equals(hashMap.get("status"))) {
                sum_3++;
            }
        }
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }

}

