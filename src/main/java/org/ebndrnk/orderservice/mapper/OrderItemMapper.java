package org.ebndrnk.orderservice.mapper;

import org.ebndrnk.orderservice.model.dto.ItemDto;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.price", target = "price")
    ItemDto toDto(OrderItem orderItem);
}
