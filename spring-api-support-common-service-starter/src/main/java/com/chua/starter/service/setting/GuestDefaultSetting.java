package com.chua.starter.service.setting;

import com.chua.starter.common.support.function.Upgrade;
import lombok.Data;

@Data
public class GuestDefaultSetting implements Upgrade<GuestDefaultSetting> {
    private boolean checkCodeOpen;
    private boolean checkTotpCodeOpen;

    @Override
    public void upgrade(GuestDefaultSetting guestDefaultSetting) {
        this.checkCodeOpen = guestDefaultSetting.checkCodeOpen;
        this.checkTotpCodeOpen = guestDefaultSetting.checkTotpCodeOpen;
    }
}
