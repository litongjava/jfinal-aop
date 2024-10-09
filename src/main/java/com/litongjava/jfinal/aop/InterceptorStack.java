package com.litongjava.jfinal.aop;

import java.util.ArrayList;
import java.util.List;

/**
 * InterceptorStack.
 */
public abstract class InterceptorStack implements AopInterceptor {

  private AopInterceptor[] inters;
  private List<AopInterceptor> interList;

  public InterceptorStack() {
    config();

    if (interList == null)
      throw new RuntimeException("You must invoke addInterceptors(...) to config your InterceptorStack");

    inters = interList.toArray(new AopInterceptor[interList.size()]);
    interList.clear();
    interList = null;
  }

  protected InterceptorStack addInterceptors(AopInterceptor... interceptors) {
    if (interceptors == null || interceptors.length == 0) {
      throw new IllegalArgumentException("Interceptors can not be null");
    }

    if (interList == null) {
      interList = new ArrayList<AopInterceptor>();
    }

    for (AopInterceptor ref : interceptors) {
      if (AopManager.me().isInjectDependency()) {
        Aop.inject(ref);
      }
      interList.add(ref);
    }

    return this;
  }

  public final void intercept(Invocation inv) {
    new InvocationWrapper(inv, inters).invoke();
  }

  public abstract void config();
}
