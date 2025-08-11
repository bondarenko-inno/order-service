package org.ebndrnk.orderservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotNull
    private String userId;

    @NotEmpty
    private List<OrderItemDto> items;

    @Getter
    @Setter
    public static class OrderItemDto {

        @NotNull
        private Long itemId;

        @NotNull
        private Long quantity;
    }
}