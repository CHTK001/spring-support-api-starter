package com.chua.starter.system.pojo.user;

import com.chua.starter.system.model.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "鐢ㄦ埛鏇存柊")
public class SysUserUpdate extends SysUser {


    /**
     * 瑙掕壊ID
     */
    @Schema(description = "瑙掕壊ID")
    private List<String> roleIds;

    /**
     * 鏄惁鏇存柊瑙掕壊
     */
    @Schema(description = "鏄惁鏇存柊瑙掕壊")
    private boolean updateRole;
}
