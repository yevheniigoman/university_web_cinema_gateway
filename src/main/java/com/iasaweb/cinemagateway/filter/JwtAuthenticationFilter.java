package com.iasaweb.cinemagateway.filter;

import com.iasaweb.cinemagateway.dto.JwtDto;
import com.iasaweb.cinemagateway.exception.JwtValidationException;
import com.iasaweb.cinemagateway.service.JwtValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

public class JwtAuthenticationFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public static HandlerFilterFunction<ServerResponse, ServerResponse> isAuthenticated(JwtValidationService jwtValidationService) {
        return (request, next) -> {
            String authHeader = request.headers().firstHeader(AUTH_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }

            String jwtRaw = authHeader.substring(BEARER_PREFIX.length());
            JwtDto jwtDto;
            try {
                jwtDto = jwtValidationService.validate(jwtRaw);
            } catch (JwtValidationException e) {
                return ServerResponse
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", e.getMessage()));
            }

            var modifiedRequest = ServerRequest
                    .from(request)
                    .attributes(attrs -> {
                        attrs.put("username", jwtDto.username());
                        attrs.put("roles", jwtDto.roles());
                    })
                    .headers(httpHeaders -> {
                        httpHeaders.remove(AUTH_HEADER);
                    })
                    .build();
            return next.handle(modifiedRequest);
        };
    }
}
