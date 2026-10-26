package com.stayfinder.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configurarea Spring Security pentru aplicație.
 * Pentru API-ul REST (/api/**) se folosește autentificare stateless prin JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Dezactivăm CSRF deoarece API-ul REST folosește token-uri JWT stateless (fără cookies de sesiune)
                .csrf(AbstractHttpConfigurer::disable)
                // Nu creăm sesiuni HTTP pe server pentru cererile REST
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Autentificare publică
                        .requestMatchers("/api/auth/**").permitAll()
                        // Vizualizare cazări publică
                        .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                        // Curs valutar public
                        .requestMatchers(HttpMethod.GET, "/api/exchange-rates/**").permitAll()
                        // Documentație Swagger / OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Consola H2 (în modul dezvoltare)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Rute rezervate doar administratorilor
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Feed personalizat și operațiuni per utilizator
                        .requestMatchers("/api/feed").authenticated()
                        .requestMatchers("/api/me/**").authenticated()
                        // Orice alt endpoint REST necesită autentificare
                        .requestMatchers("/api/**").authenticated()
                        // Resursele statice sau viitoarele pagini Thymeleaf
                        .anyRequest().permitAll()
                )
                // Permite afișarea consolei H2 în frame-uri pe același domeniu
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // Adăugăm filtrul JWT înainte de filtrul standard de username/password
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
