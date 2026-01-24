package com.example.demo.domain;

import java.io.InputStream;
import java.time.Duration;

public interface FileStorage {

    void upload(String key, InputStream inputStream, long size, String contentType);

    String generateViewUrl(String key, Duration expires);
}
