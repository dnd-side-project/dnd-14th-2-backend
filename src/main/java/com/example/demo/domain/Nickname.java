package com.example.demo.domain;

import java.util.regex.Pattern;

public record Nickname(
    String value
) {
    private static final Pattern KOREAN_DIGIT_LOWERCASE = Pattern.compile("^[가-힣0-9a-z]+$");
    private static final int MAX_LENGTH = 5;

    public Nickname {
        validateIsNotBlank(value);
        validateLengthIsNotOverMaxLength(value);
        validateIsValidCharacters(value);
    }

    private static void validateIsNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }
    }

    private static void validateLengthIsNotOverMaxLength(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("닉네임은 %d자 이내여야 합니다.", MAX_LENGTH));
        }
    }

    private static void validateIsValidCharacters(String value) {
        if (!KOREAN_DIGIT_LOWERCASE.matcher(value).matches()) {
            throw new IllegalArgumentException("닉네임은 한글, 숫자, 영어 소문자로만 이루어져야 합니다.");
        }
    }
}
