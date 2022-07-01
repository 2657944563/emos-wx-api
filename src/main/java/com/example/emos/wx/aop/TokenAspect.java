package com.example.emos.wx.aop;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 将ThreadLocalToken中的Token添加进返回的数据中
 *
 * @author 2657944563
 */
@Aspect
@Component
public class TokenAspect {
    @Autowired
    ThreadLocalToken threadLocalToken;

    //Contoller包下的所有类的所有帆帆都拦截
    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..)))")
    void aspect() {
    }

    @Around("aspect()")
    public Object asp(ProceedingJoinPoint joinPoint) throws Throwable {
        R r = (R) joinPoint.proceed();
        String token = threadLocalToken.getToken();
        if (!StrUtil.isBlank(token)) {
            r.put("token", token);
        }
        r.put("test", "测试asp效果");
        return r;
    }
}
