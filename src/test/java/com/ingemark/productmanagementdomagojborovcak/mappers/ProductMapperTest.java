package com.ingemark.productmanagementdomagojborovcak.mappers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.ingemark.productmanagementdomagojborovcak.TestBase;
import com.ingemark.productmanagementdomagojborovcak.fixtures.ProductFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest extends TestBase {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("""
            Given product not exist in list,
            when new productRequest is send to be mapped,
            then is expected map productRequest to product
            """)
    void shouldReturnProduct() {
        // given
        var productRequest = ProductFixtures.productRequestOne();
        // when
        var product = productMapper.toProduct(productRequest);
        // then
        assertThat(product.getId()).isNull();
        assertThat(product.getCode()).isEqualTo("7gfgrRHZH8");
        assertThat(product.getName()).isEqualTo("Keyboard");
        assertThat(product.getPriceEur()).isEqualTo(BigDecimal.valueOf(124.42));
        assertThat(product.getDescription()).isEqualTo("Logitech k860 wireless keyboard");
        assertThat(product.getIsAvailable()).isTrue();
    }

    @Test
    @DisplayName("""
            Given product exist in list,
            when fetched product is send to be mapped,
            then is expected map product to productResponse
            """)
    void shouldReturnProductResponse() {
        // given
        var product = ProductFixtures.productFour();
        stubHnbCurrencyExchangeApi();
        // when
        var productResponse = productMapper.toProductResponse(product);
        // then
        assertThat(productResponse.code()).isEqualTo("4g1b5s2cRR");
        assertThat(productResponse.name()).isEqualTo("PS5");
        assertThat(productResponse.priceEur()).isEqualTo(BigDecimal.valueOf(399.54));
        assertThat(productResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(BigDecimal.valueOf(399.54)));
        assertThat(productResponse.description()).isEqualTo("This is Playstation 5 product");
        assertThat(productResponse.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("""
            Given multiple products exists in list,
            when fetching all products to be mapped,
            then is expected to map products to productsResponse
            """)
    void shouldReturnProductsResponse() {
        // given
        var products = ProductFixtures.getProductList();
        stubHnbCurrencyExchangeApi();

        // when
        var productsResponse = productMapper.toProductsResponse(products);

        // then
        var productResponse = productsResponse.products().get(3);
        assertThat(productsResponse.products()).hasSize(5);
        assertThat(productResponse.code()).isEqualTo("4g1b5s2cRR");
        assertThat(productResponse.name()).isEqualTo("PS5");
        assertThat(productResponse.priceEur()).isEqualTo(BigDecimal.valueOf(399.54));
        assertThat(productResponse.priceUsd()).isEqualTo(productMapper.convertToUsdPrice(BigDecimal.valueOf(399.54)));
        assertThat(productResponse.description()).isEqualTo("This is Playstation 5 product");
        assertThat(productResponse.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("""
            Given price in euro is manually set
            when product mapper is called to convert price in euro to usd,
            then expected price that is manually multiplied should be
            equals to price from HNB currency
            """)
    void shouldReturnConvertedPriceToUSDByHNBCurrency() {
        // given
        stubHnbCurrencyExchangeApi();
        var priceEur = new BigDecimal("29.99");
        var expectedUsdPrice = priceEur.multiply(BigDecimal.valueOf(1.1000))
                .setScale(2, RoundingMode.HALF_UP);

        // when
        var actualUsdPrice = productMapper.convertToUsdPrice(priceEur);

        // then
        assertThat(actualUsdPrice).isEqualTo(expectedUsdPrice);
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
                                """)
                )
        );
    }
}