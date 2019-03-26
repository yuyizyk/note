package cn.yuyizyk.gateway.component;

import java.net.URI;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 修改 302 返回host
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
@Component
public class WrapperResponseGlobalFilter implements GlobalFilter, Ordered {

	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpResponse originalResponse = exchange.getResponse();
		URI uri = exchange.getRequest().getURI();
//		DataBufferFactory bufferFactory = originalResponse.bufferFactory();
		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
			@SuppressWarnings("serial")
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				switch (getDelegate().getStatusCode().value()) {
				case 302:
				case 301:
					HttpHeaders headers = getDelegate().getHeaders();
					String location = headers.getFirst(HttpHeaders.LOCATION);
					int i = -1;
					if (StringUtils.isEmpty(location) || (i = location.indexOf("/", 8)) == -1) {
						log.error("异常URI{} .", location);
						break;
					}

					String newLocation = uri.getScheme() + "://" + uri.getHost() + ":"
							+ (uri.getPort() > 0 ? uri.getPort() : 80) + location.substring(i);
					log.debug("ASK {}  for RELP URL :{}  TO {} ", uri.toString(), location, newLocation);
					headers.put(HttpHeaders.LOCATION, new ArrayList<String>() {
						{
							add(newLocation);
						}
					});
					break;
				default:
					break;

				}

				return super.writeWith(body);
			}
		};
		// replace response with decorator
		return chain.filter(exchange.mutate().response(decoratedResponse).build());
	}

}
