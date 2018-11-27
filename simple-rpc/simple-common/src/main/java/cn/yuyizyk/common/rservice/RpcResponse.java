package cn.yuyizyk.common.rservice;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.yuyizyk.common.util.Objs;
import lombok.Data;

/**
 * 请求实体
 * 
 * 
 *
 * @author yuyi
 */
@Data
public class RpcResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	/** */
	private String id;
	private String errorClz;
	private String error;
	private Object result;

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
				.append("error", getError()).append("result", Objs.toString(result)).toString();
	}
}
