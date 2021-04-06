package com.myself.springdemo.framework.webmvc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 这个类的目的：
// 1、将一个静态文件变成动态文件。
// 2、根据用户传来的参数不同，产生不同的结果。
// 最终输出字符串，交给response输出。
public class ViewResolver {
	private String viewName;
	private File templateFile;

	public ViewResolver(String viewName) {

	}

	public ViewResolver(String viewName, File templateFile) {
		this.viewName = viewName;
		this.templateFile = templateFile;
	}

	public String resovleView(ModelAndView mv) throws IOException {
		StringBuffer sb = new StringBuffer();

		RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");

		String line;
		if (null != (line = ra.readLine())) {
			Matcher matcher = matcher(line);
			while (matcher.find()) {
				for (int i = 1; i < matcher.groupCount(); i++) {
					// 把${}中间的字符串取出来
					String paramName = matcher.group(i);
					Object paramValue = mv.getModel().get(paramName);
					if (paramValue == null) {
						continue;
					}
					line = line.replaceAll("$\\{" + paramName + "\\}", paramValue.toString());
				}
			}
			sb.append(line);
		}

		return sb.toString();
	}

	private Matcher matcher(String str) {
		Pattern pattern = Pattern.compile("$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		return matcher;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public File getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}


}
