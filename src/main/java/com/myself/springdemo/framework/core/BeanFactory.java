package com.myself.springdemo.framework.core;

/**
 * 最顶层的接口
 */
public interface BeanFactory {
	// 从IOC容器中根据名字获得一个实例Bean
	Object getBean(String beanName);
}
