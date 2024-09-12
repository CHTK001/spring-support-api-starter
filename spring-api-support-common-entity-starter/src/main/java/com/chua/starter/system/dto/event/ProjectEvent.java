package com.chua.starter.system.dto.event;

import com.chua.starter.common.support.project.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 项目事件
 * @author CH
 * @since 2024/9/7
 */
@Data
@Schema(description = "项目事件")
public class ProjectEvent implements Serializable {

    public ProjectEvent() {
        this.applicationName = Project.getInstance().getApplicationName();
        this.applicationPort = Project.getInstance().getApplicationPort();
        this.applicationHost = Project.getInstance().getApplicationHost();
        this.applicationActive = Project.getInstance().getApplicationActive();
        this.applicationActiveInclude = Project.getInstance().getApplicationActiveInclude();
        this.contextPath = Project.getInstance().getContextPath();
        this.endpointsUrl = Project.getInstance().getEndpointsUrl();

    }

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 应用端口
     */
    private Integer applicationPort;

    /**
     * 应用地址
     */
    private String applicationHost;

    /**
     * 应用环境
     */
    private String applicationActive;

    /**
     * 应用环境
     */
    private String applicationActiveInclude;
    /**
     * 上下文路径
     */
    private String contextPath;
    /**
     * 端点地址
     */
    private String endpointsUrl;
}
