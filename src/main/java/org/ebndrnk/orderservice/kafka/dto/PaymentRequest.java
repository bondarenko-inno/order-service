package org.ebndrnk.orderservice.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be positive")
        BigDecimal paymentAmount
) {
}
