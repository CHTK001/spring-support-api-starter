package com.chua.starter.monitor.pojo.docker;

import lombok.Data;

import java.util.List;

/**
 * Docker 容器创建请求
 *
 * <p>在基础容器配置上补充 autoStart 等监控端页面需要的扩展字段。</p>
 */
@Data
public class DockerCreateContainerRequest {

    private String imageName;
    private String containerName;
    private List<String> portBindings;
    private List<String> environmentVariables;
    private List<String> volumeBindings;
    private String command;
    private String args;
    private String workingDirectory;
    private Boolean autoRemove;
    private String restartPolicy;
    private String networkMode;
    private Long cpuLimit;
    private Long memoryLimit;
    private Boolean privileged;
    private String user;
    private String hostname;
    private List<String> dnsServers;
    private List<String> extraHosts;
    private List<String> labels;
    private Boolean autoStart;
    private Boolean detached;

    public boolean shouldAutoStart() {
        return autoStart == null || autoStart;
    }
}
