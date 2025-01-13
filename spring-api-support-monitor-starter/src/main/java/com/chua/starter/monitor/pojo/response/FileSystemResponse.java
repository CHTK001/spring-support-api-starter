package com.chua.starter.monitor.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件系统
 * @author CH
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件系统")
public class FileSystemResponse {

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;


    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String absolutePath;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private FileType fileType;

    /**
     * 是否是叶子节点
     */
    @Schema(description = "是否是叶子节点")
    @JsonProperty("isLeaf")
    private Boolean isLeaf;

    public static enum FileType {

        /**
         * 文件
         */
        FILE,

        /**
         * 目录
         */
        DIRECTORY;
    }
}
