package com.smartquote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves locally stored files (PDFs, audio) at /files/** so StorageService URLs resolve.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.local.base-dir:./storage}")
    private String baseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path storagePath = Paths.get(baseDir).toAbsolutePath().normalize();
        String location = "file:" + storagePath + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
