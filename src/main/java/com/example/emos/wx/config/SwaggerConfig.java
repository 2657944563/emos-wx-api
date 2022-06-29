package com.example.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 2657944563
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket getDocket() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        //配置swagger页面信息
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title("EMOS在线办公系统");
        apiInfoBuilder.version("0.0.1");
        docket.apiInfo(apiInfoBuilder.build());

        //配置api扫描
        ApiSelectorBuilder apiSelectorBuilder = docket.select();
        //所有包都扫描
        apiSelectorBuilder.paths(PathSelectors.any());
        //扫描限定注解,只有@ApiOperation注解下的类才会被添加
        apiSelectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = apiSelectorBuilder.build();

        //配置swagger支持jwt
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> list = new ArrayList<>();
        list.add(apiKey);
        docket.securitySchemes(list);
        //配置令牌作用域
        AuthorizationScope[] scope = {new AuthorizationScope("global", "accessEverything")};
        SecurityReference reference = new SecurityReference("token", scope);
        List<SecurityReference> refList = new ArrayList<>();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();
        List<SecurityContext> cxtlist = new ArrayList<>();
        cxtlist.add(context);
        docket.securityContexts(cxtlist);

        return docket;
    }
}
