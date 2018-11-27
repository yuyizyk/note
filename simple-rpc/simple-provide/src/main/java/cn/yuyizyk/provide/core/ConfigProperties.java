package cn.yuyizyk.provide.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置中心
 * 
 * 
 *
 * @author yuyi
 */
@Component
@ConfigurationProperties
public class ConfigProperties {
	@Value("${rpc.provide.port:9900}")
	private Integer localhostPort = 9900;

	public Integer getLocalhostPort() {
		return localhostPort;
	}

	public void setLocalhostPort(Integer localhostPort) {
		this.localhostPort = localhostPort;
	}

}
