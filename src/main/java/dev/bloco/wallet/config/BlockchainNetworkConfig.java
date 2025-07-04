package dev.bloco.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "networks")
public class BlockchainNetworkConfig {

    private Map<String, List<String>> endpoints;

    public Map<String, List<String>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, List<String>> endpoints) {
        this.endpoints = endpoints;
    }
}
