package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 缁勭粐鏈烘瀯
 *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "缁勭粐鏈烘瀯")
@Schema(description = "缁勭粐鏈烘瀯")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept")
public class SysDept extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_dept_id", type = IdType.AUTO)
    @ApiModelProperty(value = "鏈烘瀯ID")
    @Schema(description = "鏈烘瀯ID")
    @NotNull(message = "鏈烘瀯ID涓嶈兘涓虹┖", groups = UpdateGroup.class)
    private Integer sysDeptId;

    /**
     * 鏈烘瀯鍚嶇О
     */
    @TableField(value = "sys_dept_name")
    @ApiModelProperty(value = "鏈烘瀯鍚嶇О")
    @Schema(description = "鏈烘瀯鍚嶇О")
    @Size(max = 255, message = "鏈烘瀯鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysDeptName;

    /**
     * 閮ㄩ棬璐熻矗浜?
     */
    @TableField(value = "sys_dept_principal")
    @ApiModelProperty(value = "閮ㄩ棬璐熻矗浜")
    @Schema(description = "閮ㄩ棬璐熻矗浜")
    @Size(max = 255, message = "閮ㄩ棬璐熻矗浜烘渶澶ч暱搴﹁灏忎簬 255")
    private String sysDeptPrincipal;

    /**
     * 璐熻矗浜鸿仈绯绘柟寮?
     */
    @TableField(value = "sys_dept_contact")
    @ApiModelProperty(value = "璐熻矗浜鸿仈绯绘柟寮")
    @Schema(description = "璐熻矗浜鸿仈绯绘柟寮")
    @Size(max = 255, message = "璐熻矗浜鸿仈绯绘柟寮忔渶澶ч暱搴﹁灏忎簬 255")
    private String sysDeptContact;

    /**
     * 浼樺厛绾?
     */
    @TableField(value = "sys_dept_sort")
    @ApiModelProperty(value = "浼樺厛绾")
    @Schema(description = "浼樺厛绾")
    private Integer sysDeptSort;

    /**
     * 鏈烘瀯缂栫爜
     */
    @TableField(value = "sys_dept_code")
    @ApiModelProperty(value = "鏈烘瀯缂栫爜")
    @Schema(description = "鏈烘瀯缂栫爜")
    @Size(max = 255, message = "鏈烘瀯缂栫爜鏈€澶ч暱搴﹁灏忎簬 255")
    @NotBlank(message = "鏈烘瀯缂栫爜涓嶈兘涓虹┖", groups = {AddGroup.class})
    private String sysDeptCode;

    /**
     * 鐖舵満鏋?
     */
    @TableField(value = "sys_dept_pid")
    @ApiModelProperty(value = "鐖舵満鏋")
    @Schema(description = "鐖舵満鏋")
    private Integer sysDeptPid;

    /**
     * 鏈烘瀯閾捐矾
     */
    @TableField(value = "sys_dept_tree_id")
    @ApiModelProperty(value = "鏈烘瀯閾捐矾")
    @Schema(description = "鏈烘瀯閾捐矾")
    @Size(max = 255, message = "鏈烘瀯閾捐矾鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysDeptTreeId;

    /**
     * 鐘舵€? 鏄惁绂佺敤; 0:鍚敤
     */
    @TableField(value = "sys_dept_status")
    @ApiModelProperty(value = "鐘舵€? 鏄惁绂佺敤; 0:鍚敤")
    @Schema(description = "鐘舵€? 鏄惁绂佺敤; 0:鍚敤")
    private Integer sysDeptStatus;

    /**
     * 澶囨敞
     */
    @TableField(value = "sys_dept_remark")
    @ApiModelProperty(value = "澶囨敞")
    @Schema(description = "澶囨敞")
    private String sysDeptRemark;

    /**
     * 鏁版嵁鏉冮檺绫诲瀷;1:鍏ㄩ儴鍙;2:鏈汉鍙;3:鎵€鍦ㄩ儴闂ㄥ彲瑙4:鎵€鍦ㄩ儴闂ㄥ強瀛愮骇鍙;5:閫夋嫨鐨勯儴闂ㄥ彲瑙"
     */
    @TableField(value = "sys_dept_data_permission")
    @ApiModelProperty(value = "鏁版嵁鏉冮檺绫诲瀷;1:鍏ㄩ儴鍙;2:鏈汉鍙;3:鎵€鍦ㄩ儴闂ㄥ彲瑙4:鎵€鍦ㄩ儴闂ㄥ強瀛愮骇鍙;5:閫夋嫨鐨勯儴闂ㄥ彲瑙")
    @Schema(description = "鏁版嵁鏉冮檺绫诲瀷;1:鍏ㄩ儴鍙;2:鏈汉鍙;3:鎵€鍦ㄩ儴闂ㄥ彲瑙4:鎵€鍦ㄩ儴闂ㄥ強瀛愮骇鍙;5:閫夋嫨鐨勯儴闂ㄥ彲瑙")
    private Integer sysDeptDataPermission;

    /**
     * 鏁版嵁鏉冮檺鍙鐨勯儴闂↖D鍒楄〃
     */
    @TableField(value = "sys_dept_data_permission_dept_id")
    @ApiModelProperty(value = "鏁版嵁鏉冮檺鍙鐨勯儴闂↖D鍒楄〃")
    @Schema(description = "鏁版嵁鏉冮檺鍙鐨勯儴闂↖D鍒楄〃")
    private String sysDeptDataPermissionDeptId;
}