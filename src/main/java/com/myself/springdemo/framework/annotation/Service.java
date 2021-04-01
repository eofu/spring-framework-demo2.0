package com.myself.springdemo.framework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})// 参数上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
	String value() default "";
}
