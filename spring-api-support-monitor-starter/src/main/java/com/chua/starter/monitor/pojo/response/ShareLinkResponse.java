package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 分享链接响应
 *
 * @author CH
 * @since 2025-12-08
 */
@Data
@Schema(description = "分享链接响应")
public class ShareLinkResponse {

    /**
     * 分享ID
     */
    @Schema(description = "分享ID")
    private String shareId;

    /**
     * 分享链接（带签名的完整 URL）
     */
    @Schema(description = "分享链接")
    private String shareUrl;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private Date expireTime;

    /**
     * 分享类型：SHARE-普通分享，FLASH-闪图
     */
    @Schema(description = "分享类型")
    private String shareType;
}
