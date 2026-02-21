package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.Mate;
import com.example.demo.domain.MateRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.Verdict;
import com.example.demo.domain.VerdictRepository;
import com.example.demo.domain.VerdictType;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import com.example.demo.util.DbUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VerdictServiceTest extends AbstractIntegrationTest {

    @Autowired
    VerdictService sut;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerdictRepository verdictRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    MateRepository mateRepository;

    @Test
    void 없는_심판에_대해_판결할_수_없다() {
        User juror = DbUtils.givenSavedUser(userRepository);
        Long noExistsVerdictId = 1L;

        assertThatThrownBy(() -> sut.judge(noExistsVerdictId, juror.getId(), VerdictType.GUILTY))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 판결입니다.");
    }

    @Test
    void 없는_유저는_판결할_수_없다() {
        // given
        var user = DbUtils.givenSavedUser(userRepository);
        var juror = DbUtils.givenSavedUser(userRepository);

        var mate = new Mate(user, juror);
        mate.accept();
        mateRepository.save(mate);

        var entry = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "old",
            user
        );
        ledgerEntryRepository.save(entry);

        Verdict verdict = verdictRepository.save(new Verdict(entry, mate));

        Long noExistsJurorId = 9999L;

        // when & then
        assertThatThrownBy(() -> sut.judge(verdict.getId(), noExistsJurorId, VerdictType.GUILTY))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 유저입니다.");
    }
}
