package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 服务器管理控制器
 *
 * @author CH
 * @since 2024/12/18
 */
@RestController
@RequestMapping("v1/gen/server")
@Tag(name = "服务器管理")
@RequiredArgsConstructor
@Slf4j
public class MonitorSysGenServerController {

    private final MonitorSysGenServerService monitorSysGenServerService;

    /**
     * 根据主键删除数据
     *
     * @param id 主键M
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "删除数据")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(@Parameter(name = "主键") String id) {
        return monitorSysGenServerService.deleteServer(id);
    }

    /**
     * 根据主键更新数据
     *
     * @param monitorSysGenServer 实体
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "更新数据")
    @PutMapping("update")
    public ReturnResult<Boolean> updateById(@Validated(UpdateGroup.class) @RequestBody MonitorSysGenServer monitorSysGenServer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return monitorSysGenServerService.updateServer(monitorSysGenServer);
    }

    /**
     * 添加数据
     *
     * @param monitorSysGenServer 实体
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "添加数据")
    @PostMapping("save")
    public ReturnResult<MonitorSysGenServer> save(@Validated(AddGroup.class) @RequestBody MonitorSysGenServer monitorSysGenServer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return monitorSysGenServerService.saveServer(monitorSysGenServer);
    }

    /**
     * 分页查询数据
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询数据")
    @GetMapping("page")
    public ReturnPageResult<MonitorSysGenServer> page(Query<MonitorSysGenServer> page, MonitorSysGenServer entity) {
        return PageResultUtils.ok(monitorSysGenServerService.pageFor(page.createPage(), entity));
    }

    /**
     * 测试服务器连接
     *
     * @param id 服务器ID
     * @return 测试结果
     */
    @Operation(summary = "测试服务器连接")
    @GetMapping("test")
    public ReturnResult<Boolean> testConnection(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.testConnection(id);
    }

    /**
     * 连接服务器
     *
     * @param id 服务器ID
     * @return 连接结果
     */
    @Operation(summary = "连接服务器")
    @PostMapping("connect")
    public ReturnResult<Boolean> connect(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.connect(id);
    }

    /**
     * 断开服务器连接
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    @Operation(summary = "断开服务器连接")
    @PostMapping("disconnect")
    public ReturnResult<Boolean> disconnect(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.disconnect(id);
    }

    /**
     * 获取服务器连接状态
     *
     * @param id 服务器ID
     * @return 连接状态
     */
    @Operation(summary = "获取服务器连接状态")
    @GetMapping("status")
    public ReturnResult<Map<String, Object>> getConnectionStatus(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getConnectionStatus(id);
    }

    /**
     * 执行服务器命令
     *
     * @param id 服务器ID
     * @param command 命令
     * @return 执行结果
     */
    @Operation(summary = "执行服务器命令")
    @PostMapping("execute")
    public ReturnResult<String> executeCommand(@Parameter(name = "服务器ID") String id,
                                              @Parameter(name = "命令") @RequestBody String command) {
        return monitorSysGenServerService.executeCommand(id, command);
    }

    /**
     * 获取服务器信息
     *
     * @param id 服务器ID
     * @return 服务器信息
     */
    @Operation(summary = "获取服务器信息")
    @GetMapping("info")
    public ReturnResult<Map<String, Object>> getServerInfo(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getServerInfo(id);
    }

    /**
     * 发送数据到服务器
     *
     * @param id 服务器ID
     * @param data 数据
     * @return 操作结果
     */
    @Operation(summary = "发送数据到服务器")
    @PostMapping("send")
    public ReturnResult<Boolean> sendData(@Parameter(name = "服务器ID") String id,
                                         @Parameter(name = "数据") @RequestBody String data) {
        return monitorSysGenServerService.sendData(id, data);
    }

    /**
     * 调整终端大小
     *
     * @param id 服务器ID
     * @param width 宽度
     * @param height 高度
     * @return 操作结果
     */
    @Operation(summary = "调整终端大小")
    @PostMapping("resize")
    public ReturnResult<Boolean> resizeTerminal(@Parameter(name = "服务器ID") String id,
                                               @Parameter(name = "宽度") int width,
                                               @Parameter(name = "高度") int height) {
        return monitorSysGenServerService.resizeTerminal(id, width, height);
    }

    /**
     * 获取文件列表
     *
     * @param id 服务器ID
     * @param path 路径
     * @return 文件列表
     */
    @Operation(summary = "获取文件列表")
    @GetMapping("files")
    public ReturnResult<Map<String, Object>> listFiles(@Parameter(name = "服务器ID") String id,
                                                       @Parameter(name = "路径") String path) {
        return monitorSysGenServerService.listFiles(id, path);
    }

    /**
     * 上传文件
     *
     * @param id 服务器ID
     * @param localPath 本地路径
     * @param remotePath 远程路径
     * @return 上传结果
     */
    @Operation(summary = "上传文件")
    @PostMapping("upload")
    public ReturnResult<Boolean> uploadFile(@Parameter(name = "服务器ID") String id,
                                           @Parameter(name = "本地路径") String localPath,
                                           @Parameter(name = "远程路径") String remotePath) {
        return monitorSysGenServerService.uploadFile(id, localPath, remotePath);
    }

    /**
     * 下载文件
     *
     * @param id 服务器ID
     * @param remotePath 远程路径
     * @param localPath 本地路径
     * @return 下载结果
     */
    @Operation(summary = "下载文件")
    @PostMapping("download")
    public ReturnResult<Boolean> downloadFile(@Parameter(name = "服务器ID") String id,
                                             @Parameter(name = "远程路径") String remotePath,
                                             @Parameter(name = "本地路径") String localPath) {
        return monitorSysGenServerService.downloadFile(id, remotePath, localPath);
    }

    /**
     * 启用服务器监控
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    @Operation(summary = "启用服务器监控")
    @PostMapping("enable-monitoring")
    public ReturnResult<Boolean> enableMonitoring(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.enableMonitoring(id);
    }

    /**
     * 停用服务器监控
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    @Operation(summary = "停用服务器监控")
    @PostMapping("disable-monitoring")
    public ReturnResult<Boolean> disableMonitoring(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.disableMonitoring(id);
    }

    /**
     * 获取服务器监控状态
     *
     * @param id 服务器ID
     * @return 监控状态
     */
    @Operation(summary = "获取服务器监控状态")
    @GetMapping("monitoring-status")
    public ReturnResult<Map<String, Object>> getMonitoringStatus(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getMonitoringStatus(id);
    }

    /**
     * 手动收集服务器指标
     *
     * @param id 服务器ID
     * @return 收集结果
     */
    @Operation(summary = "手动收集服务器指标")
    @PostMapping("collect-metrics")
    public ReturnResult<MonitorSysGenServerMetrics> collectMetrics(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.collectMetrics(id);
    }

    /**
     * 获取服务器列表（按标签分组）
     *
     * @return 分组的服务器列表
     */
    @Operation(summary = "获取服务器列表（按标签分组）")
    @GetMapping("by-tags")
    public ReturnResult<Map<String, List<MonitorSysGenServer>>> getServersByTags() {
        return monitorSysGenServerService.getServersByTags();
    }

    /**
     * 批量操作服务器
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @Operation(summary = "批量操作服务器")
    @PostMapping("batch-operation")
    public ReturnResult<Map<String, Object>> batchOperation(@RequestBody BatchOperationRequest request) {
        return monitorSysGenServerService.batchOperation(request.getIds(), request.getAction());
    }

    /**
     * 获取服务器统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取服务器统计信息")
    @GetMapping("statistics")
    public ReturnResult<Map<String, Object>> getServerStatistics() {
        return monitorSysGenServerService.getServerStatistics();
    }

    /**
     * 导出服务器配置
     *
     * @param ids 服务器ID列表
     * @return 配置数据
     */
    @Operation(summary = "导出服务器配置")
    @PostMapping("export")
    public ReturnResult<String> exportServerConfig(@RequestBody(required = false) List<String> ids) {
        return monitorSysGenServerService.exportServerConfig(ids);
    }

    /**
     * 导入服务器配置
     *
     * @param configData 配置数据
     * @return 导入结果
     */
    @Operation(summary = "导入服务器配置")
    @PostMapping("import")
    public ReturnResult<Map<String, Object>> importServerConfig(@RequestBody String configData) {
        return monitorSysGenServerService.importServerConfig(configData);
    }

    /**
     * 克隆服务器配置
     *
     * @param request 克隆请求
     * @return 克隆结果
     */
    @Operation(summary = "克隆服务器配置")
    @PostMapping("clone")
    public ReturnResult<MonitorSysGenServer> cloneServer(@RequestBody CloneServerRequest request) {
        return monitorSysGenServerService.cloneServer(request.getSourceId(), request.getTargetName());
    }

    /**
     * 获取Guacamole代理连接URL
     *
     * @param id 服务器ID
     * @return 代理连接URL
     */
    @Operation(summary = "获取Guacamole代理连接URL")
    @GetMapping("proxy/guacamole")
    public ReturnResult<String> getGuacamoleProxyUrl(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getGuacamoleProxyUrl(id);
    }

    /**
     * 测试代理连接
     *
     * @param id 服务器ID
     * @return 测试结果
     */
    @Operation(summary = "测试代理连接")
    @PostMapping("proxy/test")
    public ReturnResult<Boolean> testProxyConnection(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.testProxyConnection(id);
    }

    /**
     * 获取服务器支持的数据上报方式
     *
     * @param id 服务器ID
     * @return 支持的上报方式列表
     */
    @Operation(summary = "获取服务器支持的数据上报方式")
    @GetMapping("report/methods")
    public ReturnResult<List<String>> getSupportedReportMethods(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getSupportedReportMethods(id);
    }

    /**
     * 根据配置查询数据
     *
     * @param id         服务器ID
     * @param queryType  查询类型 (prometheus/sql)
     * @param expression 查询表达式
     * @param timeRange  时间范围参数
     * @return 查询结果
     */
    @Operation(summary = "根据配置查询数据")
    @PostMapping("query/data")
    public ReturnResult<Object> queryDataByConfig(@Parameter(name = "服务器ID") String id,
                                                  @Parameter(name = "查询类型") @RequestParam String queryType,
                                                  @Parameter(name = "查询表达式") @RequestParam String expression,
                                                  @RequestBody Map<String, Object> timeRange) {
        return monitorSysGenServerService.queryDataByConfig(id, queryType, expression, timeRange);
    }

    /**
     * 手动检测服务器信息
     *
     * @param id 服务器ID
     * @return 检测结果
     */
    @Operation(summary = "手动检测服务器信息")
    @PostMapping("detect-info")
    public ReturnResult<Map<String, Object>> detectServerInfo(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.detectServerInfo(id);
    }

    /**
     * 批量检测服务器信息
     *
     * @return 检测结果
     */
    @Operation(summary = "批量检测服务器信息")
    @PostMapping("batch-detect-info")
    public ReturnResult<Map<String, Object>> batchDetectServerInfo() {
        return monitorSysGenServerService.batchDetectServerInfo();
    }

    /**
     * 测试本地IP检测功能
     *
     * @param host 要测试的主机地址
     * @return 检测结果
     */
    @Operation(summary = "测试本地IP检测功能")
    @GetMapping("test-local-ip")
    public ReturnResult<Map<String, Object>> testLocalIpDetection(@Parameter(name = "主机地址") String host) {
        return monitorSysGenServerService.testLocalIpDetection(host);
    }

    /**
     * 更新服务器数据上报配置
     *
     * @param id     服务器ID
     * @param config 上报配置
     * @return 操作结果
     */
    @Operation(summary = "更新服务器数据上报配置")
    @PostMapping("report/config")
    public ReturnResult<Boolean> updateReportConfig(@Parameter(name = "服务器ID") String id,
                                                    @RequestBody ReportConfigRequest config) {
        return monitorSysGenServerService.updateReportConfig(id, config);
    }

    /**
     * 获取服务器数据上报配置
     *
     * @param id 服务器ID
     * @return 上报配置
     */
    @Operation(summary = "获取服务器数据上报配置")
    @GetMapping("report/config")
    public ReturnResult<Map<String, Object>> getReportConfig(@Parameter(name = "服务器ID") String id) {
        return monitorSysGenServerService.getReportConfig(id);
    }

    /**
     * 数据上报配置请求
     */
    public static class ReportConfigRequest {
        private Boolean reportEnabled;
        private String dataReportMethod;
        private String prometheusHost;
        private Integer prometheusPort;
        private String proxyType;
        private String proxyHost;
        private Integer proxyPort;

        // Getters and Setters
        public Boolean getReportEnabled() {
            return reportEnabled;
        }

        public void setReportEnabled(Boolean reportEnabled) {
            this.reportEnabled = reportEnabled;
        }

        public String getDataReportMethod() {
            return dataReportMethod;
        }

        public void setDataReportMethod(String dataReportMethod) {
            this.dataReportMethod = dataReportMethod;
        }

        public String getPrometheusHost() {
            return prometheusHost;
        }

        public void setPrometheusHost(String prometheusHost) {
            this.prometheusHost = prometheusHost;
        }

        public Integer getPrometheusPort() {
            return prometheusPort;
        }

        public void setPrometheusPort(Integer prometheusPort) {
            this.prometheusPort = prometheusPort;
        }

        public String getProxyType() {
            return proxyType;
        }

        public void setProxyType(String proxyType) {
            this.proxyType = proxyType;
        }

        public String getProxyHost() {
            return proxyHost;
        }

        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        public Integer getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
        }
    }

    /**
     * 批量操作请求
     */
    public static class BatchOperationRequest {
        private List<String> ids;
        private String action;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    /**
     * 克隆服务器请求
     */
    public static class CloneServerRequest {
        private String sourceId;
        private String targetName;

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }
    }
}
