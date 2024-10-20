package com.ingemark.productmanagementdomagojborovcak.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingemark.productmanagementdomagojborovcak.config.props.KafkaProps;
import com.ingemark.productmanagementdomagojborovcak.messaging.outbound.CreatedProductEvent;
import com.ingemark.productmanagementdomagojborovcak.messaging.outbound.DeletedProductEvent;
import com.ingemark.productmanagementdomagojborovcak.messaging.outbound.UpdatedProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProps kafkaProps;
    private final ObjectMapper objectMapper;

    public void sendProductCreatedEvent(CreatedProductEvent createdProductEvent) {
        log.info("Sending product created event...");
        var id = createdProductEvent.product().id().toString();
        sendEvent(kafkaProps.getProductsTopic(), id, createdProductEvent);
        log.info("Product created event sent");
    }

    public void sendProductUpdatedEvent(UpdatedProductEvent updatedProductEvent) {
        log.info("Sending product updated event...");
        var id = updatedProductEvent.product().id().toString();
        sendEvent(kafkaProps.getProductsTopic(), id, updatedProductEvent);
        log.info("Product updated event sent");

    }

    public void sendProductDeletedEvent(DeletedProductEvent deletedProductEvent) {
        log.info("Sending product deleted event...");
        var id = deletedProductEvent.product().id().toString();
        sendEvent(kafkaProps.getProductsTopic(), id, deletedProductEvent);
        log.info("Product deleted event sent");
    }

    private void sendEvent(String topic, String key, Object data) {
        try {
            var event = objectMapper.writeValueAsString(data);
            log.info("Sending {} with key {} to topic {}", data, key, topic);
            kafkaTemplate.send(topic, key, event).join();
        } catch (Exception ex) {
            log.error("Error sending Product event", ex);
        }
    }
}
