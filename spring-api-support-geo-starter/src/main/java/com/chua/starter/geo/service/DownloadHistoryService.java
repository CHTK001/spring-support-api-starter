package com.chua.starter.geo.service;

import com.chua.starter.geo.model.DownloadHistoryRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 下载历史记录服务，基于 JSON 文件持久化。
 *
 * @author CH
 * @since 1.0.0
 */
public interface DownloadHistoryService {

    /**
     * 保存下载成功记录。
     *
     * @param record 下载结果记录
     */
    void save(DownloadHistoryRecord record);

    /**
     * 保存下载失败记录。
     *
     * @param record 失败结果记录
     */
    void saveFailed(DownloadHistoryRecord record);

    /**
     * 获取所有历史记录（按创建时间倒序）。
     *
     * @return 历史记录列表
     */
    List<DownloadHistoryRecord> listAll();

    /**
     * 分页查询历史记录。
     *
     * @param page     页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult listPage(int page, int pageSize);

    /**
     * 删除指定记录。
     *
     * @param id 记录 标识
     * @return 是否删除成功
     */
    boolean delete(String id);

    /**
     * 清空所有历史记录。
     */
    void clear();

    /**
     * 分页结果封装。
     *
     * @param records 记录列表
     * @param total   总数
     */
    record PageResult(List<DownloadHistoryRecord> records, long total) {
        /**
         * 规范构造器：对记录列表做防御性拷贝。
         *
         * <p>value class 前置条件——集合组件必须深不可变；记录来自磁盘 JSON 文件，
         * 可能携带 空 元素，故采用可空安全包装而非 {@code List.copyOf}。</p>
         *
         * @param records 记录列表
         * @param total   总数
         */
        public PageResult {
            records = Collections.unmodifiableList(
                    new ArrayList<>(Objects.requireNonNull(records, "records 不能为 null")));
        }
    }
}
