package com.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.utils.ZKPaths;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final CuratorFramework client;

    public ZkServiceDiscovery(CuratorFramework client) {
        this.client = client;
    }

    @Override
    public List<String> getAllServiceAddresses(String serviceName) {
        try {
            String servicePath = "/rpc"+serviceName;
            log.info(serviceName);
            log.info("开始获取服务地址：" + serviceName);
            client.sync().forPath(servicePath);
            if (client.checkExists().forPath(servicePath) == null) {
               log.info("发现服务 " + serviceName + " 不存在于 Zookeeper");
                return List.of();
            }
            // 只返回 IP:PORT，不返回完整路径
            return client.getChildren().forPath(servicePath);
        } catch (Exception e) {
            throw new RuntimeException("获取服务地址失败：" + e.getMessage(), e);
        }
    }
}
