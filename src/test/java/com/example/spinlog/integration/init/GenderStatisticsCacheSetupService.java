package com.example.spinlog.integration.init;

import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheSetupService {
    private final CacheHashRepository cacheHashRepository;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching");

        StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        AllStatisticsCacheData allData = genderStatisticsRepositoryFetchService.getGenderStatisticsAllData(startDate, endDate);

        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().countData());

        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.dailyAmountSpendSums());
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.dailyAmountSaveSums());

        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().countData());

        log.info("Finish initializing Caching");
    }
}
