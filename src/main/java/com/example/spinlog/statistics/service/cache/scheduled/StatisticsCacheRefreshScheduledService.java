package com.example.spinlog.statistics.service.cache.scheduled;

import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.cache.MBTIStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheRefreshScheduledService {
    private final CacheHashRepository cacheHashRepository;

    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;

    private final MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService;
    private final MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService;

    private final StatisticsPeriodManager statisticsPeriodManager;

    // todo prometheus & grafana로 성공 여부 확인
    // todo read 문제는 없음, but article write 시에 Race condition 발생 가능성 있음 -> 캐시 업데이트 할때 lock 걸어야 함
    //  -> PERIOD CRITERIA을 별도의 클래스로 관리하여 lock 걸어야 함
    @Scheduled(cron = "0 0 4 * * *")
    public void refreshGenderStatisticsCache() {
        log.info("Start refreshing Caching.");

        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate todayStartDate = period.endDate();
        LocalDate todayEndDate = todayStartDate.plusDays(1);
        log.info("newData's startDate: {}, endDate: {}", todayStartDate, todayEndDate);

        AllStatisticsCacheData newGenderStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(todayStartDate, todayEndDate);
        AllStatisticsCacheData newMBTIStatisticsData = mbtiStatisticsRepositoryFetchService
                .getMBTIStatisticsAllData(todayStartDate, todayEndDate);
        log.info("\nnewGenderStatisticsData: {}\n", newGenderStatisticsData);
        log.info("\nnewMBTIStatisticsData: {}\n", newMBTIStatisticsData);

        LocalDate oldStartDate = period.startDate();
        LocalDate oldEndDate = oldStartDate.plusDays(1);
        log.info("expiringData's startDate: {}, endDate: {}", oldStartDate, oldEndDate);

        AllStatisticsCacheData expiringGenderStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(oldStartDate, oldEndDate);
        AllStatisticsCacheData expiringMBTIStatisticsData = mbtiStatisticsRepositoryFetchService
                .getMBTIStatisticsAllData(oldStartDate, oldEndDate);
        log.info("\nexpiringGenderStatisticsData: {}\n", expiringGenderStatisticsData);
        log.info("\nexpiringMBTIStatisticsData: {}\n", expiringMBTIStatisticsData);

        try {
            // todo lock
            decrementOldGenderCacheData(expiringGenderStatisticsData);
            decrementOldMBTICacheData(expiringMBTIStatisticsData);
            incrementNewGenderCacheData(newGenderStatisticsData);
            incrementNewMBTICacheData(newMBTIStatisticsData);
            deleteExpiringDateCache(oldStartDate);
            zeroPaddingNewDateCache(todayStartDate);
            // todo unlock
        } catch (Exception e) {
            log.error("Error occurred while updating cache data.", e);
        } finally {
            log.info("Finish refreshing Caching.");
            statisticsPeriodManager.updateStatisticsPeriod();
        }
    }

    private void incrementNewMBTICacheData(AllStatisticsCacheData cacheData) {
        log.info("try to increase all mbti data");
        mbtiStatisticsCacheWriteService.incrementAllData(cacheData);
    }

    private void decrementOldMBTICacheData(AllStatisticsCacheData cacheData) {
        log.info("try to decrease all mbti data");
        mbtiStatisticsCacheWriteService.decrementAllData(cacheData);
    }

    private void incrementNewGenderCacheData(AllStatisticsCacheData newStatisticsData) {
        log.info("try to increase all gender data");
        genderStatisticsCacheWriteService.incrementAllData(newStatisticsData);
    }

    private void decrementOldGenderCacheData(AllStatisticsCacheData expiringStatisticsData) {
        log.info("try to decrease all gender data");
        genderStatisticsCacheWriteService.decrementAllData(expiringStatisticsData);
    }

    private void zeroPaddingNewDateCache(LocalDate todayStartDate) {
        log.info("try to zero padding new date cache");
        Arrays.stream(Gender.values()).filter(g -> !g.equals(Gender.NONE))
                .map(g -> g + "::" + todayStartDate)
                .forEach(k -> {
                    if(cacheHashRepository.getDataFromHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k) == null) {
                        cacheHashRepository.putDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k, 0L);
                    }
                    if(cacheHashRepository.getDataFromHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k) == null) {
                        cacheHashRepository.putDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k, 0L);
                    }
                });

        Arrays.stream(MBTIFactor.values())
                .map(f -> f + "::" + todayStartDate)
                .forEach(k -> {
                    if(cacheHashRepository.getDataFromHash(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k) == null) {
                        cacheHashRepository.putDataInHash(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k, 0L);
                    }
                    if(cacheHashRepository.getDataFromHash(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k) == null) {
                        cacheHashRepository.putDataInHash(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k, 0L);
                    }
                });
    }

    private void deleteExpiringDateCache(LocalDate oldStartDate) {
        log.info("try to delete expiring date cache");
        Arrays.stream(Gender.values()).filter(g -> !g.equals(Gender.NONE))
                .map(g -> g + "::" + oldStartDate)
                .forEach(k -> {
                    cacheHashRepository.deleteHashKey(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k);
                    cacheHashRepository.deleteHashKey(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k);
                });
        Arrays.stream(MBTIFactor.values())
                .map(f -> f + "::" + oldStartDate)
                .forEach(k -> {
                    cacheHashRepository.deleteHashKey(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k);
                    cacheHashRepository.deleteHashKey(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k);
                });
    }
}
