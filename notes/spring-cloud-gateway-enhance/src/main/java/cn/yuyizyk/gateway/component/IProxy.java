package cn.yuyizyk.gateway.component;

import java.net.URI;

import org.springframework.web.server.ServerWebExchange;

public interface IProxy {

	public URI getProxyUri(ServerWebExchange exchange);

	public default void beforeSend(ServerWebExchange exchange) {
	}
}
