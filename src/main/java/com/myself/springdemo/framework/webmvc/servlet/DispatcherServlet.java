package com.myself.springdemo.framework.webmvc.servlet;

import com.myself.springdemo.framework.annotation.Controller;
import com.myself.springdemo.framework.annotation.RequestMapping;
import com.myself.springdemo.framework.annotation.RequestParam;
import com.myself.springdemo.framework.context.ApplicationContext;
import com.myself.springdemo.framework.webmvc.HandlerAdapter;
import com.myself.springdemo.framework.webmvc.HandlerMapping;
import com.myself.springdemo.framework.webmvc.ModelAndView;
import com.myself.springdemo.framework.webmvc.ViewResolver;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// MVC启动入口
public class DispatcherServlet extends HttpServlet {
	// private final Map<String, HandlerMapping> handlerMapping = new HashMap();

	// HandlerMapping是SpringMVC中最核心的设计。
	private final List<HandlerMapping> handlerMappings = new ArrayList();

	//
	private final Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap();

	private final List<ViewResolver> viewResolvers = new ArrayList();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "")
					.replaceAll("\\s", "\r\n @MySpringMVCDemo\r"));
		}
	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

		// 根据用户请求的url获得一个handler
		HandlerMapping handler = getHandler(req);
		if (handler == null) {
			resp.getWriter().write("404 Not Nound\r\n @MySpringMVCDemo\r");
		}

		// 通过HandlerMapping获得HandlerAdapter
		HandlerAdapter ha = getHandlerAdapter(handler);

		// 这一步只是调用方法得到返回值
		ModelAndView mv = ha.handle(req, resp, handler);

		// 这一步才是真正的输出
		processDispatchResult(resp, mv);
	}


	private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) throws IOException {
		// 调用viewResolver的resolveView()
		if (mv == null) {
			return;
		}
		if (this.viewResolvers.isEmpty()) {
			return;
		}
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (!mv.getViewName().equals(viewResolver.getViewName())) {
				continue;
			}

			String out = viewResolver.resovleView(mv);
			if (out != null) {
				resp.getWriter().write(out);
				break;
			}
		}
	}

	private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {
		if (this.handlerAdapters.isEmpty()) {
			return null;
		}
		return this.handlerAdapters.get(handlerMapping);
	}

	private HandlerMapping getHandler(HttpServletRequest req) {
		if (this.handlerMappings.isEmpty()) {
			return null;
		}
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");

		for (HandlerMapping handlerMapping : this.handlerMappings) {
			Matcher matcher = handlerMapping.getUrl().matcher(url);
			if (!matcher.matches()) {
				continue;
			}
			return handlerMapping;
		}

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
		// 初始化阶段，将参数的名字或者类型按一定的顺序保存下来
		// 后面用反射调用的时候，传的形参是一个数组
		// 可以通过记录这些参数的位置index，挨个从数组中填值。这样就和参数的顺序无关。
		for (HandlerMapping handlerMapping : this.handlerMappings) {
			// 每个方法有一个参数列表,这里保存的是形参列表
			Map<String, Integer> paramMapping = new HashMap<>();

			// 多个参数，每个参数可以使用多个annotation。所以二维数组
			// 处理命名参数
			Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations();
			for (int i = 0; i < parameterAnnotations.length; i++) {
				for (Annotation annotation : parameterAnnotations[i]) {
					if (annotation instanceof RequestParam) {
						String paramName = ((RequestParam)annotation).value();
						if (!"".equals(paramName)) {
							paramMapping.put(paramName, i);
						}
					}
				}
			}

			// 处理非命名参数（只处理@Request和@Response）
			Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Class<?> parameterType = parameterTypes[i];
				System.out.println(parameterType);
				if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
					paramMapping.put(parameterType.getName(), i);
				}
			}

			this.handlerAdapters.put(handlerMapping, new HandlerAdapter(paramMapping));
		}
	}

	private void initHandlerMappings(ApplicationContext context) {
		// 从容器中取到所有的实例
		String[] beanDefinitionNames = context.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			Object instance = context.getBean(beanDefinitionName);
			Class<?> aClass = instance.getClass();
			if (!aClass.isAnnotationPresent(Controller.class)) {
				continue;
			}

			String baseUrl = "";
			if (aClass.isAnnotationPresent(RequestMapping.class)) {
				baseUrl = aClass.getAnnotation(RequestMapping.class).value();
			}

			// 扫描所有的public方法
			Method[] methods = aClass.getMethods();
			for (Method method : methods) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}
				String regex = ("/" + baseUrl + method.getAnnotation(RequestMapping.class).value())
						// /+表示一个或多个/
						.replaceAll("/+", "/");
				Pattern pattern = Pattern.compile(regex);
				this.handlerMappings.add(new HandlerMapping(instance, method, pattern));
				System.out.println("Mapping" + regex + "," + method);
			}
		}
	}

	private void initViewResolvers(ApplicationContext context) {
		// 在页面http://localhost/first.html
		// 解决页面名字和模版文件关联的问题
		String templateRoot = context.getConfig().getProperty("templateRoot");
		String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
		File file = new File(templateRootPath);
		for (File template : file.listFiles()) {
			viewResolvers.add(new ViewResolver(template.getName(), template));
		}
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
