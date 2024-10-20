package com.ingemark.productmanagement.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder(setterPrefix = "of")
public record CurrencyExchangeRateRequest(

        @JsonProperty("drzava")
        String state,
        @JsonProperty("kupovni_tecaj")
        String currencyUSD) {
}
