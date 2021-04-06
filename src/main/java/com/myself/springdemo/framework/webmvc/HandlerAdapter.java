package com.myself.springdemo.framework.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

// 解耦。
public class HandlerAdapter {

	private final Map<String, Integer> paramMapping;

	public HandlerAdapter(Map<String, Integer> paramMapping) {
		this.paramMapping = paramMapping;
	}

	/**
	 * @param req     为什么传request？
	 *                根据用户请求的req与method中的参数信息，进行动态匹配。
	 * @param resp    为什么传response？
	 *                为了将其赋值给方法参数。
	 * @param handler 为什么传入handler？
	 *                因为handler中包含了controller、method、url信息。
	 * @return ModelAndView
	 */
	public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

		// 传来的modelAndView为空时，才会new一个默认的。
		// 1、准备好这个方法的形参列表
		// 方法重载：形参的决定因素，参数的个数，类型，顺序，方法的名字。
		Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

		// 2、拿到自定义明明参数所在位置
		// 用户通过URL传过来的参数列表。请求参数和请求参数值的映射。
		Map<String, String[]> reqParameterMap = req.getParameterMap();

		// 3、构造形参列表
		Object[] paramValues = new Object[parameterTypes.length];
		for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
			String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
			if (this.paramMapping.containsKey(param.getKey())) {
				continue;
			}

			int index = this.paramMapping.get(param.getKey());
			// 页面上传过来的值都是String类型的，而在方法中定义的类型是多种多样的。
			// 对传过来的参数进行类型转换
			paramValues[index] = castStringValue(value, parameterTypes[index]);
		}

		int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
		paramValues[reqIndex] = req;

		int resqIndex = this.paramMapping.get(HttpServletResponse.class.getName());
		paramValues[resqIndex] = resp;

		// 4、从handler中取出controller、method
		// 5、利用反射，进行调用
		Object result = handler.getMethod().invoke(handler.getController(), paramValues);
		if (result == null) {
			return null;
		}
		boolean isModelAndView = handler.getMethod().getReturnType() == ModelAndView.class;
		if (isModelAndView) {
			return (ModelAndView)result;
		} else {
			return null;
		}
	}

	private Object castStringValue(String value, Class<?> clazz) {
		if (clazz == String.class) {
			return value;
		} else if (clazz == Integer.class) {
			return Integer.valueOf(value);
		} else if (clazz == int.class) {
			return Integer.valueOf(value);
		} else {
			return null;
		}
	}
}
