package dev.bloco.domain.service;

import dev.bloco.domain.valueobject.Network;

import java.util.List;
import java.util.Map;

/**
 * Domain Service interface for configuration management.
 * Defines the contract for accessing application configuration.
 */
public interface ConfigurationService {

    /**
     * Get all configured networks
     */
    List<Network> getConfiguredNetworks();

    /**
     * Get network configuration by name
     */
    Network getNetworkByName(String name);

    /**
     * Get endpoints for a network
     */
    List<String> getNetworkEndpoints(Network network);

    /**
     * Get all network endpoints
     */
    Map<Network, List<String>> getAllNetworkEndpoints();

    /**
     * Check if a network is enabled
     */
    boolean isNetworkEnabled(Network network);

    /**
     * Get configuration value by key
     */
    String getConfigValue(String key);

    /**
     * Get configuration value with default
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * Get integer configuration value
     */
    int getIntConfigValue(String key, int defaultValue);

    /**
     * Get boolean configuration value
     */
    boolean getBooleanConfigValue(String key, boolean defaultValue);

    /**
     * Get long configuration value
     */
    long getLongConfigValue(String key, long defaultValue);
}