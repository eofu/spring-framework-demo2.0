package com.myself.springdemo.framework.beans;

import com.myself.springdemo.framework.core.FactoryBean;

public class BeanWrapper extends FactoryBean {

	// 原始的通过反射new出来，要包装起来，存下来
	private final Object originalInstance;
	private final Object wrapperInstance;
	// 会用到观察者模式
	// 1、支持事件响应，会有一个监听
	private BeanPostProcesser beanPostProcesser;

	public BeanWrapper(Object instance) {
		this.wrapperInstance = instance;
		this.originalInstance = instance;
	}

	public Object getWrapperInstance() {
		return wrapperInstance;
	}

	public void setWrapperInstance(BeanPostProcesser beanPostProcesser) {
		this.beanPostProcesser = beanPostProcesser;
	}

	// 返回代理后的class
	// 代理后会变成可能是 $proxy0
	public Class<?> getWrapperClass() {
		return this.getWrapperClass();
	}
}
