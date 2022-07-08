package com.example.emos.wx.config;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExpectionAdvice {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus()
    public String exceptionHandler(Exception e) {
        //日志异常,控制台打印异常详细信息
//        log.error("异常信息", e);

        if (e instanceof EmosException) {
            EmosException em = (EmosException) e;
//            return em.getMessage();
            return R.error(em.getMessage()).toString();
        } else if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException em = (MethodArgumentNotValidException) e;
//            return em.getBindingResult().getFieldError().getDefaultMessage();
            return R.error(em.getBindingResult().getFieldError().getDefaultMessage()).toString();
        } else if (e instanceof UnauthorizedException) {
//            return "不具备操作权限";
            return R.error("不具备操作权限").toString();
        }
//        return "网站异常，请联系管理员";
        log.error(e.getMessage(), e);
        return R.error("网站异常,请联系管理员").toString();
    }

}
