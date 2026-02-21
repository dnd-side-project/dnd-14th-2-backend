package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.JurorVerdicts;
import com.example.demo.application.dto.MyVerdicts;
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
import java.util.List;
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

    @Test
    void 소비_심판_요청시_친구_수만큼_소비_심판이_요청된다() {
        // given
        var user = DbUtils.givenSavedUser(userRepository);
        var juror1 = DbUtils.givenSavedUser(userRepository);
        var juror2 = DbUtils.givenSavedUser(userRepository);
        var juror3 = DbUtils.givenSavedUser(userRepository);

        var mate1 = new Mate(user, juror1);
        var mate2 = new Mate(user, juror2);
        var mate3 = new Mate(user, juror3);
        mate1.accept();
        mate2.accept();
        mate3.accept();
        mateRepository.save(mate1);
        mateRepository.save(mate2);
        mateRepository.save(mate3);

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
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);

        // when
        sut.requestVerdict(savedEntry.getId(), user.getId());

        // then
        List<Verdict> entries = verdictRepository.findAllByLedgerEntry_Id(savedEntry.getId());
        assertThat(entries).hasSize(3);
    }

    @Test
    void 본인_소비가_아니면_심판_요청을_할_수_없다() {
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

        // when & then
        assertThatThrownBy(() -> sut.requestVerdict(entry.getId(), juror.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당되는 가계부 항목이 존재하지 않습니다.");
    }

    @Test
    void 본인이_요청한_소비심판을_조회할_수_있다() {
        // given
        var user = DbUtils.givenSavedUser(userRepository);
        var juror1 = DbUtils.givenSavedUser(userRepository);
        var juror2 = DbUtils.givenSavedUser(userRepository);
        var juror3 = DbUtils.givenSavedUser(userRepository);

        var mate1 = new Mate(user, juror1);
        var mate2 = new Mate(user, juror2);
        var mate3 = new Mate(user, juror3);
        mate1.accept();
        mate2.accept();
        mate3.accept();
        mateRepository.save(mate1);
        mateRepository.save(mate2);
        mateRepository.save(mate3);

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
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);

        sut.requestVerdict(entry.getId(), user.getId());
        List<Verdict> verdicts = verdictRepository.findAllByLedgerEntry_Id(savedEntry.getId());
        sut.judge(verdicts.get(0).getId(), juror1.getId(), VerdictType.GUILTY);

        // when
        MyVerdicts myVerdicts = sut.getMyVerdicts(user.getId());

        // then
        assertThat(myVerdicts.verdicts()).hasSize(3);
        assertThat(myVerdicts.verdicts()).filteredOn(v -> v.verdictType() == VerdictType.GUILTY).hasSize(1);
        assertThat(myVerdicts.verdicts()).filteredOn(v -> v.verdictType() == VerdictType.PENDING).hasSize(2);
    }

    @Test
    void 내가_판결해야_할_소비심판을_조회할_수_있다() {
        // given
        var juror = DbUtils.givenSavedUser(userRepository);
        var user1 = DbUtils.givenSavedUser(userRepository);
        var user2 = DbUtils.givenSavedUser(userRepository);

        var mate1 = new Mate(user1, juror);
        var mate2 = new Mate(user2, juror);
        mate1.accept();
        mate2.accept();
        Mate savedMate1 = mateRepository.save(mate1);
        mateRepository.save(mate2);

        var entry1 = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "old",
            user1
        );
        var entry2 = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "old",
            user2
        );
        LedgerEntry savedEntry1 = ledgerEntryRepository.save(entry1);
        LedgerEntry savedEntry2 = ledgerEntryRepository.save(entry2);

        sut.requestVerdict(savedEntry1.getId(), user1.getId());
        sut.requestVerdict(savedEntry2.getId(), user2.getId());
        List<Verdict> verdicts = verdictRepository.findAllByLedgerEntry_Id(savedEntry1.getId());
        Verdict user1Verdict = verdicts.stream()
            .filter(v -> v.getMate().getId().equals(savedMate1.getId()))
            .findFirst()
            .orElseThrow();
        sut.judge(user1Verdict.getId(), juror.getId(), VerdictType.GUILTY);

        // when
        JurorVerdicts jurorVerdicts = sut.getJurorVerdicts(juror.getId());

        // then
        assertThat(jurorVerdicts.jurorVerdicts()).hasSize(2);
        assertThat(jurorVerdicts.jurorVerdicts()).filteredOn(v -> v.verdictType() == VerdictType.GUILTY).hasSize(1);
        assertThat(jurorVerdicts.jurorVerdicts()).filteredOn(v -> v.verdictType() == VerdictType.PENDING).hasSize(1);
    }
}
