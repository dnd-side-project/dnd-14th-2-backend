package com.example.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VerdictTest {

    @Test
    void 소비내역을_판결_할_수_있다() {
        var juror = new User(new Nickname("test"), new InvitationCode("INCODE"), null, null, null, null);
        var entry = new LedgerEntry(100L, LedgerType.EXPENSE, LedgerCategory.FOOD, "test", LocalDate.now(), PaymentMethod.CREDIT_CARD, null, null);
        var verdict = new Verdict(entry, juror);

        verdict.judge(juror, VerdictType.GUILTY);

        assertThat(verdict.isPending()).isFalse();
    }

    @Test
    void 배심원이_아닌_자는_심판할_수_없다() {
        var fakeJuror = new User(new Nickname("test"), new InvitationCode("INCODE"), null, null, null, null);
        var realJuror = new User(new Nickname("test"), new InvitationCode("INCODE"), null, null, null, null);
        ReflectionTestUtils.setField(fakeJuror, "id", 1L);
        ReflectionTestUtils.setField(realJuror, "id", 2L);

        var entry = new LedgerEntry(100L, LedgerType.EXPENSE, LedgerCategory.FOOD, "test", LocalDate.now(), PaymentMethod.CREDIT_CARD, null, null);
        var verdict = new Verdict(entry, realJuror);

        assertThatThrownBy(() -> verdict.judge(fakeJuror, VerdictType.GUILTY))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("판결 권한이 없습니다.");
    }

    @Test
    void 이미_판결한_심판을_재판결_할_수_없다() {
        var juror = new User(new Nickname("test"), new InvitationCode("INCODE"), null, null, null, null);
        var entry = new LedgerEntry(100L, LedgerType.EXPENSE, LedgerCategory.FOOD, "test", LocalDate.now(), PaymentMethod.CREDIT_CARD, null, null);
        var verdict = new Verdict(entry, juror);
        verdict.judge(juror, VerdictType.NOT_GUILTY);

        assertThatThrownBy(() -> verdict.judge(juror, VerdictType.GUILTY))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 판결된 심판입니다.");
    }
}
