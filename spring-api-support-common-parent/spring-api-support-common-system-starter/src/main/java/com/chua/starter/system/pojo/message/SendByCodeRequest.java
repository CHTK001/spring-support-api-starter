package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 按模板编码发送消息请求
 *
 * <p>前端发送弹窗使用：按 templateCode 定位模板，按角色(roleKeys)解析接收人邮箱/手机号，
 * 也可手工填 targets 兜底。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Schema(description = "按模板编码发送消息请求")
public record SendByCodeRequest(
        /**
         * 模板编码
         */
        @Schema(description = "模板编码")
        String templateCode,
        /**
         * 接收人(单个邮箱/手机号)，兼容旧调用；优先使用 roleKeys + targets
         */
        @Schema(description = "接收人(单个邮箱/手机号)，兼容旧调用")
        String target,
        /**
         * 角色编码列表(多选)：按角色解析出用户邮箱(EMAIL)/手机号(SMS)
         */
        @Schema(description = "角色编码列表(多选)")
        List<String> roleKeys,
        /**
         * 手工填写的接收人列表(邮箱/手机号兜底)，与角色解析结果合并去重
         */
        @Schema(description = "手工填写的接收人列表(邮箱/手机号兜底)")
        List<String> targets,
        /**
         * 模板变量(替换 ${key} 占位符)
         */
        @Schema(description = "模板变量(替换 ${key} 占位符)")
        Map<String, String> params
) {
}
