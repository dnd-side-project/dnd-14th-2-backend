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
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomValue;
            // 0부터 255까지의 바이트 값 중, CHARSET.length(26)의 배수인 234 미만의 값만 사용합니다.
            // (Byte.MAX_VALUE + 1)은 256입니다.
            // unbiasedLimit = (256 / 26) * 26 = 9 * 26 = 234
            int unbiasedLimit = (Byte.MAX_VALUE + 1) / CHARSET.length * CHARSET.length;
            do {
                randomValue = Byte.toUnsignedInt(randomBytesSource.bytes(1)[0]);
            } while (randomValue >= unbiasedLimit); // 편향 없는 범위(0~233)를 벗어나면 재시도합니다.
            int idx = randomValue % CHARSET.length;
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
