package cn.yuyizyk.common.util.seriailize;

import java.io.InputStream;

public interface Deserialization {

	public <T> T deserializeByStr(InputStream obj, Class<T> clz);

}
