package com.wafflestudio.spring2025.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Seminar 2025 Spring Boot API")
                    .version("1.0")
                    .description("시간표 관리 API 문서"),
            ).addSecurityItem(
                SecurityRequirement().addList(securitySchemeName),
            ).components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요 (Bearer 제외)"),
                    ),
            )
    }
}
