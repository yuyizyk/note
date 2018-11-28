package cn.yuyizyk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableConfigServer
@SpringBootApplication
@EnableEurekaClient
public class ScdemoConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScdemoConfigServerApplication.class, args);
	}
}
