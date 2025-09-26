package com.chua.docker.support.docker;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Docker container configuration class
 * Used to encapsulate all configuration parameters when creating containers
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Data
@Builder
public class DockerContainerConfig {

    /**
     * Image name (including tag)
     */
    private String imageName;

    /**
     * Container name
     */
    private String containerName;

    /**
     * Port binding configuration
     * Key: Host port, Value: Container port
     */
    private Map<String, String> portBindings;

    /**
     * Environment variables list
     * Format: KEY=VALUE
     */
    private List<String> environmentVariables;

    /**
     * Volume binding configuration
     * Key: Host path, Value: Container path
     */
    private Map<String, String> volumeBindings;

    /**
     * Start command
     */
    private String command;

    /**
     * Start arguments
     */
    private String args;

    /**
     * Working directory
     */
    private String workingDirectory;

    /**
     * Whether to automatically remove container (auto remove after container stops)
     */
    private Boolean autoRemove;

    /**
     * Restart policy
     */
    private String restartPolicy;

    /**
     * Network mode
     */
    private String networkMode;

    /**
     * CPU limit
     */
    private Long cpuLimit;

    /**
     * Memory limit (bytes)
     */
    private Long memoryLimit;

    /**
     * Whether to run in privileged mode
     */
    private Boolean privileged;

    /**
     * User ID
     */
    private String user;

    /**
     * Hostname
     */
    private String hostname;

    /**
     * DNS servers list
     */
    private List<String> dnsServers;

    /**
     * Extra hosts mapping
     */
    private List<String> extraHosts;

    /**
     * Labels
     */
    private Map<String, String> labels;
}