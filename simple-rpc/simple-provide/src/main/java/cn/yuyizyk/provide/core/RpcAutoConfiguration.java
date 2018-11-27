package cn.yuyizyk.provide.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource({ "classpath:application.yml", "classpath:application.xml" })
@Configuration
// @ConditionalOnClass({ ServerRegistered.class })
@EnableConfigurationProperties(ConfigProperties.class)
// @AutoConfigureAfter()
@ConditionalOnProperty(prefix = "rpc.provide", value = "enabled", matchIfMissing = true)
public class RpcAutoConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	@Autowired
	private ConfigProperties properties;
	private static ConfigurableListableBeanFactory f;
	private static ServerRegistered serverRegistered = new ServerRegistered();

	@Bean
	@ConditionalOnExpression("#{ '${rpc.provide.enabled:false}' == 'false' }")
	public ServerRegistered serverRegistered() {
		serverRegistered.setProperties(properties);
		return serverRegistered;
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		f = applicationContext.getBeanFactory();
		serverRegistered.setConfigurableListableBeanFactory(f);
	}
}
