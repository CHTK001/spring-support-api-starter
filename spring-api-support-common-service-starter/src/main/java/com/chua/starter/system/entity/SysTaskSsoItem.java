package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import com.google.common.io.Files;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @since 2024/12/4
 * @author CH    
 */

/**
 * sso任务表
 */
@Schema(description="sso任务子表")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_task_sso_item")
public class SysTaskSsoItem extends SysBase {
    @TableId(value = "sys_task_sso_item_id", type = IdType.AUTO)
    @Schema(description="")
    private Integer sysTaskSsoItemId;
    /**
     * 顺序
     */
    @TableField(value = "sys_task_sso_item_seq")
    @Schema(description="序号")
    private Integer sysTaskSsoItemSeq;

    /**
     * 任务ID
     */
    @TableField(value = "sys_task_sso_id")
    @Schema(description="任务ID")
    private Integer sysTaskSsoId;
    /**
     * 版本
     */
    @TableField(value = "sys_task_sso_item_version")
    @Schema(description="版本")
    @Version
    private Integer sysTaskSsoItemVersion;

    /**
     * 任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常
     */
    @TableField(value = "sys_task_sso_item_status")
    @Schema(description="任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常")
    private Integer sysTaskSsoItemStatus;


    /**
     * 任务md5
     */
    @TableField(value = "sys_task_sso_item_md5")
    private String sysTaskSsoItemMd5;

    /**
     * 子任务本地路径
     */
    @TableField(value = "sys_task_sso_item_local_path")
    @Schema(description="子任务本地路径")
    private String sysTaskSsoItemLocalPath;

    public void transferFrom(MultipartFile multipartFile) throws Exception{
        File file = new File(sysTaskSsoItemLocalPath);
        FileUtils.forceMkdirParent(file);
        try (InputStream inputStream = multipartFile.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file);
        ) {
            IoUtils.copy(inputStream, fileOutputStream);
        }
    }

    /**
     * 拷贝文件
     * @return
     * @throws IOException
     */
    public byte[] copyByteArray() throws IOException {
        return Files.toByteArray(new File(sysTaskSsoItemLocalPath));
    }
}