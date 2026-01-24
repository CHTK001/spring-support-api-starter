package com.chua.starter.video.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/9/20 16:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoInfoResultTemp extends VideoInfoResult {

    private Integer total;
}
