package cn.yuyizyk.common.rservice;

import java.io.Serializable;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.yuyizyk.common.entity.BoxEntity;

/**
 * 请求实体
 * 
 * 
 *
 * @author yuyi
 */
public class RpcRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	/** uuid */
	private String id;
	/** 类名 */
	private String className;
	/** 函数名称 */
	private String methodName;
	/** 参数类型 */
	private Class<?>[] parameterTypes;
	/** 参数列表 */
	private BoxEntity<Object>[] parameters;

	public Object[] getParameters() {
		return parameters == null ? null : Stream.of(parameters).map(BoxEntity::getObj).toArray(Object[]::new);
	}

	@SuppressWarnings("unchecked")
	public void setParameters(Object[] parameters) {
		this.parameters = Stream.of(parameters).map(BoxEntity::new).toArray(BoxEntity[]::new);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("c", getClassName())
				.append("m", getMethodName()).append("id", getId()).toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
}
