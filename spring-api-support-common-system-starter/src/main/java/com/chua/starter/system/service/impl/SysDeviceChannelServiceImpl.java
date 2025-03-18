package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.system.entity.SysDeviceChannel;
import com.chua.starter.system.mapper.SysDeviceChannelMapper;
import com.chua.starter.system.pojo.device.SysDeviceChannelV1Request;
import com.chua.starter.system.service.SysDeviceChannelService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeviceChannelServiceImpl extends ServiceImpl<SysDeviceChannelMapper, SysDeviceChannel> implements SysDeviceChannelService {

    final TransactionTemplate transactionTemplate;

    @Override
    public ReturnResult<Boolean> syncEventMessage(SysDeviceChannelV1Request v1Request) {
        if (StringUtils.isEmpty(v1Request.getSysDeviceSerialNumber())) {
            return ReturnResult.illegal("设备编号不能为空");
        }
        return transactionTemplate.execute(status -> {

            List<SysDeviceChannel> channels = v1Request.getChannels();

            baseMapper.delete(Wrappers.<SysDeviceChannel>lambdaQuery().eq(SysDeviceChannel::getSysDeviceSerialNumber, v1Request.getSysDeviceSerialNumber()));

            if (CollectionUtils.isNotEmpty(channels)) {
                saveBatch(channels);
            }

            return ReturnResult.ok();
        });
    }
}
