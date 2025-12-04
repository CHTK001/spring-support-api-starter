package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysMessageHistoryService;
import com.chua.starter.service.mapper.SysMessageHistoryMapper;
import com.chua.starter.system.entity.SysMessageHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息历史记录服务实现类
 *
 * @author CH
 * @since 2024/12/04
 */
@Slf4j
@Service
public class SysMessageHistoryServiceImpl extends ServiceImpl<SysMessageHistoryMapper, SysMessageHistory> 
        implements SysMessageHistoryService {

    @Override
    public IPage<SysMessageHistory> pageForHistory(Query<SysMessageHistory> query, SysMessageHistory history) {
        return this.page(query.createFullPage(), Wrappers.<SysMessageHistory>lambdaQuery()
                .like(StringUtils.isNotBlank(history.getSysMessageHistoryTitle()),
                        SysMessageHistory::getSysMessageHistoryTitle, history.getSysMessageHistoryTitle())
                .eq(StringUtils.isNotBlank(history.getSysMessageHistoryType()),
                        SysMessageHistory::getSysMessageHistoryType, history.getSysMessageHistoryType())
                .eq(history.getSysMessageHistoryReceiverId() != null,
                        SysMessageHistory::getSysMessageHistoryReceiverId, history.getSysMessageHistoryReceiverId()));
    }

    @Override
    public List<SysMessageHistory> listByUserId(Integer userId) {
        return this.list(Wrappers.<SysMessageHistory>lambdaQuery()
                .eq(SysMessageHistory::getSysMessageHistoryReceiverId, userId)
                .or().eq(SysMessageHistory::getSysMessageHistoryReceiverId, 0)
                .orderByDesc(SysMessageHistory::getSysMessageHistoryReadTime));
    }

    @Override
    public int cleanOldHistory(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return baseMapper.delete(Wrappers.<SysMessageHistory>lambdaQuery()
                .lt(SysMessageHistory::getSysMessageHistoryReadTime, threshold));
    }
}
