package org.ebndrnk.orderservice.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        @NotBlank(message = "Id is required")
        UUID id,

        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Status is required")
        PaymentStatus status,

        @NotNull(message = "Timestamp is required")
        @PastOrPresent(message = "Timestamp cannot be in the future")
        Instant timestamp,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be positive")
        BigDecimal paymentAmount
        ) {
}
