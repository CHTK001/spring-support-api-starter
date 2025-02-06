package com.chua.starter.service.support.hikvision.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 海康监控点信息
 * </p>
 *
 * @author ch
 * @since 2022-06-28
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class HikCamera {
    static final HikCamera INSTANCE = new HikCamera();
    /**
     * 查询数据记录总数
     */
    private long total = 0;
    /**
     * 当前页码
     */
    private long pageNo = 1;
    /**
     * 每页记录总数
     */
    private long pageSize = 1000;
    /**
     * 监控点列表
     */
    private List<HikCameraDevice> list;

    public static HikCamera empty() {
        return INSTANCE;
    }
}
