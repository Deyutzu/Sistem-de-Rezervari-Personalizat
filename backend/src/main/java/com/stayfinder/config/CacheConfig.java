package com.stayfinder.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Activează suportul de cache în Spring Boot.
 * Folosim Caffeine Cache (configurat în application.properties)
 * pentru a evita apelurile externe repetitive către API-uri precum Frankfurter.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
