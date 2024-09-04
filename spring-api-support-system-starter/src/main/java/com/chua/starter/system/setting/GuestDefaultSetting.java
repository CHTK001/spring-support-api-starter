package com.chua.starter.system.setting;

import com.chua.common.support.function.Upgrade;
import lombok.Data;

/**
 * 校验码设置
 * @author CH
 * @since 2024/8/13
 */
@Data
public class GuestDefaultSetting implements Upgrade<GuestDefaultSetting> {

    /**
     * 是否开启
     */
    private boolean checkCodeOpen;

    @Override
    public void upgrade(GuestDefaultSetting guestDefaultSetting) {
        this.checkCodeOpen = guestDefaultSetting.isCheckCodeOpen();
    }
}
