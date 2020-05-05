package cn.yuyizyk.tools.component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ID 生成器
 * 
 * <pre>
 * 	时间戳保证全局ID有序 ：13882816373
 * 	32^3位毫秒级ID生成索引：001 
 * 	32^2位服务ID号：02
 * </pre>
 * @介绍 https://my.oschina.net/yuyizyk/blog/4267430
 * @author yuyi
 */
@Slf4j
@Component
public class IDGenerator {
	private final AtomicInteger index = new AtomicInteger(0);
	private final AtomicLong lastTimeMillis = new AtomicLong();

	public static IDGenerator getSingle() {
		return single;
	}

	public static void setSingle(IDGenerator idGenerator) {
		single = idGenerator;
	}

	@PostConstruct
	public void setSingle() {
		setSingle(this);
	}

	private static IDGenerator single;

	public IDGenerator(ObjectProvider<ApplicationProperty> idGeneratorProperties) {
		if (idGeneratorProperties == null)
			throw new IllegalArgumentException("idGeneratorProperties  is null.");
		ApplicationProperty idGeneratorApp = idGeneratorProperties.getObject();
		if (idGeneratorApp == null) {
			throw new IllegalArgumentException("idGeneratorApp  is null.");
		}
		toFixedLStr(idGeneratorApp.getInstanceId().getNum(), workIdlen);
		this.workerIdIndex = idGeneratorApp.getInstanceId().getNum();
	}

	public static IDGenerator of(int workerId) {
		ApplicationProperty idGeneratorApp = new ApplicationProperty();
		idGeneratorApp.getInstanceId().setNum(workerId);
		return new IDGenerator(new ObjectProvider<ApplicationProperty>() {
			@Override
			public ApplicationProperty getObject() throws BeansException {
				return idGeneratorApp;
			}

			@Override
			public ApplicationProperty getObject(Object... args) throws BeansException {
				return idGeneratorApp;
			}

			@Override
			@Nullable
			public ApplicationProperty getIfAvailable() throws BeansException {
				try {
					return idGeneratorApp;
				} catch (NoUniqueBeanDefinitionException ex) {
					throw ex;
				} catch (NoSuchBeanDefinitionException ex) {
					return null;
				}
			}

			@Override
			@Nullable
			public ApplicationProperty getIfUnique() throws BeansException {
				try {
					return idGeneratorApp;
				} catch (NoSuchBeanDefinitionException ex) {
					return null;
				}
			}
		});
	}

	/** 32 位 进制表 */ // acii
	private static final char[] digits = { //
			'0', '1', '2', '3', '4', '5', '6', '7', //
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f', //
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', //
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 'w', 'x', 'y', 'z'
	};

	/** by Integer */
	static int formatUnsignedLong(long val, int shift, char[] buf, int offset, int len) {
		int charPos = len;
		int radix = 1 << shift;
		int mask = radix - 1;
		do {
			buf[offset + --charPos] = digits[((int) val) & mask];
			val >>>= shift;
		} while (val != 0 && charPos > 0);
		return charPos;
	}

	// 定长32位字符
	private static final String toFixedLStr(long val, int len) {
		assert val >= 0;
		assert val <= (digits.length ^ len);
		String str = toStr(val);// 转化为32位字符
		if (str.length() == len)
			return str;
		StringBuilder sb = new StringBuilder(len);
		for (int i = len - str.length(); i > 0; i--) {
			sb.append("0");
		}
		return sb.append(str).toString();
	}

	// 转化为32位字符
	private static final String toStr(long val) {
		return toStr(val, 5);//
	}

	private static String toStr(long val, int shift) {
		int mag = Long.SIZE - Long.numberOfLeadingZeros(val);
		int chars = Math.max(((mag + (shift - 1)) / shift), 1);
		char[] buf = new char[chars];
		formatUnsignedLong(val, shift, buf, 0, chars);
		return new String(buf);
	}

	// IDG ID
	private final int workerIdIndex;
	private static final int workIdlen = 2;

	private static final int seedlen = 3;
	// 1毫秒内能生成的ID数
	private static final int seed = (int) Math.pow(digits.length, seedlen);

	/**
	 * 时间以 2019-01-01 0：0：0 作为起始。而不是从 1970-1-1 减少时间代表的长度。
	 */
	private static final long startime = 1546272000000l;

	/**
	 * 获取当前相对 2019-05-01 的时间 毫秒数
	 * 
	 * @return
	 */
	private static final long getRelativeNowTime() {
		return System.currentTimeMillis() - startime;
	}

	public static final NumId toNumId(String idStr) {
		return NumId.byIdStr(idStr);
	}

	@Data
	public static final class NumId {
		private long relativeTime = 0l;
		private int seed = 0;
		private int workId = 0;

		public long getTime() {
			return relativeTime + startime;
		}

		public long getWeekRelativeTime() {
			return relativeTime % 604800000;
		}

		@Override
		public String toString() {
			return toNumStr();
		}

		public String toNumStr() {
			StringBuilder sb = new StringBuilder(32).append(getTime());
			String str = String.valueOf(seed);
			for (int i = 5 - str.length(); i > 0; i--) {
				sb.append('0');
			}
			sb.append(str);
			str = String.valueOf(workId);
			for (int i = 4 - str.length(); i > 0; i--) {
				sb.append('0');
			}
			sb.append(str);
			return sb.toString();
		}

		public final String toIdStr() {
			return new StringBuilder(16).append(toStr(relativeTime)).append(toFixedLStr(seed, seedlen))
					.append(toFixedLStr(workId, 2)).toString();
		}

		public static NumId byNumIdStr(String numIdStr) {
			if (numIdStr.length() <= 9)
				throw new IllegalArgumentException(" numIdStr  is error ");
			NumId id = new NumId();
			id.setWorkId(Integer.valueOf(numIdStr.substring(numIdStr.length() - 4)));
			id.setSeed(Integer.valueOf(numIdStr.substring(numIdStr.length() - 9, numIdStr.length() - 4)));
			id.setRelativeTime(Long.valueOf(numIdStr.substring(0, numIdStr.length() - 9)) - startime);
			if (id.getRelativeTime() < 0)
				throw new IllegalArgumentException(" numIdStr  is error ");
			return id;
		}

		public static NumId byIdStr(String idStr) {
			return new NumId(idStr);
		}

		public NumId(String idStr) {
			if (StringUtils.isBlank(idStr)) {
				throw new IllegalArgumentException("id is empty");
			}
			char[] cs = idStr.toCharArray();
			int i, temp = 0, len = cs.length - (seedlen + workIdlen);
			char c = 0;
			while (temp < len) {
				i = (c = cs[temp]) - 'a';
				if (i < 0) {
					i = c - '0';
				} else
					i += 10;
				relativeTime = relativeTime * 32 + i;
				temp++;
			}
			len = cs.length - workIdlen;
			while (temp < len) {
				i = (c = cs[temp]) - 'a';
				if (i < 0) {
					i = c - '0';
				} else
					i += 10;
				seed = seed * 32 + i;
				temp++;
			}
			len = cs.length;
			while (temp < cs.length) {
				i = (c = cs[temp]) - 'a';
				if (i < 0) {
					i = c - '0';
				} else
					i += 10;
				workId = workId * 32 + i;
				temp++;
			}
		}

		public NumId() {
		}
	}

	/** 获得ID */
	public final NumId nextNumId() {
		NumId id = new NumId();
		id.setWorkId(workerIdIndex);
		long t = getRelativeNowTime(), t2;
		for (; t > (t2 = lastTimeMillis.get());) {
			if (lastTimeMillis.compareAndSet(t2, t)) {
				index.set(0);
				break;
			}
		}
		if (t < t2)
			log.error("时钟回拨");// 该情况出现在系统时钟回拨。此情况出现，则代表ID极可能重复。
		id.setRelativeTime(t);
		index.compareAndSet(seed, 0);
		id.setSeed(index.incrementAndGet());
		return id;
	}

	/** 获得ID */
	public final String nextId() {
		return nextNumId().toIdStr();
	}

	public static void main(String[] args) {
		IDGenerator g = IDGenerator.of(2);
		String str = g.nextId();
		System.out.println(str);
		NumId i;
		System.out.println(i = toNumId(str));
		System.out.println(i.toString().length());
		System.out.println(i.getTime());
//		System.out.println(System.currentTimeMillis());
//		System.out.println(604800000l);
		System.out.println(i.getWeekRelativeTime());
//		System.out.println(Integer.MAX_VALUE);
		System.out.println(i.toIdStr());
		System.out.println(NumId.byNumIdStr(i.toString()).toIdStr());
	}
}
