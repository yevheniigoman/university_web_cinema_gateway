package com.iasaweb.cinemagateway;

import com.iasaweb.cinemagateway.service.JwtValidationService;
import static com.iasaweb.cinemagateway.filter.JwtAuthenticationFilter.isAuthenticated;
import static com.iasaweb.cinemagateway.filter.RoleFilter.hasRole;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.web.servlet.function.RequestPredicates.GET;

import java.util.List;

@SpringBootApplication
public class CinemaGatewayApplication {

	@Value("${services.users}")
	private String userServiceUri;

	@Value("${services.shows}")
	private String showServiceUri;

	public static void main(String[] args) {
		SpringApplication.run(CinemaGatewayApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> userRoutes() {
		return GatewayRouterFunctions
				.route("users_route")
				.route(path("/register", "/login"), http())
				.before(uri(userServiceUri))
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> movieRoutes(JwtValidationService jwtValidationService) {
		return GatewayRouterFunctions
				.route("movies_route")
				.route(path("/movies/**"), http())
				.before(uri(showServiceUri))
				.filter(isAuthenticated(jwtValidationService))
				.filter(hasRole("ROLE_ADMIN"))
				.before(request -> {
					return ServerRequest
							.from(request)
							.headers(httpHeaders -> {
								String username = request.attribute("username").orElse("").toString();
								List<String> roles = (List<String>) request.attribute("roles").orElse(List.of());
								httpHeaders.add("Cinema-User", username);
								httpHeaders.add("Cinema-Roles", String.join(" ", roles));
							})
							.build();
				})
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> showRoutes(JwtValidationService jwtDecodeService) {
		return GatewayRouterFunctions
				.route("shows_route")
				.route(GET("/shows/**"), http())
				.before(uri(showServiceUri))
				.build();
	}
}
