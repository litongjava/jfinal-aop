package com.litongjava.jfinal.aop.process;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.litongjava.jfinal.aop.annotation.AComponent;
import com.litongjava.jfinal.aop.annotation.AController;
import com.litongjava.jfinal.aop.annotation.AHttpApi;
import com.litongjava.jfinal.aop.annotation.ARepository;
import com.litongjava.jfinal.aop.annotation.AService;

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
    boolean isComponent = clazz.isAnnotationPresent(AComponent.class)
        //
        || clazz.isAnnotationPresent(AController.class)
        //
        || clazz.isAnnotationPresent(AService.class)
        //
        || clazz.isAnnotationPresent(ARepository.class)
        //
        || clazz.isAnnotationPresent(AHttpApi.class);
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
