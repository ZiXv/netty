package com.rpc;

import com.rpc.api.HelloService;
import com.rpc.client.RpcClient;
import com.rpc.client.RpcClientProxyFactory;
import com.rpc.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;


@SpringBootApplication
@Slf4j
@ComponentScan({"com.*"})
public class RpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcApplication.class, args);
    }
    @Bean
    @DependsOn("rpcClientProxyFactory") //
    public HelloService helloService(RpcClientProxyFactory proxyFactory) {
        return proxyFactory.createProxy(HelloService.class);
    }

}
