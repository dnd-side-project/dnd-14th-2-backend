package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.example.demo.util.DbUtils.kakaoUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerEntryTest {

    private static final LocalDate OCCURRED_ON = LocalDate.of(2026, 1, 24);

    private LedgerEntry entryWithUser(long amount, String description, User user) {
        return new LedgerEntry(
            amount,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            description,
            OCCURRED_ON,
            PaymentMethod.CASH,
            null,
            user
        );
    }

    private LedgerEntry entryWithTypeCategory(
        long amount,
        LedgerType type,
        LedgerCategory category,
        String description
    ) {
        return new LedgerEntry(
            amount,
            type,
            category,
            description,
            OCCURRED_ON,
            PaymentMethod.CASH,
            null,
            kakaoUser(new Nickname("test"), new InvitationCode("TEST"))
        );
    }


    @Nested
    @DisplayName("amount 검증")
    class AmountValidation {

        @Test
        @DisplayName("생성 시 amount가 0 이하면 예외")
        void amount_must_be_positive_on_create() {
            assertThatThrownBy(() -> entryWithUser(0L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액(amount)은 0보다 커야 합니다.");

            assertThatThrownBy(() -> entryWithUser(-1L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액(amount)은 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("생성 시 amount가 1 이상이면 정상 생성")
        void amount_ok_on_create() {
            LedgerEntry entry = entryWithUser(1L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));
            assertThat(entry.getAmount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("수정 시 amount가 0 이하면 예외")
        void amount_must_be_positive_on_update() {
            LedgerEntry entry = entryWithUser(1000L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));

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
    @DisplayName("type-category 정합성 검증")
    class TypeCategoryValidation {

        @Test
        @DisplayName("생성 시 수입 카테고리인데 type이 EXPENSE면 예외")
        void income_category_requires_income_type_on_create() {
            assertThatThrownBy(() -> entryWithTypeCategory(
                1000L,
                LedgerType.EXPENSE,
                LedgerCategory.SALARY,
                "월급"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리(SALARY)는 INCOME 유형만 허용합니다.");
        }

        @Test
        @DisplayName("생성 시 지출 카테고리인데 type이 INCOME이면 예외")
        void expense_category_requires_expense_type_on_create() {
            assertThatThrownBy(() -> entryWithTypeCategory(
                1000L,
                LedgerType.INCOME,
                LedgerCategory.FOOD,
                "점심"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리(FOOD)는 EXPENSE 유형만 허용합니다.");
        }

        @Test
        @DisplayName("수정 시에도 카테고리에 맞지 않는 type이면 예외")
        void type_must_match_category_on_update() {
            LedgerEntry entry = entryWithTypeCategory(1000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "점심");

            assertThatThrownBy(() -> entry.update(
                1000L,
                LedgerType.INCOME,
                LedgerCategory.FOOD,
                "점심",
                PaymentMethod.CASH,
                null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리(FOOD)는 EXPENSE 유형만 허용합니다.");
        }

        @Test
        @DisplayName("고정 타입이 없는 카테고리는 type을 강제하지 않는다")
        void category_without_fixed_type_does_not_enforce() {
            LedgerEntry incomeOther = entryWithTypeCategory(1000L, LedgerType.INCOME, LedgerCategory.OTHER, "기타수입");
            assertThat(incomeOther.getType()).isEqualTo(LedgerType.INCOME);

            LedgerEntry expenseOther = entryWithTypeCategory(1000L, LedgerType.EXPENSE, LedgerCategory.OTHER, "기타지출");
            assertThat(expenseOther.getType()).isEqualTo(LedgerType.EXPENSE);
        }
    }

    @Nested
    @DisplayName("description 검증")
    class DescriptionValidation {

        @Test
        @DisplayName("생성 시 description이 null이면 예외")
        void description_is_required_on_create() {
            assertThatThrownBy(() -> entryWithUser(1000L, null, kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 필수입니다.");
        }

        @Test
        @DisplayName("생성 시 description이 공백이면 예외")
        void description_cannot_be_blank_on_create() {
            assertThatThrownBy(() -> entryWithUser(1000L, "   ", kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("생성 시 description이 15자를 초과하면 예외 (trim 이후 길이 기준)")
        void description_must_be_at_most_15_chars_on_create() {
            assertThatThrownBy(() -> entryWithUser(1000L, "두바이쫀득쿠키가맛있을까맛없을까", kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 1자 이상 15자 이내여야 합니다.");

            assertThatThrownBy(() -> entryWithUser(1000L, "  든든한제육볶음에밥한그릇에계란까지  ", kakaoUser(new Nickname("test"), new InvitationCode("TEST"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명(description)은 1자 이상 15자 이내여야 합니다.");
        }

        @Test
        @DisplayName("생성 시 description은 trim되어 저장된다")
        void description_is_trimmed_on_create() {
            LedgerEntry entry = entryWithUser(1000L, "  점심  ", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));
            assertThat(entry.getDescription()).isEqualTo("점심");
        }

        @Test
        @DisplayName("수정 시에도 description 검증 및 trim이 적용된다")
        void description_is_trimmed_on_update() {
            LedgerEntry entry = entryWithUser(1000L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));
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
            LedgerEntry entry = entryWithUser(1000L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));

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
                kakaoUser(new Nickname("test"), new InvitationCode("TEST"))
            );
            assertThat(entry.getMemo()).isNull();
        }

        @Test
        @DisplayName("현재 구현에서는 updateMemo로 memo가 변경된다")
        void memo_can_be_updated() {
            LedgerEntry entry = entryWithUser(1000L, "점심", kakaoUser(new Nickname("test"), new InvitationCode("TEST")));
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
                kakaoUser(new Nickname("test"), new InvitationCode("TEST"))
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
