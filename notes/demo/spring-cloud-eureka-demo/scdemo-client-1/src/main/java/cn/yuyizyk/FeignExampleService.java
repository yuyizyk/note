package cn.yuyizyk;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-c2-producer", fallback = FeignServiceHystrix.class)
public interface FeignExampleService {
	@GetMapping("hello2")
	public String hello(@RequestParam(value = "name") String name);
}
