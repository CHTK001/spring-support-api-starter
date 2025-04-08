package com.chua.starter.school.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.school.dto.SchoolStudentQueryDTO;
import com.chua.starter.school.entity.SchoolStudent;
import com.chua.starter.school.service.SchoolStudentService;
import com.chua.starter.school.vo.SchoolStudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学校学生Controller
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/school/student")
@Tag(name = "学校学生", description = "学校学生相关接口")
@RequiredArgsConstructor
public class SchoolStudentController {

    final SchoolStudentService schoolStudentService;

    /**
     * 新增学生信息
     *
     * @param schoolStudent 学生信息
     * @return 是否成功
     */
    @PostMapping("/save")
    @Operation(summary = "新增学生信息", description = "新增学生信息")
    public ReturnResult<Boolean> saveSchoolStudent(
            @Validated(AddGroup.class) @ParameterObject @RequestBody SchoolStudent schoolStudent, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolStudentService.saveSchoolStudent(schoolStudent);
    }

    /**
     * 更新学生信息
     *
     * @param schoolStudent 学生信息
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新学生信息", description = "更新学生信息")
    public ReturnResult<Boolean> updateSchoolStudent(
            @Validated(UpdateGroup.class) @ParameterObject @RequestBody SchoolStudent schoolStudent, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolStudentService.updateSchoolStudent(schoolStudent);
    }

    /**
     * 分页查询学生列表
     *
     * @param query 查询条件
     * @return 学生列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询学生列表", description = "根据条件分页查询学生列表")
    public ReturnPageResult<SchoolStudentVO> page(@ParameterObject @RequestBody SchoolStudentQueryDTO query) {
        return schoolStudentService.pageSchoolStudent(query);
    }

    /**
     * 获取学生详情
     *
     * @param schoolStudentId 学生ID
     * @return 学生详情
     */
    @GetMapping("/{schoolStudentId}")
    @Operation(summary = "获取学生详情", description = "根据学生ID获取学生详情信息")
    public ReturnResult<SchoolStudentVO> getStudentDetail(@Parameter(description = "学生ID") @PathVariable Long schoolStudentId) {
        return schoolStudentService.getSchoolStudentDetail(schoolStudentId);
    }

    /**
     * 删除学生信息
     *
     * @param schoolStudentId 学生ID
     * @return 是否成功
     */
    @DeleteMapping("/{schoolStudentId}")
    @Operation(summary = "删除学生信息", description = "根据学生ID删除学生信息")
    public ReturnResult<Boolean> deleteStudent(@Parameter(description = "学生ID") @PathVariable Long schoolStudentId) {
        return schoolStudentService.deleteSchoolStudent(schoolStudentId);
    }
} 