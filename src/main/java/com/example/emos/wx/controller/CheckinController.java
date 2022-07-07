package com.example.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.controller.from.CheckinForm;
import com.example.emos.wx.db.service.contollerService.CheckinService;
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
    CheckinService checkinService;

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
        if (file.getContentType().endsWith("jpg")) {
            throw new EmosException("请上传jpg文件");
        }
        String path = imageFolder + "/" + file.getOriginalFilename().toLowerCase();
        try {
            file.transferTo(Paths.get(path));
            checkinService.createFaceModel(userId, path);
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
        if (fileName.endsWith("jpg") || fileName.endsWith("jpeg")) {
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
                checkinService.chikin(param);
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

        String s = checkinService.validCanCheckIn(jwtUtil.getUserId(token), DateUtil.now());
        return R.ok(s);
    }
}

