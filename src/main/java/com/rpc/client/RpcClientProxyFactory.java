package com.rpc.client;

import com.rpc.client.RpcClient;
import com.rpc.client.RpcClientProxy;
import com.rpc.config.RpcConfig;
import com.rpc.registry.ZkServiceDiscovery;
import com.rpc.serialization.SerializerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class RpcClientProxyFactory {
    @Lazy
    @Autowired
    private  RpcClient rpcClient;
    @Autowired
    private  ZkServiceDiscovery serviceDiscovery;
    @Autowired
    private  RpcConfig rpcConfig;





    public <T> T createProxy(Class<T> interfaceClass) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, serviceDiscovery, rpcConfig);

        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                rpcClientProxy
        );
    }
}
