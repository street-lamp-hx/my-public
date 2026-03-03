package com.yiyitech.mf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName SwaggerConfig.java
 * @Description
 * @createTime 2026年02月10日 19:52:00
 *
 * Swagger (Springfox 3) configuration.
 * 访问地址：
 * - Swagger UI: /swagger-ui/index.html
 * - OpenAPI JSON: /v3/api-docs
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    /** 是否启用 swagger（建议生产环境关闭） */
    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;

    /** 扫描 controller 的包路径（可按需改成你的 controller 根包） */
    @Value("${swagger.base-package:com.yiyitech}")
    private String basePackage;

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.OAS_30)
                .enable(swaggerEnabled)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build()
                // 让 Swagger UI 支持在右上角输入 JWT
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Yiyi Mf Ai Qc API")
                .description("Auto-generated API documentation (Springfox 3 / OpenAPI 3).")
                .version("v1")
                .build();
    }

    /**
     * 这里用 Authorization header。
     * 你的 JwtAuthenticationFilter 如果读取的是别的 header 名字，把这里的 name 改掉。
     */
    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(o -> true)
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[] { authorizationScope };
        return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
    }
}
