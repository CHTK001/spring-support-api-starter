package com.chua.starter.system.pojo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 閮ㄩ棬VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "閮ㄩ棬VO")
public class SysDeptVO {

    /**
     * 閮ㄩ棬ID
     */
    @Schema(description = "閮ㄩ棬ID")
    private Integer sysDeptId;

    /**
     * 閮ㄩ棬鍚嶇О
     */
    @Schema(description = "閮ㄩ棬鍚嶇О")
    private String sysDeptName;

    /**
     * 閮ㄩ棬缂栫爜
     */
    @Schema(description = "閮ㄩ棬缂栫爜")
    private String sysDeptCode;

    /**
     * 鐖堕儴闂↖D
     */
    @Schema(description = "鐖堕儴闂↖D")
    private Integer sysDeptPid;

    /**
     * 閮ㄩ棬閾捐矾
     */
    @Schema(description = "閮ㄩ棬閾捐矾")
    private String sysDeptTreeId;

    /**
     * 鐘舵€?     */
    @Schema(description = "鐘舵€?)
    private Integer sysDeptStatus;

    /**
     * 瀛愰儴闂?     */
    @Schema(description = "瀛愰儴闂?)
    private List<SysDeptVO> children;
}