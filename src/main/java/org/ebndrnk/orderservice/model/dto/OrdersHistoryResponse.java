package org.ebndrnk.orderservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.ebndrnk.orderservice.model.entity.OrderStatus;

import java.util.List;

@Getter
@Setter
public class OrdersHistoryResponse{


        private String id;
        private String userId;
        private OrderStatus orderStatus;
        private List<ItemDtoForHistory> items;
        private UserResponse userResponse;


        @Getter
        @Setter
        public static class ItemDtoForHistory{
            private String name;
            private Double price;
            private Long quantity;
        }
}
