package com.io.spring_boot_archetype.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for documenting the application's endpoints.
 * Use http://localhost:8080/swagger-ui/index.html to access the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the main OpenAPI definition.
     *
     * @return OpenAPI object with API metadata.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Application API")
                        .version("1.0")
                        .description("API documentation for my application's endpoints."));
    }
}
