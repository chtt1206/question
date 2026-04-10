package com.survey.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174"); // 允许的前端域名
        config.addAllowedOrigin("http://localhost:5175"); // 允许移动端项目的域名
        config.addAllowedOrigin("http://localhost:5176"); // 允许移动端项目的域名
        config.addAllowedOrigin("http://localhost:5178");
        config.addAllowedOrigin("http://localhost:5177");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS"); // 允许OPTIONS预检请求
        config.addAllowedHeader("*"); // 允许的HTTP头
        config.setAllowCredentials(true); // 允许携带凭证
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 应用到所有路径
        
        return new CorsFilter(source);
    }
}
