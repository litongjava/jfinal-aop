package com.litongjava.jfinal.aop;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AopTest {

  @Test
  public void test() {
    TestServices testServices = Aop.get(TestServices.class);
    testServices.print();
  }
  
  @Test
  public void addFetchAnnotation() {
    //.addFetchAnnotation(Autowired.class);
    Aop.addFetchBeanAnnotations(Autowired.class,Resource.class);
  }
}
