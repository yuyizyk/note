package cn.yuyizyk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFlux
public class ScdemoGatewayApplication {
	
	
	
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.path("/baidu").uri("http://baidu.com:80/"))
				.route("websocket_route", r -> r.path("/apitopic1/**").uri("ws://127.0.0.1:6605"))
				.route(r -> r.path("/userapi3/**").filters(f -> f.addResponseHeader("X-AnotherHeader", "testapi3"))

						.uri("lb://user-service/"))
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ScdemoGatewayApplication.class, args);
	}
}
