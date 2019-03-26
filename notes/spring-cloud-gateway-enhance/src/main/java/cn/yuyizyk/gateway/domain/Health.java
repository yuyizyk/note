package cn.yuyizyk.gateway.domain;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Health {
	@GetMapping("/health")
	public String health() {
		return "ok";
	}
}
