package com.chua.starter.system.pojo.role;

import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author CH
 * @since 2024/8/8
 */
@Data
@Schema(description = "瑙掕壊鑿滃崟")
public class RoleMenuUpdate {

    /**
     * 瑙掕壊ID
     */
    @NotNull(message = "瑙掕壊ID涓嶈兘涓虹┖", groups = {UpdateGroup.class, SelectGroup.class})
    @Schema(description = "瑙掕壊ID")
    private Integer roleId;

    /**
     * 鑿滃崟ID
     */
    @NotNull(message = "鑿滃崟ID涓嶈兘涓虹┖", groups = {UpdateGroup.class})
    @Schema(description = "鑿滃崟ID")
    private List<Integer> menuId;
}
