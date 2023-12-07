package com.litongjava.jfinal.aop.scaner;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.litongjava.jfinal.aop.annotation.ComponentScan;

public class ComponentScanner {

  public static List<Class<?>> scan(Class<?>... primarySources) throws Exception {
    List<Class<?>> classes = new ArrayList<>();
    for (Class<?> primarySource : primarySources) {
      // 获取注解值
      ComponentScan componentScan = primarySource.getAnnotation(ComponentScan.class);
      if (componentScan == null) {
        continue;
      }
      String[] basePackages = componentScan.value();

      // 如果未指定包或者为默认值，则从当前包开始扫描
      if (basePackages == null || basePackages.length == 0 || (basePackages.length == 1 && basePackages[0].isEmpty())) {
        basePackages = new String[] { primarySource.getPackage().getName() };
      }

      for (String basePackage : basePackages) {
        classes.addAll(findClasses(basePackage));
      }
    }

    return classes;
  }

  private static List<Class<?>> findClasses(String basePackage) throws Exception {
    List<Class<?>> classes = new ArrayList<>();
    String path = basePackage.replace('.', '/');
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    URL resource = contextClassLoader.getResource(path);

    if (resource == null) {
      throw new IllegalArgumentException("No directory found for package " + basePackage);
    }

    URLConnection connection = resource.openConnection();
    if (connection instanceof JarURLConnection) {
      JarURLConnection jarConnection = (JarURLConnection) connection;
      JarFile jarFile = jarConnection.getJarFile();

      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String entryName = entry.getName();
        if (entryName.startsWith(path) && entryName.endsWith(".class")) {
          String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
          Class<?> clazz = contextClassLoader.loadClass(className);
          classes.add(clazz);
        }
      }
    } else {
      // Handle file system resources, as you did before
      File directory = new File(resource.getFile());
      for (File file : directory.listFiles()) {
        if (file.isDirectory()) {
          classes.addAll(findClasses(basePackage + "." + file.getName()));
        } else if (file.getName().endsWith(".class")) {
          String className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);
          Class<?> clazz = contextClassLoader.loadClass(className);
          classes.add(clazz);
        }
      }
    }

    return classes;
  }
}
