package com.example.demo.application;

import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.demo.util.DbUtils.givenSavedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class LedgerStatisticsServiceTest extends AbstractIntegrationTest {

    @MockitoBean
    private Clock clock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private LedgerStatisticsService ledgerStatisticsService;

    private static final Long USER_ID = 1L;
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
            Instant.parse("2024-02-15T00:00:00Z"),
            ZONE_ID
        );
        given(clock.instant()).willReturn(fixedClock.instant());
        given(clock.getZone()).willReturn(fixedClock.getZone());
    }

    @Test
    void 정상적으로_지출_통계_조회() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        // 이번 달(2024-02) 항목들
        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 350000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.TRANSPORT, 120000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.SHOPPING, 85000L, LocalDate.of(2024, 2, 15), user)
        ));

        // 지난 달(2024-01) 항목들
        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 300000L, LocalDate.of(2024, 1, 10), user),
            ledgerEntry(type, LedgerCategory.TRANSPORT, 200000L, LocalDate.of(2024, 1, 12), user),
            ledgerEntry(type, LedgerCategory.SHOPPING, 80000L, LocalDate.of(2024, 1, 20), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(response.categoryAmounts()).hasSize(3);
        assertThat(response.categoryAmounts())
            .containsEntry(LedgerCategory.FOOD, 350000L)
            .containsEntry(LedgerCategory.TRANSPORT, 120000L)
            .containsEntry(LedgerCategory.SHOPPING, 85000L);
        assertThat(response.currentMonthTotalAmount()).isEqualTo(555000L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(580000L);
    }

    @Test
    void 정상적으로_수입_통계_조회() {
        // given
        LedgerType type = LedgerType.INCOME;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.SALARY, 3000000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.SIDE_INCOME, 500000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.BONUS, 1000000L, LocalDate.of(2024, 2, 15), user)
        ));

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.SALARY, 3000000L, LocalDate.of(2024, 1, 5), user),
            ledgerEntry(type, LedgerCategory.SIDE_INCOME, 500000L, LocalDate.of(2024, 1, 18), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.type()).isEqualTo(LedgerType.INCOME);
        assertThat(response.categoryAmounts()).hasSize(3);
        assertThat(response.currentMonthTotalAmount()).isEqualTo(4500000L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(3500000L);
    }

    @Test
    void 이번달_데이터가_없을때_카테고리_맵은_비고_총액은_0() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 300000L, LocalDate.of(2024, 1, 10), user),
            ledgerEntry(type, LedgerCategory.TRANSPORT, 200000L, LocalDate.of(2024, 1, 12), user),
            ledgerEntry(type, LedgerCategory.SHOPPING, 80000L, LocalDate.of(2024, 1, 20), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).isEmpty();
        assertThat(response.currentMonthTotalAmount()).isEqualTo(0L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(580000L);
    }

    @Test
    void 여러_카테고리의_금액을_정확히_합산() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 100000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.TRANSPORT, 50000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.SHOPPING, 30000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.HEALTH_MEDICAL, 20000L, LocalDate.of(2024, 2, 15), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.currentMonthTotalAmount()).isEqualTo(200000L);
    }

    @Test
    void 같은_카테고리가_여러번_등장하면_합산된다() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 10000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.FOOD, 25000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.FOOD, 5000L, LocalDate.of(2024, 2, 15), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).containsEntry(LedgerCategory.FOOD, 40000L);
        assertThat(response.currentMonthTotalAmount()).isEqualTo(40000L);
    }

    @Test
    void 카테고리가_하나인_경우에도_정상_계산() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 500000L, LocalDate.of(2024, 2, 15), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.currentMonthTotalAmount()).isEqualTo(500000L);
        assertThat(response.categoryAmounts()).hasSize(1);
        assertThat(response.categoryAmounts()).containsEntry(LedgerCategory.FOOD, 500000L);
    }

    @Test
    void 모든_카테고리를_빠짐없이_변환() {
        // given
        LedgerType type = LedgerType.EXPENSE;
        User user = givenSavedUser(userRepository);

        saveAllLedgerEntry(List.of(
            ledgerEntry(type, LedgerCategory.FOOD, 10000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.TRANSPORT, 20000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.SHOPPING, 30000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.HEALTH_MEDICAL, 40000L, LocalDate.of(2024, 2, 15), user),
            ledgerEntry(type, LedgerCategory.LEISURE_HOBBY, 50000L, LocalDate.of(2024, 2, 15), user)
        ));

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).hasSize(5);
        assertThat(response.categoryAmounts()).containsKeys(
            LedgerCategory.FOOD,
            LedgerCategory.TRANSPORT,
            LedgerCategory.SHOPPING,
            LedgerCategory.HEALTH_MEDICAL,
            LedgerCategory.LEISURE_HOBBY
        );
    }

    private void saveAllLedgerEntry(List<LedgerEntry> ledgerEntries) {
        for (LedgerEntry ledgerEntry : ledgerEntries) {
            ledgerEntryRepository.save(ledgerEntry);
        }
    }

    private LedgerEntry ledgerEntry(LedgerType type, LedgerCategory category, long amount, LocalDate occurredOn, User user) {
        return new LedgerEntry(
            amount,
            type,
            category,
            "desc",
            occurredOn,
            PaymentMethod.CREDIT_CARD,
            "memo",
            user
        );
    }
}