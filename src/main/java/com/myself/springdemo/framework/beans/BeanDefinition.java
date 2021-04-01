package com.myself.springdemo.framework.beans;

// 存储文件中的信息
// 保存在内存中的配置
public class BeanDefinition {
	private final boolean lazyInit = false;
	private String beanClassName;
	private String factoryBeanName;

	public String getBeanClassName() {
		return beanClassName;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}
}
