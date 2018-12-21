package cn.yuyizyk.compent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Handler {
	private final HandlerAction<?> handlerAction;

	public Handler(HandlerAction<?> handlerAction) {
		this.handlerAction = handlerAction;
	}

	protected Object invoke(HttpServletRequest request, HttpServletResponse response, Object[] args) throws Throwable {
		return handlerAction.invoke(request, response, args);
	}

	//
	// public void run(ServletRequest request, ServletResponse response) throws
	// Throwable {
	//
	// }
}
