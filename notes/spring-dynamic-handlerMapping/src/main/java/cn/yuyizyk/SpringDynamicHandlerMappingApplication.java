package cn.yuyizyk;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SpringDynamicHandlerMappingApplication {

	public static void main(String[] args) throws IOException {
		log.info(System.getProperty("java.class.path"));
		log.info(new ClassPathResource("").getClassLoader().toString());
		
		log.info(new ClassPathResource("/").getFile().getCanonicalPath());
		
		SpringApplication.run(SpringDynamicHandlerMappingApplication.class, args);
	}

}
