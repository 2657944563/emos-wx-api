package com.example.emos.wx.config.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 返回Shiro处理的SecurityManager
     *
     * @param realm
     * @return
     */
    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm realm) {

        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(realm);
        manager.setRememberMeManager(null);
        return manager;
    }

    /**
     * 配置拦截路径、拦截器
     *
     * @param manager
     * @param filter
     * @return
     */
    @Bean()
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager manager, OAuth2Filter filter) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
//        shiroFilter.setLoginUrl(); // 设置登录页面
        shiroFilter.setSecurityManager(manager);
        Map<String, Filter> filtermaps = new HashMap();
        //过滤器map
        filtermaps.put("oauth2", filter);
        //添加filter
        shiroFilter.setFilters(filtermaps);
        //过滤的路径，或者不拦截的路径
        Map<String, String> paths = new LinkedHashMap<>();
        paths.put("/webjars/**", "anon");
        paths.put("/druid/**", "anon");
        paths.put("/app/**", "anon");
        paths.put("/sys/login/**", "anon");
        paths.put("/swagger/**", "anon");
        paths.put("/v2/api-docs/**", "anon");
        paths.put("/swagger-ui.html", "anon");
        paths.put("/swagger-resources/**", "anon");
        paths.put("/captcha.jpg", "anon");
        paths.put("/user/register", "anon");
        paths.put("/user/login", "anon");
        paths.put("/test/**", "anon");
        //除了派出的路径，都通过oauth2这个filter来拦截处理
        paths.put("/**", "oauth2");
        //添加拦截路径
        shiroFilter.setFilterChainDefinitionMap(paths);
        return shiroFilter;
    }

    /**
     * 一个后处理器，干啥的不清楚
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager manager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(manager);
        return authorizationAttributeSourceAdvisor;
    }
}
