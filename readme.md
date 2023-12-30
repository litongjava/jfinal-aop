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
## usage
### 创建类实例并调用
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
### 切面使用
1. `Cat` 类：
这是一个简单的类，包含一个名为 `eat` 的方法。这个方法被 `@Before(Aspect1.class)` 注解修饰，表示在调用 `eat` 方法之前，会先执行 `Aspect1` 类的 `intercept` 方法。

```
package com.issues01;

import com.litongjava.jfinal.aop.Before;

public class Cat {
  @Before(Aspect1.class)
  public String eat() {
    return "eat chat";
  }
}
```
2. `Aspect1` 类：
这是一个拦截器类，实现了 `Interceptor` 接口。在 `intercept` 方法中，它首先打印出 "Before Aspect1 invoking"，然后获取被拦截方法的相关信息，包括方法对象、方法名、参数和目标对象，并打印出这些信息。然后，它调用 `invocation.invoke()` 执行被拦截的方法，并将返回值设置为 "set new value"。最后，打印出 "After Aspect1 invoking"。

```
package com.issues01;

import com.litongjava.jfinal.aop.Interceptor;
import com.litongjava.jfinal.aop.Invocation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;


@Slf4j
public class Aspect1 implements Interceptor {

  @Override
  public void intercept(Invocation invocation) {
    System.out.println("Before Aspect1 invoking");
    Method method = invocation.getMethod();
    String methodName = invocation.getMethodName();
    Object[] args = invocation.getArgs();
    Object target = invocation.getTarget();
    log.info("method:{}", method);
    log.info("methodName:{}", methodName);
    log.info("args:{}", Arrays.toString(args));
    log.info("target:{}", target);
    Object invoke = invocation.invoke();
    invocation.setReturnValue("set new value");
    log.info("invoke:{}", invoke);

    System.out.println("After Aspect1 invoking");
  }
}
```

3. `CatMainTest` 类：
这是主类，包含 `main` 方法和 `index` 方法。在 `main` 方法中运行 `index` 方法。在 `index` 方法中，它首先打印出 Java 版本，然后获取 `Cat` 类的实例，并调用 `eat` 方法，最后打印出 `eat` 方法的返回值。

```
package com.issues01;

import com.litongjava.jfinal.aop.Aop;


public class CatMainTest {

  public static void main(String[] args) {
    new CatMainTest().index();
    //SimpleApp.run(CatMainTest.class.getName(), "index");
  }

  public void index() {
    String javaVersion = System.getProperty("java.version");
    System.out.println("java-version:" + javaVersion);
    // ProxyManager.me().setProxyFactory(new CglibProxyFactory());
    Cat cat = Aop.get(Cat.class);
    String eat = cat.eat();
    System.out.println("result:" + eat);
  }
}

```
output
```
java-version:1.8.0_121
22:34:33.877 [main] DEBUG com.litongjava.jfinal.proxy.ProxyGenerator - 
Generate proxy class "com.issues01.Cat$$EnhancerByJFinal":
package com.issues01;
import com.litongjava.jfinal.aop.Invocation;
public class Cat$$EnhancerByJFinal extends Cat {
	
	public  java.lang.String eat() {
		Invocation inv = new Invocation(this, 1L,
			args -> {
				return  Cat$$EnhancerByJFinal.super.eat(
					);
				
			}
			);
		
		inv.invoke();
		
		return inv.getReturnValue();
	}
}


Before Aspect1 invoking
22:34:34.250 [main] INFO com.issues01.Aspect1 - method:public java.lang.String com.issues01.Cat.eat()
22:34:34.251 [main] INFO com.issues01.Aspect1 - methodName:eat
22:34:34.251 [main] INFO com.issues01.Aspect1 - args:[]
22:34:34.251 [main] INFO com.issues01.Aspect1 - target:com.issues01.Cat$$EnhancerByJFinal@55a561cf
22:34:34.251 [main] INFO com.issues01.Aspect1 - invoke:eat chat
After Aspect1 invoking
result:set new value
```

### 扫描类并初始化
这段代码主要包含四个部分：`DemoService` 接口，`DemoServiceImpl` 类，`DemoController` 类和 `DemoApp` 类。

1. `DemoService` 接口：
这是一个简单的接口，定义了一个 `Hello` 方法。

```java
package com.issues02;

public interface DemoService {
  public String Hello();
}
```

2. `DemoServiceImpl` 类：
这是 `DemoService` 接口的实现类，它被 `@Service` 注解修饰，表示它是一个服务类。在 `Hello` 方法中，它返回了一个字符串 "Hello"。

```java
package com.issues02;

import com.litongjava.jfinal.aop.annotation.Service;

@Service
public class DemoServiceImpl implements DemoService {

  public String Hello(){
    return "Hello";
  }
}
```

3. `DemoController` 类：
这是一个控制器类，被 `@Controller` 注解修饰。它包含一个 `DemoService` 类型的成员变量 `demoService`，并使用 `@Autowired` 注解进行自动注入。在 `hello` 方法中，它调用了 `demoService` 的 `Hello` 方法，并返回了其结果。

```java
package com.issues02;

import com.litongjava.jfinal.aop.Autowired;
import com.litongjava.jfinal.aop.Inject;
import com.litongjava.jfinal.aop.annotation.Controller;

import java.lang.annotation.Inherited;


@Controller
public class DemoController {

//  @Inject
//  private DemoService demoService;

  @Autowired
  private DemoService demoService;

  public String hello() {
    return demoService.Hello();
  }
}
```

4. `DemoApp` 类：
这是主类，包含 `main` 方法。在 `main` 方法中，它首先扫描 `DemoApp` 类所在的包，然后初始化注解。接着，它获取 `DemoController` 类的实例，并调用 `hello` 方法，最后打印出 `hello` 方法的返回值。

```java
package com.issues02;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.annotation.ComponentScan;
import com.litongjava.jfinal.aop.process.BeanProcess;
import com.litongjava.jfinal.aop.scaner.ComponentScanner;

import java.util.List;

@ComponentScan
public class DemoApp {
  public static void main(String[] args) throws Exception {
    List<Class<?>> scannedClasses = Aop.scan(DemoApp.class);
    Aop.initAnnotation(scannedClasses);

    DemoController demoController = Aop.get(DemoController.class);
    String hello = demoController.hello();
    System.out.println(hello);
    Aop.close();
  }
}

```

output
```
Hello
```