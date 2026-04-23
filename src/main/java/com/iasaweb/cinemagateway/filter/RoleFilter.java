package com.iasaweb.cinemagateway.filter;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Optional;

public class RoleFilter {
    public static HandlerFilterFunction<ServerResponse, ServerResponse> hasRole(String role) {
        return (request, next) -> {
            Optional<Object> result = request.attribute("roles");
            if (result.isEmpty()) {
                return ServerResponse.status(HttpStatus.FORBIDDEN).build();
            }

            String roles = (String) result.get();
            if (!roles.equals(role)) {
                return ServerResponse.status(HttpStatus.FORBIDDEN).build();
            }
            return next.handle(request);
        };
    }
}
