package com.ingemark.productmanagement;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ingemark.productmanagement.cache.CurrencyCacheService;
import com.ingemark.productmanagement.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
public abstract class TestBase {

    @Autowired
    protected CurrencyCacheService currencyCacheService;

    @Autowired
    protected ProductRepository productRepository;

    protected final WireMockServer wireMockServer = new WireMockServer();

    @BeforeEach
    protected void setUp() throws Exception {
        clearCurrencyCache();
        productRepository.deleteAll();
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private void clearCurrencyCache() throws Exception {
        var currencyUSDField = CurrencyCacheService.class.getDeclaredField("currencyUSD");
        currencyUSDField.setAccessible(true);
        currencyUSDField.set(currencyCacheService, null);
    }
}
