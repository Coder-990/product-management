package com.ingemark.productmanagementdomagojborovcak.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "product-management.schedulers.hnb-cache-refresher")
public class HnbCacheRefresherSchedulerProps {

    private Boolean enabled;

}
