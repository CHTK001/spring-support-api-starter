package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.SystemSoftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import java.util.Objects;

/**
 * 系统软件管理(v1) 控制器
 *
 * <p>提供与前端约定的 v1 路由别名，兼容现有前端接口（例如：v1/system/soft/page）。</p>
 *
 * 作者: CH
 * 创建时间: 2025-10-16
 * 版本: 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("v1/system/soft")
@RequiredArgsConstructor
@Tag(name = "系统软件管理(v1)", description = "系统软件 v1 版本兼容接口")
public class SystemSoftV1Controller {

    private final SystemSoftService systemSoftService;

    /**
     * 安装软件请求DTO
     */
    public static class SoftwareInstallRequest {
        private Integer softId;
        private List<Integer> serverIds;
        private String imageTag;
        private String installMethod;
        private String installParams;
        private String command;
        private List<PortMapping> ports;
        private List<EnvironmentVariable> env;
        private List<VolumeMapping> volumes;
        private String networkMode;
        private String restartPolicy;
        private Long memoryLimit;
        private Double cpuLimit;
        private String workingDir;
        private String user;
        private Boolean autoStart;
        private Boolean autoRemove;
        private Boolean privileged;
        private String healthcheck;
        private Integer maxRetries;
        
        // Getters and Setters
        public Integer getSoftId() { return softId; }
        public void setSoftId(Integer softId) { this.softId = softId; }
        
        public List<Integer> getServerIds() { return serverIds; }
        public void setServerIds(List<Integer> serverIds) { this.serverIds = serverIds; }
        
        public String getImageTag() { return imageTag; }
        public void setImageTag(String imageTag) { this.imageTag = imageTag; }
        
        public String getInstallMethod() { return installMethod; }
        public void setInstallMethod(String installMethod) { this.installMethod = installMethod; }
        
        public String getInstallParams() { return installParams; }
        public void setInstallParams(String installParams) { this.installParams = installParams; }
        
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        
        public List<PortMapping> getPorts() { return ports; }
        public void setPorts(List<PortMapping> ports) { this.ports = ports; }
        
        public List<EnvironmentVariable> getEnv() { return env; }
        public void setEnv(List<EnvironmentVariable> env) { this.env = env; }
        
        public List<VolumeMapping> getVolumes() { return volumes; }
        public void setVolumes(List<VolumeMapping> volumes) { this.volumes = volumes; }
        
        public String getNetworkMode() { return networkMode; }
        public void setNetworkMode(String networkMode) { this.networkMode = networkMode; }
        
        public String getRestartPolicy() { return restartPolicy; }
        public void setRestartPolicy(String restartPolicy) { this.restartPolicy = restartPolicy; }
        
        public Long getMemoryLimit() { return memoryLimit; }
        public void setMemoryLimit(Long memoryLimit) { this.memoryLimit = memoryLimit; }
        
        public Double getCpuLimit() { return cpuLimit; }
        public void setCpuLimit(Double cpuLimit) { this.cpuLimit = cpuLimit; }
        
        public String getWorkingDir() { return workingDir; }
        public void setWorkingDir(String workingDir) { this.workingDir = workingDir; }
        
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        
        public Boolean getAutoStart() { return autoStart; }
        public void setAutoStart(Boolean autoStart) { this.autoStart = autoStart; }
        
        public Boolean getAutoRemove() { return autoRemove; }
        public void setAutoRemove(Boolean autoRemove) { this.autoRemove = autoRemove; }
        
        public Boolean getPrivileged() { return privileged; }
        public void setPrivileged(Boolean privileged) { this.privileged = privileged; }
        
        public String getHealthcheck() { return healthcheck; }
        public void setHealthcheck(String healthcheck) { this.healthcheck = healthcheck; }
        
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    }
    
    /**
     * 端口映射配置
     */
    public static class PortMapping {
        private String host;
        private String container;
        private String protocol = "tcp";
        
        // Getters and Setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public String getContainer() { return container; }
        public void setContainer(String container) { this.container = container; }
        
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
    }
    
    /**
     * 环境变量配置
     */
    public static class EnvironmentVariable {
        private String key;
        private String value;
        
        // Getters and Setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
    
    /**
     * 数据卷映射配置
     */
    public static class VolumeMapping {
        private String host;
        private String container;
        private Boolean readOnly = false;
        
        // Getters and Setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public String getContainer() { return container; }
        public void setContainer(String container) { this.container = container; }
        
        public Boolean getReadOnly() { return readOnly; }
        public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }
    }

    /**
     * 获取软件统计信息
     *
     * 方法说明: 提供软件统计数据，包括总数、启用数、禁用数、官方软件数等
     * 入参: 无
     * 出参: ReturnResult<Map<String, Object>> 统计结果
     *
     * @return 软件统计信息
     * @author CH
     * @since 1.0.0
     */
    @Operation(summary = "获取软件统计信息(v1)", description = "获取软件总数、启用数、禁用数、官方软件数等统计数据")
    @GetMapping("/stats")
    public ReturnResult<Map<String, Object>> getSoftwareStats() {
        try {
            log.debug("[v1] 获取软件统计信息");

            // 统计总数
            long totalSoftware = systemSoftService.count();

            // 统计启用数
            QueryWrapper<SystemSoft> enabledWrapper = new QueryWrapper<>();
            enabledWrapper.eq("system_soft_status", 1);
            long enabledSoftware = systemSoftService.count(enabledWrapper);

            // 统计禁用数
            long disabledSoftware = totalSoftware - enabledSoftware;

            // 统计官方软件数
            QueryWrapper<SystemSoft> officialWrapper = new QueryWrapper<>();
            officialWrapper.eq("system_soft_is_official", 1);
            long officialSoftware = systemSoftService.count(officialWrapper);

            // 构建返回结果
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalSoftware", totalSoftware);
            stats.put("enabledSoftware", enabledSoftware);
            stats.put("disabledSoftware", disabledSoftware);
            stats.put("officialSoftware", officialSoftware);

            log.debug("[v1] 获取软件统计信息成功: total={}, enabled={}, disabled={}, official={}",
                    totalSoftware, enabledSoftware, disabledSoftware, officialSoftware);
            return ReturnResult.ok(stats);
        } catch (Exception e) {
            log.error("[v1] 获取软件统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询软件列表
     *
     * 方法说明: 提供与前端约定的分页查询接口，等价于旧接口的功能。
     * 入参: page 当前页, size 每页大小, softCode 软件代码, softName 软件名称, category 分类, status 状态
     * 出参: ReturnResult<IPage<SystemSoft>> 分页结果
     */
    @Operation(summary = "分页查询软件列表(v1)", description = "v1/system/soft/page")
    @GetMapping("/page")
    public ReturnResult<IPage<SystemSoft>> pageForSoft(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String category,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer status) {
        try {
            log.debug("[v1] 分页查询软件列表, page: {}, size: {}, softCode: {}, softName: {}, category: {}, status: {}",
                    page, size, softCode, softName, category, status);

            QueryWrapper<SystemSoft> wrapper = new QueryWrapper<>();
            if (Objects.nonNull(softCode) && !softCode.isEmpty()) {
                wrapper.like("system_soft_code", softCode);
            }
            if (Objects.nonNull(softName) && !softName.isEmpty()) {
                wrapper.like("system_soft_name", softName);
            }
            if (Objects.nonNull(category) && !category.isEmpty()) {
                wrapper.eq("system_soft_category", category);
            }
            if (Objects.nonNull(status)) {
                wrapper.eq("system_soft_status", status);
            }

            IPage<SystemSoft> result = systemSoftService.page(new Page<>(page, size), wrapper);
            log.debug("[v1] 分页查询软件列表成功, count: {}", result.getRecords().size());
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[v1] 分页查询软件列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 安装软件到指定服务器（支持完整配置）
     *
     * @param request 安装请求
     * @return 安装结果
     */
    @Operation(summary = "安装软件(v1)", description = "安装软件到指定服务器，支持完整配置参数")
    @PostMapping("/install")
    public ReturnResult<Map<String, Object>> installSoftware(@RequestBody SoftwareInstallRequest request) {
        try {
            log.debug("[v1] 安装软件, softId: {}, serverIds: {}, imageTag: {}", 
                    request.getSoftId(), request.getServerIds(), request.getImageTag());
            
            if (request.getSoftId() == null) {
                return ReturnResult.error("软件ID不能为空");
            }
            if (request.getServerIds() == null || request.getServerIds().isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            
            // 调用服务层安装方法，传递完整配置
            ReturnResult<String> result = systemSoftService.installSoftwareWithConfig(
                    request.getSoftId(), 
                    request.getServerIds(), 
                    buildInstallConfig(request)
            );
            
            if (result.isOk()) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("operationId", result.getData());
                response.put("message", "安装任务已创建");
                
                log.debug("[v1] 安装软件成功, operationId: {}", result.getData());
                return ReturnResult.ok(response);
            } else {
                log.error("[v1] 安装软件失败: {}", result.getMsg());
                return ReturnResult.error(result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("[v1] 安装软件失败", e);
            return ReturnResult.error("安装失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建安装配置对象
     */
    private Map<String, Object> buildInstallConfig(SoftwareInstallRequest request) {
        Map<String, Object> config = new java.util.HashMap<>();
        
        // 基础配置
        config.put("imageTag", request.getImageTag() != null ? request.getImageTag() : "latest");
        config.put("command", request.getCommand());
        config.put("installMethod", request.getInstallMethod());
        config.put("installParams", request.getInstallParams());
        
        // 端口映射
        if (request.getPorts() != null) {
            List<Map<String, String>> ports = new java.util.ArrayList<>();
            for (PortMapping port : request.getPorts()) {
                if (port.getHost() != null && port.getContainer() != null) {
                    Map<String, String> portMap = new java.util.HashMap<>();
                    portMap.put("host", port.getHost());
                    portMap.put("container", port.getContainer());
                    portMap.put("protocol", port.getProtocol() != null ? port.getProtocol() : "tcp");
                    ports.add(portMap);
                }
            }
            config.put("ports", ports);
        }
        
        // 环境变量
        if (request.getEnv() != null) {
            List<Map<String, String>> envs = new java.util.ArrayList<>();
            for (EnvironmentVariable env : request.getEnv()) {
                if (env.getKey() != null && env.getValue() != null) {
                    Map<String, String> envMap = new java.util.HashMap<>();
                    envMap.put("key", env.getKey());
                    envMap.put("value", env.getValue());
                    envs.add(envMap);
                }
            }
            config.put("env", envs);
        }
        
        // 数据卷映射
        if (request.getVolumes() != null) {
            List<Map<String, Object>> volumes = new java.util.ArrayList<>();
            for (VolumeMapping volume : request.getVolumes()) {
                if (volume.getHost() != null && volume.getContainer() != null) {
                    Map<String, Object> volumeMap = new java.util.HashMap<>();
                    volumeMap.put("host", volume.getHost());
                    volumeMap.put("container", volume.getContainer());
                    volumeMap.put("readOnly", volume.getReadOnly() != null ? volume.getReadOnly() : false);
                    volumes.add(volumeMap);
                }
            }
            config.put("volumes", volumes);
        }
        
        // 网络和资源配置
        config.put("networkMode", request.getNetworkMode());
        config.put("restartPolicy", request.getRestartPolicy());
        config.put("memoryLimit", request.getMemoryLimit());
        config.put("cpuLimit", request.getCpuLimit());
        config.put("workingDir", request.getWorkingDir());
        config.put("user", request.getUser());
        config.put("autoStart", request.getAutoStart() != null ? request.getAutoStart() : true);
        config.put("autoRemove", request.getAutoRemove() != null ? request.getAutoRemove() : false);
        config.put("privileged", request.getPrivileged() != null ? request.getPrivileged() : false);
        config.put("healthcheck", request.getHealthcheck());
        config.put("maxRetries", request.getMaxRetries() != null ? request.getMaxRetries() : 3);
        
        return config;
    }
    
    /**
     * 同步镜像请求DTO
     */
    public static class SyncImagesRequest {
        private List<Integer> serverIds;
        
        // Getters and Setters
        public List<Integer> getServerIds() { return serverIds; }
        public void setServerIds(List<Integer> serverIds) { this.serverIds = serverIds; }
    }
    
    /**
     * 同步镜像：从服务器同步Docker镜像到SystemSoftImage表
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @Operation(summary = "同步镜像(v1)", description = "从指定服务器同步Docker镜像到系统")
    @PostMapping("/sync-images")
    public ReturnResult<Map<String, Object>> syncImages(@RequestBody SyncImagesRequest request) {
        try {
            log.debug("[v1] 同步镜像, serverIds: {}", request.getServerIds());
            
            if (request.getServerIds() == null || request.getServerIds().isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            
            // 调用服务层同步镜像方法
            ReturnResult<Map<String, Object>> result = systemSoftService.syncImagesFromServers(request.getServerIds());
            
            if (result.isOk()) {
                log.debug("[v1] 同步镜像成功, result: {}", result.getData());
                return result;
            } else {
                log.error("[v1] 同步镜像失败: {}", result.getMsg());
                return ReturnResult.error(result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("[v1] 同步镜像失败", e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }
}
