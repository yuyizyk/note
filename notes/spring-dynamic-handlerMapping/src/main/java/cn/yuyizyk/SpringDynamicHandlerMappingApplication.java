package cn.yuyizyk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringDynamicHandlerMappingApplication {

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.class.path"));
		SpringApplication.run(SpringDynamicHandlerMappingApplication.class, args);
	}

}
