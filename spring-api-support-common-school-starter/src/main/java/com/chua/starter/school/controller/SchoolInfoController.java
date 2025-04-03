package com.chua.starter.school.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.school.dto.SchoolQueryDTO;
import com.chua.starter.school.service.SchoolInfoService;
import com.chua.starter.school.vo.SchoolInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 学校信息Controller
 *
 * @author CH
 */
@RestController
@RequestMapping("/school")
@Tag(name = "学校信息", description = "学校信息相关接口")
public class SchoolInfoController {

    @Autowired
    private SchoolInfoService schoolInfoService;

    /**
     * 分页查询学校列表
     *
     * @param query 查询条件
     * @return 学校列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询学校列表", description = "根据条件分页查询学校列表")
    public ResponseEntity<IPage<SchoolInfoVO>> listSchools(@RequestBody SchoolQueryDTO query) {
        return ResponseEntity.ok(schoolInfoService.pageSchoolInfo(query));
    }

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取学校详情", description = "根据学校ID获取学校详情信息")
    public ResponseEntity<SchoolInfoVO> getSchoolDetail(@Parameter(description = "学校ID") @PathVariable Long id) {
        return ResponseEntity.ok(schoolInfoService.getSchoolDetail(id));
    }

    /**
     * 根据学校代码获取学校详情
     *
     * @param schoolCode 学校代码
     * @return 学校详情
     */
    @GetMapping("/code/{schoolCode}")
    @Operation(summary = "根据学校代码获取学校详情", description = "根据学校代码获取学校详情信息")
    public ResponseEntity<SchoolInfoVO> getSchoolDetailByCode(@Parameter(description = "学校代码") @PathVariable String schoolCode) {
        return ResponseEntity.ok(schoolInfoService.getSchoolDetailByCode(schoolCode));
    }
} 