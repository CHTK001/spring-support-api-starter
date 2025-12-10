package com.chua.starter.monitor.controller.file;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemFileShare;
import com.chua.starter.monitor.pojo.request.CreateShareLinkRequest;
import com.chua.starter.monitor.pojo.response.ShareLinkResponse;
import com.chua.starter.monitor.service.SystemFileShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文件分享控制器
 *
 * @author CH
 * @since 2025-12-08
 */
@Slf4j
@RestController
@RequestMapping("/v1/file-share")
@Tag(name = "文件分享管理")
public class SystemFileShareController {

    @Resource
    private SystemFileShareService fileShareService;

    /**
     * 创建分享链接
     *
     * @param request 分享请求
     * @return 分享链接信息
     */
    @PostMapping("/share/create")
    @Operation(summary = "创建分享链接")
    public ReturnResult<ShareLinkResponse> createShareLinkForFileShare(@RequestBody CreateShareLinkRequest request) {
        log.info("创建分享链接: serverId={}, bucket={}, filePath={}, expireMinutes={}", 
                request.getServerId(), request.getBucket(), request.getFilePath(), request.getExpireMinutes());
        
        // 创建分享记录
        SystemFileShare share = fileShareService.createShareLink(
                request.getServerId(),
                request.getBucket(),
                request.getFilePath(),
                request.getFileName(),
                request.getExpireMinutes()
        );
        
        // 生成短链接
        String shortUrl = fileShareService.generateShortUrl(request.getBaseUrl(), share.getShareId());
        
        ShareLinkResponse response = new ShareLinkResponse();
        response.setShareId(share.getShareId());
        response.setShareUrl(shortUrl);
        response.setExpireTime(share.getExpireTime());
        response.setShareType(share.getShareType());
        
        return ReturnResult.ok(response);
    }

    /**
     * 创建闪图链接（阅后即焚）
     *
     * @param request 分享请求
     * @return 闪图链接信息
     */
    @PostMapping("/flash/create")
    @Operation(summary = "创建闪图链接")
    public ReturnResult<ShareLinkResponse> createFlashImageForFileShare(@RequestBody CreateShareLinkRequest request) {
        log.info("创建闪图链接: serverId={}, bucket={}, filePath={}", 
                request.getServerId(), request.getBucket(), request.getFilePath());
        
        // 创建闪图记录
        SystemFileShare share = fileShareService.createFlashImage(
                request.getServerId(),
                request.getBucket(),
                request.getFilePath(),
                request.getFileName()
        );
        
        // 生成短链接
        String shortUrl = fileShareService.generateShortUrl(request.getBaseUrl(), share.getShareId());
        
        ShareLinkResponse response = new ShareLinkResponse();
        response.setShareId(share.getShareId());
        response.setShareUrl(shortUrl);
        response.setExpireTime(null);
        response.setShareType("FLASH");
        
        return ReturnResult.ok(response);
    }

    /**
     * 获取分享列表
     *
     * @param serverId 服务器ID
     * @return 分享列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取分享列表")
    public ReturnResult<List<SystemFileShare>> getShareListForFileShare(@RequestParam Long serverId) {
        List<SystemFileShare> shares = fileShareService.lambdaQuery()
                .eq(SystemFileShare::getServerId, serverId)
                .eq(SystemFileShare::getIsDeleted, 0)
                .orderByDesc(SystemFileShare::getCreateTime)
                .list();
        return ReturnResult.ok(shares);
    }

    /**
     * 删除分享链接
     *
     * @param shareId 分享ID
     * @return 结果
     */
    @DeleteMapping("/{shareId}")
    @Operation(summary = "删除分享链接")
    public ReturnResult<Boolean> deleteShareLinkForFileShare(@PathVariable String shareId) {
        log.info("删除分享链接: shareId={}", shareId);
        fileShareService.markDeleted(shareId);
        return ReturnResult.ok(true);
    }
}
