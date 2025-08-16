package com.litongjava.jfinal.aop;

import org.junit.Test;

public class AopTest {

  @Test
  public void test() {
    TestServices testServices = Aop.get(TestServices.class);
    testServices.print();
  }

  @Test
  public void addFetchAnnotation() {
    // Aop.addFetchAnnotation(Autowired.class);
//    Aop.addFetchBeanAnnotations(Autowired.class,Resource.class);
  }
}
