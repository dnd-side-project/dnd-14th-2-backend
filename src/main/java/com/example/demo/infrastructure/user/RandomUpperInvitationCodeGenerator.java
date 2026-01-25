package com.example.demo.infrastructure.user;

import com.example.demo.application.user.InvitationCodeGenerator;
import com.example.demo.domain.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RandomUpperInvitationCodeGenerator implements InvitationCodeGenerator {

    private static final int CODE_LENGTH = 6;
    private static final char[] CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final String salt;

    public RandomUpperInvitationCodeGenerator(@Value("${invitation.secret-salt}") String salt) {
        this.salt = salt;
    }

    @Override
    public String generate(User user) {
        byte[] hash = sha256(user.getNickname() + salt);
        return toUppercaseCode(hash);
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256은 지원되지 않습니다.", e);
        }
    }

    private String toUppercaseCode(byte[] hash) {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = Byte.toUnsignedInt(hash[i]) % CHARSET.length;
            sb.append(CHARSET[index]);
        }

        return sb.toString();
    }
}
