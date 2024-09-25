package com.litongjava.jfinal.aop;

/**
 * Interceptor.
 */
public interface Interceptor {
  void intercept(Invocation inv);
}
