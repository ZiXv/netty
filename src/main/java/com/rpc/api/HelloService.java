package com.rpc.api;

import java.util.concurrent.CompletableFuture;

public interface HelloService {
    CompletableFuture<String> sayHello(String name);
}
