package com.chua.starter.school.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiGroup;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.school.dto.SchoolQueryDTO;
import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.service.SchoolInfoService;
import com.chua.starter.school.vo.SchoolInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学校信息Controller
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/school")
@Tag(name = "学校信息", description = "学校信息相关接口")
@RequiredArgsConstructor
public class SchoolInfoController {

    final SchoolInfoService schoolInfoService;

    /**
     * 新增学校信息
     *
     * @param schoolInfo 学校信息
     * @return 是否成功
     */
    @PostMapping("/save")
    @Operation(summary = "新增学校信息", description = "新增学校信息")
    public ReturnResult<Boolean> saveSchSchoolInfo(
            @Validated(AddGroup.class) @ParameterObject @RequestBody SchoolInfo schoolInfo, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolInfoService.saveSchSchoolInfo(schoolInfo);
    }

    /**
     * 更新学校信息
     *
     * @param schoolInfo 学校信息
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新学校信息", description = "更新学校信息")
    public ReturnResult<Boolean> updateSchoolInfo(
            @Validated(UpdateGroup.class) @ParameterObject @RequestBody SchoolInfo schoolInfo, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolInfoService.updateSchoolInfo(schoolInfo);
    }

    /**
     * 分页查询学校列表
     *
     * @param query 查询条件
     * @return 学校列表
     */
    @PostMapping("/page")
    @ApiGroup(IgnoreGroup.class)
    @Operation(summary = "分页查询学校列表", description = "根据条件分页查询学校列表")
    public ReturnPageResult<SchoolInfoVO> page(@ParameterObject @RequestBody SchoolQueryDTO query) {
        return schoolInfoService.pageSchoolInfo(query);
    }

    /**
     * 获取学校详情
     *
     * @param schoolId 学校ID
     * @return 学校详情
     */
    @GetMapping("/{schoolId}")
    @Operation(summary = "获取学校详情", description = "根据学校ID获取学校详情信息")
    public ReturnResult<SchoolInfoVO> getSchoolDetail(@Parameter(description = "学校ID") @PathVariable Long schoolId) {
        return schoolInfoService.getSchoolDetail(schoolId);
    }
    /**
     * 删除学校详情
     *
     * @param schoolId 学校ID
     * @return 学校详情
     */
    @DeleteMapping("/{schoolId}")
    @Operation(summary = "删除学校详情", description = "根据学校ID删除学校详情")
    public ReturnResult<Boolean> deleteSchool(@Parameter(description = "学校ID") @PathVariable Long schoolId) {
        return schoolInfoService.deleteSchool(schoolId);
    }
}