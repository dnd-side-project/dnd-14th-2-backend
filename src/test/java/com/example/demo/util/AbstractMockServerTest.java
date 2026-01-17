package com.example.demo.util;

import java.io.IOException;
import java.time.Duration;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public abstract class AbstractMockServerTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new mockwebserver3.MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    protected RestClient anyRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(1));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(mockWebServer.url("").toString())
            .build();
    }

    protected MockWebServer getMockWebServer() {
        return mockWebServer;
    }
}
