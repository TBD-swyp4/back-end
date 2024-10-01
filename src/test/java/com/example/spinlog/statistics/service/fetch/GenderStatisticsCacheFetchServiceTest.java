package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import com.example.spinlog.utils.StatisticsCacheUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.getGenderEmotionStatisticsAmountSumKeyName;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheFetchServiceTest {
    HashCacheService hashCacheService = mock(HashCacheService.class);
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService =
            new GenderStatisticsCacheFetchService(hashCacheService);

    @Nested
    class getAmountAveragesEachGenderAndEmotionLast30Days {
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 1000L,
                            "MALE::SAD", 2000L,
                            "FEMALE::PROUD", 3000L,
                            "FEMALE::SAD", 4000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 5L,
                            "FEMALE::PROUD", 5L,
                            "FEMALE::SAD", 5L));

            // when
            CountsAndSums results = genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(registerType);

            // then
            List<GenderEmotionAmountAverageDto> dtos = convertToGenderEmotionAmountAverageDto(results);
            for(GenderEmotionAmountAverageDto dto : dtos) {
                switch(dto.getGender() + "::" + dto.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(dto.getAmountAverage()).isEqualTo(200L);
                        break;
                    case "MALE::SAD":
                        assertThat(dto.getAmountAverage()).isEqualTo(400L);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(dto.getAmountAverage()).isEqualTo(600L);
                        break;
                    case "FEMALE::SAD":
                        assertThat(dto.getAmountAverage()).isEqualTo(800L);
                        break;
                }
            }
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDayLast30Days{
        @Test
        void CacheService로부터_데이터를_조회한_후_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::2024-07-01", 1000L,
                            "MALE::2024-07-02", 2000L,
                            "FEMALE::2024-07-01", 3000L,
                            "FEMALE::2024-07-02", 4000L));
            
            // when
            Map<String, Object> results = genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType);

            // then
            List<GenderDailyAmountSumDto> dtos = convertToGenderDailyAmountSumDto(results);
            for(GenderDailyAmountSumDto dto : dtos) {
                switch (dto.getGender() + "::" + dto.getLocalDate()) {
                    case "MALE::2024-07-01":
                        assertThat(dto.getAmountSum()).isEqualTo(1000L);
                        break;
                    case "MALE::2024-07-02":
                        assertThat(dto.getAmountSum()).isEqualTo(2000L);
                        break;
                    case "FEMALE::2024-07-01":
                        assertThat(dto.getAmountSum()).isEqualTo(3000L);
                        break;
                    case "FEMALE::2024-07-02":
                        assertThat(dto.getAmountSum()).isEqualTo(4000L);
                        break;
                }
            }
        }
    }

    @Nested
    class getSatisfactionAveragesEachGenderLast30Days{
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE", 34.0,
                            "FEMALE", 78.0));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE", 10L,
                            "FEMALE", 20L));

            // when
            CountsAndSums results = genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType);

            // then
            List<GenderSatisfactionAverageDto> dtos = convertToGenderSatisfactionAverageDto(results);
            for(GenderSatisfactionAverageDto dto : dtos) {
                switch(dto.getGender()) {
                    case MALE:
                        assertThat(dto.getSatisfactionAverage()).isEqualTo(34.0f/10);
                        break;
                    case FEMALE:
                        assertThat(dto.getSatisfactionAverage()).isEqualTo(78.0f/20);
                        break;
                }
            }
        }
    }
}