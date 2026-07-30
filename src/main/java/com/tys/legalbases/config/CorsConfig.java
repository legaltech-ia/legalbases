package com.tys.legalbases.config;

import com.tys.legalbases.util.AppConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Single CORS policy for every controller, replacing the per-controller @CrossOrigin annotations. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(AppConstants.CROSS_ORIGIN_ALLOWED_ORIGINS)
                .allowedHeaders(AppConstants.CROSS_ORIGIN_ALLOWED_HEADERS)
                .allowedMethods("*");
    }
}
