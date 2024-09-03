package com.litongjava.jfinal.dubbo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.litongjava.jfinal.aop.AopManager;

public class Dubbo {

  private static final Map<Class<?>, Object> dubboCache = new ConcurrentHashMap<>();
  private static final Map<Class<?>, ReferenceConfig<?>> referenceMap = new ConcurrentHashMap<>();

  private static ApplicationConfig applicationConfig;
  private static RegistryConfig registryConfig;
  private static int defaultTimeout;

  public static void initialize(ApplicationConfig appConfig, RegistryConfig regConfig, int timeout) {
    applicationConfig = appConfig;
    registryConfig = regConfig;
    defaultTimeout = timeout;
  }

  public static ApplicationConfig getApplicationConfig() {
    return applicationConfig;
  }

  public static RegistryConfig getRegistryConfig() {
    return registryConfig;
  }

  @SuppressWarnings("unchecked")
  public static <T> ReferenceConfig<T> getReference(Class<T> targetClass) {
    return (ReferenceConfig<T>) referenceMap.get(targetClass);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> targetClass) {
    Object ret = dubboCache.get(targetClass);
    if (ret != null) {
      return (T) ret;
    }

    synchronized (Dubbo.class) {
      ret = dubboCache.get(targetClass);
      if (ret != null) {
        return (T) ret;
      }

      ReferenceConfig<T> reference = new ReferenceConfig<>();
      reference.setInterface(targetClass);
      reference.setApplication(applicationConfig);
      reference.setRegistry(registryConfig);
      reference.setTimeout(defaultTimeout);

      referenceMap.put(targetClass, reference);

      T result = reference.get();
      if (result != null) {
        AopManager.me().addSingletonObject(targetClass, result);
        dubboCache.put(targetClass, result);
      }

      return result;
    }
  }

  public static void clear() {
    referenceMap.clear();
    dubboCache.clear();
  }
}
