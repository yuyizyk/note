package cn.yuyizyk.gateway.domain;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yytaliance.entity.DoubleEntity;
import com.yytaliance.ercloud.api.ErcServiceProxy;
import com.yytaliance.session.redis.JedisClient;
import com.yytaliance.util.NetUtils;
import com.yytaliance.util.Objs;

import cn.yuyizyk.gateway.component.IProxy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ProxyRemote {



	private static URI error404URI;

	static {
		try {
			error404URI = new URI("http://www.localhost/common/404/404.html");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static final String virtualDesktopBeanName = "desktop";

	@Bean
	public IProxy dev() {
		return exchange -> {
			URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
			if (uri == null)
				return error404URI;
			String host = exchange.getRequest().getURI().toString();
			if (StringUtils.isEmpty(host) && host.contains("://") && host.contains(".dev")) {
				log.error("异常URI{} .", host);
				return error404URI;
			}
			int i;
			host = host.substring((i = host.indexOf("://") + 3), host.indexOf(".dev", i));

			if (!host.contains(".")) {
				return error404URI;
			}

			String port = host.substring((i = host.lastIndexOf(".")) + 1);
			host = host.substring(0, i);
			if (!NetUtils.isIp(host)) {
				return error404URI;
			}
			URI newUri;
			try {
				newUri = new URI("http://" + host + ":" + port + uri.getPath().toString()
						+ (Objs.isEmpty(uri.getQuery()) ? "" : "?" + uri.getQuery()));
				log.info("ASK:{},R:{}", exchange.getRequest().getURI().toString(), newUri);
				return newUri;
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return error404URI;
		};
	}

}
