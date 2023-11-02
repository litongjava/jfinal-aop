package com.litongjava.jfinal.aop;

import org.junit.Test;

import com.litongjava.services.AppleService;

public class AopFactoryTest {

  @Test
  public void testGetOnly() {
    // Aop.inject(AppleService.class);
//    Aop.get(AppleService.class);
    AppleService only = AopManager.me().getAopFactory().getOnly(AppleService.class);
    System.out.println(only);

  }

  @Test
  public void testContains() {
    boolean only = AopManager.me().getAopFactory().contains(AppleService.class);
    System.out.println(only);
  }

}
