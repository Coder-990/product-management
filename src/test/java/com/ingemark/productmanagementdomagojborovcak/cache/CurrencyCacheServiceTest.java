package com.ingemark.productmanagementdomagojborovcak.cache;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.ingemark.productmanagementdomagojborovcak.TestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class CurrencyCacheServiceTest extends TestBase {

    @Test
    @DisplayName("""
            Given request for HNB USD currency
            when method is called get currency and currency check is nullable
            then populate currency USD method is called and should return
            log message with USD currency
            """)
    void shouldReturnUSDCurrencyIfCurrencyCacheIsEmpty(CapturedOutput capturedOutput) {
        // given
        stubHnbCurrencyExchangeApi();

        // when
        var currencyUSD = currencyCacheService.getCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll())
                .contains("Cache currency is empty!")
                .contains("Populating USD currency...")
                .contains("Cache populated with currency 1.10");

        assertThat(currencyUSD).isEqualTo("1.10");
    }

    @Test
    @DisplayName("""
            Given request for HNB USD currency and method is called for populate USD currency
            when method is called get currency and currency check is not nullable
            then populate currency USD method should not be called and should return
            USD currency
            """)
    void shouldReturnPopulatedCurrencyIfCurrencyCacheIsEmpty(CapturedOutput capturedOutput) {
        // given
        stubHnbCurrencyExchangeApi();
        currencyCacheService.populateCurrencyUSD();

        // when
        var currencyUSD = currencyCacheService.getCurrencyUSD();

        // then
        assertThat(capturedOutput.getAll()).doesNotContain("Cache currency is empty!");
        assertThat(currencyUSD).isEqualTo("1.10");
    }

    void stubHnbCurrencyExchangeApi() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/mock/products"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                   {
                                      "broj_tecajnice":"206",
                                      "datum_primjene":"2023-10-19",
                                      "drzava":"SAD",
                                      "drzava_iso":"USA",
                                      "sifra_valute":"840",
                                      "valuta":"USD",
                                      "kupovni_tecaj":"1,1000",
                                      "srednji_tecaj":"1,0565",
                                      "prodajni_tecaj":"1,0549"
                                   }
                                ]
                                """)));
    }
}