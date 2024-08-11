package com.litongjava.jfinal.dubbo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

public class DubboProvider {
  private static ApplicationConfig applicationConfig;
  private static RegistryConfig registryConfig;
  private static ProviderConfig providerConfig;
  private static List<ServiceConfig<?>> serviceConfigList = new ArrayList<>();

  public static void init(ApplicationConfig applicationConfig, RegistryConfig registryConfig, ProviderConfig providerConfig) {
    DubboProvider.applicationConfig = applicationConfig;
    DubboProvider.registryConfig = registryConfig;
    DubboProvider.providerConfig = providerConfig;
  }

  public static void export() {
    for (ServiceConfig<?> serviceConfig : serviceConfigList) {
      serviceConfig.export();
    }

  }

  public static void unexport() {
    for (ServiceConfig<?> serviceConfig : serviceConfigList) {
      serviceConfig.unexport();
    }
  }

  /**
   * add
   * @param <T>
   * @param clazz
   * @param impl
   * @return
   */
  public static <T> ServiceConfig<T> add(Class<T> clazz, T impl) {
    ServiceConfig<T> serviceConfig = new ServiceConfig<>();
    serviceConfig.setInterface(clazz);
    serviceConfig.setRef(impl);
    serviceConfig.setApplication(applicationConfig);
    serviceConfig.setRegistry(registryConfig);
    serviceConfig.setProvider(providerConfig);
    serviceConfigList.add(serviceConfig);
    return serviceConfig;
  }

}
