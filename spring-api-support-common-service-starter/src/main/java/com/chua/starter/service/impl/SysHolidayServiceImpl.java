package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.advanced.support.other.holiday.Holiday;
import com.chua.advanced.support.other.holiday.HolidayResolver;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.service.SysHolidayService;
import com.chua.starter.service.mapper.SysHolidayMapper;
import com.chua.starter.service.entity.SysHoliday;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * @author CH
 * @since 2025/9/11 16:45
 */
@Service
public class SysHolidayServiceImpl extends ServiceImpl<SysHolidayMapper, SysHoliday> implements SysHolidayService {

    @Override
    public Boolean sync() {
        HolidayResolver holidayResolver = ServiceProvider.of(HolidayResolver.class).getNewExtension("Timor");
        List<SysHoliday> result = new LinkedList<>();
        for (Holiday holiday : holidayResolver.resolve(String.valueOf(LocalDate.now().getYear()))) {
            SysHoliday sysHoliday = new SysHoliday();
            sysHoliday.setSysHolidayYear(holiday.getDate().getYear());
            sysHoliday.setSysHolidayDate(holiday.getDate());
            sysHoliday.setSysHolidayName(holiday.getName());
            sysHoliday.setSysHolidayIsHoliday(holiday.isHoliday());

            if (exists(Wrappers.<SysHoliday>lambdaQuery().eq(SysHoliday::getSysHolidayDate, sysHoliday.getSysHolidayDate()))) {
                continue;
            }
            result.add(sysHoliday);
        }

        if (!result.isEmpty()) {
            this.saveBatch(result);
        }

        return true;
    }

    @Override
    public List<SysHoliday> getHolidays(Integer year) {
        int newYear = year == null ? LocalDate.now().getYear() : year;
        return this.list(Wrappers.<SysHoliday>lambdaQuery().eq(SysHoliday::getSysHolidayYear, newYear));
    }
}
