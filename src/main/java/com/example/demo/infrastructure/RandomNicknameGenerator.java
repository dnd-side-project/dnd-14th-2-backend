package com.example.demo.infrastructure;

import com.example.demo.domain.Nickname;
import com.example.demo.domain.NicknameGenerator;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomNicknameGenerator implements NicknameGenerator {

    private static final int MAX_RETRY = 10;

    private static final String[] adjectives = {"예쁜", "작은", "큰편", "빠른", "느린", "높은", "낮은", "굵은",
        "가볍", "무겁", "밝은", "어둠", "쉬운", "느긋", "온화", "졸린", "넓은", "좁은", "좋은", "나쁜"};

    private static final String[] animals = {"고라니", "사슴벌", "코끼리", "호랑이", "북극곰", "얼룩말", "캥거루",
        "다람쥐", "원숭이", "앵무새", "올빼미", "독수리", "개복치", "까마귀", "물고기", "여왕벌", "붕장어", "고양이", "햄스터", "코알라"};

    @Override
    public Nickname generate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < MAX_RETRY; i++) {
            String candidate = adjectives[rnd.nextInt(adjectives.length)] + animals[rnd.nextInt(animals.length)];
            try {
                return new Nickname(candidate);
            } catch (IllegalArgumentException ignored) {
                // 규칙 위반이면 재시도
            }
        }
        throw new IllegalStateException("유효한 닉네임 생성에 실패했습니다. (tries=" + MAX_RETRY + ")");
    }
}
