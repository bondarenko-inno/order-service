package org.ebndrnk.orderservice.integration;

import org.ebndrnk.orderservice.client.UserClient;
import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.ebndrnk.orderservice.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "user-service.url=http://localhost:${wiremock.server.port}"
        }
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class UserClientTest extends TestContainersConfig {

    @Autowired
    private UserClient userClient;


    @Test
    void getUserByEmail_shouldReturnMockedUser() {
        String email = "test@example.com";

        stubFor(get(urlPathEqualTo("/api/users/by-email"))
                .withQueryParam("email", equalTo(email))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "id": 1,
                          "name": "John",
                          "surname": "Doe",
                          "email": "test@example.com"
                        }
                        """)));

        // when
        UserResponse response = userClient.getUserByEmail(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(email);

        // ✅ Верная проверка: проверяем query param
        verify(getRequestedFor(urlPathEqualTo("/api/users/by-email"))
                .withQueryParam("email", equalTo(email)));
    }

}
