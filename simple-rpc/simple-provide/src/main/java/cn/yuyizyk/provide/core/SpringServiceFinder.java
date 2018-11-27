package cn.yuyizyk.provide.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import cn.yuyizyk.common.entity.Action;
import cn.yuyizyk.common.entity.SimpleEntry;
import cn.yuyizyk.common.rservice.RPCServerName;
import cn.yuyizyk.common.util.AnnotationGetter;
import cn.yuyizyk.provide.filter.RServerFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
@Data
public class SpringServiceFinder implements InstantiationAwareBeanPostProcessor {

	private final Map<String, Set<String>> handlerTreeMap = new HashMap<>();
	private final Map<String, Object> handlerMap = new HashMap<>();
	private Action<String> findServerCallBack;
	private Action<RServerFilter> findFilterCallBack;

	/**
	 * 将注册到spring 容器中的服务注册到rpc服务中 <br/>
	 */
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		if (RServerFilter.class.isAssignableFrom(bean.getClass())) {
			log.info(" find RPC server filter {} ", bean.getClass().getName());
			findFilterCallBack.apply((RServerFilter) bean);
		}
		SimpleEntry<RPCServerName, Class<?>> s = AnnotationGetter.getRPCAnnInfo(bean.getClass());
		if (s != null) {
			for (String sname : s.getKey().value()) {
				if (!handlerTreeMap.containsKey(sname)) {
					handlerTreeMap.put(sname, new HashSet<String>());
					findServerCallBack.action(sname);
				}
				handlerTreeMap.get(sname).add(s.getValue().getName());
				log.info(" find RPC [{}]serivce ：{}  is Bean :{}", sname, s.getValue().getName(), beanName);
				handlerMap.put(s.getValue().getName(), bean);
			}
		}
		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);

	}

	/**
	 * 获得spring 容器 中的服务
	 * 
	 * @param serverName
	 * @param clzName
	 * @return
	 */
	public Object getObj(String clzName) {
		return handlerMap.get(clzName);
	}

}
