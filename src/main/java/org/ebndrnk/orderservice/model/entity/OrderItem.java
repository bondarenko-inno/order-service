package org.ebndrnk.orderservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BasicEntity {

    @Comment("Order to which this item belongs")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    @Comment("Item being ordered")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    @NotNull
    private Item item;

    @Comment("Quantity of the item in this order")
    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Long quantity;
}
