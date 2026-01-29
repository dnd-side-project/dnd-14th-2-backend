package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerEntryTest {

    private static final LocalDate OCCURRED_ON = LocalDate.of(2026, 1, 24);

    private User user() {
        return new User(
            "test@example.com",
            "https://profile.com/image.png",
            Provider.KAKAO,
            "kakao-test-1"
        );
    }

    private LedgerEntry entry(long amount, String description) {
        return new LedgerEntry(
            amount,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            description,
            OCCURRED_ON,
            PaymentMethod.CASH,
            null,
            user()
        );
    }

    @Nested
    @DisplayName("amount 검증")
    class AmountValidation {

        @Test
        @DisplayName("생성 시 amount가 0 이하면 예외")
        void amount_must_be_positive_on_create() {
            assertThatThrownBy(() -> entry(0L, "점심"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액(amount)은 0보다 커야 합니다.");

            assertThatThrownBy(() -> entry(-1L, "점심"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액(amount)은 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("생성 시 amount가 1 이상이면 정상 생성")
        void amount_ok_on_create() {
            LedgerEntry entry = entry(1L, "점심");
            assertThat(entry.getAmount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("수정 시 amount가 0 이하면 예외")
        void amount_must_be_positive_on_update() {
            LedgerEntry entry = entry(1000L, "점심");

            assertThatThrownBy(() -> entry.update(
                0L,
                LedgerType.EXPENSE,
                LedgerCategory.FOOD,
                "점심",
                PaymentMethod.CASH,
                null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액(amount)은 0보다 커야 합니다.");
        }
    }

    @Nested
    @DisplayName("description 검증")
    class DescriptionValidation {

        @Test
        @DisplayName("생성 시 description이 null이면 예외")
        void description_is_required_on_create() {
            assertThatThrownBy(() -> entry(1000L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 필수입니다.");
        }

        @Test
        @DisplayName("생성 시 description이 공백이면 예외")
        void description_cannot_be_blank_on_create() {
            assertThatThrownBy(() -> entry(1000L, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("생성 시 description이 15자를 초과하면 예외 (trim 이후 길이 기준)")
        void description_must_be_at_most_15_chars_on_create() {
            assertThatThrownBy(() -> entry(1000L, "두바이쫀득쿠키가맛있을까맛없을까"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 1자 이상 15자 이내여야 합니다.");

            assertThatThrownBy(() -> entry(1000L, "  든든한제육볶음에밥한그릇에계란까지  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 1자 이상 15자 이내여야 합니다.");
        }

        @Test
        @DisplayName("생성 시 description은 trim되어 저장된다")
        void description_is_trimmed_on_create() {
            LedgerEntry entry = entry(1000L, "  점심  ");
            assertThat(entry.getDescription()).isEqualTo("점심");
        }

        @Test
        @DisplayName("수정 시에도 description 검증 및 trim이 적용된다")
        void description_is_trimmed_on_update() {
            LedgerEntry entry = entry(1000L, "점심");
            entry.update(
                1000L,
                LedgerType.EXPENSE,
                LedgerCategory.FOOD,
                "  저녁  ",
                PaymentMethod.CASH,
                null
            );
            assertThat(entry.getDescription()).isEqualTo("저녁");
        }

        @Test
        @DisplayName("수정 시에도 description 검증 및 trim이 적용된다")
        void description_is_not_blank_on_update() {
            LedgerEntry entry = entry(1000L, "점심");

            assertThatThrownBy(() -> entry.update(
                1000L,
                LedgerType.EXPENSE,
                LedgerCategory.FOOD,
                "   ",
                PaymentMethod.CASH,
                null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("memo 검증")
    class MemoValidation {

        @Test
        @DisplayName("현재 구현에서는 memo는 null도 허용된다")
        void memo_can_be_null() {
            LedgerEntry entry = new LedgerEntry(
                1000L,
                LedgerType.EXPENSE,
                LedgerCategory.FOOD,
                "점심",
                OCCURRED_ON,
                PaymentMethod.CASH,
                null,
                user()
            );
            assertThat(entry.getMemo()).isNull();
        }

        @Test
        @DisplayName("현재 구현에서는 updateMemo로 memo가 변경된다")
        void memo_can_be_updated() {
            LedgerEntry entry = entry(1000L, "점심");
            entry.updateMemo("메모");
            assertThat(entry.getMemo()).isEqualTo("메모");
        }

        @Test
        @DisplayName("memo는 100자를 초과하면 예외")
        void memo_must_be_at_most_100_chars() {
            String over100 = "하".repeat(101);
            assertThatThrownBy(() -> new LedgerEntry(
                1000L,
                LedgerType.EXPENSE,
                LedgerCategory.FOOD,
                "점심",
                OCCURRED_ON,
                PaymentMethod.CASH,
                over100,
                user()
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
