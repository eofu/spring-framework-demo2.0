package com.myself.springdemo.framework.webmvc.servlet;

import com.myself.springdemo.framework.context.ApplicationContext;
import com.myself.springdemo.framework.webmvc.HandlerAdapter;
import com.myself.springdemo.framework.webmvc.HandlerMapping;
import com.myself.springdemo.framework.webmvc.ModelAndView;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// MVC启动入口
public class DispatcherServlet extends HttpServlet {
	// private final Map<String, HandlerMapping> handlerMapping = new HashMap();

	// HandlerMapping是SpringMVC中最核心的设计。
	private final List<HandlerMapping> handlerMappings = new ArrayList();

	//
	private final List<HandlerAdapter> handlerAdapters = new ArrayList();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// String url = req.getRequestURI();
		// String contextPath = req.getContextPath();
		// url = url.replace(contextPath, "").replaceAll("/+", "/");
		// HandlerMapping handler = handlerMapping.get(url);

		// try {
		// 	ModelAndView mv = (ModelAndView)handler.getMethod().invoke(handler.getController());
		// } catch (IllegalAccessException e) {
		// 	e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// 	e.printStackTrace();
		// }


		// doDispatch(req, resp);
	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
		HandlerMapping handler = getHandler(req);

		HandlerAdapter ha = getHandlerAdapter(handler);

		ModelAndView mv = ha.handle(req, resp, handler);

		processDispatchResult(resp, mv);
	}

	private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) {
		// 调用viewResolver的resolveView()
	}

	private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {
		return null;
	}

	private HandlerMapping getHandler(HttpServletRequest req) {
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		ApplicationContext applicationContext = new ApplicationContext(config.getInitParameter("contextConfigLocation"));

		initStrategies(applicationContext);
	}

	private void initStrategies(ApplicationContext context) {
		// 文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
		initMultipartResolver(context);
		// 本地化解析
		initLocaleResolver(context);
		// 主题解析
		initThemeResolver(context);
		/**
		 * 用来保存urlController中配置的RequstMapping和method到一个对应关系
		 */
		initHandlerMappings(context);// handlerMapping将请求映射到处理器。
		/**
		 * 用来动态匹配method参数，包括类型转换，动态赋值
		 */
		initHandlerAdapters(context);// 通过handlerAdapter进行多类型参数动态匹配
		// 如果执行中遇到异常
		initHandlerExceptionResolvers(context);
		// 直接解析请求到视图名
		initRequestToViewNameTranslator(context);
		/**
		 * 通过viewResolver实现动态模版解析
		 */
		initViewResolvers(context);// 通过viewResolver解析逻辑视图到具体视图
		// flash映射管理器
		initFlashMapManager(context);
	}

	private void initHandlerAdapters(ApplicationContext context) {
	}

	private void initHandlerMappings(ApplicationContext context) {

	}

	private void initViewResolvers(ApplicationContext context) {
	}

	private void initFlashMapManager(ApplicationContext context) {
	}

	private void initRequestToViewNameTranslator(ApplicationContext context) {
	}

	private void initHandlerExceptionResolvers(ApplicationContext context) {
	}

	private void initThemeResolver(ApplicationContext context) {
	}

	private void initLocaleResolver(ApplicationContext context) {
	}

	private void initMultipartResolver(ApplicationContext context) {
	}
}
