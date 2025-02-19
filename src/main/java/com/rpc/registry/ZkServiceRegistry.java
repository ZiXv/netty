package com.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j

public class ZkServiceRegistry {
    private final CuratorFramework client;

    @Autowired
    public ZkServiceRegistry(CuratorFramework client) {
        this.client = client;
    }

    public void registerService(String serviceName, String address) {
        try {
            String servicePath = "/rpc"+serviceName;
            log.info(serviceName);
            String instancePath = ZKPaths.makePath(servicePath, address);
            log.info("开始注册服务: {} -> {}", serviceName, address);
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().creatingParentsIfNeeded().forPath(servicePath);
            }

            client.create().forPath(instancePath);
            log.info("已成功注册服务: {} -> {}", serviceName, address);
        } catch (Exception e) {
            log.error("注册服务失败: {}", serviceName, e);
        }
    }
}
