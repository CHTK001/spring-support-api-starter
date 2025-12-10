package com.chua.starter.monitor.controller.app;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.pojo.node.OpenApi;
import com.chua.starter.monitor.service.app.MonitorAppService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控应用控制器
 */
@RestController
@RequestMapping("v1/app")
@Tag(name = "上报数据接口")
@RequiredArgsConstructor
public class MonitorAppController {

    private final MonitorAppService monitorAppService;

    /**
     * 查询数据详情
     *
     * @param monitorId monitorId
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("api-docs/{monitorId}/{index}")
    public ReturnResult<List<OpenApi>> apiDocs(@PathVariable("monitorId") Integer monitorId,
                                               @PathVariable("index") String index) {
        return monitorAppService.render(monitorId, index);
    }

    /**
     * 查询数据详情
     *
     * @param monitorId monitorId
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("detail/{monitorId}")
    public ReturnResult<MonitorApplication> detail(@PathVariable("monitorId") Integer monitorId) {
        return monitorAppService.detail(monitorId);
    }


    /**
     * 删除数据
     *
     * @param monitorId monitorId
     * @return 删除结果
     */
    @Operation(summary = "删除基础数据")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(Integer monitorId) {
        return ReturnResult.guess(monitorAppService.removeById(monitorId));
    }


    /**
     * 保存或更新数据
     *
     * @param application application
     * @return 保存结果
     */
    @Operation(summary = "保存或更新数据")
    @PostMapping("save")
    public ReturnResult<Boolean> saveOrUpdate(@RequestBody MonitorApplication application) {
        return monitorAppService.saveOrUpdateApplication(application);
    }


    /**
     * 分页查询数据
     *
     * @param entity 结果
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询基础数据")
    @GetMapping("page")
    public ReturnPageResult<MonitorApplication> page(Query<MonitorApplication> page, @Valid MonitorApplication entity,
                                                     BindingResult bindingResult) {
        return PageResultUtils.ok(monitorAppService.pageFor(page, entity));
    }

    /**
     * 分页查询数据
     *
     * @param entity 结果
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "查询基础数据")
    @GetMapping("list")
    public ReturnResult<List<MonitorApplication>> list(MonitorApplication entity) {
        return monitorAppService.list(entity);
    }

}
