package org.ebndrnk.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.orderservice.kafka.dto.PaymentResponse;
import org.ebndrnk.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessListener {
    private final OrderService orderService;

    @KafkaListener(
            topics = "payment.create",
            groupId = "order-service-group",
            containerFactory = "paymentResponseListenerContainerFactory"
    )
    public void handlePaymentCreated(PaymentResponse paymentResponse, Acknowledgment acknowledgment) {
        try {

            orderService.setOrderStatus(paymentResponse);
            log.info("📥 PaymentProcessed event consumed: {}", paymentResponse);
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
