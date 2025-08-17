package org.ebndrnk.orderservice.mapper;

import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(source = "status", target = "orderStatus")
    OrderResponse entityToResponse(Order order);
}
