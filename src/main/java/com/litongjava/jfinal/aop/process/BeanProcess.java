package com.litongjava.jfinal.aop.process;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.litongjava.annotation.AAutowired;
import com.litongjava.annotation.AConfiguration;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.model.DestroyableBean;
import com.litongjava.jfinal.model.MultiReturn;

public class BeanProcess {
  // 创建一个队列来存储 process 方法的返回值
  private Queue<Class<?>> componentClass = new LinkedList<>();
  private Queue<Class<?>> configurationClass = new LinkedList<>();

  @SuppressWarnings("unchecked")
  public void initAnnotation(List<Class<?>> scannedClasses) {
    if (scannedClasses == null || scannedClasses.size() < 1) {
      return;
    }
    ConfigurationAnnotaionProcess configurationAnnotaionProcess = new ConfigurationAnnotaionProcess();

    // interface,impl
    Map<Class<Object>, Class<? extends Object>> mapping = new ConcurrentHashMap<>();

    for (Class<?> clazz : scannedClasses) {
      boolean annotationPresent = clazz.isAnnotationPresent(AConfiguration.class);
      if (annotationPresent) {
        configurationClass.add(clazz);
        continue;
      }
      if (Aop.isComponent(clazz)) {
        componentClass.add(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
          mapping.put((Class<Object>) interfaces[0], (Class<? extends Object>) clazz);
        }
      }
    }

    MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration = configurationAnnotaionProcess.processConfiguration(configurationClass, mapping);

    if (processConfiguration != null) {
      List<DestroyableBean> destroyableBeans = processConfiguration.getR2();
      Aop.addDestroyableBeans(destroyableBeans);
    }

    // 处理componment注解
    this.processComponent(componentClass, mapping);

  }

  public Queue<Object> processComponent(Queue<Class<?>> componentClass, Map<Class<Object>, Class<? extends Object>> mapping) {
    Queue<Object> componentBeans = new LinkedList<>();
    for (Class<?> clazz : componentClass) {
      Object object = Aop.get(clazz, mapping);
      componentBeans.add(object);
    }
    return componentBeans;
  }

  @SuppressWarnings("unused")
  private void processAutowired(Queue<Object> beans) {
    for (Object bean : beans) {
      Class<?> clazz = bean.getClass();
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(AAutowired.class)) {
          Object value = Aop.get(field.getType());
          try {
            field.setAccessible(true);
            field.set(bean, value);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
