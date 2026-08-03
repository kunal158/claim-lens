package com.claimlens.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI claimlensOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Claimlens API")
                        .description("API documentation for claim ingestion and reel processing endpoints")
                        .version("v1")
                        .license(new License().name("Proprietary")));
    }
}

