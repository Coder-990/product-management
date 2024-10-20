package com.ingemark.productmanagement.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = "product-management.clients.hnb-api")
public class HnbApiClientProps {

    private String currencyUsdUrl;
}
