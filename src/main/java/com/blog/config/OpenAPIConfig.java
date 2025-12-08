package com.blog.config;   // or com.example.blog.config if that's your base

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration

// --- IMPORTANT: This tells Swagger we use Bearer JWT tokens ---
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)

public class OpenAPIConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blog API")
                        .description("REST API for Blog, with JWT Auth")
                        .version("1.0.0"))
                // --- ALL endpoints secured unless permitAll ---
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
