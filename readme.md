# jfinal-aop

jfinal-aop 是一个基于 JVM 的高性能 AOP 框架，源自 JFinal AOP 模块，并在此基础上进行了独立演进。  
框架采用 **动态生成 Java 源码并编译代理类** 的方式实现方法拦截，从而避免反射调用带来的性能损耗。

在运行时，jfinal-aop 会为存在拦截器的方法生成代理类，使最终的方法调用接近普通 Java 方法调用的性能。

当前版本 **不支持 AOT（Ahead-of-Time）编译模式**。

---

## Maven 依赖

该包已发布到 Maven Central：

https://central.sonatype.com/artifact/nexus.io/jfinal-aop

```xml
<dependency>
  <groupId>nexus.io</groupId>
  <artifactId>jfinal-aop</artifactId>
  <version>1.3.8</version>
</dependency>
````

---

# 基本使用

## 创建类实例并调用

jfinal-aop 推荐通过 `Aop.get()` 创建对象，而不是直接使用 `new`。

这样框架才能为对象生成代理并启用拦截器。

```java
package nexus.io.jfinal.aop;

import org.junit.Test;

public class AopTest {

  @Test
  public void test() {
    TestServices testServices = Aop.get(TestServices.class);
    testServices.print();
  }
}
```

---

# 切面使用示例

## 1 Cat 类

定义一个普通类，并在方法上使用 `@AopBefore` 指定拦截器。

```java
package com.issues01;

import nexus.io.jfinal.aop.AopBefore;

public class Cat {

  @AopBefore(Aspect1.class)
  public String eat() {
    return "eat chat";
  }
}
```

---

## 2 拦截器实现

拦截器实现 `AopInterceptor` 接口。

在 `intercept` 方法中可以：

* 获取方法信息
* 获取参数
* 控制方法执行
* 修改返回值

```java
package com.issues01;

import nexus.io.jfinal.aop.AopInterceptor;
import nexus.io.jfinal.aop.AopInvocation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class Aspect1 implements AopInterceptor {

  @Override
  public void intercept(AopInvocation invocation) {

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

---

## 3 启动测试

```java
package com.issues01;

import nexus.io.jfinal.aop.Aop;

public class CatMainTest {

  public static void main(String[] args) {
    new CatMainTest().index();
  }

  public void index() {

    String javaVersion = System.getProperty("java.version");
    System.out.println("java-version:" + javaVersion);

    Cat cat = Aop.get(Cat.class);

    String eat = cat.eat();

    System.out.println("result:" + eat);
  }
}
```

---

## 运行输出

```
Generate proxy class "com.issues01.Cat$$EnhancerByJFinal":

package com.issues01;

import nexus.io.jfinal.aop.AopInvocation;

public class Cat$$EnhancerByJFinal extends Cat {

  public String eat() {

    AopInvocation inv = new AopInvocation(this, 1L,
      args -> {
        return Cat$$EnhancerByJFinal.super.eat();
      }
    );

    inv.invoke();

    return inv.getReturnValue();
  }
}
```

拦截器输出：

```
Before Aspect1 invoking
method:public java.lang.String com.issues01.Cat.eat()
methodName:eat
args:[]
target:com.issues01.Cat$$EnhancerByJFinal
invoke:eat chat
After Aspect1 invoking
result:set new value
```

---

# 扫描类并初始化

jfinal-aop 支持组件扫描与依赖注入。

下面示例展示如何扫描组件并自动注入依赖。

---

## DemoService 接口

```java
package com.issues02;

public interface DemoService {

  String Hello();
}
```

---

## DemoServiceImpl 实现类

```java
package com.issues02;

import nexus.io.jfinal.aop.annotation.Service;

@Service
public class DemoServiceImpl implements DemoService {

  public String Hello() {
    return "Hello";
  }
}
```

---

## DemoController

```java
package com.issues02;

import nexus.io.jfinal.aop.Autowired;
import nexus.io.jfinal.aop.annotation.Controller;

@Controller
public class DemoController {

  @Autowired
  private DemoService demoService;

  public String hello() {
    return demoService.Hello();
  }
}
```

---

## DemoApp 启动类

```java
package com.issues02;

import nexus.io.jfinal.aop.Aop;

import java.util.List;

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

---

运行输出：

```
Hello
```

---

# 使用 registerInterceptor 绑定注解与拦截器

jfinal-aop 支持将 **自定义注解与拦截器自动绑定**。

这样业务代码无需直接依赖拦截器类。

例如实现一个事务注解：

```
@ATransactional
```

而不是：

```
@AopBefore(TransactionInterceptor.class)
```

---

## 定义注解

```java
package demo.jooq.tx;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ATransactional {
}
```

---

## 定义事务拦截器

```java
package demo.jooq.tx;

import nexus.io.jfinal.aop.Aop;
import nexus.io.jfinal.aop.AopInterceptor;
import nexus.io.jfinal.aop.AopInvocation;

public class TransactionInterceptor implements AopInterceptor {

  @Override
  public void intercept(AopInvocation inv) {

    TransactionManager txManager = Aop.get(TransactionManager.class);

    txManager.tx(() -> {
      inv.invoke();
    });
  }
}
```

---

## 注册注解拦截器

在系统启动时进行注册：

```java
package demo.jooq.config;

import nexus.io.context.BootConfiguration;
import nexus.io.jfinal.aop.AopInterceptorManager;

import demo.jooq.tx.ATransactional;
import demo.jooq.tx.TransactionInterceptor;

public class JooqBootConfig implements BootConfiguration {

  @Override
  public void config() {

    AopInterceptorManager.me()
        .registerInterceptor(ATransactional.class,
                             TransactionInterceptor.class);
  }
}
```

---

## 使用事务注解

```java
package demo.jooq.service;

import demo.jooq.tx.ATransactional;

public class SystemAdminService {

  @ATransactional
  public void changePassword2(String loginName, String newPassword) {

    systemAdminDao.updatePassword(loginName, newPassword);
  }
}
```

此时框架会自动为该方法应用 `TransactionInterceptor`。

---

# 设计原理

jfinal-aop 的核心思想是：

1. 扫描类方法
2. 判断方法是否存在拦截器
3. 动态生成代理类源码
4. 编译并加载代理类
5. 代理方法通过 `AopInvocation` 执行拦截器链

生成的代理方法类似：

```java
public String test() {

  AopInvocation inv = new AopInvocation(this, methodKey,
    args -> {
      return super.test();
    }
  );

  inv.invoke();

  return inv.getReturnValue();
}
```

由于代理代码是普通 Java 方法调用，因此避免了反射调用带来的性能损耗。

---

# 项目特点

* 基于 JVM 的高性能 AOP
* 动态生成代理类源码
* 避免反射调用
* 支持拦截器链
* 支持组件扫描
* 支持依赖注入
* 支持注解拦截器注册

---

# 注意事项

1. 请使用 `Aop.get()` 创建对象，否则不会生成代理。
2. 拦截器需要实现 `AopInterceptor` 接口。
3. `registerInterceptor` 必须在应用启动阶段执行。
4. 当前版本 **不支持 AOT 编译模式**。

---

# License

Apache License 2.0