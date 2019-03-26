package cn.yuyizyk.gateway.component;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.yytaliance.component.SpringContextUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProxyFilter implements GlobalFilter, Ordered {

	@Override
	public int getOrder() {
		return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1000;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//		exchange.getRequest().getURI()
		URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
		String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
		if (url == null || (!"proxy".equals(url.getScheme()) && !"proxy".equals(schemePrefix))) {
			return chain.filter(exchange);
		}
		addOriginalRequestUrl(exchange, url);

		String beanName = url.getHost();
		IProxy proxy = null;
		try {
			proxy = SpringContextUtils.getBean(beanName);
		} catch (Exception e) {
			log.error("异常{}",     e.getLocalizedMessage());
		}
		if (proxy == null) {
			log.error("异常URI{} 解析:N{}", url, beanName);
			return chain.filter(exchange);
		}

		URI requestUrl = proxy.getProxyUri(exchange);
		if (requestUrl == null) {
			log.error("异常URI{} 解析:N{}", url, beanName);
			requestUrl = url;
		}

		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
		Mono<Void> mono = chain.filter(exchange);
		proxy.beforeSend(exchange);

		return mono;
	}

}
