package org.ebndrnk.orderservice.mapper;

import org.ebndrnk.orderservice.model.dto.ItemDto;
import org.ebndrnk.orderservice.model.dto.ItemResponse;
import org.ebndrnk.orderservice.model.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemResponse entityToResponse(Item item);
}
