// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.util;

import com.chua.common.support.utils.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生日头像框生效窗口（全局唯一定义）
 *
 * <p>生日框只在生日前 {@link #BIRTHDAY_WINDOW_DAYS} 天到后 {@link #BIRTHDAY_WINDOW_DAYS} 天内生效，
 * 窗口外（含未设置生日）按"不佩戴"渲染。用户表里存的仍是生日框标识，所以次年生日会自动重新生效，
 * 无需任何迁移或定时清理。所有对外输出佩戴框的接口都必须经 {@link #resolveEffective} 收口，
 * 否则登录态与刷新后的展示会不一致。</p>
 *
 * @since 2026-09-19
 * @author CH
 */
public class BirthdayFramePolicy {

    /**
     * 工具类不可实例化
     */
    private BirthdayFramePolicy() {
    }

    /**
     * 生日框种子标识（sys_avatar_frame.sys_avatar_frame_id）
    */
    public static final Integer BIRTHDAY_FRAME_ID = 100;

    /**
     * 不佩戴
    */
    public static final Integer NOT_WORN_FRAME_ID = 0;

    /**
     * 生日窗口：生日前后各 7 天
    */
    public static final int BIRTHDAY_WINDOW_DAYS = 7;

    /**
     * 生日日期解析：yyyy-MM-dd / yyyy/MM/dd / yyyy.MM.dd
    */
    private static final Pattern BIRTHDAY_PATTERN = Pattern.compile("(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    /**
     * 换算当前生效的头像框
     *
     * @param wornFrameId 用户表记录的佩戴框标识，可为空
     * @param birthday    用户生日字符串
     * @param frameShow   佩戴框显隐开关，可为空
     * @return 长度为 2 的数组：[0]=生效框标识，[1]=生效显隐开关；非生日框时原值返回
     */
    public static Integer[] resolveEffective(Integer wornFrameId, String birthday, Integer frameShow) {
        if (!BIRTHDAY_FRAME_ID.equals(wornFrameId) || inBirthdayWindow(birthday)) {
            return new Integer[]{wornFrameId, frameShow};
        }
        return new Integer[]{NOT_WORN_FRAME_ID, 0};
    }

    /**
     * 判断指定生日是否处于生日窗口内（忽略年份，只比月/日，跨年按环形日历计算）
     *
     * @param birthday 生日字符串（yyyy-MM-dd 等）
     * @return 是否处于生日窗口；生日为空或格式不合法返回 false
     */
    public static boolean inBirthdayWindow(String birthday) {
        if (StringUtils.isBlank(birthday)) {
            return false;
        }
        Matcher matcher = BIRTHDAY_PATTERN.matcher(birthday.trim());
        if (!matcher.matches()) {
            return false;
        }
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        LocalDate today = LocalDate.now();
        // 平年没有 2/29，按 2/28 计算
        if (month == 2 && day == 29 && !today.isLeapYear()) {
            day = 28;
        }
        LocalDate thisYearBirthday;
        try {
            thisYearBirthday = LocalDate.of(today.getYear(), month, day);
        } catch (Exception e) {
            return false;
        }
        long yearLength = today.lengthOfYear();
        long diff = Math.abs(ChronoUnit.DAYS.between(thisYearBirthday, today));
        return Math.min(diff, yearLength - diff) <= BIRTHDAY_WINDOW_DAYS;
    }
}