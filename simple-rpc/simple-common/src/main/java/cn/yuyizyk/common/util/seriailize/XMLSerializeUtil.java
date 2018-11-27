package cn.yuyizyk.common.util.seriailize;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cn.yuyizyk.common.util.Objs;

/**
 * javaBean与xml之间的序列化和反序列化
 * 
 * 
 *
 * @author yuyi
 */
public class XMLSerializeUtil extends AbstractSerializeUtil {

	// public static void main(String[] args) {
	// BoxEntity<Map<String, Object>> b = new BoxEntity<>();
	// Map<String, Object> map = new HashMap<>();
	// b.setObj(map);
	// XStream x = new XStream(new DomDriver());
	// x.processAnnotations(b.getClass());// 开启对b.getClass的注解解析
	// // 类别名
	// x.alias(b.getClass().getSimpleName(), b.getClass());// 将class box 节名 输出为
	// b.getClass().getSimpleName()
	// // 类成员别名
	// x.aliasField("value", BoxEntity.class, "obj");
	// x.aliasAttribute(BoxEntity.class, "obj", "value");
	//
	// // x.useAttributeFor(BoxEntity.class, "obj");
	// // registerConverter(Converter converter) ，注册一个转换器。
	// System.out.println(x.toXML(b));
	// }

	/**
	 * 将对象xml化
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T deserializeByXmlStr(String xml, Class<T> clz) {
		if (Objs.isEmpty(xml) || clz == null) {
			return null;
		}
		XStream x = new XStream(new DomDriver());
		x.alias(clz.getSimpleName(), clz);
		return (T) x.fromXML(xml);
	}

	/**
	 * 将对象xml化
	 * 
	 * @param obj
	 * @return
	 */
	public String serializeToXmlStr(Object obj) {
		if (Objs.isEmpty(obj)) {
			return null;
		}
		XStream x = new XStream(new DomDriver());
		// x.processAnnotations(obj.getClass());
		x.alias(obj.getClass().getSimpleName(), obj.getClass());
		return x.toXML(obj);
	}

	@Override
	public void serialization(Object obj, OutputStream os) {
		if (Objs.isEmpty(obj)) {
			return;
		}
		XStream x = new XStream(new DomDriver());
		// x.processAnnotations(obj.getClass());
		x.alias(obj.getClass().getSimpleName(), obj.getClass());
		x.toXML(obj, os);
	}

	@Override
	public <T> String serializeToStr(T obj) {
		return serializeToXmlStr(obj);
	}

	@Override
	public <T> T deserializeByStr(String obj, Class<T> clz) {
		return deserializeByXmlStr(obj, clz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeByStr(InputStream obj, Class<T> clz) {
		if (Objs.isEmpty(obj) || clz == null) {
			return null;
		}
		XStream x = new XStream(new DomDriver());
		x.alias(clz.getSimpleName(), clz);
		return (T) x.fromXML(obj);
	}

}
