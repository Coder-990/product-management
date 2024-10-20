package com.ingemark.productmanagementdomagojborovcak.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "product-management.kafka")
public class KafkaProps {

    private String productsTopic;
}
