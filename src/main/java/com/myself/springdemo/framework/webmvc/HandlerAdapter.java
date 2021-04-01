package com.myself.springdemo.framework.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 解耦。
public class HandlerAdapter {

	/**
	 * @param req     为什么传request？
	 *                根据用户请求的req与method中的参数信息，进行动态匹配。
	 * @param resp    为什么传response？
	 *                为了将其赋值给方法参数。
	 * @param handler 为什么传入handler？
	 *                因为handler中包含了controller、method、url信息。
	 * @return
	 */
	public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) {

		// 传来的modelAndView为空时，才会new一个默认的。
		return null;
	}
}
