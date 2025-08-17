package org.ebndrnk.orderservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.ebndrnk.orderservice.model.entity.OrderStatus;

import java.util.List;


@Getter
@Setter
public class OrderResponse{
    private Long id;
    private String userId;
    private OrderStatus orderStatus;
    private List<ItemDto> items;
    private UserResponse userResponse;
}
