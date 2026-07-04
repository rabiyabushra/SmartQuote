package com.smartquote.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for SmartQuote REST APIs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartQuoteOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartQuote API")
                        .description("REST APIs for SmartQuote quotation management, voice input, and sharing.")
                        .version("1.0.0")
                        .contact(new Contact().name("SmartQuote Team")));
    }
}
