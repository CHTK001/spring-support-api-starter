package com.chua.starter.collect.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.collect.entity.CollectNotice;
import com.chua.starter.collect.service.CollectNoticeService;
import com.chua.starter.collect.vo.CollectNotifyChannelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 执行通知接口。
 *
 * <p>配置按流程或全局（flowId=0）生效，渠道清单来自注册表，第三方注册新的
 * {@code CollectNotifyChannel} 后这里与前端下拉都会自动出现。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Ignore
@Tag(name = "数据采集-执行通知")
@RestController
@RequestMapping("/v2/collect/notice")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectNoticeController {

    /**
     * 通知服务。
     */
    private final CollectNoticeService noticeService;

    /**
     * 通知配置列表。
     *
     * @param flowId 流程ID，为空返回全部
     * @return 通知配置列表
     */
    @Operation(summary = "通知配置列表")
    @GetMapping("/list")
    public ReturnResult<List<CollectNotice>> list(@RequestParam(required = false) Integer flowId) {
        return ReturnResult.ok(noticeService.listNotices(flowId));
    }

    /**
     * 可用渠道及其接收目标填法。
     *
     * @return 渠道列表
     */
    @Operation(summary = "可用渠道")
    @GetMapping("/channels")
    public ReturnResult<List<CollectNotifyChannelVO>> channels() {
        return ReturnResult.ok(noticeService.channelOptions());
    }

    /**
     * 新增通知配置。
     *
     * @param notice 通知配置
     * @return 保存后的配置
     */
    @Operation(summary = "新增通知配置")
    @PostMapping("/save")
    public ReturnResult<CollectNotice> save(@RequestBody CollectNotice notice) {
        return ReturnResult.ok(noticeService.addNotice(notice));
    }

    /**
     * 修改通知配置。
     *
     * @param notice 通知配置
     * @return 是否成功
     */
    @Operation(summary = "修改通知配置")
    @PutMapping("/update")
    public ReturnResult<Boolean> update(@RequestBody CollectNotice notice) {
        return ReturnResult.ok(noticeService.editNotice(notice));
    }

    /**
     * 删除通知配置。
     *
     * @param noticeId 通知ID
     * @return 是否成功
     */
    @Operation(summary = "删除通知配置")
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(@RequestParam Integer noticeId) {
        return ReturnResult.ok(noticeService.deleteNotice(noticeId));
    }

    /**
     * 按配置试发一条通知。
     *
     * @param noticeId 通知ID
     * @return 发送结果
     */
    @Operation(summary = "试发通知")
    @PostMapping("/test")
    public ReturnResult<MessageResponse> test(@RequestParam Integer noticeId) {
        return ReturnResult.ok(noticeService.testNotice(noticeId));
    }
}
