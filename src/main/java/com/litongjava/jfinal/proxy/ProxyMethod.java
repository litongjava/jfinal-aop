package com.litongjava.jfinal.proxy;

import java.lang.reflect.Method;

import com.litongjava.jfinal.aop.AopInterceptor;
import com.litongjava.jfinal.aop.InterceptorManager;

/**
 * ProxyMethod
 * 
 * 在 ProxyFactory 生成、编译、加载代理类彻底完成之后，
 * 再将 ProxyMethod 放入缓存，避免中途出现异常时缓存
 * 不完整的 ProxyMethod 对象
 */
public class ProxyMethod {

  static final InterceptorManager interMan = InterceptorManager.me();

  private Long key;

  private Class<?> targetClass;
  private Class<?> proxyClass;
  private Method method;
  private AopInterceptor[] interceptors = null;

  public void setKey(long key) {
    this.key = key;
  }

  public Long getKey() {
    return key;
  }

  public void setTargetClass(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  public Class<?> getTargetClass() {
    return targetClass;
  }

  /**
   * 代理类在 ProxyFactory 中才被 loadClass，所以本方法在 ProxyFactory 中被调用
   */
  public void setProxyClass(Class<?> proxyClass) {
    this.proxyClass = proxyClass;
  }

  public Class<?> getProxyClass() {
    return proxyClass;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  /**
   * 分离类的生成与对象的创建，避免 ProxyGenerator 与 AopFactory 形成死循环
   * 
   * 本方法仅在 Invocation 构造方法中调用
   */
  public AopInterceptor[] getInterceptors() {
    if (interceptors == null) {
      AopInterceptor[] ret = interMan.buildServiceMethodInterceptor(targetClass, method);
      interceptors = ret;
    }
    return interceptors;
  }
}
