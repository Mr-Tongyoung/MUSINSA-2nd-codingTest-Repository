package com.example.courseRegistrationSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("대학교 수강신청 시스템 API")
                        .description("수강신청, 수강취소, 시간표 조회 등 대학교 수강신청 시스템의 REST API 명세")
                        .version("1.0.0"));
    }
}
