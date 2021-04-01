package com.myself.springdemo.framework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})//类上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	String value() default "";
}
