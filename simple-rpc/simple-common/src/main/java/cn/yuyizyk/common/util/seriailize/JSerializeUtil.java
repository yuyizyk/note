package cn.yuyizyk.common.util.seriailize;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.yuyizyk.common.util.Objs;

/**
 * 
 * java 原生序列化工具<br/>
 * 效率较低
 * 
 *
 * @author yuyi
 */
public class JSerializeUtil extends AbstractSerializeUtil {
	private final static Logger log = LoggerFactory.getLogger(JSerializeUtil.class);

	@Override
	public <T> void serialization(T obj, OutputStream byteOut) {
		try (ObjectOutputStream oos = new ObjectOutputStream(byteOut)) {
			oos.writeObject(obj);
			oos.flush();
		} catch (Exception e) {
			log.error("异常", e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeByStr(InputStream bais, Class<T> clz) {
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (T) ois.readObject();
		} catch (Exception e) {
			log.error("异常", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> String serializeToStr(T obj) {
		if (false == (obj instanceof Serializable)) {
			throw new IllegalArgumentException(" obj class not implements Serializable ! obj is " + obj);
		}
		return serializationToStr((Serializable) obj);
	}

	/**
	 * java 原生序列化
	 * 
	 * @param obj
	 * @return
	 */
	public String serializationToStr(Serializable obj) {
		if (Objs.isEmpty(obj)) {
			return null;
		}
		return Base64.getEncoder().encodeToString(serialize(obj));
	}

	/**
	 * java 原生反序列化
	 * 
	 * @param obj
	 * @return
	 */
	public Serializable deserializeByStr(String s) {
		if (Objs.isEmpty(s))
			return null;
		return unserialize(Base64.getDecoder().decode(s), Serializable.class);
	}

	/**
	 * java 原生反序列化
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeByStr(String s, Class<T> clz) {
		Serializable ser = deserializeByStr(s);
		return (T) ser;
	}

}
