Aop类的初始化过程
```
Exception in thread "main" java.lang.ExceptionInInitializerError
	at com.litongjava.jfinal.aop.AopFactory.createObject(AopFactory.java:174)
	at com.litongjava.jfinal.aop.AopFactory.doGetSingleton(AopFactory.java:92)
	at com.litongjava.jfinal.aop.AopFactory.doGet(AopFactory.java:66)
	at com.litongjava.jfinal.aop.AopFactory.get(AopFactory.java:49)
	at com.litongjava.jfinal.aop.Aop.get(Aop.java:87)
	at com.litongjava.tio.boot.TioApplication.run(TioApplication.java:17)
	at com.litongjava.tio.boot.TioApplication.run(TioApplication.java:13)
	at com.litongjava.tio.web.hello.HelloApp.main(HelloApp.java:12)
	at java.base@21.0.1/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
Caused by: java.lang.IllegalArgumentException: File not found in CLASSPATH or JAR : "com/litongjava/jfinal/proxy/proxy_class_template.jf"
	at com.jfinal.template.source.ClassPathSource.<init>(ClassPathSource.java:65)
	at com.jfinal.template.source.ClassPathSourceFactory.getSource(ClassPathSourceFactory.java:29)
	at com.jfinal.template.Engine.buildTemplateBySourceFactory(Engine.java:208)
	at com.jfinal.template.Engine.getTemplate(Engine.java:195)
	at com.litongjava.jfinal.proxy.ProxyGenerator.<init>(ProxyGenerator.java:51)
	at com.litongjava.jfinal.proxy.ProxyFactory.<init>(ProxyFactory.java:31)
	at com.litongjava.jfinal.proxy.Proxy.<clinit>(Proxy.java:18)

```