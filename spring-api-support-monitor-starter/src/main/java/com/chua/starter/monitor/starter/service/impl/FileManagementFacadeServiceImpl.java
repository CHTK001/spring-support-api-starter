package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.report.client.starter.pojo.FileOperationRequest;
import com.chua.report.client.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.FileManagementService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.utils.MonitorSysGenServerSettingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件管理门面服务实现 统一处理文件管理的路由逻辑
 * 
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
public class FileManagementFacadeServiceImpl {

    private final MonitorSysGenServerSettingService serverSettingService;
    private final LocalFileManagementServiceImpl localFileManagementService;
    private final SshFileManagementServiceImpl sshFileManagementService;
    private final NodeFileManagementServiceImpl nodeFileManagementService;
    private final ApiFileManagementServiceImpl apiFileManagementService;

    public FileManagementFacadeServiceImpl(MonitorSysGenServerSettingService serverSettingService,
            LocalFileManagementServiceImpl localFileManagementService,
            SshFileManagementServiceImpl sshFileManagementService,
            @Qualifier("nodeFileManagementServiceImpl") NodeFileManagementServiceImpl nodeFileManagementService,
            ApiFileManagementServiceImpl apiFileManagementService) {
        this.serverSettingService = serverSettingService;
        this.localFileManagementService = localFileManagementService;
        this.sshFileManagementService = sshFileManagementService;
        this.nodeFileManagementService = nodeFileManagementService;
        this.apiFileManagementService = apiFileManagementService;
    }

    /**
     * 列出文件
     */
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
                                                         String sortBy, String sortOrder) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.listFiles(serverId, path, includeHidden, sortBy, sortOrder);

        } catch (Exception e) {
            log.error("列出文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("列出文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件树
     */
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden) {
        return getFileTree(serverId, path, maxDepth, includeHidden, false, 1000, 0);
    }

    /**
     * 获取文件树（支持懒加载）
     */
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex) {
        try {
            log.info(
                    "获取文件树: serverId={}, path={}, maxDepth={}, includeHidden={}, lazyLoad={}, pageSize={}, pageIndex={}",
                    serverId, path, maxDepth, includeHidden, lazyLoad, pageSize, pageIndex);

            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.getFileTree(serverId, path, maxDepth, includeHidden, lazyLoad, pageSize, pageIndex);

        } catch (Exception e) {
            log.error("获取文件树失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件树失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     */
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.uploadFile(serverId, targetPath, file, overwrite);

        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.downloadFile(serverId, filePath);

        } catch (Exception e) {
            log.error("下载文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 预览文件
     */
    public ResponseEntity<byte[]> previewFile(Integer serverId, String filePath, String previewType,
            Integer maxSizeMB) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                        .body("服务器文件管理模式未配置或未启用".getBytes(StandardCharsets.UTF_8));
            }

            ReturnResult<FileOperationResponse> fileOperationResponseReturnResult = service.previewFile(serverId,
                    filePath, previewType, maxSizeMB);
            if (!fileOperationResponseReturnResult.isOk()) {
                return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                        .body(fileOperationResponseReturnResult.getMsg().getBytes(StandardCharsets.UTF_8));
            }
            FileOperationResponse fileOperationResponse = fileOperationResponseReturnResult.getData();
            if (!fileOperationResponse.getSuccess()) {
                return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                        .body(fileOperationResponse.getMessage().getBytes(StandardCharsets.UTF_8));
            }
            return ResponseEntity.ok().contentType(fileOperationResponse.getMediaType())
                    .contentLength(fileOperationResponse.getAll().length).body(fileOperationResponse.getAll());
        } catch (Exception e) {
            log.error("预览文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body(("预览文件失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 删除文件
     */
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.deleteFile(serverId, path, recursive);

        } catch (Exception e) {
            log.error("删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文件
     */
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.renameFile(serverId, oldPath, newName);

        } catch (Exception e) {
            log.error("重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建目录
     */
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.createDirectory(serverId, path, recursive);

        } catch (Exception e) {
            log.error("创建目录失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    /**
     * 执行文件操作
     */
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.executeOperation(serverId, request);

        } catch (Exception e) {
            log.error("执行文件操作失败: serverId={}, operation={}", serverId, request.getOperation(), e);
            return ReturnResult.error("执行文件操作失败: " + e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.testConnection(serverId);

        } catch (Exception e) {
            log.error("测试连接失败: serverId={}", serverId, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 复制文件
     */
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.copyFile(serverId, sourcePath, targetPath, overwrite);

        } catch (Exception e) {
            log.error("复制文件失败: serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath, e);
            return ReturnResult.error("复制文件失败: " + e.getMessage());
        }
    }

    /**
     * 移动文件
     */
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.moveFile(serverId, sourcePath, targetPath, overwrite);

        } catch (Exception e) {
            log.error("移动文件失败: serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath, e);
            return ReturnResult.error("移动文件失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths,
            Boolean recursive) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            return service.batchDeleteFiles(serverId, paths, recursive);

        } catch (Exception e) {
            log.error("批量删除文件失败: serverId={}, paths={}", serverId, paths, e);
            return ReturnResult.error("批量删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 读取文件内容
     */
    public ReturnResult<String> readFileContent(Integer serverId, String filePath, String encoding) {
        try {
            FileManagementService service = getFileManagementService(serverId);
            if (service == null) {
                return ReturnResult.error("服务器文件管理模式未配置或未启用");
            }

            // 通过预览接口读取文件内容
            ReturnResult<FileOperationResponse> result = service.previewFile(serverId, filePath, "text", 10);
            if (result.isOk() && result.getData() != null) {
                return ReturnResult.ok(result.getData().getContent());
            } else {
                return ReturnResult.error(result.getMsg());
            }

        } catch (Exception e) {
            log.error("读取文件内容失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("读取文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件内容
     */
    public ReturnResult<FileOperationResponse> writeFileContent(Integer serverId, String filePath, String content,
            String encoding, Boolean createBackup) {
        try {
            // 暂时返回不支持的错误，因为当前的FileOperationRequest不支持content字段
            // 后续可以通过扩展FileOperationRequest或使用其他方式实现
            return ReturnResult.error("当前版本暂不支持直接写入文件内容，请使用文件上传功能");

        } catch (Exception e) {
            log.error("保存文件内容失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("保存文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件管理模式
     */
    public String getFileManagementMode(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerSetting> result = serverSettingService.getByServerId(serverId);
            if (result.isOk() && result.getData() != null) {
                return MonitorSysGenServerSettingUtils.getFileManagementMode(result.getData());
            }
            return null;
        } catch (Exception e) {
            log.error("获取文件管理模式失败: serverId={}", serverId, e);
            return null;
        }
    }

    /**
     * 根据服务器ID获取对应的文件管理服务
     */
    private FileManagementService getFileManagementService(Integer serverId) {
        String mode = getFileManagementMode(serverId);
        if (mode == null) {
            log.warn("服务器文件管理模式未配置: serverId={}", serverId);
            return null;
        }

        log.debug("服务器文件管理模式: serverId={}, mode={}", serverId, mode);

        switch (mode.toUpperCase()) {
        case "LOCAL":
            return localFileManagementService;
        case "SSH":
            return sshFileManagementService;
        case "NODE":
            return nodeFileManagementService;
        case "API":
            return apiFileManagementService;
        case "NONE":
        default:
            log.warn("文件管理功能未启用或模式不支持: serverId={}, mode={}", serverId, mode);
            return null;
        }
    }
}
