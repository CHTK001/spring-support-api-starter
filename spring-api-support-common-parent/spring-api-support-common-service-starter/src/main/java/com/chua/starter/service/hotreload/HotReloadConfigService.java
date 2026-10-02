package com.chua.starter.service.hotreload;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 热重载配置缓存服务。
 *
 * <p>配置值统一以字符串形式缓存到 Redis，避免每次业务调用都查询 sys_setting。
 * 保存热更新配置后由事件桥接器清理对应 key，实现进程内即时热重载。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotReloadConfigService {

    /**
     * 热重载配置缓存名
     */
    public static final String CACHE_NAME = "sys:hot-reload:config";

    /**
     * 缓存全量 key
     */
    public static final String CACHE_KEY_ALL = "all";

    private final SysSettingMapper sysSettingMapper;

    private final SysSettingService sysSettingService;

    /**
     * 按配置编码读取启用中的热重载配置值。
     *
     * <p>⚠️ {@code unless = "#result == null"} 不能省：查不到配置时本方法返回 null，
     * 而 {@code CacheProperties.cacheNullValues} 默认 false
     * （见 {@code RedisCacheManagerConfiguration} 的 {@code disableCachingNullValues()}），
     * Redis 缓存遇到 null 会抛
     * {@code IllegalArgumentException: Cache 'sys:hot-reload:config' does not allow 'null' values}。
     * 调用方（如 HeaderPolicyFilter）按「异常放行」处理，于是**规则静默失效**：
     * 库里明明有爬虫拦截规则，请求却全部放行。全新库必现（配置行不存在就是 miss）。
     * 这里显式声明「不缓存 null」，与全局「null 需显式开启」的设计口径一致。</p>
     *
     * @param code 配置编码
     * @return 配置值，不存在或未启用时返回 {@code null}
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CACHE_NAME,
            key = "#code", unless = "#result == null")
    public String get(String code) {
        if (null == code || code.isBlank()) {
            return null;
        }
        SysSetting setting = sysSettingMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, SysSettingService.HOT_RELOAD_GROUP)
                .eq(SysSetting::getSysSettingCode, code.trim())
                .eq(SysSetting::getSysSettingStatus, 1)
                .last("limit 1"));
        return null == setting ? null : setting.getSysSettingValue();
    }

    /**
     * 清理单个配置编码缓存。
     *
     * @param code 配置编码
     */
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CACHE_NAME, key = "#code")
    public void evict(String code) {
        log.info("[HotReload] 配置缓存已清理: code={}", code);
    }

    /**
     * 清理全部热重载配置缓存。
     */
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CACHE_NAME, allEntries = true)
    public void evictAll() {
        log.info("[HotReload] 全量配置缓存已清理");
    }

    /**
     * 统计当前启用的热重载配置数量，供管理端验收展示。
     *
     * @return 启用配置数量
     */
    public long countEnabled() {
        Long count = sysSettingMapper.selectCount(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, SysSettingService.HOT_RELOAD_GROUP)
                .eq(SysSetting::getSysSettingStatus, 1));
        return null == count ? 0L : count;
    }
}
