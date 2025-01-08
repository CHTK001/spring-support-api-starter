package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.response.FileSystemResponse;
import com.chua.starter.monitor.service.FileSystemService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件系统服务
 *
 * @author CH
 */
@Service
public class FileSystemServiceImpl implements FileSystemService {
    @Override
    public ReturnResult<List<FileSystemResponse>> list(String parentPath, String name) {
        if (StringUtils.isEmpty(parentPath)) {
            List<FileSystemResponse> list = new LinkedList<>();
            FileSystems.getDefault().getFileStores().forEach(fileStore -> {
                list.add(new FileSystemResponse(fileStore.name(), fileStore.name(), FileSystemResponse.FileType.DIRECTORY));
            });

            return ReturnResult.ok(list);
        }

        try {
            return ReturnResult.ok(Files.list(Path.of(parentPath))
                    .map(path -> new FileSystemResponse(path.getFileName().toString(), path.toString(), path.toFile().isDirectory() ?  FileSystemResponse.FileType.DIRECTORY : FileSystemResponse.FileType.FILE))
                    .toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
