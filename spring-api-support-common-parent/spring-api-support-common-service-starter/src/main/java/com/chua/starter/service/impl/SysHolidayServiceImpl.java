package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.service.SysHolidayService;
import com.chua.starter.service.mapper.SysHolidayMapper;
import com.chua.starter.entity.service.SysHoliday;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author CH
 * @since 2025/9/11 16:45
 */
@Service
public class SysHolidayServiceImpl extends ServiceImpl<SysHolidayMapper, SysHoliday> implements SysHolidayService {

    @Override
    public Boolean sync() {
        return false;
    }

    @Override
    public List<SysHoliday> getHolidays(Integer year) {
        int newYear = year == null ? java.time.LocalDate.now().getYear() : year;
        return this.list(Wrappers.<SysHoliday>lambdaQuery().eq(SysHoliday::getSysHolidayYear, newYear));
    }
}
