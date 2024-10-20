package com.ingemark.productmanagement.client;

import com.ingemark.productmanagement.client.request.CurrencyExchangeRateRequest;
import com.ingemark.productmanagement.config.props.HnbApiClientProps;
import com.ingemark.productmanagement.exceptions.NotFoundException;
import com.ingemark.productmanagement.exceptions.PlatformException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HnbApiRestClient {

    private final HnbApiClientProps hnbApiClientProps;

    private final RestClient restClient;

    public String getUsdBuyingRateCurrency() {
        return Stream.ofNullable(restClient
                        .get()
                        .uri(hnbApiClientProps.getCurrencyUsdUrl())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, ((request, response) -> {
                            throw new PlatformException("Hnb api error",
                                    new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                        }))
                        .toEntity(CurrencyExchangeRateRequest[].class)
                        .getBody())
                .filter(Objects::nonNull)
                .flatMap(Stream::of)
                .filter(c -> Objects.equals(c.state(), "SAD"))
                .map(CurrencyExchangeRateRequest::currencyUSD)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unable to get currency rate for USD"));
    }
}
