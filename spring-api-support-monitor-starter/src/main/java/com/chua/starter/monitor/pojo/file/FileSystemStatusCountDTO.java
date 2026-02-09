package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件系统状态统计DTO
 * 用于返回各状态文件的数量统计
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "文件系统状态统计")
@Schema(description = "文件系统状态统计")
public class FileSystemStatusCountDTO {

    /**
     * 文件状态：0-待合并，1-合并中，2-已完成，3-合并失败
     */
    @ApiModelProperty(value = "文件状态：0-待合并，1-合并中，2-已完成，3-合并失败")
    @Schema(description = "文件状态：0-待合并，1-合并中，2-已完成，3-合并失败")
    private Integer fileSystemStatus;

    /**
     * 该状态下的文件数量
     */
    @ApiModelProperty(value = "该状态下的文件数量")
    @Schema(description = "该状态下的文件数量")
    private Long count;

    /**
     * 状态名称（用于显示）
     */
    @ApiModelProperty(value = "状态名称")
    @Schema(description = "状态名称")
    private String statusName;

    public FileSystemStatusCountDTO() {
    }

    public Integer getFileSystemStatus() {
        return fileSystemStatus;
    }

    public void setFileSystemStatus(Integer fileSystemStatus) {
        this.fileSystemStatus = fileSystemStatus;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    /**
     * 根据状态码获取状态名称
     */
    public String getStatusName() {
        if (statusName != null) {
            return statusName;
        }
        
        if (fileSystemStatus == null) {
            return "未知";
        }
        
        switch (fileSystemStatus) {
            case 0:
                return "待合并";
            case 1:
                return "合并中";
            case 2:
                return "已完成";
            case 3:
                return "合并失败";
            default:
                return "未知状态(" + fileSystemStatus + ")";
        }
    }
}
