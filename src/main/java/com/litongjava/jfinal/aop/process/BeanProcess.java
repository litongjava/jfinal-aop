package com.litongjava.jfinal.aop.process;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.AopManager;
import com.litongjava.jfinal.aop.Autowired;
import com.litongjava.jfinal.aop.annotation.Bean;
import com.litongjava.jfinal.aop.annotation.Configuration;
import com.litongjava.jfinal.model.DestroyableBean;
import com.litongjava.jfinal.model.MultiReturn;
import com.litongjava.jfinal.model.Pair;

public class BeanProcess {
  // 创建一个队列来存储 process 方法的返回值
  private Queue<Class<?>> componentClass = new LinkedList<>();
  private Queue<Class<?>> configurationClass = new LinkedList<>();

  public Object process(Class<?> clazz, Method method) {
    try {
      // 调用 @Bean 方法
      Object bean = method.invoke(clazz.getDeclaredConstructor().newInstance());

      // 如果 @Bean 注解中定义了 initMethod，调用该方法进行初始化
      Bean beanAnnotation = method.getAnnotation(Bean.class);
      if (!beanAnnotation.initMethod().isEmpty()) {
        Method initMethod = bean.getClass().getMethod(beanAnnotation.initMethod());
        initMethod.invoke(bean);
      }

      Class<?> returnType = method.getReturnType();
      // 将bean添加到容器中，或进行其他操作,
      AopManager.me().addSingletonObject(returnType, bean);
      // 为单例注入依赖以后，再添加为单例供后续使用
      Aop.inject(bean);

      return bean;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void initAnnotation(List<Class<?>> scannedClasses) {
    if (scannedClasses == null) {
      return;
    }
    // 1. 分类为 Configuration类和其他类,先处理Configuration类
    for (Class<?> clazz : scannedClasses) {
      if (clazz.isAnnotationPresent(Configuration.class)) {
        configurationClass.add(clazz);
      }
      if (Aop.isComponent(clazz)) {
        componentClass.add(clazz);
      }
    }

    MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration = this
        .processConfiguration(configurationClass);
    Queue<Object> beans = processConfiguration.getR1();
    List<DestroyableBean> destroyableBeans = processConfiguration.getR2();
    Aop.addDestroyableBeans(destroyableBeans);
    
    this.processAutowired(beans);

    // 处理componment注解
    Queue<Object> componentBeans = this.processComponent(componentClass);
    // 处理autoWird注解
    this.processAutowired(componentBeans);

  }

  public MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration(
      Queue<Class<?>> configurationClass) {
    // 用于存储Bean方法及其类的信息
    List<Pair<Method, Class<?>>> beanMethods = new ArrayList<>();
    for (Class<?> clazz : configurationClass) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Bean.class)) {
          beanMethods.add(new Pair<>(method, clazz));
        }
      }
    }

    // 2. 按照priority对beanMethods排序
    beanMethods.sort(Comparator.comparingInt(m -> m.getKey().getAnnotation(Bean.class).priority()));
    Queue<Object> beans = new LinkedList<>();
    List<DestroyableBean> destroyableBeans = new ArrayList<>();
    // 3. 初始化beans
    for (Pair<Method, Class<?>> beanMethod : beanMethods) {
      Object beanInstance = this.process(beanMethod.getValue(), beanMethod.getKey());
      beans.add(beanInstance);

      Bean beanAnnotation = beanMethod.getKey().getAnnotation(Bean.class);
      if (!beanAnnotation.destroyMethod().isEmpty()) {

        try {
          // 尝试找到销毁方法
          Method destroyMethod = beanMethod.getValue().getMethod(beanAnnotation.destroyMethod());
          destroyableBeans.add(new DestroyableBean(beanInstance, destroyMethod));
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        } catch (SecurityException e) {
          e.printStackTrace();
        }

      }
    }
    // 添加到奥
    return new MultiReturn<Queue<Object>, List<DestroyableBean>, Void>(true, beans, destroyableBeans);

  }

  @SuppressWarnings("unchecked")
  public Queue<Object> processComponent(Queue<Class<?>> componentClass) {
    Queue<Object> componentBeans = new LinkedList<>();
    for (Class<?> clazz : componentClass) {
      Class<?>[] interfaces = clazz.getInterfaces();
      if (interfaces.length > 0) {
        AopManager.me().addMapping((Class<Object>) interfaces[0], (Class<? extends Object>) clazz);
      }
      Object object = Aop.get(clazz);
      componentBeans.add(object);
    }
    return componentBeans;
  }

  private void processAutowired(Queue<Object> beans) {
    for (Object bean : beans) {
      Class<?> clazz = bean.getClass();
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Autowired.class)) {
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
