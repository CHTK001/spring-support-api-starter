package com.chua.starter.system.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 事件
 * @author CH
 * @since 2024/9/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "事件")
public class SqlEvent extends ProjectEvent implements Serializable {



    /**
     * sql
     */
    private String sql;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

}
