package cn.yuyizyk.compent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.yuyizyk.action.BaseAction;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 * @param <T>
 */
public class HandlerAction<T extends BaseAction> {
	private final static Logger log = LoggerFactory.getLogger(TransactionHandlerAction.class);
	protected final Class<T> interfaceClass;
	protected final Method method;

	public HandlerAction(Class<T> clz, Method m) {
		this.interfaceClass = clz;
		this.method = m;
	}

	private Supplier<T> getter;

	private T getAction() {
		if (getter != null)
			return getter.get();

		Class<?> temp = interfaceClass;
		List<Field> fields = new ArrayList<>();
		do {
			for (Field f : temp.getDeclaredFields()) {
				if (Stream.of(f.getAnnotations()).anyMatch(ac -> Autowired.class.equals(ac.annotationType())
						|| Resource.class.equals(ac.annotationType()))) {
					fields.add(f);
				}
			}
		} while (!temp.equals(BaseAction.class) && (temp = temp.getSuperclass()) != null);
		getter = () -> {
			try {
				T action = interfaceClass.newInstance();
				for (Field f : fields) {
					f.setAccessible(true);
					f.set(action, SpringContextUtils.getBean(f.getType()));
				}
				return action;
			} catch (InstantiationException | IllegalAccessException e1) {
				log.error("Class: {} method :{} ,field is :{}", interfaceClass, method, fields);
				log.error("初始化失败: ", e1);
				throw new IllegalArgumentException(e1);
			}
		};

		return getter.get();
	}

	protected BaseAction init(HttpServletRequest request, HttpServletResponse response)
			throws InstantiationException, IllegalAccessException {
		final BaseAction action = getAction();
		return action.init(request, response);
	}

	public Object invoke(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Throwable {
		try {
			return method.invoke(init(request, response), args);
		} finally {
			// TODO 会话结束 销毁操作
		}
	}

}
