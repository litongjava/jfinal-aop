package nexus.io.jfinal.aop;

import org.junit.Test;

import nexus.io.jfinal.aop.Aop;

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
