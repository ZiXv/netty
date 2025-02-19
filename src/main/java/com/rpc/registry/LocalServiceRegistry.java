package com.rpc.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalServiceRegistry implements ServiceRegistry {
    
    /**
     * 本地缓存：serviceName -> 服务实现对象
     */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 注册本地服务
     */
    public void registerService(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
    }

    @Override
    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }
}
