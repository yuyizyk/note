package cn.yuyizyk.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.yuyizyk.common.entity.SimpleEntry;
import cn.yuyizyk.common.rservice.RPCIService;
import cn.yuyizyk.common.rservice.RPCServerName;
import cn.yuyizyk.common.rservice.RService;

/**
 * 注解获取
 * 
 * 
 *
 * @author yuyi
 */
public class AnnotationGetter {
	@SuppressWarnings("unchecked")
	public static SimpleEntry<RPCServerName, Class<?>> getRPCAnnInfo(Class<?> clz) {
		if (RPCIService.class.isAssignableFrom(clz)) {
			RPCServerName rpcs = null;
			Class<?> c1 = clz, rserviceClz = null;
			List<Class<?>>[] li = new List[] { new ArrayList<Class<?>>(), new ArrayList<Class<?>>() };
			int i = 0;
			if (clz.isInterface()) {
				li[i].add(clz);
			} else
				li[i] = Stream.of(c1.getInterfaces())
						.filter(tc -> RPCIService.class.isAssignableFrom(tc) && !RPCIService.class.equals(tc))
						.collect(Collectors.toList());
			seach: {
				while (!li[i].isEmpty()) {
					li[1 - i].clear();
					for (int j = 0; j < li[i].size(); j++) {
						rpcs = (rpcs == null ? (c1 = li[i].get(j)).getAnnotation(RPCServerName.class) : rpcs);
						if (rserviceClz == null && (c1).isAnnotationPresent(RService.class)) {
							rserviceClz = c1;
						}
						if (rpcs != null && rserviceClz != null) {
							break seach;
						}
						li[1 - i].addAll(Stream.of(c1.getInterfaces())
								.filter(tc -> RPCIService.class.isAssignableFrom(tc) && !RPCIService.class.equals(tc))
								.collect(Collectors.toList()));
					}
					i = 1 - i;
				}
			}

			if (rpcs != null && rserviceClz != null && !RPCIService.class.equals(rserviceClz))
				return new SimpleEntry<RPCServerName, Class<?>>(rpcs, rserviceClz);
		}
		return null;
	}

}
