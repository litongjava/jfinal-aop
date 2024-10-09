package com.litongjava.jfinal.proxy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProxyClassLoader
 */
public class ProxyClassLoader extends ClassLoader {

  protected Map<String, byte[]> byteCodeMap = new ConcurrentHashMap<>();

  static {
    registerAsParallelCapable();
  }

  public ProxyClassLoader() {
    super(getParentClassLoader());
  }

  protected static ClassLoader getParentClassLoader() {
    ClassLoader ret = Thread.currentThread().getContextClassLoader();
    return ret != null ? ret : ProxyClassLoader.class.getClassLoader();
  }

  public Class<?> loadProxyClass(ProxyClass proxyClass) {
    for (Entry<String, byte[]> e : proxyClass.getByteCode().entrySet()) {
      byteCodeMap.putIfAbsent(e.getKey(), e.getValue());
    }

    try {
      return loadClass(proxyClass.getPkg() + "." + proxyClass.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytes = byteCodeMap.get(name);
    if (bytes != null) {
      Class<?> ret = defineClass(name, bytes, 0, bytes.length);
      byteCodeMap.remove(name);
      return ret;
    }

    return super.findClass(name);
  }
}
