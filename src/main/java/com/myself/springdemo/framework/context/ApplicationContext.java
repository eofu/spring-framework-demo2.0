package com.myself.springdemo.framework.context;

import com.myself.springdemo.framework.annotation.AutoWired;
import com.myself.springdemo.framework.annotation.Controller;
import com.myself.springdemo.framework.annotation.Service;
import com.myself.springdemo.framework.beans.BeanDefinition;
import com.myself.springdemo.framework.beans.BeanPostProcesser;
import com.myself.springdemo.framework.beans.BeanWrapper;
import com.myself.springdemo.framework.context.support.BeanDefinitionReader;
import com.myself.springdemo.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements BeanFactory {

	private final String[] configLocations;

	// 保存配置信息
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

	// 用来保证注册式单例的容器
	private final Map<String, Object> beanCacheMap = new HashMap<>();

	// 用来存储所有的被代理过的对象
	private final Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

	private BeanDefinitionReader reader;

	public ApplicationContext(String... locations) {
		this.configLocations = locations;
		this.refresh();
	}

	// 自动化依赖注入
	private void doAutoWired() {
		for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
			String beanName = beanDefinitionEntry.getKey();
			if (!beanDefinitionEntry.getValue().isLazyInit()) {
				getBean(beanName);
			}
		}
	}

	// 通过读取beanDefinition的信息，反射创建一个实例返回。
	// Spring中，不会返回原始对象，会用一个beanWrapper进行一次包装
	// 装饰器模式：
	// 1、保留原来的OOP关系
	// 2、需要对他进行扩展，增强（用来进行AOP准备）
	@Override
	public Object getBean(String beanName) {
		BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
		String className = beanDefinition.getBeanClassName();

		// 实例化对象，instantionBean是一个实例
		Object instantionBean = instantionBean(beanDefinition);
		if (instantionBean == null) {
			return null;
		}

		// 生成通知事件
		BeanPostProcesser beanPostProcesser = new BeanPostProcesser();

		// 实例初始化以前调用一次
		beanPostProcesser.postProcesserBeforeInitialization(instantionBean, beanName);

		// 将Bean的实例进行一次包装
		BeanWrapper beanWrapper = new BeanWrapper(instantionBean);
		// beanWrapper.setWrapperInstance(beanPostProcesser);
		this.beanWrapperMap.put(beanName, beanWrapper);

		// 实例初始化以后调用一次
		beanPostProcesser.postProcesserAfterInitialization(instantionBean, beanName);

		populateBeanName(beanName, instantionBean);

		// 通过这样调用相当于给我们自己留有了可操作空间
		return this.beanWrapperMap.get(beanName).getWrapperInstance();
	}

	// 传一个BeanDefinition返回一个实例Bean
	private Object instantionBean(BeanDefinition beanDefinition) {
		Object instance;
		String className = beanDefinition.getBeanClassName();
		try {
			// 根据class才能确认一个类是否有实例
			if (this.beanCacheMap.containsKey(className)) {
				instance = this.beanCacheMap.get(className);
			} else {
				Class<?> aClass = Class.forName(className);
				instance = aClass.newInstance();
				this.beanCacheMap.put(className, instance);
			}
			return instance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void populateBeanName(String beanName, Object instance) {
		Class<?> aClass = instance.getClass();
		if (!(aClass.isAnnotationPresent(Controller.class) || aClass.isAnnotationPresent(Service.class))) {
			return;
		}
		for (Field field : aClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(AutoWired.class)) {
				continue;
			}

			AutoWired annotation = field.getAnnotation(AutoWired.class);
			String autoWiredBeanName = annotation.value().trim();
			if ("".equals(autoWiredBeanName)) {
				autoWiredBeanName = field.getType().getTypeName();
			}

			field.setAccessible(true);

			try {
				field.set(instance, this.beanWrapperMap.get(autoWiredBeanName).getWrapperInstance());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void refresh() {
		// 定位:在properties中放入application.properties的信息，并根据信息递归扫描所配置的包下的.class存入registryBeanClasses
		reader = new BeanDefinitionReader(configLocations);

		// 加载：得到registryBeanClasses
		List<String> definitions = reader.loadDefinitions();

		// 注册
		doRegistry(definitions);

		// 依赖注入（lazy-init=true要执行依赖注入）
		// 在这里调用getBean()
		doAutoWired();
	}

	// 将BeanDefinition注册到IOC容器beanDefinitionMap中
	private void doRegistry(List<String> definitions) {
		try {
			for (String className : definitions) {
				Class<?> beanClass = Class.forName(className);
				// 如果是接口，不能实例化。用实现类来时实例。
				if (beanClass.isInterface()) {
					continue;
				}

				// 根据className对Bean进行包装
				BeanDefinition beanDefinition = reader.registerBean(className);

				// 根据全路径保存beanDefinition信息
				if (beanDefinition != null) {
					this.beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
				}

				// 获取类所实现的类，并覆盖存入beanDefinitionMap
				Class<?>[] interfaces = beanClass.getInterfaces();
				for (Class<?> anInterface : interfaces) {
					// 如果是多个实现类，只能覆盖。
					// 这个情况可以自定义名字
					this.beanDefinitionMap.put(anInterface.getName(), beanDefinition);
				}

				// 到此，容器初始化完成

				// beanName有三种情况：
				// 1、默认是首字母类名小写
				// 2、自定义名字
				// 3、接口注入
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getBeanDefinitionNames() {
		return this.beanDefinitionMap.keySet().// 取出所有的key
				//  Set.toArray()默认返回Object数组，传了new String[]数组则返回String数组，括号内为规定的数组长度
						toArray(new String[this.beanDefinitionMap.size()]);
	}

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public Properties getConfig() {
		return this.reader.getConfig();
	}

}
