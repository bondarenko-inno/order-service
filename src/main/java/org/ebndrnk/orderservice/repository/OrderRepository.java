package org.ebndrnk.orderservice.repository;

import org.ebndrnk.orderservice.model.entity.Order;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllById(Long id);

    List<Order> findAllByStatusIn(List<OrderStatus> statuses);
}
