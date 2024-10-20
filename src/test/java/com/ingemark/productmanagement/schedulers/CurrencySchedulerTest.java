package com.ingemark.productmanagement.schedulers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.ingemark.productmanagement.TestBase;
import com.ingemark.productmanagement.config.props.HnbCacheRefresherSchedulerProps;
import com.ingemark.productmanagement.scheduler.CurrencyScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class CurrencySchedulerTest extends TestBase {

    @Autowired
    private CurrencyScheduler currencyScheduler;

    @Autowired
    private HnbCacheRefresherSchedulerProps hnbCacheRefresherSchedulerProps;

    @Test
    @DisplayName("""
            Given HNB cache refresher scheduler is set to disabled,
            when refresh currency method is called to check if currency cache is not empty,
            then log is checked and returned value of disabled scheduler with log message
            """)
    void shouldReturnCurrencySchedulerIsDisabledLog(CapturedOutput capturedOutput) {
        // given
        hnbCacheRefresherSchedulerProps.setEnabled(false);

        // when
        currencyScheduler.refreshCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll())
                .contains("HNB USD currency scheduler started...")
                .contains("USD HNB currency check scheduler is disabled!");
    }

    @Test
    @DisplayName("""
            Given HNB cache refresher scheduler is set to null,
            when refresh currency method is called to check if currency cache is not empty,
            then log is checked and returned value of disabled scheduler with log message
            """)
    void shouldReturnCurrencySchedulerIsNullAndAndSchedulerIsDisabledLog(CapturedOutput capturedOutput) {
        // given
        hnbCacheRefresherSchedulerProps.setEnabled(null);

        // when
        currencyScheduler.refreshCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll())
                .contains("HNB USD currency scheduler started...")
                .contains("USD HNB currency check scheduler is disabled!");
    }

    @Test
    @DisplayName("""
            Given HNB cache refresher scheduler is set to enabled,
            when refresh currency method is called to check if currency cache is empty,
            then log is checked with populating USD currency and returned value
            of fetched HNB daily USD currency with log message
            """)
    void shouldReturnCurrencySchedulerIsEnabledLog(CapturedOutput capturedOutput) {
        // given
        stubHnbCurrencyExchangeApi();
        hnbCacheRefresherSchedulerProps.setEnabled(true);

        // when
        currencyScheduler.refreshCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll())
                .contains("HNB USD currency scheduler started...")
                .doesNotContain("Cache currency is empty!")
                .contains("Populating USD currency...")
                .contains("Cache populated with currency 1.10")
                .contains("HNB USD currency scheduler finished!");
    }

    @Test
    @DisplayName("""
            Given HNB cache refresher scheduler is set to enabled and HNB API service is down
            when refresh currency method is called while trying to get USD currency
            then scheduler is started and population currency method is triggered,
            Platform exception is thrown with message "Hnb api error" and json body in log
            """)
    void shouldReturnHnbApiErrorWithJsonBodyInLogMessage(CapturedOutput capturedOutput) {
        // given
        stubErrorOfHnbCurrencyExchangeApi();
        hnbCacheRefresherSchedulerProps.setEnabled(true);

        // when
        currencyScheduler.refreshCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll())
                .contains("HNB USD currency scheduler started...")
                .doesNotContain("USD HNB currency check scheduler is disabled!")
                .contains("Populating USD currency...")
                .contains("Hnb api error :")
                .contains("""
                        {
                          "error": "service HNB API currently out of reach!"
                        }
                        """)
                .doesNotContain("Cache populated with currency")
                .contains("Error in populating USD currency: ")
                .contains("HNB USD currency scheduler finished!");
    }

    void stubHnbCurrencyExchangeApi() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/mock/products"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                  {
                                      "broj_tecajnice": "206",
                                      "datum_primjene": "2023-10-19",
                                      "drzava": "SAD",
                                      "drzava_iso": "USA",
                                      "sifra_valute": "840",
                                      "valuta": "USD",
                                      "kupovni_tecaj": "1,1000",
                                      "srednji_tecaj": "1,0565",
                                      "prodajni_tecaj": "1,0549"
                                  }
                                ]
                                """)
                )
        );
    }

    void stubErrorOfHnbCurrencyExchangeApi() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/mock/products"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                  "error": "service HNB API currently out of reach!"
                                }
                                """)
                )
        );
    }
}
