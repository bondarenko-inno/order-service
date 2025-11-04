package org.ebndrnk.orderservice.mapper;

import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.dto.OrdersHistoryResponse;
import org.ebndrnk.orderservice.model.entity.Order;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(source = "status", target = "orderStatus")
    OrderResponse entityToResponse(Order order);

    @Mapping(source = "status", target = "orderStatus")
    @Mapping(source = "items", target = "items")
    OrdersHistoryResponse entityToHistoryResponse(Order order);


    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.price", target = "price")
    OrdersHistoryResponse.ItemDtoForHistory orderItemsToItemDtos(OrderItem items);
}
