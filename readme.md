# jfinal-aop

来自jfinal,基于jvm的原生aop框架.高效,简洁

```

```

```
package com.litongjava.jfinal.aop;

import org.junit.Test;

public class AopTest {

  @Test
  public void test() {
    TestServices testServices = Aop.get(TestServices.class);
    testServices.print();
  }
}
```
