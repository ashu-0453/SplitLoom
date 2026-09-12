package com.splitloom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI splitLoomOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SplitLoom API — Hyperlocal Quick-Commerce Price & Split-Cart Optimizer")
                        .description("High-performance distributed backend engine powered by Java 21 Virtual Threads, Redis Geospatial, PostgreSQL + pgvector, and Spring AI for multi-store basket optimization across Zepto, Blinkit, and Swiggy Instamart.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Astush Sharma")
                                .email("astush.sharma4@gmail.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
