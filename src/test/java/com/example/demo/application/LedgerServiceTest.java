package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.dto.LedgerEntriesByDateRangeResponse;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import com.example.demo.util.DbUtils;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class LedgerServiceTest extends AbstractIntegrationTest {
    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }


    @Test
    void 가계부_항목을_생성할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            savedUser.getId(),
            12000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "점심",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "메모"
        );

        // when
        LedgerResult result = ledgerService.createLedgerEntry(command);
        flushAndClear();

        // then
        assertThat(result).isNotNull();

        LedgerEntry saved = ledgerEntryRepository.findById(result.ledgerId())
            .orElseThrow(() -> new AssertionError("저장된 가계부 항목을 찾을 수 없습니다. id=" + result.ledgerId()));

        assertThat(saved.getId()).isEqualTo(result.ledgerId());
        assertThat(saved.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(saved.getAmount()).isEqualTo(12000L);
        assertThat(saved.getType()).isEqualTo(LedgerType.EXPENSE);
        assertThat(saved.getCategory()).isEqualTo(LedgerCategory.FOOD);
        assertThat(saved.getDescription()).isEqualTo("점심");
        assertThat(saved.getOccurredOn()).isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(saved.getMemo()).isEqualTo("메모");
    }

    @Test
    void 존재하지_않는_사용자면_생성_시_예외를_던진다() {
        // given
        long nonExistentUserId = 999999L;
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            nonExistentUserId,
            1000L,
            LedgerType.EXPENSE,
            LedgerCategory.OTHER,
            "테스트",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        // when & then
        assertThatThrownBy(() -> ledgerService.createLedgerEntry(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    void 사용자와_가계부ID로_가계부_항목을_조회할_수_있다() {
        // given
        User user = DbUtils.givenSavedUser(userRepository);
        LedgerEntry entry = new LedgerEntry(
            5000L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "버스",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.DEBIT_CARD,
            null,
            user
        );
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);
        flushAndClear();

        // when
        LedgerResult result = ledgerService.getLedgerEntry(user.getId(), savedEntry.getId());

        // then
        assertThat(result.ledgerId()).isEqualTo(savedEntry.getId());
        assertThat(result.amount()).isEqualTo(5000L);
        assertThat(result.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(result.category()).isEqualTo(LedgerCategory.TRANSPORT);
        assertThat(result.description()).isEqualTo("버스");
        assertThat(result.occurredOn()).isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(result.memo()).isNull();
    }

    @Test
    void 다른_사용자의_가계부를_조회하면_예외를_던진다() {
        // given
        User savedUser1 = DbUtils.givenSavedUser(userRepository);
        LedgerEntry entry = new LedgerEntry(
            5000L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "버스",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.DEBIT_CARD,
            null,
            savedUser1
        );
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);
        flushAndClear();

        User savedUser2 = DbUtils.givenSavedUser(userRepository);

        // when & then
        assertThatThrownBy(() -> ledgerService.getLedgerEntry(savedUser2.getId(), savedEntry.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당되는 가계부 항목이 존재하지 않습니다.");
    }

    @Test
    void 메모를_수정할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LedgerEntry entry = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            null,
            savedUser
        );
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);
        flushAndClear();

        // when
        ledgerService.updateLedgerMemo(savedUser.getId(), savedEntry.getId(), "아메리카노");
        flushAndClear();

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(savedEntry.getId()).orElseThrow();
        assertThat(updated.getMemo()).isEqualTo("아메리카노");
    }

    @Test
    void 가계부_항목을_수정할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LedgerEntry entry = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "old",
            savedUser
        );
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);
        flushAndClear();

        // when
        UpsertLedgerCommand newCommand = new UpsertLedgerCommand(
            savedUser.getId(),
            15000L,
            LedgerType.EXPENSE,
            LedgerCategory.SHOPPING,
            "옷",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            "new"
        );
        LedgerResult result = ledgerService.updateLedgerEntry(savedEntry.getId(), newCommand);
        flushAndClear();

        // then
        assertThat(result.amount()).isEqualTo(15000L);
        LedgerEntry updated = ledgerEntryRepository.findById(savedEntry.getId()).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("옷");
        assertThat(updated.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updated.getMemo()).isEqualTo("new");
    }

    @Test
    void 가계부_항목을_삭제할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LedgerEntry entry = new LedgerEntry(
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            null,
            savedUser
        );
        LedgerEntry savedEntry = ledgerEntryRepository.save(entry);

        // when
        ledgerService.deleteLedgerEntry(savedUser.getId(), savedEntry.getId());
        flushAndClear();

        // then
        assertThat(ledgerEntryRepository.findById(savedEntry.getId())).isEmpty();
    }

    @Test
    void 날짜_범위로_내역을_조회할_수_있고_발생일_오름차순_동일일자는_ID_오름차순으로_정렬된다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LocalDate start = LocalDate.of(2026, 1, 24);
        LocalDate end = LocalDate.of(2026, 1, 26);

        LedgerEntry entry1 = new LedgerEntry(
            1000L,
            LedgerType.INCOME,
            LedgerCategory.ALLOWANCE,
            "용돈",
            start,
            PaymentMethod.BANK_TRANSFER,
            null,
            savedUser
        );
        ledgerEntryRepository.save(entry1);

        LedgerEntry entry2 = new LedgerEntry(
            300L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "간식",
            start,
            PaymentMethod.CASH,
            null,
            savedUser
        );
        ledgerEntryRepository.save(entry2);

        LedgerEntry entry3 = new LedgerEntry(
            200L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "지하철",
            end,
            PaymentMethod.DEBIT_CARD,
            null,
            savedUser
        );
        ledgerEntryRepository.save(entry3);
        flushAndClear();

        // when
        LedgerEntriesByDateRangeResponse response = ledgerService.getSummary(savedUser.getId(), start, end);
        List<LedgerResult> result = response.results();

        // then
        assertThat(result).hasSize(3);

        assertThat(result.get(0).occurredOn()).isEqualTo(start);
        assertThat(result.get(1).occurredOn()).isEqualTo(start);
        assertThat(result.get(2).occurredOn()).isEqualTo(end);
        assertThat(result.get(0).ledgerId()).isLessThan(result.get(1).ledgerId());

        LedgerResult r1 = result.get(0);
        assertThat(r1.amount()).isEqualTo(1000L);
        assertThat(r1.type()).isEqualTo(LedgerType.INCOME);
        assertThat(r1.category()).isEqualTo(LedgerCategory.ALLOWANCE);
        assertThat(r1.description()).isEqualTo("용돈");
        assertThat(r1.paymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(r1.memo()).isNull();

        LedgerResult r2 = result.get(1);
        assertThat(r2.amount()).isEqualTo(300L);
        assertThat(r2.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(r2.category()).isEqualTo(LedgerCategory.FOOD);
        assertThat(r2.description()).isEqualTo("간식");
        assertThat(r2.paymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(r2.memo()).isNull();

        LedgerResult r3 = result.get(2);
        assertThat(r3.amount()).isEqualTo(200L);
        assertThat(r3.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(r3.category()).isEqualTo(LedgerCategory.TRANSPORT);
        assertThat(r3.description()).isEqualTo("지하철");
        assertThat(r3.paymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(r3.memo()).isNull();
    }
}
