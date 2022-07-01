package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.emos.wx.config.JwtUtil;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Resource
    JwtUtil jwtUtil;
    @Resource
    RedisTemplate redisTemplate;
    @Value("${emos.jwt.cache-expire}")
    int cacheExpire;
    @Resource
    ThreadLocalToken threadLocalToken;


    /**
     * 用于给Shiro验证提供Token对象
     *
     * @param servletRequest  连接请求
     * @param servletResponse 连接回复
     * @return 返回自定义封装的Token对象
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String token = getRequestToken((HttpServletRequest) servletRequest);
        if (!StrUtil.isBlank(token)) {
            return new OAuth2Token(token);
        }
        return null;
    }

    /**
     * 放行options试探请求
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest http = (HttpServletRequest) request;
        return http.getMethod().equals(RequestMethod.OPTIONS.name());
        //除了试探请求，都要被Shiro处理
    }

    /**
     * 不被放行的请求需要被这个方法过滤，然后尝试获取请求的token，根据token来判断接下来的流程
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest res = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        //设置返回数据的格式
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        //开启跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", res.getHeader("Origin"));

        threadLocalToken.clear();
        String token = getRequestToken(res);
        //如果没有获取到令牌，那么没有登录，需要登录
        if (StrUtil.isBlank(token)) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().println("未登录状态，无令牌");
            return false;
        }
        try {
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {
            /**
             * 刷新令牌：本地令牌过期，但是redis令牌未过期
             * 尝试更新本地令牌以及redis令牌
             */
            if (redisTemplate.hasKey(token)) {
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                //未令牌赋值新的值,将redis设置新的令牌
                token = jwtUtil.createToken(userId);
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);
            } else {
                //重新登陆：redis的令牌也过期了，那么就需要重新登录
                resp.getWriter().println("令牌过期，重新登录");
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        } catch (JWTDecodeException e) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().println("无效令牌");
            return false;
        }
        //认证授权：简介调用自定义的realm认证授权，如果认证授权通过返回true，否者flase
        return executeLogin(request, response);
    }

    /**
     * 认证失败处理：处理认证失败的响应结果
     *
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest res = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        //设置返回数据的格式
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        //开启跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", res.getHeader("Origin"));
        //未验证状态
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            resp.getWriter().println(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取请求中的token数据
     *
     * @param http
     * @return
     */
    private String getRequestToken(HttpServletRequest http) {
        String token = http.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = http.getParameter("token");
        }
        return token;
    }
}
