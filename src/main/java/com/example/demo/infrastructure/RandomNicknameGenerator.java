package com.example.demo.infrastructure;

import com.example.demo.domain.NicknameGenerator;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNicknameGenerator implements NicknameGenerator {

    private static final String[] adjectives = {"예쁜", "작은", "큰편", "빠른", "느린", "높은", "낮은", "굵은",
        "가볍", "무겁", "밝은", "어둠", "쉬운", "느긋", "온화", "졸린", "넓은", "좁은", "좋은", "나쁜"};

    private static final String[] animals = {"고라니", "사슴벌", "코끼리", "호랑이", "북극곰", "얼룩말", "캥거루",
        "다람쥐", "원숭이", "앵무새", "올빼미", "독수리", "개복치", "까마귀", "물고기", "여왕벌", "붕장어", "고양이", "햄스터", "코알라"};
    public static final int RETRY_COUNT = 10;

    @Override
    public String generate(int minLength, int maxLength) {
        validateLengthRange(minLength, maxLength);
        ThreadLocalRandom current = ThreadLocalRandom.current();

        for (int retry = 0; retry < RETRY_COUNT; retry++) {
            String adjective = adjectives[current.nextInt(adjectives.length)];
            String animal = animals[current.nextInt(animals.length)];

            String nickname = adjective + animal;
            int nicknameLength = nickname.length();
            if (nicknameLength >= minLength && nicknameLength <= maxLength) {
                return nickname;
            }
        }
        throw new IllegalStateException("닉네임 생성에 실패했습니다. (min=" + minLength + ", max=" + maxLength + ")");
    }

    private void validateLengthRange(int minLength, int maxLength) {
        if (minLength > maxLength) {
            throw new IllegalArgumentException("잘못된 길이 범위입니다.");
        }
    }
}
