package com.example.demo.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record InvitationCode(
    String value
) {

    private static final int CODE_LENGTH = 6;
    private static final char[] CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final Pattern PATTERN = Pattern.compile("^[A-Z]+$");

    public InvitationCode {
        validateCodeLength(value);
        validateIsUpperCode(value);
    }

    public static InvitationCode generate(RandomBytesSource randomBytesSource) {
        byte[] b = randomBytesSource.bytes(CODE_LENGTH);
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = Byte.toUnsignedInt(b[i]) % CHARSET.length;
            sb.append(CHARSET[idx]);
        }

        return new InvitationCode(sb.toString());
    }

    private static void validateIsUpperCode(String value) {
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("초대코드는 영어 대문자로 이루어져야 합니다.");
        }
    }

    private static void validateCodeLength(String value) {
        if (value == null || value.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("초대코드는 6자리여야 합니다.");
        }
    }
}
