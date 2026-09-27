package com.chua.starter.service.pojo.sso;

import com.chua.common.support.base.validator.group.UploadGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * 任务创建响应
 *
 * @param taskId 任务编号
 * @param fileMd5 文件md5
 * @param taskSeq 任务摘要
 * @param fileSize 文件大小
 *
 * @since 2024/12/4
 * @author CH
 */
@Schema(description = "任务创建请求")
public record SysFileUploadTaskPartRequest(
        @Schema(description = "任务ID")
        @NotNull(message = "任务ID不能为空", groups = {UploadGroup.class})
        String taskId,
        @Schema(description = "文件md5") String fileMd5,
        @Schema(description = "任务编号")
        @NotNull(message = "任务编号不能为空", groups = {UploadGroup.class})
        Integer taskSeq,
        @Schema(description = "文件大小")
        @NotNull(message = "文件大小不能为空", groups = {UploadGroup.class})
        long fileSize
) {

    /**
     * 规范构造器：任务标识与任务编号空值敌对。
     *
     * <p>value class 前置条件——语义必填的引用组件不允许为 null。{@code taskId} 与
     * {@code taskSeq} 已由 {@link NotNull} 声明为必填，此处校验是其子集；
     * {@code fileMd5} 未声明必填，可能缺省，故不做校验；{@code fileSize} 为基本类型，
     * 不存在空值问题。</p>
     *
     * @param taskId   任务编号
     * @param fileMd5  文件md5
     * @param taskSeq  任务摘要
     * @param fileSize 文件大小
     */
    public SysFileUploadTaskPartRequest {
        taskId = Objects.requireNonNull(taskId, "taskId 不能为 null");
        taskSeq = Objects.requireNonNull(taskSeq, "taskSeq 不能为 null");
    }
}
