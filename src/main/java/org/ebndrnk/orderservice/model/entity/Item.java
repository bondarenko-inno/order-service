package org.ebndrnk.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item extends BasicEntity {

    @Comment("Name of the item, must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    @NotNull
    @Size(min = 2, max = 100)
    private String name;

    @Comment("Price of the item, must be greater than 0")
    @Column(nullable = false)
    @NotNull
    @DecimalMin("0.01")
    private Double price;
}
