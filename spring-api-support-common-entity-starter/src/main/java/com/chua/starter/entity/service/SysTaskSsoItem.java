package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import com.google.common.io.Files;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SSO任务子项表
 *
 * @author CH
 * @since 2024/12/4
 */
@ApiModel(description = "SSO任务子项表")
@Schema(description = "SSO任务子项表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_task_sso_item")
public class SysTaskSsoItem extends SysBase {

    /**
     * 子项主键ID
     */
    @TableId(value = "sys_task_sso_item_id", type = IdType.AUTO)
    @ApiModelProperty(value = "子项主键ID")
    @Schema(description = "子项主键ID")
    private Integer sysTaskSsoItemId;

    /**
     * 序号
     */
    @TableField(value = "sys_task_sso_item_seq")
    @ApiModelProperty(value = "序号")
    @Schema(description = "序号")
    private Integer sysTaskSsoItemSeq;

    /**
     * 任务ID
     */
    @TableField(value = "sys_task_sso_id")
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private Integer sysTaskSsoId;

    /**
     * 版本号(乐观锁)
     */
    @TableField(value = "sys_task_sso_item_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    @Version
    private Integer sysTaskSsoItemVersion;

    /**
     * 任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常
     */
    @TableField(value = "sys_task_sso_item_status")
    @ApiModelProperty(value = "任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常")
    @Schema(description = "任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常")
    private Integer sysTaskSsoItemStatus;

    /**
     * 任务MD5校验值
     */
    @TableField(value = "sys_task_sso_item_md5")
    @ApiModelProperty(value = "任务MD5校验值")
    @Schema(description = "任务MD5校验值")
    private String sysTaskSsoItemMd5;

    /**
     * 子任务本地路径
     */
    @TableField(value = "sys_task_sso_item_local_path")
    @ApiModelProperty(value = "子任务本地路径")
    @Schema(description = "子任务本地路径")
    private String sysTaskSsoItemLocalPath;

    /**
     * 从上传文件传输到本地路径
     *
     * @param multipartFile 上传文件
     * @throws Exception 传输异常
     */
    public void transferFrom(MultipartFile multipartFile) throws Exception {
        File file = new File(sysTaskSsoItemLocalPath);
        FileUtils.forceMkdirParent(file);
        try (InputStream inputStream = multipartFile.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IoUtils.copy(inputStream, fileOutputStream);
        }
    }

    /**
     * 拷贝文件字节数组
     *
     * @return 文件字节数组
     * @throws IOException 读取异常
     */
    public byte[] copyByteArray() throws IOException {
        return Files.toByteArray(new File(sysTaskSsoItemLocalPath));
    }
}
