package com.ingemark.productmanagementdomagojborovcak.scheduler;

import com.ingemark.productmanagementdomagojborovcak.cache.CurrencyCacheService;
import com.ingemark.productmanagementdomagojborovcak.config.props.HnbCacheRefresherSchedulerProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyScheduler {

    private final HnbCacheRefresherSchedulerProps hnbCacheRefresherSchedulerProps;

    private final CurrencyCacheService currencyCacheService;

    @Scheduled(cron = "${product-management.scheduler.hnb-cache-refresher.cron}")
    public void refreshCurrencyUSD() {
        log.info("HNB USD currency scheduler started...");
        if (Boolean.FALSE.equals(hnbCacheRefresherSchedulerProps.getEnabled()) ||
                Objects.isNull(hnbCacheRefresherSchedulerProps.getEnabled())) {
            log.info("USD HNB currency check scheduler is disabled!");
            return;
        }
        try {
            currencyCacheService.populateCurrencyUSD();
        } catch (Exception ex) {
            log.error("Error in populating USD currency: ", ex);
        }
        log.info("HNB USD currency scheduler finished!");
    }
}
