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
        "윤슬", "가람", "솔비", "새솔", "아람", "은하", "온유", "루나", "소망", "미소",
        "호수", "산들", "바람", "햇살", "별빛", "달빛", "노을", "바다", "파랑", "하늘",
        "숲속", "들꽃", "나무", "잎새", "이슬", "윤기", "온기", "미풍", "은빛", "여명",
        "봄비", "여울", "물결", "잔향", "고요", "평온", "설렘", "기쁨", "행복", "미래",
        "소원", "희망", "꿈결", "커피", "웃음", "사랑", "마음", "순수", "청춘", "찬란",
        "설탕", "우주", "별똥", "빛결", "아련", "포근", "달콤", "말랑", "반짝", "샛별"
    };
    private static final String SUFFIX = "0123456789abcdefghijklmnopqrstuvwxyz";

    @Override
    public Nickname generate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        StringBuilder name = new StringBuilder(WORDS[rnd.nextInt(WORDS.length)]);
        for (int i = 0; i < 3; i++) {
            name.append(SUFFIX.charAt(rnd.nextInt(SUFFIX.length())));
        }
        return new Nickname(name.toString());
    }
}
