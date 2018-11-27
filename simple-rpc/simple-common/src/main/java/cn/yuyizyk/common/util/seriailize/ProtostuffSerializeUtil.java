package cn.yuyizyk.common.util.seriailize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.yuyizyk.common.entity.BoxEntity;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 
 * Protostuff 序列化工具<br/>
 * 效率高
 * 
 *
 * @author yuyi
 */
public class ProtostuffSerializeUtil extends AbstractSerializeUtil {
	private final static Logger log = LoggerFactory.getLogger(ProtostuffSerializeUtil.class);

	// public static void main(String[] args) {
	// List<String> b = new ArrayList<>();
	// b.add("123");
	// BoxEntity<List<String>> bb = new BoxEntity<>();
	// bb.setObj(b);
	// ProtostuffSerializeUtil u = new ProtostuffSerializeUtil();
	// String s = u.serializeToStr(bb);
	// System.out.println(s);
	// System.out.println(JSONObject.toJSONString(bb = u.deserializeByStr(s,
	// BoxEntity.class)));
	// System.out.println(bb);
	//
	// }

	@SuppressWarnings("rawtypes")
	@Override
	public <T> void serialization(T obj, OutputStream os) {
		if (obj == null || os == null)
			return;
		BoxEntity<T> box = new BoxEntity<>();
		box.setObj(obj);
		Schema<BoxEntity> schema = RuntimeSchema.getSchema(BoxEntity.class);
		try {
			ProtostuffIOUtil.writeTo(os, box, schema, LinkedBuffer.allocate(256));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public <T> T deserializeByStr(InputStream is, Class<T> clz) {
		BoxEntity<T> box = new BoxEntity<>();
		Schema<BoxEntity> schema = RuntimeSchema.getSchema(BoxEntity.class);
		try {
			ProtostuffIOUtil.mergeFrom(is, box, schema);
		} catch (IOException e) {
			log.error("异常 ", e);
		}
		return box.getObj();
	}

	@Override
	public <T> String serializeToStr(T obj) {
		if (obj == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(serialize(obj));
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public <T> byte[] serialize(T obj) {
		if (obj == null) {
			return null;
		}
		Schema<BoxEntity> schema = RuntimeSchema.getSchema(BoxEntity.class);
		BoxEntity<T> box = new BoxEntity<>();
		box.setObj(obj);
		return ProtostuffIOUtil.toByteArray(box, schema, LinkedBuffer.allocate(256));
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public <T> T unserialize(byte[] bytes, Class<T> clazz) {
		BoxEntity<T> box = new BoxEntity<>();
		Schema<BoxEntity> schema = RuntimeSchema.getSchema(BoxEntity.class);
		ProtostuffIOUtil.mergeFrom(bytes, box, schema);
		return box.getObj();

	}

	@Override
	public <T> T deserializeByStr(String obj, Class<T> clz) {
		return unserialize(Base64.getDecoder().decode(obj), clz);
	}

	@SuppressWarnings("rawtypes")
	public <T> T deserializeByStr(byte[] data, Class<T> class1) {
		BoxEntity<T> box = new BoxEntity<>();
		Schema<BoxEntity> schema = RuntimeSchema.getSchema(BoxEntity.class);
		ProtostuffIOUtil.mergeFrom(data, box, schema);
		return box.getObj();
	}

}
