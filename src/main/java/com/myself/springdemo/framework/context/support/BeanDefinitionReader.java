package com.myself.springdemo.framework.context.support;

import com.myself.springdemo.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// 对配置文件进行定位、读取、解析
public class BeanDefinitionReader {
	// 配置信息
	private final Properties config = new Properties();

	// 配置的.class类的全路径
	private final List<String> registryBeanClasses = new ArrayList<>();

	private final String SCANNER_PACKAGE = "scannerPackage";

	public BeanDefinitionReader(String... locations) {
		InputStream rs = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
		try {
			config.load(rs);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.doScanner(config.getProperty(SCANNER_PACKAGE));
	}

	public List<String> loadDefinitions() {
		return this.registryBeanClasses;
	}

	// 每注册一个className，就返回一个beanDefinition
	// 对配置信息进行一个包装
	public BeanDefinition registerBean(String className) {
		if (this.registryBeanClasses.contains(className)) {
			BeanDefinition beanDefinition = new BeanDefinition();
			beanDefinition.setBeanClassName(className);
			beanDefinition.setFactoryBeanName(this.toLowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
			return beanDefinition;
		}

		return null;
	}

	// 递归扫描相关联的.class，并保存
	private void doScanner(String packageName) {
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());

		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				doScanner(packageName + "." + file.getName());
			} else {
				registryBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
			}
		}
	}

	private String toLowerFirstCase(String str) {
		char[] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
}
