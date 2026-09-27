package com.chua.starter.monitor.pojo.acme;

import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/**
 * ACME 证书详情 VO
 * 包含证书信息、关联账户信息和订单信息
 *
 * @param certificate 证书信息
 * @param account 关联账户信息
 * @param orders 关联订单列表
 * @param daysUntilExpiry 距离到期天数
 * @param soonExpiring 是否即将到期（30天内）
 * @param curlCommands cURL
 *                     自检命令列表（用于验证HTTP-01挑战是否
 *                     可访问）
 * @param currentStep 当前步骤（用于前端展示申请流程） 1 -
 *                    提交申请（pending） 2 - 等待验证（validating） 3 -
 *                    签发完成（valid）或失败/吐销（失败/revoked/e
 *                    xpired）
 * @param stepDescription 步骤描述（当前步骤的中文描述）
 *
 * @since 2025-12-06
 * @author CH
 */
@ApiModel(description = "ACME 证书详情 VO")
@Schema(description = "ACME 证书详情 VO")
public record AcmeCertificateDetailVo(
        @ApiModelProperty("证书信息")
        @Schema(description = "证书信息")
        MonitorAcmeCertificate certificate,
        @ApiModelProperty("关联账户信息")
        @Schema(description = "关联账户信息")
        MonitorAcmeAccount account,
        @ApiModelProperty("关联订单列表")
        @Schema(description = "关联订单列表")
        List<MonitorAcmeOrder> orders,
        @ApiModelProperty("距离到期天数")
        @Schema(description = "距离到期天数")
        Long daysUntilExpiry,
        @ApiModelProperty("是否即将到期")
        @Schema(description = "是否即将到期")
        Boolean soonExpiring,
        @ApiModelProperty("curl 自检命令列表")
        @Schema(description = "curl 自检命令列表，用于验证HTTP-01挑战是否可从外网访问")
        java.util.List<String> curlCommands,
        @ApiModelProperty("当前步骤: 1-提交申请, 2-等待验证, 3-签发完成")
        @Schema(description = "当前步骤: 1-提交申请, 2-等待验证, 3-签发完成")
        Integer currentStep,
        @ApiModelProperty("步骤描述")
        @Schema(description = "当前步骤的中文描述")
        String stepDescription
) {
    /**
     * 规范构造器：对关联订单列表与 cURL 自检命令列表做防御性拷贝，并校验证书信息非空。
     *
     * <p>value class 前置条件——集合组件必须深不可变、规范构造器必须空值敌对。</p>
     *
     * <p>cURL 自检命令列表仅在 HTTP-01 验证阶段生成，其余状态 缺省，
     * 故保留 空 语义；其余可空分量（关联账户、到期天数、当前步骤等）保持原有可空契约。</p>
     *
     * @param certificate     证书信息
     * @param account         关联账户信息
     * @param orders          关联订单列表
     * @param daysUntilExpiry 距离到期天数
     * @param soonExpiring    是否即将到期（30天内）
     * @param curlCommands    cURL 自检命令列表
     * @param currentStep     当前步骤
     * @param stepDescription 步骤描述
     */
    public AcmeCertificateDetailVo {
        certificate = Objects.requireNonNull(certificate, "certificate 不能为 null");
        orders = List.copyOf(Objects.requireNonNull(orders, "orders 不能为 null"));
        curlCommands = curlCommands == null ? null : List.copyOf(curlCommands);
    }
}
