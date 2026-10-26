package com.stayfinder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatele afișate în Swagger UI (http://localhost:8080/swagger-ui.html)
 * și configurarea schemei de securitate JWT Bearer.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI stayFinderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StayFinder API")
                        .version("0.4.0")
                        .description("API REST pentru descoperirea și rezervarea de cazări, "
                                + "folosit de aplicația Android și de interfața web."))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introdu token-ul JWT obținut de la /api/auth/login sau /api/auth/register")));
    }
}
