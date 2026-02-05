package com.example.demo.application.user;

import com.example.demo.domain.Nickname;
import com.example.demo.domain.NicknameGenerator;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomNicknameGenerator implements NicknameGenerator {

    private static final String[] WORDS = {
        "토끼", "오리", "펭귄", "판다", "라마", "수달", "사슴", "여우", "고니", "거위",
        "키위", "망고", "딸기", "체리", "사과", "포도", "수박", "참외", "나비", "천사",
        "쿠키", "초코", "캔디", "젤리", "푸딩", "도넛", "와플", "구름", "공주", "요정",
        "하트", "리본", "왕관", "보석", "인형", "모찌", "초이", "로이", "조이", "쵸비",
        "윤슬", "가람", "솔비", "새솔", "아람", "은하", "온유", "루나", "소망", "미소"
    };
    private static final String SUFFIX = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    public Nickname generate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        String words = WORDS[rnd.nextInt(WORDS.length)];

        for (int t = 0; t < 3; t++) {
            words += SUFFIX.charAt(rnd.nextInt(SUFFIX.length()));
        }
        return new Nickname(words);
    }
}
