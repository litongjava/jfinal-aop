package com.litongjava.jfinal.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AComponent
public @interface AConfiguration {
  @AliasFor(annotation = AComponent.class)
  String value() default "";
}
