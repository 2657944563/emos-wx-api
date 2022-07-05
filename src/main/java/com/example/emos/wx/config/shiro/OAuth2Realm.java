package com.example.emos.wx.config.shiro;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.contollerService.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * shiro实现认证授权方法
 *
 * @author 2657944563
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    JwtUtil jwtUtil;
    @Resource
    UserService userService;

    /**
     * 验证传入的令牌对象是否是自定义的token对象
     *
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 认证（登录的时候调用方法）
     *
     * @param authenticationToken 封装了数据传递的token
     * @return 返回认证对象
     * @throws AuthenticationException 抛出认证的错误，其他错误会被shiro捕捉应该返回shiro给的异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // token中获取userid查询用户的详细数据
        int userId = jwtUtil.getUserId((String) authenticationToken.getPrincipal());
        TbUser tbUser = userService.selectByUserId(userId);
        // 添加用户信息
        if (tbUser == null) {
            throw new LockedAccountException(R.error("用户数据异常").toString());
//            throw  new EmosException(R.error("用户数据异常").toString()); // 这里不能用自定义异常以及其他异常，shiro会拦截并且处理，返回默认异常信息
        }
        return new SimpleAuthenticationInfo(tbUser, authenticationToken.getPrincipal(), tbUser.getName());
    }


    /**
     * 授权（验证权限 的方法）
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo simple = new SimpleAuthorizationInfo();
        simple.addRole("any");
        //TODO 查询权限并且添加进simple
        return simple;
    }
}
