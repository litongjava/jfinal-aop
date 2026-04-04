package nexus.io.jfinal.aop;

/**
 * Interceptor.
 */
public interface AopInterceptor {
  void intercept(AopInvocation inv);
}
