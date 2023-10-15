# jfinal-aop

来自jfinal,基于jvm的原生aop框架.高效,简洁

```
<dependency>
  <groupId>com.litongjava</groupId>
  <artifactId>jfinal-aop</artifactId>
  <version>1.0.1</version>
</dependency>
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
