package com.myself.springdemo.framework.beans;

// 用于做事件监听的
public class BeanPostProcesser {

	public Object postProcesserBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	public Object postProcesserAfterInitialization(Object bean, String beanName) {
		return bean;
	}
}
