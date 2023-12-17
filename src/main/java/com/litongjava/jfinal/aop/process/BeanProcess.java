package com.litongjava.jfinal.aop.process;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.AopManager;
import com.litongjava.jfinal.aop.Autowired;
import com.litongjava.jfinal.aop.annotation.Bean;
import com.litongjava.jfinal.aop.annotation.Configuration;
import com.litongjava.jfinal.aop.annotation.Initialization;
import com.litongjava.jfinal.model.DestroyableBean;
import com.litongjava.jfinal.model.MultiReturn;
import com.litongjava.jfinal.model.Pair;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanProcess {
  // 创建一个队列来存储 process 方法的返回值
  private Queue<Class<?>> componentClass = new LinkedList<>();
  private Queue<Class<?>> configurationClass = new LinkedList<>();

  @SuppressWarnings("unchecked")
  public void initAnnotation(List<Class<?>> scannedClasses) {
    if (scannedClasses == null) {
      return;
    }
    // for(int i=0;i<scannedClasses.size();i++) {
    // log.info("{}",scannedClasses.get(i).toString());
    // }
    // interface,impl
    Map<Class<Object>, Class<? extends Object>> mapping = new ConcurrentHashMap<>();
    // 1. 分类为 Configuration类和其他类,先处理Configuration类
    for (Class<?> clazz : scannedClasses) {
      boolean annotationPresent = clazz.isAnnotationPresent(Configuration.class);
      // log.info("{},{}",clazz.toString(),annotationPresent);
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

    MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration = this
        .processConfiguration(configurationClass, mapping);
    // Queue<Object> beans = processConfiguration.getR1();
    List<DestroyableBean> destroyableBeans = processConfiguration.getR2();
    Aop.addDestroyableBeans(destroyableBeans);

    // 处理autoWird注解,Aop框架已经内置改支持
    // this.processAutowired(beans);

    // 处理componment注解
    // Queue<Object> componentBeans = this.processComponent(componentClass);
    this.processComponent(componentClass, mapping);
    //
    // this.processAutowired(componentBeans);

  }

  /**
   * 处理有@Bean注解的方法
   * @param clazz
   * @param method
   * @param mapping 
   * @return
   */
  public Object processConfigBean(Class<?> clazz, Method method, Map<Class<Object>, Class<? extends Object>> mapping) {
    try {
      // 调用 @Bean 方法
      Object object = Aop.get(clazz, mapping);
      Object bean = method.invoke(object);

      // 如果 @Bean 注解中定义了 initMethod，调用该方法进行初始化
      Bean beanAnnotation = method.getAnnotation(Bean.class);
      if (!beanAnnotation.initMethod().isEmpty()) {
        Method initMethod = bean.getClass().getMethod(beanAnnotation.initMethod());
        initMethod.invoke(bean);
      }
      Class<? extends Object> readBeanClass = bean.getClass();
      String beanClassName = readBeanClass.getName();
      log.info("inited config bean:{}", beanClassName);

      Class<?> returnType = method.getReturnType();
      // 将bean添加到容器中，或进行其他操作,
      if (!returnType.getName().equals(readBeanClass.getName())) {
        AopManager.me().addMapping(returnType, readBeanClass);
        log.info("add bean mapping:{} from {}", returnType, beanClassName);
      }
      AopManager.me().addSingletonObject(bean);

      // 为单例注入依赖以后，再添加为单例供后续使用
      Aop.inject(bean);

      return bean;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 处理有@Configuration注解的类
   * @param configurationClass
   * @param mapping 
   * @return
   */
  public MultiReturn<Queue<Object>, List<DestroyableBean>, Void> processConfiguration(
      Queue<Class<?>> configurationClass, Map<Class<Object>, Class<? extends Object>> mapping) {
    // 用于存储Bean方法及其类的信息
    List<Pair<Method, Class<?>>> beanMethods = new ArrayList<>();
    List<Pair<Method, Class<?>>> initializationMethods = new ArrayList<>();
    for (Class<?> clazz : configurationClass) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Bean.class)) {
          beanMethods.add(new Pair<>(method, clazz));
        }
        if (method.isAnnotationPresent(Initialization.class)) {
          initializationMethods.add(new Pair<>(method, clazz));
        }
      }
    }

    // 2. 按照priority对beanMethods排序
    beanMethods.sort(Comparator.comparingInt(m -> m.getKey().getAnnotation(Bean.class).priority()));
    initializationMethods.sort(Comparator.comparingInt(m -> m.getKey().getAnnotation(Bean.class).priority()));
    Queue<Object> beans = new LinkedList<>();
    List<DestroyableBean> destroyableBeans = new ArrayList<>();
    // 3. 初始化beans
    for (Pair<Method, Class<?>> beanMethod : beanMethods) {
      Object beanInstance = this.processConfigBean(beanMethod.getValue(), beanMethod.getKey(), mapping);
      beans.add(beanInstance);

      Bean beanAnnotation = beanMethod.getKey().getAnnotation(Bean.class);
      if (!beanAnnotation.destroyMethod().isEmpty()) {

        try {
          // 尝试找到销毁方法
          Method destroyMethod = beanInstance.getClass().getMethod(beanAnnotation.destroyMethod());
          destroyableBeans.add(new DestroyableBean(beanInstance, destroyMethod));
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        } catch (SecurityException e) {
          e.printStackTrace();
        }

      }
    }
    // 4.初始化 initialization
    for (Pair<Method, Class<?>> beanMethod : initializationMethods) {
      this.processConfigInitialization(beanMethod.getValue(), beanMethod.getKey(), mapping);
    }
    // 返回初始化的Bean和可以销毁的bean
    return new MultiReturn<Queue<Object>, List<DestroyableBean>, Void>(true, beans, destroyableBeans);

  }

  private void processConfigInitialization(Class<?> clazz, Method method,
      Map<Class<Object>, Class<? extends Object>> mapping) {
    // 调用 @Bean 方法
    try {
      // 添加到到bean容器
      method.invoke(Aop.get(clazz, mapping));
      // method.invoke(Aop.get(clazz));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }

  }

  public Queue<Object> processComponent(Queue<Class<?>> componentClass,
      Map<Class<Object>, Class<? extends Object>> mapping) {
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
