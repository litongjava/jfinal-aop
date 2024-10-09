package com.litongjava.jfinal.aop;

/**
 * Interceptor.
 */
public interface AopInterceptor {
  void intercept(Invocation inv);
}
