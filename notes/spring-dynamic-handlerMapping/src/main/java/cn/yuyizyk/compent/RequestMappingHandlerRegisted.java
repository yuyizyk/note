package cn.yuyizyk.compent;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 
 * 
 * 
 * @author yuyi
 */
@Component
public class RequestMappingHandlerRegisted implements InstantiationAwareBeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean.getClass().equals(RequestMappingHandlerMapping.class)) {
			RequestMappingHandlerMapping rhm = (RequestMappingHandlerMapping) bean;
			SrcActionAnalysis.registerMapping(rhm);
		}
		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

}
