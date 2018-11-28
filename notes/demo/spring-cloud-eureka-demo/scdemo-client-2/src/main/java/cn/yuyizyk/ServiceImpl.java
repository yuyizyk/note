package cn.yuyizyk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceImpl {
	@Autowired
	RestTemplate restTemplate;

	public String method1(String name) {
		return restTemplate.getForObject("http://service-c1-producer/hello?name=" + name, String.class);
	}
}
