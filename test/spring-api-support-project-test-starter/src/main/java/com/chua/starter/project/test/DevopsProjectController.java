package com.chua.starter.project.test;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.mapper.SysProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DevOps 项目桥接 Controller
 * 将前端 /v2/devops/project/* 请求映射到现有的 sys_project 表
 */
@RestController
@RequestMapping("/v2/devops/project")
public class DevopsProjectController {

    @Autowired
    private SysProjectMapper sysProjectMapper;

    /**
     * 项目列表 - 返回前端 SysProject 格式
     */
    @GetMapping("/list")
    public ReturnResult<List<Map<String, Object>>> list() {
        List<SysProject> all = sysProjectMapper.selectList(null);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> result = all.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("projectId", p.getSysProjectId());
            m.put("projectName", p.getSysProjectName());
            m.put("projectDescription", p.getSysProjectRemark());
            m.put("projectIcon", p.getSysProjectIcon());
            m.put("serverOs", "linux");
            m.put("sortOrder", p.getSysProjectSort());
            m.put("status", p.getSysProjectStatus());
            m.put("serverEnabled", 0);
            m.put("runtimeStatus", "unknown");
            m.put("serverUsername", null);
            m.put("secretCount", 0);
            m.put("createTime", p.getCreateTime() != null ? p.getCreateTime().format(dtf) : null);
            m.put("updateTime", p.getUpdateTime() != null ? p.getUpdateTime().format(dtf) : null);
            return m;
        }).collect(Collectors.toList());
        return ReturnResult.ok(result);
    }

    /**
     * 新增项目 - 映射到 sys_project 表
     */
    @PostMapping("/save")
    public ReturnResult<Map<String, Object>> save(@RequestBody Map<String, Object> body) {
        SysProject entity = new SysProject();
        entity.setSysProjectName((String) body.getOrDefault("projectName", "未命名项目"));
        entity.setSysProjectRemark((String) body.getOrDefault("projectDescription", ""));
        entity.setSysProjectGroup((String) body.getOrDefault("projectGroup", "default"));
        Object sort = body.get("sortOrder");
        entity.setSysProjectSort(sort instanceof Number ? ((Number) sort).intValue() : 0);
        Object status = body.get("status");
        entity.setSysProjectStatus(status instanceof Number ? ((Number) status).intValue() : 1);
        entity.setSysProjectDelete(0);
        sysProjectMapper.insert(entity);

        Map<String, Object> result = Map.of("projectId", entity.getSysProjectId());
        return ReturnResult.ok(result);
    }

    /**
     * 更新项目
     */
    @PutMapping("/update")
    public ReturnResult<Boolean> update(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("projectId");
        if (idObj == null) {
            return ReturnResult.illegal("项目ID不能为空");
        }
        Integer projectId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());

        SysProject entity = sysProjectMapper.selectById(projectId);
        if (entity == null) {
            return ReturnResult.illegal("项目不存在");
        }

        if (body.containsKey("projectName")) entity.setSysProjectName((String) body.get("projectName"));
        if (body.containsKey("projectDescription")) entity.setSysProjectRemark((String) body.get("projectDescription"));
        if (body.containsKey("sortOrder")) entity.setSysProjectSort(((Number) body.get("sortOrder")).intValue());
        if (body.containsKey("status")) entity.setSysProjectStatus(((Number) body.get("status")).intValue());

        sysProjectMapper.updateById(entity);
        return ReturnResult.ok(true);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(@RequestParam Integer projectId) {
        if (projectId == null) {
            return ReturnResult.illegal("项目ID不能为空");
        }
        sysProjectMapper.deleteById(projectId);
        return ReturnResult.ok(true);
    }
}
