package org.ebndrnk.orderservice.model.dto;

import java.util.List;

public record UpdateOrderRequest(
        List<OrderRequest.OrderItemDto> items
) {
}
