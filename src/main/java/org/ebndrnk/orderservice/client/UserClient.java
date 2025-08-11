package org.ebndrnk.orderservice.client;

import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/users/by-email")
    UserResponse getUserByEmail(@RequestParam String email);
}