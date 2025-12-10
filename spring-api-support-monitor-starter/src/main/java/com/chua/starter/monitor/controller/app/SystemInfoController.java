package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统信息控制器
 * 提供系统磁盘、目录等信息查询功能
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "系统信息管理")
@Tag(name = "系统信息管理", description = "系统磁盘、目录等信息查询相关接口")
@Slf4j
@RestController
@RequestMapping("/v1/system-info")
@RequiredArgsConstructor
public class SystemInfoController {

    /**
     * 磁盘驱动器信息
     */
    @Data
    @ApiModel(description = "磁盘驱动器信息")
    public static class DriveInfo {
        @ApiModelProperty(value = "驱动器路径")
        private String path;

        @ApiModelProperty(value = "驱动器名称")
        private String name;

        @ApiModelProperty(value = "驱动器类型")
        private String type;

        @ApiModelProperty(value = "总空间(字节)")
        private Long totalSpace;

        @ApiModelProperty(value = "可用空间(字节)")
        private Long freeSpace;

        @ApiModelProperty(value = "已用空间(字节)")
        private Long usedSpace;
    }

    /**
     * 目录信息
     */
    @Data
    @ApiModel(description = "目录信息")
    public static class DirectoryInfo {
        @ApiModelProperty(value = "目录路径")
        private String path;

        @ApiModelProperty(value = "目录名称")
        private String name;

        @ApiModelProperty(value = "是否为目录")
        private Boolean isDirectory;

        @ApiModelProperty(value = "文件大小(字节)")
        private Long size;

        @ApiModelProperty(value = "最后修改时间")
        private Long lastModified;

        @ApiModelProperty(value = "是否可读")
        private Boolean canRead;

        @ApiModelProperty(value = "是否可写")
        private Boolean canWrite;

        @ApiModelProperty(value = "是否隐藏")
        private Boolean hidden;
    }

    /**
     * 获取系统磁盘驱动器列表
     */
    @ApiOperation("获取系统磁盘驱动器列表")
    @Operation(summary = "获取系统磁盘驱动器列表", description = "获取当前系统所有可用的磁盘驱动器信息")
    @GetMapping("/drives")
    public ReturnResult<List<DriveInfo>> getSystemDrives() {
        log.info("获取系统磁盘驱动器列表");

        try {
            List<DriveInfo> drives = new ArrayList<>();
            File[] roots = File.listRoots();

            for (File root : roots) {
                DriveInfo drive = new DriveInfo();
                drive.setPath(root.getAbsolutePath());
                drive.setName(getDriveName(root));
                drive.setType(getDriveType(root));
                drive.setTotalSpace(root.getTotalSpace());
                drive.setFreeSpace(root.getFreeSpace());
                drive.setUsedSpace(root.getTotalSpace() - root.getFreeSpace());
                drives.add(drive);
            }

            log.info("获取到 {} 个磁盘驱动器", drives.size());
            return ReturnResult.ok(drives);
        } catch (Exception e) {
            log.error("获取系统磁盘驱动器列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定路径下的目录列表
     */
    @ApiOperation("获取目录列表")
    @Operation(summary = "获取目录列表", description = "获取指定路径下的所有目录（不包含文件）")
    @GetMapping("/directories")
    public ReturnResult<List<DirectoryInfo>> getDirectories(
            @ApiParam("目录路径") @Parameter(description = "目录路径") @RequestParam String path,
            @ApiParam("是否包含隐藏目录") @Parameter(description = "是否包含隐藏目录") @RequestParam(defaultValue = "false") Boolean includeHidden) {

        log.info("获取目录列表: path={}, includeHidden={}", path, includeHidden);

        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return ReturnResult.error("指定路径不存在或不是目录");
            }

            List<DirectoryInfo> directories = Files.list(dirPath)
                    .filter(Files::isDirectory)
                    .filter(p -> includeHidden || !isHidden(p))
                    .map(this::convertToDirectoryInfo)
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .collect(Collectors.toList());

            log.info("获取到 {} 个目录", directories.size());
            return ReturnResult.ok(directories);
        } catch (Exception e) {
            log.error("获取目录列表失败: path={}", path, e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建目录
     */
    @ApiOperation("创建目录")
    @Operation(summary = "创建目录", description = "在指定路径下创建新目录")
    @PostMapping("/directories")
    public ReturnResult<Boolean> createDirectory(
            @ApiParam("父目录路径") @Parameter(description = "父目录路径") @RequestParam String parentPath,
            @ApiParam("新目录名称") @Parameter(description = "新目录名称") @RequestParam String directoryName) {

        log.info("创建目录: parentPath={}, directoryName={}", parentPath, directoryName);

        try {
            Path parentDir = Paths.get(parentPath);
            if (!Files.exists(parentDir) || !Files.isDirectory(parentDir)) {
                return ReturnResult.error("父目录不存在或不是目录");
            }

            Path newDir = parentDir.resolve(directoryName);
            if (Files.exists(newDir)) {
                return ReturnResult.error("目录已存在");
            }

            Files.createDirectories(newDir);
            log.info("目录创建成功: {}", newDir.toAbsolutePath());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("创建目录失败: parentPath={}, directoryName={}", parentPath, directoryName, e);
            return ReturnResult.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 验证路径是否有效
     */
    @ApiOperation("验证路径")
    @Operation(summary = "验证路径", description = "验证指定路径是否存在且可访问")
    @GetMapping("/validate-path")
    public ReturnResult<Boolean> validatePath(
            @ApiParam("路径") @Parameter(description = "路径") @RequestParam String path) {

        log.info("验证路径: path={}", path);

        try {
            Path dirPath = Paths.get(path);
            boolean exists = Files.exists(dirPath);
            boolean isDirectory = Files.isDirectory(dirPath);
            boolean canRead = Files.isReadable(dirPath);
            boolean canWrite = Files.isWritable(dirPath);

            if (!exists) {
                return ReturnResult.error("路径不存在");
            }
            if (!isDirectory) {
                return ReturnResult.error("路径不是目录");
            }
            if (!canRead) {
                return ReturnResult.error("路径不可读");
            }
            if (!canWrite) {
                return ReturnResult.error("路径不可写");
            }

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("验证路径失败: path={}", path, e);
            return ReturnResult.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取驱动器名称
     */
    private String getDriveName(File root) {
        String path = root.getAbsolutePath();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return "本地磁盘 (" + path.substring(0, 2) + ")";
        } else {
            return path.equals("/") ? "根目录" : path;
        }
    }

    /**
     * 获取驱动器类型
     */
    private String getDriveType(File root) {
        // 简单的驱动器类型判断，实际可以通过JNI或其他方式获取更准确的信息
        return "FIXED";
    }

    /**
     * 判断文件是否隐藏
     */
    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 转换为目录信息对象
     */
    private DirectoryInfo convertToDirectoryInfo(Path path) {
        DirectoryInfo info = new DirectoryInfo();
        info.setPath(path.toAbsolutePath().toString());
        info.setName(path.getFileName().toString());
        info.setIsDirectory(Files.isDirectory(path));

        try {
            info.setSize(Files.isDirectory(path) ? 0L : Files.size(path));
            info.setLastModified(Files.getLastModifiedTime(path).toMillis());
            info.setCanRead(Files.isReadable(path));
            info.setCanWrite(Files.isWritable(path));
            info.setHidden(Files.isHidden(path));
        } catch (Exception e) {
            log.warn("获取文件属性失败: {}", path, e);
            info.setSize(0L);
            info.setLastModified(0L);
            info.setCanRead(false);
            info.setCanWrite(false);
            info.setHidden(false);
        }

        return info;
    }
}
