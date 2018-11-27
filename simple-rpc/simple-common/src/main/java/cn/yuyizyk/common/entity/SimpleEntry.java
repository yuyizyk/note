package cn.yuyizyk.common.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 * @param <K>
 * @param <V>
 */
public class SimpleEntry<K, V> extends DoubleEntity<K, V> implements Map.Entry<K, V>, Serializable {

	public SimpleEntry() {
	}

	public SimpleEntry(K k, V v) {
		super(k, v);
	}

	public boolean equals(SimpleEntry<K, V> obj) {
		return super.equals(obj);
	}

	public boolean equals(K k, V v) {
		return (getKey() == k || (getKey() != null && getKey().equals(k)))
				&& (getValue() == v || (getValue() != null && getValue().equals(v)));
	}

	private transient static final long serialVersionUID = 1L;

	public SimpleEntry<K, V> setKey(K k) {
		super.setEntity1(k);
		return this;
	}

	@Override
	public K getKey() {
		return getEntity1();
	}

	@Override
	public V getValue() {
		return getEntity2();
	}

	@Override
	public V setValue(V value) {
		V val = getEntity2();
		setEntity2(value);
		return val;
	}

}
