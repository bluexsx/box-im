package com.bx.implatform.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"com.bx"};
        return GroupedOpenApi.builder().group("IM-Platform")
            .pathsToMatch(paths)
            .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("Blue");
        return new OpenAPI().info(new Info()
            .title("盒子IM接口文档")
            .description("盒子IM业务平台服务")
            .contact(contact)
            .version("3.0")
            .termsOfService("https://www.boxim.online")
            .license(new License().name("MIT")
                .url("https://www.boxim.online")));
    }

}
