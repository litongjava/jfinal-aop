package com.litongjava.jfinal.aop;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationInterceptorRegistry {

  // 注解类型 -> 拦截器 class 列表
  private final Map<Class<? extends Annotation>, List<Class<? extends AopInterceptor>>> mapping = new ConcurrentHashMap<>();

  private volatile boolean hasMappings = false;

  @SafeVarargs
  public final void register(Class<? extends Annotation> ann, Class<? extends AopInterceptor>... interceptors) {
    mapping.put(ann, Arrays.asList(interceptors));
  }

  public final void register(Class<? extends Annotation> ann, Class<? extends AopInterceptor> interceptor) {
    hasMappings = true;
    mapping.put(ann, Arrays.asList(interceptor));
  }

  public List<Class<? extends AopInterceptor>> get(Class<? extends Annotation> ann) {
    hasMappings = true;
    return mapping.getOrDefault(ann, Collections.emptyList());
  }

  public boolean contains(Class<? extends Annotation> ann) {
    return mapping.containsKey(ann);
  }

  public Set<Class<? extends Annotation>> keys() {
    return mapping.keySet();
  }

  public boolean hasMappings() {
    return hasMappings;
  }
}
