package com.litongjava.jfinal.proxy;

/**
 * Proxy
 */
public class Proxy {

  static ProxyFactory proxyFactory = new ProxyFactory();

  /**
   * 获取代理对象
   * @param target 被代理的类
   * @return 代理对象
   */
  public static <T> T get(Class<T> target) {
    if (proxyFactory == null) {
      proxyFactory = new ProxyFactory();
    }
    return proxyFactory.get(target);
  }

  public static void clean() {
    proxyFactory = null;
  }
}
