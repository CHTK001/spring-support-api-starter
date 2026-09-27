package com.chua.starter.video.setting;

import java.util.List;

/**
 * 视频配置存储抽象。
 *
 * <p>生产实现 {@link SysSettingStore} 复用通用配置表 {@code sys_setting}；
 * 当宿主未引入配置服务时使用 {@link Noop}，此时视频模块回退内置 JSON。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface VideoSettingStore {

    /**
     * 配置项读写。
     *
     * @param name  配置名称（分组内唯一）
     * @param value 配置值（≤255）
     * @param sort  排序号
     */
    record Row(String name, String value, int sort) {
    }

    /**
     * 存储是否可用。
     *
     * @return 可用返回 true
     */
    boolean available();

    /**
     * 读取分组内全部启用配置。
     *
     * @param group 分组
     * @return 配置行
     */
    List<Row> list(String group);

    /**
     * 新增或更新配置。
     *
     * @param group     分组
     * @param name      配置名称
     * @param value     配置值
     * @param valueType 值类型（String/Bool/JSON）
     * @param remark    备注
     * @param sort      排序号
     */
    void upsert(String group, String name, String value, String valueType, String remark, int sort);

    /**
     * 按名称前缀删除配置。
     *
     * @param group      分组
     * @param namePrefix 名称前缀
     * @return 删除行数
     */
    int deleteByPrefix(String group, String namePrefix);

    /**
     * 空实现：不可用，读写无操作。
     */
    class Noop implements VideoSettingStore {

        @Override
        public boolean available() {
            return false;
        }

        @Override
        public List<Row> list(String group) {
            return List.of();
        }

        @Override
        public void upsert(String group, String name, String value, String valueType, String remark, int sort) {
            // no-op
        }

        @Override
        public int deleteByPrefix(String group, String namePrefix) {
            return 0;
        }
    }
}
