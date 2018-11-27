package cn.yuyizyk.common.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.yuyizyk.common.util.Objs;

/**
 * 
 * 三合一
 * 
 *
 * @author yuyi
 * @param <T>
 * @param <P>
 */
public class TripleEntity<T, P, A> extends DoubleEntity<T, P> {

	private A entity3;

	public TripleEntity() {
	}

	public static final <T, P, A> TripleEntity<T, P, A> builder(T t, P p, A a) {
		return new TripleEntity<>(t, p, a);
	}

	public boolean isEmpty() {
		return Objs.isEmpty(getEntity1(), getEntity2());
	}

	public TripleEntity(T t, P p, A a) {
		super(t, p);
		setEntity3(a);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("entity1", getEntity1())
				.append("entity2", getEntity1()).append("entity3", getEntity3()).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TripleEntity))
			return false;
		TripleEntity<?, ?, ?> be = (TripleEntity<?, ?, ?>) obj;
		return (getEntity1() == be.getEntity1() || (getEntity1() != null && getEntity1().equals(be.getEntity1())))
				&& (getEntity2() == be.getEntity2() || (getEntity2() != null && getEntity2().equals(be.getEntity2())))
				&& (getEntity3() == be.getEntity3() || (getEntity3() != null && getEntity3().equals(be.getEntity3())));
	}

	public A getEntity3() {
		return entity3;
	}

	public void setEntity3(A entity3) {
		this.entity3 = entity3;
	}

}
