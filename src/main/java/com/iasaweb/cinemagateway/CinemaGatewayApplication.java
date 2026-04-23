package com.iasaweb.cinemagateway;

import com.iasaweb.cinemagateway.service.JwtValidationService;
import static com.iasaweb.cinemagateway.filter.JwtAuthenticationFilter.isAuthenticated;
import static com.iasaweb.cinemagateway.filter.RoleFilter.hasRole;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.web.servlet.function.RequestPredicates.*;

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
	public RouterFunction<ServerResponse> usersRoute() {
		return route("users_route")
				.route(path("/register", "/login"), http())
				.before(uri(userServiceUri))
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> moviesRoute(JwtValidationService jwtValidationService) {
		return route("movies_route")
				.route(path("/movies/**"), http())
				.before(uri(showServiceUri))
				.filter(isAuthenticated(jwtValidationService))
				.filter(hasRole("ROLE_ADMIN"))
				.before(request -> {
					return ServerRequest
							.from(request)
							.headers(httpHeaders -> {
								String username = request.attribute("username").orElse("").toString();
								String roles = (String) request.attribute("roles").orElse("");
								httpHeaders.add("Cinema-User", username);
								httpHeaders.add("Cinema-Roles", roles);
							})
							.build();
				})
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> ticketsRoute(JwtValidationService jwtValidationService) {
		return route("tickets_route")
				.route(path("/shows/{show_id}/tickets"), http())
				.before(uri(showServiceUri))
				.filter(isAuthenticated(jwtValidationService))
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> showsGetRoute() {
		return route("shows_get_route")
				.route(GET("/shows/**"), http())
				.before(uri(showServiceUri))
				.build();
	}

	@Bean
	RouterFunction<ServerResponse> showsPostAndPutRoute(JwtValidationService jwtValidationService) {
		return route("shows_post_put_route")
				.route(POST("/shows/**").and(PUT("/shows/**")), http())
				.before(uri(showServiceUri))
				.filter(isAuthenticated(jwtValidationService))
				.filter(hasRole("ROLE_ADMIN"))
				.build();
	}
}
