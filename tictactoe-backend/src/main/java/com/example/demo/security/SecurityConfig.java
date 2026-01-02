package com.example.demo.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF
                .csrf(csrf -> csrf.disable())

                // 2. Kích hoạt CORS (QUAN TRỌNG CHO GAME)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Phân quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // --- Các API Public ---
                                "/api/login",
                                "/api/register",
                                "/ws/**", // Cho phép WebSocket

                                // --- MỞ KHÓA SWAGGER (BẠN ĐANG THIẾU ĐOẠN NÀY) ---
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        // Các API còn lại bắt buộc phải có Token
                        .anyRequest().authenticated())

                // 4. Không dùng Session (Stateless)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Thêm Filter kiểm tra Token
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình chi tiết cho CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép tất cả các nguồn
        configuration.setAllowedOriginPatterns(List.of("*"));
        // Cho phép các phương thức
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cho phép tất cả các header
        configuration.setAllowedHeaders(List.of("*"));
        // Cho phép gửi credentials
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}