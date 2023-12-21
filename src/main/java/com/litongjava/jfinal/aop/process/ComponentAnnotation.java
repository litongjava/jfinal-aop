package com.litongjava.jfinal.aop.process;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.litongjava.jfinal.aop.annotation.Component;
import com.litongjava.jfinal.aop.annotation.Controller;
import com.litongjava.jfinal.aop.annotation.HttpApi;
import com.litongjava.jfinal.aop.annotation.Repository;
import com.litongjava.jfinal.aop.annotation.Service;

/**
 * 允许外部类添加组件注解
 * @author Tong Li
 */
public class ComponentAnnotation {

  private static List<Class<? extends Annotation>> annotations = new ArrayList<>();

  public static Class<?> addComponentAnnotation(Class<? extends Annotation> clazz) {
    annotations.add(clazz);
    return clazz;
  }

  public static List<Class<? extends Annotation>> getAnnotations() {
    return annotations;
  }

  /**
   * 判断clazz是否是一个组件类注解
   * @param clazz
   * @return
   */
  public static boolean isComponent(Class<?> clazz) {
    boolean isComponent = clazz.isAnnotationPresent(Component.class)
        //
        || clazz.isAnnotationPresent(Controller.class)
        //
        || clazz.isAnnotationPresent(Service.class)
        //
        || clazz.isAnnotationPresent(Repository.class)
        //
        || clazz.isAnnotationPresent(HttpApi.class);
    if (isComponent) {
      return isComponent;
    }
    
    for (Class<? extends Annotation> annotation : annotations) {
      boolean annotationPresent = clazz.isAnnotationPresent(annotation);
      if(annotationPresent) {
        return annotationPresent;
      }
    }
    return false;
  }
}
