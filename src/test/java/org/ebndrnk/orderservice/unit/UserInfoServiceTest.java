package org.ebndrnk.orderservice.unit;

import org.ebndrnk.orderservice.client.UserClient;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.ebndrnk.orderservice.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserInfoServiceTest {

    private UserClient userClient;
    private UserInfoService userInfoService;

    @BeforeEach
    void setUp() {
        userClient = Mockito.mock(UserClient.class);
        userInfoService = new UserInfoService(userClient);
    }

    @Test
    void addUserInfoToOrderResponse_shouldAddUserResponseFromClient() {
        String email = "test@example.com";
        OrderResponse orderResponse = new OrderResponse();
        UserResponse expectedUserResponse = new UserResponse(1L, "John", "Doe", "test@example.com", LocalDateTime.now(), false);

        when(userClient.getUserByEmail(email)).thenReturn(expectedUserResponse);

        OrderResponse result = userInfoService.addUserInfoToOrderResponse(orderResponse, email);

        verify(userClient, times(1)).getUserByEmail(email);
        assertThat(result.getUserResponse()).isEqualTo(expectedUserResponse);
    }
}
