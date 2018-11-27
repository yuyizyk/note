package cn.yuyizyk.common.entity;

import java.util.function.Function;

@FunctionalInterface
public interface Action<T> extends Function<T, Void> {
	void action(T t);

	@Override
	default Void apply(T t) {
		action(t);
		return null;
	}

}
