package com.example.demo.infrastructure.aws;

import com.example.demo.domain.FileStorage;
import java.io.InputStream;
import java.time.Duration;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Fallback
@Component
public class DummyS3FileStorage implements FileStorage {

    @Override
    public void upload(String key, InputStream inputStream, long size, String contentType) {

    }

    @Override
    public String generateViewUrl(String key, Duration expires) {
        return "";
    }
}
