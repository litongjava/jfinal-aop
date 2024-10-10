package com.litongjava.jfinal.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Clear is used to clear all interceptors or the specified interceptors,
 * it can not clear the interceptor which declare on method.
 * 
 * <pre>
 * Example:
 * 1: clear all interceptors but InterA and InterB will be kept, because InterA and InterB declare on method
 * @Clear
 * @Before({InterA.class, InterB.class})
 * public void method(...)
 * 
 * 2: clear InterA and InterB, InterC and InterD will be kept
 * @Clear({InterA.class, InterB.class})
 * @Before({InterC.class, InterD.class})
 * public void method(...)
 * </pre>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AopClear {
	Class<? extends AopInterceptor>[] value() default {};
}

