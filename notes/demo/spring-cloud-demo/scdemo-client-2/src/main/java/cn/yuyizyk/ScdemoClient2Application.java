package cn.yuyizyk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
@EnableEurekaClient
@RefreshScope
public class ScdemoClient2Application {
	@Value("${domain}")
	String domain;

	@GetMapping("/hello2")
	public String index2(@RequestParam(value = "name") String name) {
		return "this is ScdemoClient2Application hello2 " + name + "  " + domain;
	}

	public static void main(String[] args) {
		SpringApplication.run(ScdemoClient2Application.class, args);
	}

	// LoadBalanced 注解表明restTemplate使用LoadBalancerClient执行请求
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	ServiceImpl ribbonServiceImpl;

	@GetMapping("/hello/{name}")
	public String index(@PathVariable("name") String name) {
		return ribbonServiceImpl.method1(name);
	}
}
