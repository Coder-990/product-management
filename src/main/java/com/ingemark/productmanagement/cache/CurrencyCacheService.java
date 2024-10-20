package com.ingemark.productmanagement.cache;

import com.ingemark.productmanagement.client.HnbApiRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyCacheService {

    private final HnbApiRestClient hnbApiRestClient;

    private BigDecimal currencyUSD;

    public BigDecimal getCurrencyUSD() {
        if (Objects.isNull(currencyUSD)) {
            log.info("Cache currency is empty!");
            populateCurrencyUSD();
        }
        return currencyUSD;
    }

    public void populateCurrencyUSD() {
        log.info("Populating USD currency...");
        currencyUSD = new BigDecimal(hnbApiRestClient.getUsdBuyingRateCurrency()
                .replace(",", "."))
                .setScale(2, RoundingMode.HALF_UP);
        log.info("Cache populated with currency {}", currencyUSD);
    }
}
