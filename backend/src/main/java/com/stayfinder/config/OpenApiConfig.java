package com.stayfinder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatele afișate în Swagger UI (http://localhost:8080/swagger-ui.html).
 * Documentația endpoint-urilor e generată automat din controllere;
 * aici doar dăm un titlu și o descriere.
 * În faza 4 adăugăm aici schema de securitate "Bearer JWT" (butonul Authorize).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stayFinderOpenApi() {
        return new OpenAPI().info(new Info()
                .title("StayFinder API")
                .version("0.1.0")
                .description("API REST pentru descoperirea și rezervarea de cazări, "
                        + "folosit de aplicația Android și de interfața web."));
    }
}
