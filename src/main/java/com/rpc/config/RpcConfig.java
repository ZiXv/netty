package com.rpc.config;

import com.rpc.protocol.constant.SerialType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rpc.server")
public class RpcConfig {
    private SerialType serialType;
    private String host;
    private int port;
}
