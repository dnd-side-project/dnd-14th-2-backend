package com.example.demo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient googleOauthRestClient() {
        return RestClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build();
    }
}
