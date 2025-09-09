package org.ebndrnk.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.orderservice.kafka.dto.PaymentRequest;
import org.ebndrnk.orderservice.model.entity.Order;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedPublisher {

    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    private static final String TOPIC = "order.create";

    public void publishOrderCreated(Order order, BigDecimal amount) {
        PaymentRequest event = new PaymentRequest(
                order.getId().toString(),
                order.getUserId(),
                amount
        );

        try {
            kafkaTemplate.send(TOPIC, event.userId(), event);
            log.info("📤 OrderCreated event published: orderId={}, userId={}, amount={}",
                    order.getId(), event.userId(), amount);
        } catch (Exception e) {
            throw new KafkaException("Failed to send order event for userId: " + event.userId(), e);
        }
    }
}
