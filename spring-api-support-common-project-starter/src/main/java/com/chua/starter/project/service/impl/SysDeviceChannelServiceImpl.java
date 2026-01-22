package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.CollectionUtils;
import com.chua.starter.project.mapper.SysDeviceChannelMapper;
import com.chua.starter.project.service.SysDeviceChannelService;
import com.chua.starter.entity.project.SysDeviceChannel;
import com.chua.starter.project.pojo.device.SysDeviceChannelV1Request;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 设备通道服务实现类
 *
 * @author CH
 * @since 2023-04-05
 */
@Service
@RequiredArgsConstructor
public class SysDeviceChannelServiceImpl extends ServiceImpl<SysDeviceChannelMapper, SysDeviceChannel> implements SysDeviceChannelService {

    /**
     * 事务模板，用于保证数据一致性
     */
    final TransactionTemplate transactionTemplate;

    /**
     * 同步设备通道事件消息
     *
     * @param v1Request 设备通道请求对象，包含设备序列号和通道列表信息
     *                  示例: {
     *                  "sysDeviceSerialNumber": "DS-K1T341W-123456",
     *                  "channels": [
     *                  {
     *                  "sysDeviceChannelName": "通道1",
     *                  "sysDeviceChannelNo": 1,
     *                  "sysDeviceSerialNumber": "DS-K1T341W-123456",
     *                  "sysDeviceChannelType": "video",
     *                  "sysDeviceChannelStatus": 1,
     *                  "sysDeviceChannelUse": 1
     *                  }
     *                  ]
     *                  }
     * @return 返回操作结果，成功返回true，失败返回错误信息
     */
    @Override
    public ReturnResult<Boolean> syncEventMessage(SysDeviceChannelV1Request v1Request) {
        // 校验设备序列号是否为空
        if (StringUtils.isEmpty(v1Request.getSysDeviceSerialNumber())) {
            return ReturnResult.illegal("设备编号不能为空");
        }

        // 使用事务执行删除和插入操作
        return transactionTemplate.execute(status -> {
            // 获取通道列表
            List<SysDeviceChannel> channels = v1Request.getChannels();

            // 根据设备序列号删除原有通道信息
            baseMapper.delete(Wrappers.<SysDeviceChannel>lambdaQuery()
                    .eq(SysDeviceChannel::getSysDeviceSerialNumber, v1Request.getSysDeviceSerialNumber()));

            // 批量保存新的通道信息
            if (CollectionUtils.isNotEmpty(channels)) {
                saveBatch(channels);
            }

            return ReturnResult.ok(true);
        });
    }
}
