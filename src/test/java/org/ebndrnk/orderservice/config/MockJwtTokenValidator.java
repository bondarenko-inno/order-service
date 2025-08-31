package org.ebndrnk.orderservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.ebndrnk.common.filter.JwtTokenValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Profile("test")
@Component
public class MockJwtTokenValidator extends JwtTokenValidator {

    @Override
    public Claims validateToken(String token) {
        if (token.equals("testJwt")) {
            Claims claims = Jwts.claims();
            claims.put("role", "ROLE_USER");
            claims.setSubject("test@test.com");
            return claims;
        } else {
            throw new RuntimeException("Invalid token");
        }
    }
}