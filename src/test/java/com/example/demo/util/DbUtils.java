package com.example.demo.util;

import com.example.demo.domain.InvitationCode;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.Nickname;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import java.time.LocalDate;

public class DbUtils {

    private static int userCounter = 0;

    public static User kakaoUser(String providerId, Nickname nickname, InvitationCode invitationCode) {
        return new User(
            nickname,
            invitationCode,
            "test@example.com",
            "https://profile.com/image.png",
            Provider.KAKAO,
            providerId
        );
    }

    public static User kakaoUser(Nickname nickname, InvitationCode invitationCode) {
        userCounter++;
        return kakaoUser(
            "kakao-test-" + userCounter,
            nickname,
            invitationCode
        );
    }

    public static User kakaoUser() {
        userCounter++;
        return kakaoUser(
            "kakao-test-" + userCounter,
            new Nickname("u" + String.format("%04d", userCounter % 10000)),
            new InvitationCode("CODE" + twoLetterSuffix(userCounter))
        );
    }

    private static String twoLetterSuffix(int value) {
        int v = value % (26 * 26);
        char first = (char) ('A' + (v / 26));
        char second = (char) ('A' + (v % 26));
        return "" + first + second;
    }

    public static User givenSavedUser(UserRepository repo) {
        return repo.save(kakaoUser());
    }

    public static User givenSavedUser(UserRepository repo, Nickname nickname, InvitationCode invitationCode) {
        return repo.save(kakaoUser(nickname, invitationCode));
    }

}
