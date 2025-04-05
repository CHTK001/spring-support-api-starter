package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.mapper.MaintenanceFileMapper;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import com.jcraft.jsch.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 维护文件服务实现
 * 提供文件上传、文件解压等操作
 *
 * @author CH
 */
@Service
public class MaintenanceFileServiceImpl implements MaintenanceFileService {

    /**
     * 维护主机数据访问对象
     */
    @Resource
    private MaintenanceHostMapper maintenanceHostMapper;

    /**
     * 维护文件数据访问对象
     */
    @Resource
    private MaintenanceFileMapper maintenanceFileMapper;

    /**
     * 上传文件到维护组下的所有主机
     *
     * @param file    文件对象
     * @param request 请求参数
     * @return 上传结果
     */
    @Override
    public ReturnResult<FileUploadResponse> uploadFile(MultipartFile file, FileUploadRequest request) {
        try {
            // 1. 查询维护组下的所有启用的主机
            LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, request.getMaintenanceGroupId())
                    .eq(MaintenanceHost::getMaintenanceHostEnabled, true);
            List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(queryWrapper);

            if (hosts.isEmpty()) {
                return ReturnResult.error("维护组下没有可用的主机");
            }

            // 2. 保存文件到临时目录
            Path tempFile = Files.createTempFile("upload_", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            // 3. 记录文件信息
            MaintenanceFile maintenanceFile = new MaintenanceFile();
            maintenanceFile.setMaintenanceGroupId(request.getMaintenanceGroupId());
            maintenanceFile.setMaintenanceFileName(file.getOriginalFilename());
            maintenanceFile.setMaintenanceFilePath(request.getTargetPath());
            maintenanceFile.setMaintenanceFileSize(file.getSize());

            String fileType = getFileExtension(file.getOriginalFilename());
            boolean isCompressed = "zip".equalsIgnoreCase(fileType) || "tar".equalsIgnoreCase(fileType) ||
                    "gz".equalsIgnoreCase(fileType);

            maintenanceFile.setMaintenanceFileIsCompressed(isCompressed);
            maintenanceFile.setMaintenanceFileCreateTime(LocalDateTime.now());

            maintenanceFileMapper.insert(maintenanceFile);

            // 4. 上传文件到每个主机
            FileUploadResponse response = new FileUploadResponse();
            Map<String, String> results = new HashMap<>();
            boolean allSuccess = true;

            for (MaintenanceHost host : hosts) {
                try {
                    uploadFileToHost(
                            tempFile.toFile(),
                            host.getMaintenanceHostAddress(),
                            host.getMaintenanceHostPort(),
                            host.getMaintenanceHostUsername(),
                            host.getMaintenanceHostPassword(),
                            request.getTargetPath(),
                            file.getOriginalFilename(),
                            request.getOverwrite(),
                            isCompressed && request.getExtract()
                    );
                    results.put(host.getMaintenanceHostAddress(), "上传成功");
                } catch (Exception e) {
                    results.put(host.getMaintenanceHostAddress(), "上传失败: " + e.getMessage());
                    allSuccess = false;
                }
            }

            // 5. 删除临时文件
            Files.delete(tempFile);

            response.setResults(results);
            response.setAllSuccess(allSuccess);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 上传文件到指定主机
     *
     * @param file       文件
     * @param host       主机地址
     * @param port       端口
     * @param username   用户名
     * @param password   密码
     * @param targetPath 目标路径
     * @param fileName   文件名
     * @param overwrite  是否覆盖
     * @param extract    是否解压
     * @throws JSchException JSch异常
     * @throws SftpException SFTP异常
     * @throws IOException   IO异常
     */
    private void uploadFileToHost(File file, String host, Integer port, String username, String password,
                                  String targetPath, String fileName, Boolean overwrite, Boolean extract)
            throws JSchException, SftpException, IOException {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 建立SSH连接
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(30000);

            // 打开SFTP通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(30000);

            // 创建目标目录（如果不存在）
            try {
                channelSftp.cd(targetPath);
            } catch (SftpException e) {
                // 目录不存在，创建目录
                String[] folders = targetPath.split("/");
                String path = "";
                for (String folder : folders) {
                    if (folder.isEmpty()) {
                        path += "/";
                        continue;
                    }
                    path += folder + "/";
                    try {
                        channelSftp.cd(path);
                    } catch (SftpException ex) {
                        channelSftp.mkdir(path);
                        channelSftp.cd(path);
                    }
                }
            }

            // 检查文件是否已存在
            try {
                channelSftp.lstat(targetPath + "/" + fileName);
                // 文件存在
                if (overwrite == null || !overwrite) {
                    try {
                        throw new Exception("文件已存在且未指定覆盖");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                // 如果需要覆盖，先删除现有文件
                channelSftp.rm(targetPath + "/" + fileName);
            } catch (SftpException e) {
                // 文件不存在，可以继续
            }

            // 上传文件
            channelSftp.put(new FileInputStream(file), fileName);

            // 如果是压缩文件且需要解压
            if (extract) {
                // 使用exec通道执行解压命令
                Channel execChannel = null;
                try {
                    execChannel = session.openChannel("exec");
                    String command;
                    String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

                    if (fileName.toLowerCase().endsWith(".zip")) {
                        // 先清除目标目录内容，然后解压
                        command = "mkdir -p " + targetPath + " && " +
                                "rm -rf " + targetPath + "/* && " +
                                "unzip -o " + fullPath + " -d " + targetPath;
                    } else if (fileName.toLowerCase().endsWith(".tar") ||
                            fileName.toLowerCase().endsWith(".tar.gz") ||
                            fileName.toLowerCase().endsWith(".tgz")) {
                        command = "mkdir -p " + targetPath + " && " +
                                "rm -rf " + targetPath + "/* && " +
                                "tar -xf " + fullPath + " -C " + targetPath;
                    } else {
                        try {
                            throw new Exception("不支持的压缩文件格式");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    ((ChannelExec) execChannel).setCommand(command);

                    execChannel.setInputStream(null);
                    execChannel.connect(30000);

                    // 等待命令执行完成
                    while (!execChannel.isClosed()) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            // 忽略
                        }
                    }

                    if (execChannel.getExitStatus() != 0) {
                        try {
                            throw new Exception("解压失败，退出状态码: " + execChannel.getExitStatus());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } finally {
                    if (execChannel != null) {
                        execChannel.disconnect();
                    }
                }
            }
        } finally {
            // 关闭连接
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
} 