package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.entity.CouponTemplate;
import com.chua.payment.support.entity.Merchant;
import com.chua.payment.support.mapper.CouponTemplateMapper;
import com.chua.payment.support.mapper.MerchantMapper;
import com.chua.payment.support.service.CouponTemplateService;
import com.chua.payment.support.vo.CouponTemplateVO;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 优惠券模板管理控制器
 *
 * @author CH
 * @since 2026-05-11
 */
@Tag(name = "优惠券管理", description = "优惠券模板管理相关接口")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponTemplateService couponTemplateService;
    private final CouponTemplateMapper couponTemplateMapper;
    private final MerchantMapper merchantMapper;

    @Operation(summary = "分页查询优惠券")
    @GetMapping("/page")
    @Permission(value = "payment:coupon:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<CouponTemplateVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String couponName,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<CouponTemplate>()
                .eq(merchantId != null, CouponTemplate::getMerchantId, merchantId)
                .like(StringUtils.hasText(couponName), CouponTemplate::getName, couponName)
                .eq(status != null, CouponTemplate::getStatus, status != null && status == 1 ? "ENABLED" : "DISABLED")
                .orderByDesc(CouponTemplate::getCreatedAt);
        Page<CouponTemplate> pageResult = couponTemplateMapper.selectPage(new Page<>(page, size), wrapper);
        PageResult<CouponTemplateVO> result = new PageResult<>(
                toVOList(pageResult.getRecords()),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );
        return ReturnResult.success(result);
    }

    @Operation(summary = "根据编码查询优惠券")
    @GetMapping("/code/{couponCode}")
    @Permission(value = "payment:coupon:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<CouponTemplateVO> getByCode(@PathVariable String couponCode) {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getCouponCode, couponCode);
        CouponTemplate entity = couponTemplateMapper.selectOne(wrapper);
        if (entity == null) {
            return ReturnResult.success(null);
        }
        return ReturnResult.success(toVO(entity));
    }

    @Operation(summary = "创建优惠券")
    @PostMapping
    @Permission(value = "payment:coupon:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<CouponTemplateVO> create(@RequestBody CouponTemplateVO vo) {
        CouponTemplate entity = toEntity(vo);
        CouponTemplate saved = couponTemplateService.create(entity);
        return ReturnResult.success(toVO(saved));
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    @Permission(value = "payment:coupon:edit", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<CouponTemplateVO> update(@PathVariable Long id, @RequestBody CouponTemplateVO vo) {
        CouponTemplate entity = toEntity(vo);
        entity.setId(id);
        CouponTemplate updated = couponTemplateService.update(entity);
        return ReturnResult.success(toVO(updated));
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    @Permission(value = "payment:coupon:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(@PathVariable Long id) {
        boolean success = couponTemplateService.delete(id);
        return ReturnResult.success(success);
    }

    @Operation(summary = "启用优惠券")
    @PutMapping("/{id}/enable")
    @Permission(value = "payment:coupon:edit", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> enable(@PathVariable Long id) {
        CouponTemplate entity = couponTemplateMapper.selectById(id);
        if (entity != null) {
            entity.setStatus("ENABLED");
            couponTemplateMapper.updateById(entity);
        }
        return ReturnResult.success(true);
    }

    @Operation(summary = "停用优惠券")
    @PutMapping("/{id}/disable")
    @Permission(value = "payment:coupon:edit", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> disable(@PathVariable Long id) {
        CouponTemplate entity = couponTemplateMapper.selectById(id);
        if (entity != null) {
            entity.setStatus("DISABLED");
            couponTemplateMapper.updateById(entity);
        }
        return ReturnResult.success(true);
    }

    // ===== 转换方法 =====

    private List<CouponTemplateVO> toVOList(List<CouponTemplate> entities) {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> merchantNameMap = merchantMapper.selectList(null).stream()
                .collect(Collectors.toMap(Merchant::getId, Merchant::getMerchantName, (a, b) -> a));
        return entities.stream().map(e -> toVO(e, merchantNameMap)).collect(Collectors.toList());
    }

    private CouponTemplateVO toVO(CouponTemplate entity) {
        return toVO(entity, Collections.emptyMap());
    }

    private CouponTemplateVO toVO(CouponTemplate entity, Map<Long, String> merchantNameMap) {
        if (entity == null) return null;
        CouponTemplateVO vo = new CouponTemplateVO();
        vo.setId(entity.getId());
        vo.setCouponCode(entity.getCouponCode());
        vo.setCouponName(entity.getName());
        vo.setCouponType("CASH");
        // 根据实体类型映射到前端格式
        if ("DISCOUNT".equals(entity.getType())) {
            vo.setDiscountType("PERCENT");
            vo.setDiscountValue(entity.getDiscountRate() != null
                    ? entity.getDiscountRate().multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        } else {
            // REDUCE / CASH → 固定金额
            vo.setDiscountType("FIXED");
            vo.setDiscountValue(entity.getReduceAmount() != null ? entity.getReduceAmount() : BigDecimal.ZERO);
        }
        vo.setMinAmount(entity.getConditionAmount() != null ? entity.getConditionAmount() : BigDecimal.ZERO);
        vo.setTotalCount(entity.getTotalCount());
        vo.setUsedCount(entity.getUsedCount());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setStatus("ENABLED".equals(entity.getStatus()) ? 1 : 0);
        vo.setMerchantId(entity.getMerchantId());
        vo.setMerchantName(merchantNameMap.getOrDefault(entity.getMerchantId(), null));
        vo.setDescription(entity.getDescription());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private CouponTemplate toEntity(CouponTemplateVO vo) {
        CouponTemplate entity = new CouponTemplate();
        entity.setName(vo.getCouponName());
        entity.setCouponCode(vo.getCouponCode());
        if ("PERCENT".equals(vo.getDiscountType())) {
            entity.setType("DISCOUNT");
            entity.setDiscountRate(vo.getDiscountValue() != null
                    ? vo.getDiscountValue().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            entity.setReduceAmount(BigDecimal.ZERO);
        } else {
            entity.setType("CASH");
            entity.setReduceAmount(vo.getDiscountValue() != null ? vo.getDiscountValue() : BigDecimal.ZERO);
            entity.setDiscountRate(BigDecimal.ZERO);
        }
        entity.setConditionAmount(vo.getMinAmount());
        entity.setTotalCount(vo.getTotalCount());
        entity.setUsedCount(0);
        entity.setStartTime(vo.getStartTime());
        entity.setEndTime(vo.getEndTime());
        entity.setStatus(vo.getStatus() != null && vo.getStatus() == 1 ? "ENABLED" : "DISABLED");
        entity.setMerchantId(vo.getMerchantId());
        entity.setDescription(vo.getDescription());
        return entity;
    }
}
