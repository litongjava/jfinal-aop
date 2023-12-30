# jfinal-aop

from jfinal, based on jvm's native high-performance aop framework

The package is distributed through Maven Central.
[jfinal-aop](https://central.sonatype.com/artifact/com.litongjava/jfinal-aop),
```
<dependency>
  <groupId>com.litongjava</groupId>
  <artifactId>jfinal-aop</artifactId>
  <version>1.1.7</version>
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
