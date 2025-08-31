package org.ebndrnk.orderservice.util;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ebndrnk.common.filter.JwtTokenValidator;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class JwtParser {
    private final HttpServletRequest request;
    private final JwtTokenValidator jwtTokenValidator;
    private Claims claims;

    @PostConstruct
    public void init(){
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);

        claims = jwtTokenValidator.validateToken(token);
    }

    public String getEmailFromToken() {
        return claims.getSubject();
    }

}
