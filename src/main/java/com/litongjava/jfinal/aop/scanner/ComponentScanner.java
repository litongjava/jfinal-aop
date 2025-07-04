package com.litongjava.jfinal.aop.scanner;

import java.util.List;

public interface ComponentScanner {
  
  public List<Class<?>> scan(Class<?>[] primarySources, boolean printScannedClasses) throws Exception;

}
