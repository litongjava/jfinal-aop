package com.litongjava.jfinal.spring;

/**
 * Created by litonglinux@qq.com on 2022/1/20_13:22
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/*
 * SpringConfig工具类
 */
public class SpringBeanContextUtils implements ApplicationContextAware {
  private static ApplicationContext context = null;

  /**
   * 设置applicationContext
   */
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static void setContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static ApplicationContext getContext() {
    if (context == null) {
      return null;
    }
    return context;
  }

  /**
   * 获取bean
   */
  public synchronized static Object getBean(String beanName) {
    return context.getBean(beanName);
  }

  /**
   * 获取bean
   *
   * @param beanName
   * @param clazz
   * @return
   */
  @SuppressWarnings("all")
  public synchronized static <T> T getBean(Class<T> clazz) {
    if (context == null) {
      return null;
    }
    T t = context.getBean(clazz);
    return t;
  }

  /**
   * 获取所有beans
   */
  public static String[] getBeanDefinitionNames() throws Exception {
    if (context == null) {
      throw new Exception("context is null");
    } else {
      return context.getBeanDefinitionNames();
    }
  }
}