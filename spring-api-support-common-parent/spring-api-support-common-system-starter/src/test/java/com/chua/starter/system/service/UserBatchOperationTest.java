// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysPostMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserLayoutMapper;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserPostMapper;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysMiniMenuService;
import com.chua.starter.system.service.SysUserRoleService;
import com.chua.starter.system.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户批量操作单元测试
 *
 * 测试批量删除和批量状态更新的参数校验和业务逻辑
 *
 * <p>P3C40 去mockito改造: 参数校验类测试使用真实 {@link SysUserServiceImpl};
 * 参数校验路径(空列表/非法状态值)不触碰任何依赖, 构造器传 null 即可。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("用户批量操作测试")
class UserBatchOperationTest {

    private final SysUserServiceImpl sysUserService;

    /**
     * 构造方法，创建 用户批次Operation测试 实例。
     */
    UserBatchOperationTest() {
        // 参数校验路径(空列表/非法状态值)在触碰任何依赖前即提前返回, 构造器传 null
        sysUserService = new SysUserServiceImpl(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null);
    }

    /**
     * 测试：删除批次Empty列出。
     */
    @Test
    @DisplayName("批量删除-空列表应返回非法参数")
    void testDeleteBatchEmptyList() {
        ReturnResult<Boolean> result = sysUserService.deleteBatch(Collections.emptyList());
        assertFalse(result.isSuccess(), "空列表应返回失败");
        assertNotNull(result.getMsg());
    }

    /**
     * 测试：删除批次Null列出。
     */
    @Test
    @DisplayName("批量删除-null列表应返回非法参数")
    void testDeleteBatchNullList() {
        ReturnResult<Boolean> result = sysUserService.deleteBatch(null);
        assertFalse(result.isSuccess(), "null列表应返回失败");
    }

    /**
     * 测试：更新状态批次Empty列出。
     */
    @Test
    @DisplayName("批量状态更新-空列表应返回非法参数")
    void testUpdateStatusBatchEmptyList() {
        ReturnResult<Boolean> result = sysUserService.updateStatusBatch(Collections.emptyList(), 1);
        assertFalse(result.isSuccess(), "空列表应返回失败");
    }

    /**
     * 测试：更新状态批次Invalid状态。
     */
    @Test
    @DisplayName("批量状态更新-非法状态值应返回失败")
    void testUpdateStatusBatchInvalidStatus() {
        List<Long> userIds = Arrays.asList(1L, 2L);
        ReturnResult<Boolean> result = sysUserService.updateStatusBatch(userIds, 99);
        assertFalse(result.isSuccess(), "非法状态值应返回失败");
    }

    /**
     * 测试：更新状态批次Null状态。
     */
    @Test
    @DisplayName("批量状态更新-状态为null应返回失败")
    void testUpdateStatusBatchNullStatus() {
        List<Long> userIds = Arrays.asList(1L, 2L);
        ReturnResult<Boolean> result = sysUserService.updateStatusBatch(userIds, null);
        assertFalse(result.isSuccess(), "null状态应返回失败");
    }
}