package org.ebndrnk.orderservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ebndrnk.orderservice.kafka.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${environment.kafka.address}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PaymentResponse> paymentResponseConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        JsonDeserializer<PaymentResponse> deserializer = new JsonDeserializer<>(PaymentResponse.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentResponse> paymentResponseListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentResponseConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
