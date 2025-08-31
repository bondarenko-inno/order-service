package org.ebndrnk.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.ebndrnk.orderservice.config.TestContainersConfig;
import org.ebndrnk.orderservice.model.dto.OrderRequest;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.ebndrnk.orderservice.repository.ItemRepository;
import org.ebndrnk.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8071)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class OrderControllerTest extends TestContainersConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    private Item item;

    private final static String TEST_TOKEN = "Bearer testJwt";



    @BeforeEach
    void setUpMocks() {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/by-email"))
                .withQueryParam("email", equalTo("test@test.com"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"email\": \"test@test.com\", \"name\": \"Test User\"}")
                        .withStatus(200)));
    }


    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        item = new Item();

        item.setName("TestItem");
        item.setPrice(15.0);
        item.setQuantity(100L);

        item = itemRepository.save(item);

    }

    @Test
    @Order(1)
    void createOrder_success() throws Exception {
        OrderRequest.OrderItemDto itemDto = new OrderRequest.OrderItemDto();
        itemDto.setItemId(item.getId());
        itemDto.setQuantity(2L);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemDto));


        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer testJwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value("test@test.com"))
                .andExpect(jsonPath("$.orderStatus").value("PROCESSING"))

                .andExpect(jsonPath("$.items[0].name").value("TestItem"))
                .andExpect(jsonPath("$.items[0].price").value(15.0))

                .andExpect(jsonPath("$.userResponse.id").value(1))
                .andExpect(jsonPath("$.userResponse.name").value("Test User"))
                .andExpect(jsonPath("$.userResponse.email").value("test@test.com"));
    }

    @Test
    @Order(2)
    void createOrder_insufficientQuantity() throws Exception {
        OrderRequest.OrderItemDto itemDto = new OrderRequest.OrderItemDto();
        itemDto.setItemId(item.getId());
        itemDto.setQuantity(1000L);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemDto));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(3)
    void getOrdersByStatus_success() throws Exception {
        var orderId = createSampleOrder(OrderStatus.PROCESSING);

        mockMvc.perform(get("/orders/by-status")
                        .param("statuses", "PROCESSING")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(orderId));
    }

    @Test
    @Order(4)
    void getOrdersByStatus_notFound() throws Exception {
        mockMvc.perform(get("/orders/by-status")
                        .param("statuses", "CLOSED")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Long createSampleOrder(OrderStatus status) {
        var order = new org.ebndrnk.orderservice.model.entity.Order();
        order.setUserId("test@test.com");
        order.setStatus(status);

        var orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(2L);



        order.setItems(List.of(orderItem));
        return orderRepository.save(order).getId();
    }
}
