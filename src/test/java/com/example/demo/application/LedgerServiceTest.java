package com.example.demo.application;

import com.example.demo.application.dto.DailyLedgerDetail;
import com.example.demo.application.dto.DailySummary;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.domain.*;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class LedgerServiceTest extends AbstractIntegrationTest {
    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Long userId;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "https://profile.com/image.png", Provider.KAKAO, "kakao-test-1");
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void 가계부_항목을_생성할_수_있다() {
        // given
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            userId,
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

        // then
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualTo(12000L);
        assertThat(result.type()).isEqualTo(LedgerType.EXPENSE);

        LedgerEntry saved = ledgerEntryRepository.findById(result.ledgerId()).orElseThrow();
        assertThat(saved.getDescription()).isEqualTo("점심");
    }

    @Test
    void 존재하지_않는_사용자면_생성_시_예외를_던진다() {
        // given
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            999999L,
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
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(
            5000, LedgerType.EXPENSE, LedgerCategory.TRANSPORT, "버스",
            LocalDate.of(2026, 1, 24), PaymentMethod.DEBIT_CARD, null, this.user
        ));

        // when
        LedgerResult result = ledgerService.getLedgerEntry(userId, entry.getId());

        // then
        assertThat(result.ledgerId()).isEqualTo(entry.getId());
        assertThat(result.description()).isEqualTo("버스");
    }

    @Test
    void 다른_사용자의_가계부를_조회하면_예외를_던진다() {
        // given
        User user2 = new User("user2@example.com", "profile.com/image.jpg", Provider.GOOGLE, "google-test-1");
        User savedUser2 = userRepository.save(user2);

        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(
            5000L, LedgerType.EXPENSE, LedgerCategory.TRANSPORT, "버스",
            LocalDate.of(2026, 1, 24), PaymentMethod.DEBIT_CARD, null, user
        ));

        // when & then
        assertThatThrownBy(() -> ledgerService.getLedgerEntry(savedUser2.getId(), entry.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당되는 가계부 항목이 존재하지 않습니다.");
    }

    @Test
    void 메모를_수정할_수_있다() {
        // given
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(
            7000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "커피",
            LocalDate.of(2026, 1, 24), PaymentMethod.CREDIT_CARD, null, this.user
        ));

        // when
        ledgerService.updateLedgerMemo(userId, entry.getId(), "아메리카노");

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getMemo()).isEqualTo("아메리카노");
    }

    @Test
    void 가계부_항목을_수정할_수_있다() {
        // given
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(
            7000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "커피",
            LocalDate.of(2026, 1, 24), PaymentMethod.CREDIT_CARD, "old", this.user
        ));

        UpsertLedgerCommand command = new UpsertLedgerCommand(
            userId,
            9000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피(대)",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "new"
        );

        // when
        LedgerResult result = ledgerService.updateLedgerEntry(entry.getId(), command);

        // then
        assertThat(result.amount()).isEqualTo(9000L);
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("커피(대)");
        assertThat(updated.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(updated.getMemo()).isEqualTo("new");
    }

    @Test
    void 가계부_항목을_삭제할_수_있다() {
        // given
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(
            7000L, LedgerType.EXPENSE, LedgerCategory.FOOD, "커피",
            LocalDate.of(2026, 1, 24), PaymentMethod.CREDIT_CARD, null, this.user
        ));

        // when
        ledgerService.deleteLedgerEntry(userId, entry.getId());

        // then
        assertThat(ledgerEntryRepository.findById(entry.getId())).isEmpty();
    }

    @Test
    void 날짜_범위로_일별_요약을_조회할_수_있고_데이터가_없는_날짜는_0으로_채운다() {
        // given
        LocalDate start = LocalDate.of(2026, 1, 24);
        LocalDate end = LocalDate.of(2026, 1, 26);

        ledgerEntryRepository.save(new LedgerEntry(
            1000L, LedgerType.INCOME, LedgerCategory.SAVINGS_FINANCE, "용돈",
            start, PaymentMethod.BANK_TRANSFER, null, this.user
        ));
        ledgerEntryRepository.save(new LedgerEntry(
            300L, LedgerType.EXPENSE, LedgerCategory.FOOD, "간식",
            start, PaymentMethod.CASH, null, this.user
        ));

        ledgerEntryRepository.save(new LedgerEntry(
            200L, LedgerType.EXPENSE, LedgerCategory.TRANSPORT, "지하철",
            end, PaymentMethod.DEBIT_CARD, null, this.user
        ));

        // when
        List<DailySummary> result = ledgerService.getSummary(userId, start, end);

        // then
        assertThat(result).hasSize(3);

        DailySummary d1 = result.get(0);
        assertThat(d1.date()).isEqualTo(start);
        assertThat(d1.incomeTotal()).isEqualTo(1000L);
        assertThat(d1.expenseTotal()).isEqualTo(300L);

        DailySummary d2 = result.get(1);
        assertThat(d2.date()).isEqualTo(start.plusDays(1));
        assertThat(d2.incomeTotal()).isEqualTo(0L);
        assertThat(d2.expenseTotal()).isEqualTo(0L);

        DailySummary d3 = result.get(2);
        assertThat(d3.date()).isEqualTo(end);
        assertThat(d3.incomeTotal()).isEqualTo(0L);
        assertThat(d3.expenseTotal()).isEqualTo(200L);
    }

    @Test
    void 특정_날짜의_가계부_항목과_수입지출_합계를_조회할_수_있다() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 24);

        ledgerEntryRepository.save(new LedgerEntry(
            2000L, LedgerType.INCOME, LedgerCategory.SAVINGS_FINANCE, "입금",
            date, PaymentMethod.BANK_TRANSFER, null, this.user
        ));
        ledgerEntryRepository.save(new LedgerEntry(
            500L, LedgerType.EXPENSE, LedgerCategory.FOOD, "점심",
            date, PaymentMethod.CREDIT_CARD, null, this.user
        ));
        ledgerEntryRepository.save(new LedgerEntry(
            700L, LedgerType.EXPENSE, LedgerCategory.TRANSPORT, "버스",
            date, PaymentMethod.DEBIT_CARD, null, this.user
        ));

        // when
        DailyLedgerDetail detail = ledgerService.getLedgerEntriesByDate(userId, date);

        // then
        assertThat(detail.date()).isEqualTo(date);
        assertThat(detail.incomeTotal()).isEqualTo(2000L);
        assertThat(detail.expenseTotal()).isEqualTo(1200L);
        assertThat(detail.results()).hasSize(3);
    }
}