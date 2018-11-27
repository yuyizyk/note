package cn.yuyizyk.common.entity;

import java.io.Serializable;

public class BoxEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private T obj;

	public BoxEntity() {
	}

	public BoxEntity(T obj) {
		setObj(obj);
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}
}
