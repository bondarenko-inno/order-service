package org.ebndrnk.orderservice.repository;

import org.ebndrnk.orderservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
