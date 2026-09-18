package com.chua.starter.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.signin.dto.UserSignInSummaryDTO;
import com.chua.starter.signin.model.SignInRecord;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到记录 映射器
 *
 * <p>基于 MyBatis-Plus {@link BaseMapper} 提供签到记录的基础 CRUD，
 * 并通过 XML 自定义签到查询、月度日历、连续天数和管理员汇总等复杂查询。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Mapper
public interface SignInRecordMapper extends BaseMapper<SignInRecord> {

    /**
     * 查询用户在指定日期的签到记录。
     *
     * @param userId   用户标识
     * @param signDate 签到日期
     * @return 签到记录，不存在返回 空
     */
    SignInRecord selectByUserIdAndDate(Long userId, LocalDate signDate);

    /**
     * 查询用户在指定月份所有已签到的日期列表。
     *
     * @param userId    用户标识
     * @param yearMonth 年月，格式 yyyy-MM
     * @return 已签到日期列表
     */
    List<LocalDate> selectDatesByUserIdAndMonth(Long userId, String yearMonth);

    /**
     * 查询用户全部签到记录，按日期倒序排列。
     *
     * @param userId 用户标识
     * @return 签到记录列表
     */
    List<SignInRecord> selectByUserIdOrderByDateDesc(Long userId);

    /**
     * 查询用户历史最高连续签到天数。
     *
     * @param userId 用户标识
     * @return 最高连续天数，无记录返回 0
     */
    Integer selectMaxStreakByUserId(Long userId);

    /**
     * 查询用户累计总积分。
     *
     * @param userId 用户标识
     * @return 总积分，无记录返回 0
     */
    Integer selectTotalPointsByUserId(Long userId);

    /**
     * 分页查询用户签到汇总（管理员端）。
     *
     * @param offset 起始偏移量
     * @param limit  每页条数
     * @return 用户签到汇总列表
     */
    List<UserSignInSummaryDTO> selectUserSummaryPage(Long offset, Integer limit);

    /**
     * 统计参与签到的用户总数（管理员端）。
     *
     * @return 用户总数
     */
    Long countUserSummary();
}