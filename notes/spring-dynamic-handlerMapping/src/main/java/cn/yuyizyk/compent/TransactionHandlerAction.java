package cn.yuyizyk.compent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.yuyizyk.action.BaseAction;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 * @param <T>
 */
public class TransactionHandlerAction<T extends BaseAction> extends HandlerAction<T> {
	private final HandlerAction<T> action;

	public TransactionHandlerAction(HandlerAction<T> action) {
		super(action.interfaceClass, action.method);
		this.action = action;
	}

	@Override
	public Object invoke(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Throwable {
		try {
			// TODO 初始化事务
			return action.invoke(request, response, args);
		} catch (Throwable e) {
			// TODO 事务回滚
			throw e;
		} finally {
			// TODO 事务结束
		}

	}

}
