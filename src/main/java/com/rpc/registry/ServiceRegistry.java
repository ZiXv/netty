package com.rpc.registry;

public interface ServiceRegistry {
    /**
     * 根据服务名获取本地已注册的服务实例
     * @param serviceName 服务名
     * @return 服务实例对象
     */
    Object getService(String serviceName);
    void registerService(String serviceName, Object serviceImpl);
}
