package cn.yuyizyk.provide.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import cn.yuyizyk.common.rservice.RpcRequest;
import cn.yuyizyk.common.rservice.RpcResponse;
import cn.yuyizyk.provide.filter.RServerAfterFilter;
import cn.yuyizyk.provide.filter.RServerFilter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理请求
 * 
 * 
 * {@link https://blog.csdn.net/qq924862077/article/details/52946617}
 *
 * @author yuyi
 */
@Slf4j
@Sharable
public class InvokerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	public static ConcurrentHashMap<String, Object> classMap = new ConcurrentHashMap<String, Object>();
	private final Function<String, Object> getterBeanByClz;
	private final List<RServerAfterFilter> rServerAfterFilters = new ArrayList<>();

	public InvokerHandler addFilters(List<RServerFilter> filters) {
		filters.forEach(this::addFilter);
		return this;
	}

	public InvokerHandler addFilter(RServerFilter filter) {
		if (RServerAfterFilter.class.isAssignableFrom(filter.getClass())) {
			rServerAfterFilters.add((RServerAfterFilter) filter);
		}

		return this;
	}

	public InvokerHandler(Function<String, Object> getterBeanByClz) {
		this.getterBeanByClz = getterBeanByClz;
	}

	// @Override
	// public void channelRead(ChannelHandlerContext ctx, Object msg) throws
	// Exception {
	// RpcRequest req = (RpcRequest) msg;
	// Object claszz = null;
	// if (!classMap.containsKey(req.getClassName())) {
	// claszz = Class.forName(req.getClassName()).newInstance();
	// classMap.put(req.getClassName(), claszz);
	// } else {
	// claszz = classMap.get(req.getClassName());
	// }
	// Method method = claszz.getClass().getMethod(req.getMethodName(),
	// req.getParameterTypes());
	// Object result = method.invoke(claszz, req.getParameters());
	// ctx.write(result);
	// ctx.flush();
	// ctx.close();
	// }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		log.debug(" RpcRequest : {} ", request);
		RpcResponse response = new RpcResponse();
		response.setId(request.getId());
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable t) {
			log.error("", t);
			response.setErrorClz(t.getClass().getName());
			response.setError(t.getLocalizedMessage());
		}
		log.debug(" RpcResponse : {} ", response);
		ctx.writeAndFlush(response)/* .addListener(ChannelFutureListener.CLOSE) */;
		for (RServerAfterFilter rServerAfterFilter : rServerAfterFilters)
			rServerAfterFilter.after();
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Object serviceBean = getterBeanByClz.apply(className);
		if (serviceBean == null) {
			throw new RuntimeException("没有对应Service:" + className);
		}

		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		/*
		 * Method method = serviceClass.getMethod(methodName, parameterTypes);
		 * method.setAccessible(true); return method.invoke(serviceBean, parameters);
		 */

		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		Object result = serviceFastMethod.invoke(serviceBean, parameters);
		return result;
	}
}
