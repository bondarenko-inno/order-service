package org.ebndrnk.orderservice.model.dto;

public record OrderItemDto(
        String name,
        Double price,
        Long quantity
) {
}
