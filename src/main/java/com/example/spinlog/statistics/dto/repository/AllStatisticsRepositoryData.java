package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.statistics.dto.DailyAmountSumDto;
import com.example.spinlog.statistics.dto.EmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.user.entity.Gender;
import lombok.Builder;

import java.util.List;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Builder
public record AllStatisticsRepositoryData(
    List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts,
    List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts,
    List<DailyAmountSumDto> dailyAmountSpendSums,
    List<DailyAmountSumDto> dailyAmountSaveSums,
    List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts,
    List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts) {
    public AllStatisticsCacheData toCacheDate(MBTIFactor mbtiFactor){
        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(
                        toMBTIEmotionAmountSumAndCountStatisticsData(toMBTIEmotionAmountDtoList(emotionAmountSpendSumsAndCounts, mbtiFactor)))
                .emotionAmountSaveSumAndCountStatisticsData(
                        toMBTIEmotionAmountSumAndCountStatisticsData(toMBTIEmotionAmountDtoList(emotionAmountSaveSumsAndCounts, mbtiFactor)))
                .dailyAmountSpendSums(
                        toMBTIDateMap(toMBTIDailyAmountDtoList(dailyAmountSpendSums, mbtiFactor)))
                .dailyAmountSaveSums(
                        toMBTIDateMap(toMBTIDailyAmountDtoList(dailyAmountSaveSums, mbtiFactor)))
                .satisfactionSpendSumAndCountStatisticsData(
                        toMBTISatisfactionSumAndCountStatisticsData(toMBTISatisfactionDtoList(satisfactionSpendSumsAndCounts, mbtiFactor)))
                .satisfactionSaveSumAndCountStatisticsData(
                        toMBTISatisfactionSumAndCountStatisticsData(toMBTISatisfactionDtoList(satisfactionSaveSumsAndCounts, mbtiFactor)))
                .build();
    }

    public AllStatisticsCacheData toCacheDate(Gender gender) {
        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(
                        toGenderEmotionAmountSumAndCountStatisticsData(emotionAmountSpendSumsAndCounts, gender))
                .emotionAmountSaveSumAndCountStatisticsData(
                        toGenderEmotionAmountSumAndCountStatisticsData(emotionAmountSaveSumsAndCounts, gender))
                .dailyAmountSpendSums(
                        toGenderDateMap(dailyAmountSpendSums, gender))
                .dailyAmountSaveSums(
                        toGenderDateMap(dailyAmountSaveSums, gender))
                .satisfactionSpendSumAndCountStatisticsData(
                        toGenderSatisfactionSumAndCountStatisticsData(satisfactionSpendSumsAndCounts, gender))
                .satisfactionSaveSumAndCountStatisticsData(
                        toGenderSatisfactionSumAndCountStatisticsData(satisfactionSaveSumsAndCounts, gender))
                .build();
    }
}
