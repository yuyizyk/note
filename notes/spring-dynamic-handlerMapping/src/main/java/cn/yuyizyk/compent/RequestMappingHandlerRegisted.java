package cn.yuyizyk.compent;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cn.yuyizyk.action.BaseAction;
import cn.yuyizyk.util.ClassLoaderUtil;

/**
 * 
 * 
 * 
 * @author yuyi
 */
@Component
public class RequestMappingHandlerRegisted implements InstantiationAwareBeanPostProcessor {
	private final static Logger log = LoggerFactory.getLogger(RequestMappingHandlerRegisted.class);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean.getClass().equals(RequestMappingHandlerMapping.class)) {
			RequestMappingHandlerMapping rhm = (RequestMappingHandlerMapping) bean;
			Set<Class<?>> clzs = ClassLoaderUtil.getClzFromPkg(BaseAction.class.getPackage().getName());
			log.info("即将初始化 handleraction size[{}]...", clzs.size());
			clzs.forEach(c -> SrcActionAnalysis.registerMapping(rhm, c));
		}
		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

}
