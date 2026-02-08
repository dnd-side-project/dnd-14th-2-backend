package com.example.demo.application;

import com.example.demo.application.dto.CategoryAmountDto;
import com.example.demo.application.dto.LedgerStatisticsResponse;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LedgerStatisticsServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
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

    void 정삭적으로_지출_통계_조회() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        // 이번 달 카테고리별 금액
        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.FOOD, 350000L),
            new CategoryAmountDto(LedgerCategory.TRANSPORT, 120000L),
            new CategoryAmountDto(LedgerCategory.SHOPPING, 85000L)
        );

        // Repository Mock 설정
        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2024, 2, 1)),  // 이번 달 시작
            eq(LocalDate.of(2024, 2, 29))  // 이번 달 끝
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2024, 1, 1)),  // 지난 달 시작
            eq(LocalDate.of(2024, 1, 31))  // 지난 달 끝
        )).willReturn(580000L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(response.categoryAmounts()).hasSize(3);
        assertThat(response.categoryAmounts())
            .containsEntry("FOOD", 350000L)
            .containsEntry("TRANSPORT", 120000L)
            .containsEntry("SHOPPING", 85000L);
        assertThat(response.currentMonthTotalAmount()).isEqualTo(555000L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(580000L);

        // Repository 호출 검증
        verify(ledgerEntryRepository).findCategoryAmountsByUserAndTypeAndPeriod(
            USER_ID, type,
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 29)
        );
        verify(ledgerEntryRepository).findTotalAmountByUserAndTypeAndPeriod(
            USER_ID, type,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        );
    }

    @Test
    void 정상적으로_수입_통계_조회() {
        // given
        LedgerType type = LedgerType.INCOME;

        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.SALARY, 3000000L),
            new CategoryAmountDto(LedgerCategory.SIDE_INCOME, 500000L),
            new CategoryAmountDto(LedgerCategory.BONUS, 1000000L)
        );

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(3500000L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.type()).isEqualTo(LedgerType.INCOME);
        assertThat(response.categoryAmounts()).hasSize(3);
        assertThat(response.currentMonthTotalAmount()).isEqualTo(4500000L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(3500000L);
    }

    @Test
    void 이번달_데이터가_없을때_총액은_0() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(Collections.emptyList());

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(580000L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).isEmpty();
        assertThat(response.currentMonthTotalAmount()).isEqualTo(0L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(580000L);
    }

    @Test
    void 지난달_데이터가_없는경우_총액은_0() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.FOOD, 200000L)
        );

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(null);  // null 반환

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.currentMonthTotalAmount()).isEqualTo(200000L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(0L);  // null이 0으로 변환
    }

    @Test
    void 이번달과_지난달_데이터가_없는_경우() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(Collections.emptyList());

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).isEmpty();
        assertThat(response.currentMonthTotalAmount()).isEqualTo(0L);
        assertThat(response.lastMonthTotalAmount()).isEqualTo(0L);
    }


    @Test
    void 올해_1월의_지난달은_작년_12월() {
        // given
        Clock januaryClock = Clock.fixed(
            Instant.parse("2024-01-15T00:00:00Z"),
            ZONE_ID
        );
        given(clock.instant()).willReturn(januaryClock.instant());
        given(clock.getZone()).willReturn(januaryClock.getZone());

        LedgerType type = LedgerType.EXPENSE;

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2024, 1, 1)),   // 이번 달: 2024년 1월
            eq(LocalDate.of(2024, 1, 31))
        )).willReturn(Collections.emptyList());

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2023, 12, 1)),  // 지난 달: 2023년 12월
            eq(LocalDate.of(2023, 12, 31))
        )).willReturn(500000L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        verify(ledgerEntryRepository).findTotalAmountByUserAndTypeAndPeriod(
            USER_ID, type,
            LocalDate.of(2023, 12, 1),
            LocalDate.of(2023, 12, 31)
        );
    }

    @Test
    void 윤년_2월의_마지막날은_29일() {
        // given
        Clock februaryClock = Clock.fixed(
            Instant.parse("2024-02-15T00:00:00Z"),  // 2024는 윤년
            ZONE_ID
        );
        given(clock.instant()).willReturn(februaryClock.instant());
        given(clock.getZone()).willReturn(februaryClock.getZone());

        LedgerType type = LedgerType.EXPENSE;

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2024, 2, 1)),
            eq(LocalDate.of(2024, 2, 29))   // 윤년은 29일까지
        )).willReturn(Collections.emptyList());

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        verify(ledgerEntryRepository).findCategoryAmountsByUserAndTypeAndPeriod(
            USER_ID, type,
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 29)
        );
    }

    @Test
    void 평년_2월의_마지막날은_28일() {
        // given
        Clock februaryClock = Clock.fixed(
            Instant.parse("2023-02-15T00:00:00Z"),  // 2023은 평년
            ZONE_ID
        );
        given(clock.instant()).willReturn(februaryClock.instant());
        given(clock.getZone()).willReturn(februaryClock.getZone());

        LedgerType type = LedgerType.EXPENSE;

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            eq(USER_ID),
            eq(type),
            eq(LocalDate.of(2023, 2, 1)),
            eq(LocalDate.of(2023, 2, 28))   // 평년은 28일까지
        )).willReturn(Collections.emptyList());

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        ledgerStatisticsService.getMonthlyStatistics(USER_ID, type);

        // then
        verify(ledgerEntryRepository).findCategoryAmountsByUserAndTypeAndPeriod(
            USER_ID, type,
            LocalDate.of(2023, 2, 1),
            LocalDate.of(2023, 2, 28)
        );
    }

    @Test
    void 여러_카테고리의_금앨을_정확히_합산() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.FOOD, 100000L),
            new CategoryAmountDto(LedgerCategory.TRANSPORT, 50000L),
            new CategoryAmountDto(LedgerCategory.SHOPPING, 30000L),
            new CategoryAmountDto(LedgerCategory.HEALTH_MEDICAL, 20000L)
        );

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.currentMonthTotalAmount()).isEqualTo(200000L);
    }

    @Test
    void 카테고리가_하나인_경우에도_정상_계산() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.FOOD, 500000L)
        );

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.currentMonthTotalAmount()).isEqualTo(500000L);
        assertThat(response.categoryAmounts()).hasSize(1);
    }


    @Test
    void 모든_카테고리를_빠짐없이_변환() {
        // given
        LedgerType type = LedgerType.EXPENSE;

        List<CategoryAmountDto> categoryAmounts = List.of(
            new CategoryAmountDto(LedgerCategory.FOOD, 10000L),
            new CategoryAmountDto(LedgerCategory.TRANSPORT, 20000L),
            new CategoryAmountDto(LedgerCategory.SHOPPING, 30000L),
            new CategoryAmountDto(LedgerCategory.HEALTH_MEDICAL, 40000L),
            new CategoryAmountDto(LedgerCategory.LEISURE_HOBBY, 50000L)
        );

        given(ledgerEntryRepository.findCategoryAmountsByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(categoryAmounts);

        given(ledgerEntryRepository.findTotalAmountByUserAndTypeAndPeriod(
            any(), any(), any(), any()
        )).willReturn(0L);

        // when
        LedgerStatisticsResponse response = ledgerStatisticsService
            .getMonthlyStatistics(USER_ID, type);

        // then
        assertThat(response.categoryAmounts()).hasSize(5);
        assertThat(response.categoryAmounts()).containsKeys(
            "FOOD", "TRANSPORT", "SHOPPING", "HEALTH_MEDICAL", "LEISURE_HOBBY"
        );
    }

}