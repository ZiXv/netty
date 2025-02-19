package com.rpc.registry;

import java.util.List;

public interface ServiceDiscovery {
    /**
     * 获取某个服务的所有可用地址
     * @param serviceName 服务名称
     * @return 地址列表
     */
    List<String> getAllServiceAddresses(String serviceName);
}
