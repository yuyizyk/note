package cn.yuyizyk.common.util.seriailize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import cn.yuyizyk.common.entity.FastByteArrayOutputStream;
import cn.yuyizyk.common.util.Objs;

/**
 * 
 * 序列化工具
 * 
 *
 * @author yuyi
 */
public abstract class AbstractSerializeUtil implements Serialization, Deserialization {

	public abstract <T> String serializeToStr(T obj);

	public abstract <T> T deserializeByStr(String obj, Class<T> clz);

	/**
	 * 原生序列化
	 * 
	 * @param <T>
	 *            对象类型
	 * @param obj
	 *            要被序列化的对象
	 * @return 序列化后的字节码
	 */
	public <T> byte[] serialize(T obj) {
		if (null == obj || false == (obj instanceof Serializable)) {
			return null;
		}
		FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream();
		serialization(obj, byteOut);
		return byteOut.toByteArray();
	}

	/**
	 * 原生反序列化
	 * 
	 * @param <T>
	 *            对象类型
	 * @param bytes
	 *            反序列化的字节码
	 * @return 反序列化后的对象
	 */
	public <T> T unserialize(byte[] bytes, Class<T> clz) {
		if (Objs.isEmpty(bytes))
			return null;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return deserializeByStr(bais, clz);
	}

}
