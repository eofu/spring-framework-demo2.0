package com.myself.springdemo.demo.controller;

import com.myself.springdemo.demo.service.DemoService;
import com.myself.springdemo.framework.annotation.AutoWired;
import com.myself.springdemo.framework.annotation.Controller;
import com.myself.springdemo.framework.annotation.RequestMapping;
import com.myself.springdemo.framework.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoController {

	@AutoWired
	private DemoService demoService;

	@RequestMapping("/query.json")
	public String query(HttpServletResponse response, HttpServletRequest request, @RequestParam("name") String name) {
		return demoService.getName(name);
	}

	@RequestMapping("edit.json")
	public void edit(HttpServletResponse response, HttpServletRequest request, Integer id) {

	}
}
