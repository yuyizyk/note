package cn.yuyizyk;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
@EnableFeignClients
@RefreshScope
public class ScdemoClient1Application {
	@Value("${domain}")
	String domain;

	@RequestMapping("/hello")
	public String hello(@RequestParam String name) {
		return "hello " + name + "，this is new ScdemoClient1Application world" + domain;
	}

	@Resource
	private FeignExampleService feignExampleService;

	@GetMapping("/hello/{name}")
	public String index(@PathVariable("name") String name) {
		return feignExampleService.hello(name);
	}

	public static void main(String[] args) {
		SpringApplication.run(ScdemoClient1Application.class, args);
	}

	// LoadBalanced 注解表明restTemplate使用LoadBalancerClient执行请求
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
