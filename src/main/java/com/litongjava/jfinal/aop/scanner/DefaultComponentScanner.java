package com.litongjava.jfinal.aop.scanner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.litongjava.annotation.AComponentScan;
import com.litongjava.annotation.AComponentScan.Filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultComponentScanner {

  public List<Class<?>> scan(Class<?>[] primarySources, boolean printScannedClasses) throws Exception {
    List<Class<?>> classes = new ArrayList<>();
    List<Filter> allExcludeFilters = new ArrayList<>();
    List<String> allBasePackages = new ArrayList<>();

    for (Class<?> primarySource : primarySources) {
      // 获取注解值
      AComponentScan componentScan = primarySource.getAnnotation(AComponentScan.class);
      if (componentScan != null) {
        // 添加排除的过滤器
        for (Filter filter : componentScan.excludeFilters()) {
          allExcludeFilters.add(filter);
        }

        // 添加基础包
        String[] basePackages = componentScan.value();
        if (basePackages == null || basePackages.length == 0 || (basePackages.length == 1 && basePackages[0].isEmpty())) {
          basePackages = new String[] { primarySource.getPackage().getName() };
        }
        for (String basePackage : basePackages) {
          allBasePackages.add(basePackage);
        }
      }
    }

    for (String basePackage : allBasePackages) {
      classes.addAll(findClasses(basePackage, allExcludeFilters.toArray(new Filter[0]), printScannedClasses));
    }

    return classes;
  }

  private List<Class<?>> findClasses(String basePackage, Filter[] excludeFilters, boolean printScannedClasses) throws Exception {
    List<Class<?>> classes = new ArrayList<>();
    String path = basePackage.replace('.', '/');
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = contextClassLoader.getResources(path);
    List<URL> findedResource = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if (findedResource.contains(resource)) {
        continue;
      }
      findedResource.add(resource);
      URLConnection connection = resource.openConnection();
      log.info("resource:{}", resource);
      if (connection instanceof JarURLConnection) {
        // 处理 Jar 文件
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
        File classRootDirctory = new File(resource.getFile());
        Queue<File> directories = new LinkedList<>();
        directories.add(classRootDirctory);

        while (!directories.isEmpty()) {
          File currentDirectory = directories.poll();
          for (File file : currentDirectory.listFiles()) {
            if (file.isDirectory()) {
              directories.add(file);
            } else if (file.getName().endsWith(".class")) {
              if (printScannedClasses) {
                log.info("class:{}", file.getName());
              }

              String className = file.getName().substring(0, file.getName().length() - 6);
              String classFullName = null;
              if (currentDirectory != classRootDirctory) {
                String relativePath = currentDirectory.getAbsolutePath().substring(classRootDirctory.getAbsolutePath().length() + 1).replace(File.separatorChar, '.');
                classFullName = basePackage + '.' + relativePath + '.' + className;

              } else {
                classFullName = basePackage + '.' + className;
              }

              try {
                Class<?> clazz = contextClassLoader.loadClass(classFullName);
                classes.add(clazz);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    }

    // 应用排除过滤器
    return applyExcludeFilters(classes, excludeFilters);
  }

  private List<Class<?>> applyExcludeFilters(List<Class<?>> classes, Filter[] excludeFilters) {
    if (excludeFilters == null || excludeFilters.length == 0) {
      return classes; // 没有排除过滤器，返回原始列表
    }

    List<Class<?>> filteredClasses = new ArrayList<>(classes);
    for (Filter filter : excludeFilters) {
      switch (filter.type()) {
      case ASSIGNABLE_TYPE:
        // 对于 ASSIGNABLE_TYPE，移除指定的类
        for (Class<?> excludeClass : filter.value()) {
          filteredClasses.removeIf(clazz -> clazz.equals(excludeClass));
        }
        break;
      case ANNOTATION:
        // 对于 ANNOTATION，移除带有指定注解的类
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> annotation = (Class<? extends Annotation>) filter.value()[0];
        filteredClasses.removeIf(clazz -> clazz.isAnnotationPresent(annotation));
        break;
      case REGEX:
        // 对于 REGEX，移除匹配正则表达式的类
        for (String regex : filter.pattern()) {
          Pattern pattern = Pattern.compile(regex);
          filteredClasses.removeIf(clazz -> pattern.matcher(clazz.getName()).matches());
        }
        break;
      case CUSTOM:
        // 对于 CUSTOM，使用自定义的逻辑
        break;
      default:
        break;
      }
    }
    return filteredClasses;
  }
}
