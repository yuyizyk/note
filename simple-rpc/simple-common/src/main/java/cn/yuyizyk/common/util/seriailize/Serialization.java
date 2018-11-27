package cn.yuyizyk.common.util.seriailize;

import java.io.OutputStream;

public interface Serialization {
	public <T> void serialization(T obj, OutputStream os);

	
}
