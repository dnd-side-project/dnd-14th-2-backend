package com.example.demo.infrastructure.user;

import com.example.demo.domain.RandomBytesSource;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureRandomBytesSource implements RandomBytesSource {

    private final SecureRandom random = new SecureRandom();

    @Override
    public byte[] bytes(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }
}
