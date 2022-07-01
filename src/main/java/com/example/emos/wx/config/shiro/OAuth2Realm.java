package com.example.emos.wx.config.shiro;

import com.example.emos.wx.config.JwtUtil;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * shiro实现认证授权方法
 *
 * @author 2657944563
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    JwtUtil jwtUtil;

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
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //TODO 验证用户可用性，是否冻结
        AuthenticationInfo authInfo = new SimpleAuthenticationInfo();
        //TODO 添加用户信息
        return authInfo;
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
        //TODO 查询权限并且添加进simple
        return simple;
    }
}
