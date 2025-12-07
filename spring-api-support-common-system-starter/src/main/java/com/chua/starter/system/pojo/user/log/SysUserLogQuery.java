package com.chua.starter.service.pojo.user.log;

import com.chua.starter.system.pojo.DateQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 鐢ㄦ埛鏃ュ織鏌ヨ
 * @author CH
 * @since 2024/8/23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "鐢ㄦ埛鏃ュ織鏌ヨ")
public class SysUserLogQuery extends DateQuery {
    /**
     * 璐﹀彿
     */
    @Schema(description="璐﹀彿")
    @Size(max = 255,message = "璐﹀彿鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogUsername;

    /**
     * 璇锋眰妯″潡
     */
    @Schema(description = "璇锋眰妯″潡")
    private String sysLogFrom;

    /**
     * 璇锋眰鍦板潃
     */
    @Schema(description = "璇锋眰鍦板潃")
    private String sysLogAddress;

    /**
     * 璇锋眰IP
     */
    @Schema(description = "璇锋眰IP")
    private String sysLogIp;

    /**
     * 璇锋眰鐘舵€; 0: 澶辫触; 1: 鎴愬姛
     */
    @Schema(description = "璇锋眰鐘舵€; 0: 澶辫触; 1: 鎴愬姛")
    private Integer sysLogStatus;

}
