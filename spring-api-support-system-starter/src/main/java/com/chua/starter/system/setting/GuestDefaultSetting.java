package com.chua.starter.system.setting;

import com.chua.common.support.function.Upgrade;
import lombok.Data;

/**
 * @author CH
 * @since 2024/8/13
 */
@Data
public class GuestDefaultSetting implements Upgrade<GuestDefaultSetting> {

    /**
     * 是否开启
     */
    private boolean openVerifyCode;

    @Override
    public void upgrade(GuestDefaultSetting guestDefaultSetting) {
        this.openVerifyCode = guestDefaultSetting.isOpenVerifyCode();
    }
}
