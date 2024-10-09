package com.litongjava.jfinal.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.litongjava.jfinal.proxy.Callback;
import com.litongjava.jfinal.proxy.ProxyMethod;
import com.litongjava.jfinal.proxy.ProxyMethodCache;

/**
 * Invocation is used to invoke the interceptors and the target method
 */
@SuppressWarnings("unchecked")
public class AopInvocation {

  private static final Object[] NULL_ARGS = new Object[0]; // Prevent new Object[0] by jvm for args of method invoking

  private Object target;
  private Method method;
  private Object[] args;
  private Callback callback;
  private AopInterceptor[] inters;
  private Object returnValue;

  private int index = 0;

  public AopInvocation(Object target, Long proxyMethodKey, Callback callback, Object... args) {
    this.target = target;

    ProxyMethod proxyMethod = ProxyMethodCache.get(proxyMethodKey);
    this.method = proxyMethod.getMethod();
    this.inters = proxyMethod.getInterceptors();

    this.callback = callback;
    this.args = args;
  }

  public AopInvocation(Object target, Long proxyMethodKey, Callback callback) {
    this(target, proxyMethodKey, callback, NULL_ARGS);
  }

  /**
   * 用于扩展 ProxyFactory
   */
  public AopInvocation(Object target, Method method, AopInterceptor[] inters, Callback callback, Object[] args) {
    this.target = target;

    this.method = method;
    this.inters = inters;

    this.callback = callback;
    this.args = args;
  }

  // InvocationWrapper need this constructor
  protected AopInvocation() {
  }

  public Object invoke() {
    if (index < inters.length) {
      inters[index++].intercept(this);
    } else if (index++ == inters.length) { // index++ ensure invoke action only one time
      try {
        returnValue = callback.call(args);
      } catch (InvocationTargetException e) {
        Throwable t = e.getTargetException();
        if (t == null) {
          t = e;
        }
        throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    return returnValue;
  }

  public Object getArg(int index) {
    if (index >= args.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return args[index];
  }

  public void setArg(int index, Object value) {
    if (index >= args.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    args[index] = value;
  }

  public Object[] getArgs() {
    return args;
  }

  /**
   * Get the target object which be intercepted
   * <pre>
   * Example:
   * OrderService os = getTarget();
   * </pre>
   */
  public <T> T getTarget() {
    return (T) target;
  }

  /**
   * Return the method of this action.
   * <p>
   * You can getMethod.getAnnotations() to get annotation on action method to do more things
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Return the method name of this action's method.
   */
  public String getMethodName() {
    return method.getName();
  }

  /**
   * Get the return value of the target method
   */
  public <T> T getReturnValue() {
    return (T) returnValue;
  }

  /**
   * Set the return value of the target method
   */
  public void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }
}
