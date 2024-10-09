package com.litongjava.jfinal.proxy;

import java.util.Objects;

/**
 * ProxyManager
 */
public class ProxyManager {

  private static final ProxyManager me = new ProxyManager();

  private ProxyManager() {
  }

  public static ProxyManager me() {
    return me;
  }

  public ProxyManager setProxyFactory(ProxyFactory proxyFactory) {
    Objects.requireNonNull(proxyFactory, "proxyFactory can not be null");
    Proxy.proxyFactory = proxyFactory;
    return this;
  }

  public ProxyFactory setPrintGeneratedClassToConsole(boolean printGeneratedClassToConsole) {
    Proxy.proxyFactory.getProxyGenerator().setPrintGeneratedClassToConsole(printGeneratedClassToConsole);
    return Proxy.proxyFactory;
  }

  public ProxyFactory setPrintGeneratedClassToLog(boolean printGeneratedClassToLog) {
    Proxy.proxyFactory.getProxyGenerator().setPrintGeneratedClassToLog(printGeneratedClassToLog);
    return Proxy.proxyFactory;
  }

  public ProxyFactory getProxyFactory() {
    return Proxy.proxyFactory;
  }
}
