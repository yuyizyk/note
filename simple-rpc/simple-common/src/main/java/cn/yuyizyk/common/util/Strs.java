package cn.yuyizyk.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Strs extends org.apache.commons.lang3.StringUtils {

	/**
	 * 日期型正则文本格式（YYYY-MM-DD）
	 */
	public static final String DATE_FORMAT_REGULARITY = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";

	/**
	 * 时间型正则文本格式（YYYY-MM-DD HH:MM:SS）
	 */
	public static final String TIME_FORMAT_REGULARITY = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8])))))) ((0[0-9])|(1[0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9]{1}";

	/**
	 * 
	 * @Description: 替换空格 ，半角 、全角
	 */
	public static String ReplaceBlank(String str) {
		if (str == null)
			return str;
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(str);
		str = m.replaceAll("");
		str = remove(str, "　");
		str = remove(str, " ");
		return str;
	}

	public static final String find(String str, String regx) {
		if (str == null)
			return str;
		Pattern p = Pattern.compile(regx);
		Matcher m = p.matcher(str);
		return m.find() ? m.group() : null;
	}

	/**
	 * 判断是否为日期型数据
	 * 
	 * @Description: 判断是否为日期型数据(YYYY-MM-DD)
	 */
	public static boolean isDate(String date) {
		/**
		 * 判断日期格式和范围
		 */
		Pattern pat = Pattern.compile(DATE_FORMAT_REGULARITY);

		Matcher mat = pat.matcher(date);

		boolean dateType = mat.matches();

		return dateType;
	}

	/**
	 * 判断是否为时间型数据
	 * 
	 * @Description: 判断是否为时间型数据(YYYY-MM-DD HH:MM:SS)
	 */
	public static boolean isTime(String dateString) {
		return dateString.matches(TIME_FORMAT_REGULARITY);
	}

	public static void main(String[] args) {
		// String date = "2017-11-15";
		// System.out.println(isDate(date));
		// String time = "2017-4-31 23:23:23";
		// System.out.println(isTime(time));
		// System.out.println(Charset.defaultCharset());
		// String s = "asdf中国你a好";
		// System.out.println(getOmitStr(s, 16));
		// System.out.println(getAsciiLength(getOmitStr(s, 16)));
	}

	/**
	 * 根据字符的Ascii来获得具体的长度
	 * 
	 * @param s
	 * @return
	 */
	public static int getAsciiLength(String str) {
		int length = 0;
		for (int i = 0; i < str.length(); i++) {
			int ascii = Character.codePointAt(str, i);
			if (ascii >= 0 && ascii <= 255)
				length++;
			else
				length += 2;

		}
		return length;
	}

	/**
	 * 获得指定长度的省略字符串
	 * 
	 * @param str
	 * @param length
	 * @return
	 */
	public static String getOmitStr(String str, int length) {
		if (length >= str.length() * 2) {
			return str;
		}
		StringBuffer sb = new StringBuffer();
		int len = 0;
		for (int i = 0; i < str.length(); i++) {
			int ascii = Character.codePointAt(str, i);
			if (ascii >= 0 && ascii <= 255) {
				if (len >= length - 3) {
					break;
				}
				len++;
			} else {
				if (len >= length - 4) {
					break;
				}
				len += 2;
			}
			sb.append(str.charAt(i));
		}
		if (length >= len + 3) {
			sb.append("...");
		}
		return sb.toString();
	}

	public static String getEncodingString(String str, String defaEncod) {
		String enc = getEncoding(str);
		if (StringUtils.isBlank(enc)) {
			enc = defaEncod;
		}
		if (StringUtils.isBlank(enc)) {
			enc = "utf-8";
		}
		try {
			return new String(str.getBytes(enc), "utf-8");
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * 获得文件编码
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { // 判断是不是GB2312
				String s = encode;
				return s; // 是的话，返回“GB2312“，以下代码同理
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { // 判断是不是ISO-8859-1
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { // 判断是不是UTF-8
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { // 判断是不是GBK
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return ""; // 如果都不是，说明输入的内容不属于常见的编码格式。
	}

	public static String clobToString(Clob clob) throws SQLException, IOException {
		String reString = "";
		Reader is = clob.getCharacterStream();// 得到流
		BufferedReader br = new BufferedReader(is);
		String s = br.readLine();
		StringBuffer sb = new StringBuffer();
		while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			sb.append(s);
			s = br.readLine();
		}
		reString = sb.toString();
		if (br != null) {
			br.close();
		}
		if (is != null) {
			is.close();
		}
		return reString;
	}

	/** UTF-8 */
	public static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

	/**
	 * 将对象转为字符串<br>
	 * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
	 * 
	 * @param obj
	 *            对象
	 * @return 字符串
	 */
	public static String toString(Object obj) {
		return toString(obj, CHARSET_UTF_8);
	}

	/**
	 * 将对象转为字符串<br>
	 * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
	 * 
	 * @param obj
	 *            对象
	 * @param charsetName
	 *            字符集
	 * @return 字符串
	 */
	public static String toString(Object obj, String charsetName) {
		return toString(obj, Charset.forName(charsetName));
	}

	/**
	 * 对象是否为数组对象
	 * 
	 * @param obj
	 *            对象
	 * @return 是否为数组对象，如果为{@code null} 返回false
	 */
	public static boolean isArray(Object obj) {
		if (null == obj) {
			return false;
		}
		return obj.getClass().isArray();
	}

	/**
	 * 数组或集合转String
	 * 
	 * @param obj
	 *            集合或数组对象
	 * @return 数组字符串，与集合转字符串格式相同
	 */
	public static String arr2String(Object obj) {
		if (null == obj) {
			return null;
		}
		if (isArray(obj)) {
			try {
				return Arrays.deepToString((Object[]) obj);
			} catch (Exception e) {
				final String className = obj.getClass().getComponentType().getName();
				switch (className) {
				case "long":
					return Arrays.toString((long[]) obj);
				case "int":
					return Arrays.toString((int[]) obj);
				case "short":
					return Arrays.toString((short[]) obj);
				case "char":
					return Arrays.toString((char[]) obj);
				case "byte":
					return Arrays.toString((byte[]) obj);
				case "boolean":
					return Arrays.toString((boolean[]) obj);
				case "float":
					return Arrays.toString((float[]) obj);
				case "double":
					return Arrays.toString((double[]) obj);
				default:
					throw e;
				}
			}
		}
		return obj.toString();
	}

	/**
	 * 将对象转为字符串<br>
	 * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
	 * 
	 * @param obj
	 *            对象
	 * @param charset
	 *            字符集
	 * @return 字符串
	 */
	public static String toString(Object obj, Charset charset) {
		if (isEmpty(obj)) {
			return null;
		}

		if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof byte[]) {
			return toString((byte[]) obj, charset);
		} else if (obj instanceof Byte[]) {
			return toString((Byte[]) obj, charset);
		} else if (obj instanceof ByteBuffer) {
			return toString((ByteBuffer) obj, charset);
		} else if (isArray(obj)) {
			return arr2String(obj);
		}

		return obj.toString();
	}

	/**
	 * 将byte数组转为字符串
	 * 
	 * @param bytes
	 *            byte数组
	 * @param charset
	 *            字符集
	 * @return 字符串
	 */
	public static String toString(byte[] bytes, String charset) {
		return toString(bytes, isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset));
	}

	/**
	 * 解码字节码
	 * 
	 * @param data
	 *            字符串
	 * @param charset
	 *            字符集，如果此字段为空，则解码的结果取决于平台
	 * @return 解码后的字符串
	 */
	public static String toString(byte[] data, Charset charset) {
		if (isEmpty(data)) {
			return null;
		}

		if (isEmpty(charset)) {
			return new String(data);
		}
		return new String(data, charset);
	}

	/**
	 * 将Byte数组转为字符串
	 * 
	 * @param bytes
	 *            byte数组
	 * @param charset
	 *            字符集
	 * @return 字符串
	 */
	public static String toString(Byte[] bytes, String charset) {
		return toString(bytes, isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset));
	}

	/**
	 * 解码字节码
	 * 
	 * @param data
	 *            字符串
	 * @param charset
	 *            字符集，如果此字段为空，则解码的结果取决于平台
	 * @return 解码后的字符串
	 */
	public static String toString(Byte[] data, Charset charset) {
		if (isEmpty(data)) {
			return null;
		}

		byte[] bytes = new byte[data.length];
		Byte dataByte;
		for (int i = 0; i < data.length; i++) {
			dataByte = data[i];
			bytes[i] = (isEmpty(dataByte)) ? -1 : dataByte.byteValue();
		}

		return toString(bytes, charset);
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty([])        = true
	 * isEmpty([null])       = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Object[] args) {
		return args == null || args.length == 0;
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty([])        = true
	 * isEmpty([null])       = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Collection<?> args) {
		return args == null || args.isEmpty();
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty({})        = true
	 * isEmpty({null=null})       = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Map<?, ?> args) {
		return args == null || args.isEmpty();
	}

	/**
	 * 
	 * @param args
	 * @return args == null
	 */
	public static final boolean isEmpty(final Object args) {
		return Objects.isNull(args);
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty("")        = true
	 * isEmpty(" ")       = false
	 * isEmpty("bob")     = false
	 * isEmpty("  bob  ") = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final CharSequence cs) {
		return StringUtils.isEmpty(cs);
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final long... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final int... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final short... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final char... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final byte... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final double... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final float... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array
	 *            数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final boolean... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 将编码的byteBuffer数据转换为字符串
	 * 
	 * @param data
	 *            数据
	 * @param charset
	 *            字符集，如果为空使用当前系统字符集
	 * @return 字符串
	 */
	public static String toString(ByteBuffer data, String charset) {
		if (isEmpty(data)) {
			return null;
		}

		return toString(data, Charset.forName(charset));
	}

	/**
	 * 将编码的byteBuffer数据转换为字符串
	 * 
	 * @param data
	 *            数据
	 * @param charset
	 *            字符集，如果为空使用当前系统字符集
	 * @return 字符串
	 */
	public static String toString(ByteBuffer data, Charset charset) {
		if (isEmpty(charset)) {
			charset = Charset.defaultCharset();
		}
		return charset.decode(data).toString();
	}

	/**
	 * {@link CharSequence} 转为字符串，null安全
	 * 
	 * @param cs
	 *            {@link CharSequence}
	 * @return 字符串
	 */
	public static String toString(CharSequence cs) {
		return isEmpty(cs) ? null : cs.toString();
	}

	public static Charset charset(String charsetName) {
		return isBlank(charsetName) ? Charset.defaultCharset() : Charset.forName(charsetName);
	}
}
