package com.bibliotech.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    @Value("${jwt.secret:bibliotech-super-secret-key-that-is-at-least-256-bits-long-for-hs256}")
    private String secret;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/validate",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui"
    );

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        ServerHttpRequest request =
                exchange.getRequest();

        String path =
                request.getURI().getPath();

        /*
         * ============================================================
         * CORS PREFLIGHT
         * ============================================================
         *
         * Browser sends OPTIONS before some POST/PUT/DELETE requests.
         *
         * OPTIONS must not require JWT.
         *
         * Spring Cloud Gateway handles the actual CORS headers through
         * application.yml.
         */

        if (request.getMethod() == HttpMethod.OPTIONS) {

            return chain.filter(exchange);
        }

        /*
         * ============================================================
         * PUBLIC ENDPOINTS
         * ============================================================
         */

        if (isPublicEndpoint(path)) {

            return chain.filter(exchange);
        }

        /*
         * ============================================================
         * AUTHORIZATION HEADER
         * ============================================================
         */

        String authHeader =
                request.getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return onError(
                    exchange,
                    "Missing or invalid Authorization header",
                    HttpStatus.UNAUTHORIZED
            );
        }

        /*
         * ============================================================
         * EXTRACT JWT
         * ============================================================
         */

        String token =
                authHeader.substring(7);

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            /*
             * ========================================================
             * PASS USER INFORMATION DOWNSTREAM
             * ========================================================
             */

            ServerHttpRequest.Builder reqBuilder = request.mutate();
            if (claims.get("userId") != null) {
                reqBuilder.header("X-User-Id", String.valueOf(claims.get("userId")));
            }
            if (claims.getSubject() != null) {
                reqBuilder.header("X-User-Email", claims.getSubject());
            }
            if (claims.get("role") != null) {
                reqBuilder.header("X-User-Role", String.valueOf(claims.get("role")));
            }

            ServerHttpRequest modifiedRequest = reqBuilder.build();

            return chain.filter(
                    exchange
                            .mutate()
                            .request(modifiedRequest)
                            .build()
            );

        } catch (Exception e) {

            System.out.println(
                    "JWT validation failed: "
                            + e.getMessage()
            );

            return onError(
                    exchange,
                    "Invalid or expired JWT token",
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    /*
     * ================================================================
     * PUBLIC ENDPOINT CHECK
     * ================================================================
     */

    private boolean isPublicEndpoint(String path) {

        return PUBLIC_ENDPOINTS
                .stream()
                .anyMatch(path::startsWith);
    }

    /*
     * ================================================================
     * ERROR RESPONSE
     * ================================================================
     */

    private Mono<Void> onError(
            ServerWebExchange exchange,
            String error,
            HttpStatus status) {

        ServerHttpResponse response =
                exchange.getResponse();

        response.setStatusCode(status);

        return response.setComplete();
    }

    /*
     * ================================================================
     * FILTER ORDER
     * ================================================================
     */

    @Override
    public int getOrder() {

        return -1;
    }
}