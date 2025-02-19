package com.rpc.server;

import com.rpc.api.HelloService;

import java.util.concurrent.CompletableFuture;

public class HelloServiceImpl implements HelloService {

    @Override
    public CompletableFuture<String> sayHello(String name) {
        return CompletableFuture.supplyAsync(() -> {
            // 你也可以直接 return CompletableFuture.completedFuture("Hello " + name);
            // 这里用 supplyAsync 举例
            return "Hello " + name; 
        });
    }
}
