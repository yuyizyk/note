package cn.yuyizyk.action.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.yuyizyk.action.BaseAction;

public class IndexAction extends BaseAction {

	public void index() {
		print("{\"key\":\"\"}");
	}

	public void index2(String pStr, String pStr_2) {
		print("{\"pStr_2\":\"" + pStr_2 + "\",\"pStr\":\"" + pStr + "\"");
	}

	@GetMapping({ "", "/" })
	public void index(String pStr, String pStr_2) {
		print("{\"pStr_2\":\"" + pStr_2 + "\",\"pStr\":\"" + pStr + "\"");
	}

	@RequestMapping("index3/{pStr}")
	public void index3(@PathVariable("pStr") String pStr, String pStr_2) {
		print("{\"pStr_2\":\"" + pStr_2 + "\",\"pStr\":\"" + pStr + "\"");
	}

}
