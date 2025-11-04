package org.ebndrnk.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(org.ebndrnk.common.security.SecurityCommonLibConfig.class)
public class SecurityCommonLibConfig {
}
