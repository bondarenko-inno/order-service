package org.ebndrnk.orderservice.mapper;

import org.ebndrnk.orderservice.kafka.dto.PaymentStatus;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper {

    OrderStatus toOrderStatus(PaymentStatus paymentStatus);
}
