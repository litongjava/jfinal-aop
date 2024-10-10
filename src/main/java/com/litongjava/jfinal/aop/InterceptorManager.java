/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.litongjava.jfinal.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InterceptorManager.
 * 1：管理控制层、业务层全局拦截器
 * 2：缓存业务层 Class 级拦截器数组。业务层拦截器被整体缓存在 ProxyMethod 中
 * 3：用于创建 Interceptor、组装 Interceptor
 * 4：除手动 new 出来的拦截器以外，其它所有拦截器均为单例
 * 
 * 无法使用 Method 或 Before 对象缓存业务层 Method 级拦截器：
 * 1：不同对象或相同对象获取同一个 Class 中同一个 Method 得到的对象 id 值不相同
 * 2：不同对象获取同一个 method 之上的 Before 得到的对象 id 值不相同
 */
public class InterceptorManager {
	
	public static final AopInterceptor[] NULL_INTERS = new AopInterceptor[0];
	
	// 控制层与业务层全局拦截器
	private AopInterceptor[] globalActionInters = NULL_INTERS;
	private AopInterceptor[] globalServiceInters = NULL_INTERS;
	
	// 单例拦截器
	private final ConcurrentHashMap<Class<? extends AopInterceptor>, AopInterceptor> singletonMap = new ConcurrentHashMap<Class<? extends AopInterceptor>, AopInterceptor>(32, 0.5F);
	
	// 业务层 Class 级别拦截器缓存
	private final ConcurrentHashMap<Class<?>, AopInterceptor[]> serviceClassInters = new ConcurrentHashMap<Class<?>, AopInterceptor[]>(32, 0.5F);
	
	private static final InterceptorManager me = new InterceptorManager();
	
	private InterceptorManager() {}
	
	public static InterceptorManager me() {
		return me;
	}
	
	
	// 缓存业务层 Class 级拦截器
	public AopInterceptor[] createServiceInterceptor(Class<?> serviceClass) {
		AopInterceptor[] result = serviceClassInters.get(serviceClass);
		if (result == null) {
			result = createInterceptor(serviceClass.getAnnotation(AopBefore.class));
			serviceClassInters.put(serviceClass, result);
		}
		return result;
	}
	
	public AopInterceptor[] buildServiceMethodInterceptor(Class<?> serviceClass, Method method) {
		return doBuild(globalServiceInters, NULL_INTERS, createServiceInterceptor(serviceClass), serviceClass, method);
	}
	
	private AopInterceptor[] doBuild(AopInterceptor[] globalInters, AopInterceptor[] routesInters, AopInterceptor[] classInters, Class<?> targetClass, Method method) {
		AopInterceptor[] methodInters = createInterceptor(method.getAnnotation(AopBefore.class));
		
		Class<? extends AopInterceptor>[] clearIntersOnMethod;
		AopClear clearOnMethod = method.getAnnotation(AopClear.class);
		if (clearOnMethod != null) {
			clearIntersOnMethod = clearOnMethod.value();
			if (clearIntersOnMethod.length == 0) {	// method 级 @Clear 且不带参
				return methodInters;
			}
		} else {
			clearIntersOnMethod = null;
		}
		
		Class<? extends AopInterceptor>[] clearIntersOnClass;
		AopClear clearOnClass = targetClass.getAnnotation(AopClear.class);
		if (clearOnClass != null) {
			clearIntersOnClass = clearOnClass.value();
			if (clearIntersOnClass.length == 0) {	// class 级 @clear 且不带参
				globalInters = NULL_INTERS;
				routesInters = NULL_INTERS;
			}
		} else {
			clearIntersOnClass = null;
		}
		
		ArrayList<AopInterceptor> result = new ArrayList<AopInterceptor>(globalInters.length + routesInters.length + classInters.length + methodInters.length);
		for (AopInterceptor inter : globalInters) {
			result.add(inter);
		}
		for (AopInterceptor inter : routesInters) {
			result.add(inter);
		}
		if (clearIntersOnClass != null && clearIntersOnClass.length > 0) {
			removeInterceptor(result, clearIntersOnClass);
		}
		for (AopInterceptor inter : classInters) {
			result.add(inter);
		}
		if (clearIntersOnMethod != null && clearIntersOnMethod.length > 0) {
			removeInterceptor(result, clearIntersOnMethod);
		}
		for (AopInterceptor inter : methodInters) {
			result.add(inter);
		}
		return result.toArray(new AopInterceptor[result.size()]);
	}
	
	private void removeInterceptor(ArrayList<AopInterceptor> target, Class<? extends AopInterceptor>[] clearInters) {
		for (Iterator<AopInterceptor> it = target.iterator(); it.hasNext();) {
			AopInterceptor curInter = it.next();
			if (curInter != null) {
				Class<? extends AopInterceptor> curInterClass = curInter.getClass();
				for (Class<? extends AopInterceptor> ci : clearInters) {
					if (curInterClass == ci) {
						it.remove();
						break;
					}
				}
			} else {
				it.remove();
			}
		}
	}
	
	public AopInterceptor[] createInterceptor(AopBefore beforeAnnotation) {
		if (beforeAnnotation == null) {
			return NULL_INTERS;
		}
		return createInterceptor(beforeAnnotation.value());
	}
	
	public AopInterceptor[] createInterceptor(Class<? extends AopInterceptor>[] interceptorClasses) {
		if (interceptorClasses == null || interceptorClasses.length == 0) {
			return NULL_INTERS;
		}
		
		AopInterceptor[] result = new AopInterceptor[interceptorClasses.length];
		try {
			for (int i=0; i<result.length; i++) {
				result[i] = singletonMap.get(interceptorClasses[i]);
				if (result[i] == null) {
					// 此处不能使用 Aop.get(...)，避免生成代理类
					result[i] = (AopInterceptor)interceptorClasses[i].newInstance();
					if (AopManager.me().isInjectDependency()) {
						Aop.inject(result[i]);
					}
					singletonMap.put(interceptorClasses[i], result[i]);
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void addGlobalActionInterceptor(AopInterceptor... inters) {
		addGlobalInterceptor(true, inters);
	}
	
	public void addGlobalServiceInterceptor(AopInterceptor... inters) {
		addGlobalInterceptor(false, inters);
	}
	
	private synchronized void addGlobalInterceptor(boolean forAction, AopInterceptor... inters) {
		if (inters == null || inters.length == 0) {
			throw new IllegalArgumentException("interceptors can not be null.");
		}
		
		for (AopInterceptor inter : inters) {
			if (inter == null) {
				throw new IllegalArgumentException("interceptor can not be null.");
			}
			if (singletonMap.containsKey(inter.getClass())) {
				throw new IllegalArgumentException("interceptor already exists, interceptor must be singlton, do not create more then one instance of the same Interceptor Class.");
			}
		}
		
		for (AopInterceptor inter : inters) {
			if (AopManager.me().isInjectDependency()) {
				Aop.inject(inter);
			}
			singletonMap.put(inter.getClass(), inter);
		}
		
		AopInterceptor[] globalInters = forAction ? globalActionInters : globalServiceInters;
		AopInterceptor[] temp = new AopInterceptor[globalInters.length + inters.length];
		System.arraycopy(globalInters, 0, temp, 0, globalInters.length);
		System.arraycopy(inters, 0, temp, globalInters.length, inters.length);
		
		if (forAction) {
			globalActionInters = temp;
		} else {
			globalServiceInters = temp;
		}
	}
	
	public java.util.List<Class<?>> getGlobalServiceInterceptorClasses() {
		ArrayList<Class<?>> ret = new ArrayList<>(globalServiceInters.length + 3);
		for (AopInterceptor i : globalServiceInters) {
			ret.add(i.getClass());
		}
		return ret;
	}
}



