package org.ebndrnk.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.ebndrnk.orderservice.client.UserClient;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserClient userClient;

    public OrderResponse addUserInfoToOrderResponse(OrderResponse orderResponse, String email) {
        orderResponse.setUserResponse(userClient.getUserByEmail(email));

        return orderResponse;
    }

}
