package com.litongjava.jfinal.proxy;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.jfinal.kit.SyncWriteMap;

/**
 * ProxyMethodCache
 */
public class ProxyMethodCache {

  private static final AtomicLong atomicLong = new AtomicLong();
  private static final Map<Long, ProxyMethod> cache = new SyncWriteMap<>(2048, 0.25F);

  public static Long generateKey() {
    return atomicLong.incrementAndGet();
  }

  public static void put(ProxyMethod proxyMethod) {
    Objects.requireNonNull(proxyMethod, "proxyMethod can not be null");
    Objects.requireNonNull(proxyMethod.getKey(), "the key of proxyMethod can not be null");
    if (cache.containsKey(proxyMethod.getKey())) {
      throw new RuntimeException("the key of proxyMethod already exists");
    }

    cache.putIfAbsent(proxyMethod.getKey(), proxyMethod);
  }

  public static ProxyMethod get(Long key) {
    return cache.get(key);
  }

  public static void clean() {
    atomicLong.set(0);
    cache.clear();
  }
}
