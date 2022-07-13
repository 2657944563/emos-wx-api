package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.controller.from.DeleteMessageRefByIdForm;
import com.example.emos.wx.controller.from.SearchMessageByIdForm;
import com.example.emos.wx.controller.from.SearchMessageByPageForm;
import com.example.emos.wx.controller.from.UpdateUnreadMessageForm;
import com.example.emos.wx.db.service.MessageService;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("message")
@Api("消息模块接口")
public class MessageController {
    @Resource
    JwtUtil jwtUtil;
    @Resource
    MessageTask messageTask;
    @Resource
    MessageService messageService;

    /**
     * 分页查询列表接口
     *
     * @param from  分页数据
     * @param token 前端token
     * @return 查询到的消息列表
     */
    @PostMapping("searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm from, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = from.getPage();
        int length = from.getLength();
        long start = (page - 1) * length;
        List<HashMap> hashMaps = messageService.searchMessageByPage(userId, start, length);
        return R.ok().put("result", hashMaps);
    }

    /**
     * 查询某一条消息的主体
     *
     * @param form  需要查询的消息id封装
     * @param token 用户的token
     * @return 返回消息map
     */
    @PostMapping("searchMessageById")
    @ApiOperation("查询某一条消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form, @RequestHeader("token") String token) {
        HashMap map = messageService.searchMessageById(form.getId());
        return R.ok().put("result", map);
    }

    /**
     * 更新某条消息为已读状态
     *
     * @param form  封装消息id
     * @param token 用户token
     * @return 返回是否成功更新
     */
    @PostMapping("updateUnreadMessage")
    @ApiOperation("未读消息更新已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form, @RequestHeader("token") String token) {
        long l = messageService.updateUnreadMessage(form.getId());
        return R.ok().put("result", l > 0 ? true : false);
    }

    @PostMapping("deleteMessageRefById")
    @ApiOperation("删除某条消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form, @RequestHeader("token") String token) {
        long l = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result", l > 0 ? true : false);
    }

    /**
     * 用户前端主动轮询调用刷新消息，将mq中的新消息提取出来，然后存放进mongodb
     *
     * @param token 用户token
     * @return 返回消息信息，新消息数量，未读消息数量
     */
    @GetMapping("/refreshMessage")
    @ApiOperation("用户主动刷新消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        messageTask.receiveAsync(userId + "");
        long lastCount = messageService.searchLastCount(userId);
        long unreadCount = messageService.searchUnreadCount(userId);
        return R.ok().put("lastRows", lastCount).put("unreadRows", unreadCount);
    }
}
