package com.ingemark.productmanagement.helpers;

import lombok.Data;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Data
@Component
public class KafkaTestListener {

    private String latestEvent;

    @KafkaListener(topics = "${product-management.kafka.products-topic}", groupId = "${spring.application.name}")
    public void createProductConsumer(String event){
        latestEvent = event;
    }
}
