package cn.yuyizyk.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseAction {

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public BaseAction init(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		return this;
	}

	protected void print(Object obj) {
		try {
			response.getWriter().println(obj.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}

}
