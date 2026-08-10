package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.mapper.SysFileSystemPartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文件系统分片服务实现类
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileSystemPartServiceImpl extends ServiceImpl<SysFileSystemPartMapper, SysFileSystemPart> implements SysFileSystemPartService {

    @Override
    public List<SysFileSystemPart> listByFileId(Integer fileId) {
        if (fileId == null) {
            return List.of();
        }
        return list(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)
                .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));
    }

    @Override
    public List<SysFileSystemPart> listByFileIdAndVersion(Integer fileId, Integer version) {
        if (fileId == null || version == null) {
            return List.of();
        }
        return list(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)
                .eq(SysFileSystemPart::getSysFileSystemPartVersion, version)
                .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByFileId(Integer fileId) {
        if (fileId == null) {
            return 0;
        }
        return Math.toIntExact(count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)));
    }

    @Override
    public long countCompletedByFileId(Integer fileId) {
        if (fileId == null) {
            return 0L;
        }
        return count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetFailedToPending(Integer fileId) {
        if (fileId == null) {
            return 0;
        }
        return baseMapper.update(null, Wrappers.<SysFileSystemPart>lambdaUpdate()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.ERROR)
                .set(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.PENDING)
                .set(SysFileSystemPart::getSysFileSystemPartErrorMessage, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markProcessing(Integer fileId) {
        if (fileId == null) {
            return 0;
        }
        return baseMapper.update(null, Wrappers.<SysFileSystemPart>lambdaUpdate()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId)
                .set(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.UPLOADING)
                .set(SysFileSystemPart::getSysFileSystemPartProcessTime, java.time.LocalDateTime.now()));
    }

    @Override
    public ReturnResult<Boolean> verifyIntegrity(Integer fileId, int expectedChunks) {
        if (fileId == null || expectedChunks <= 0) {
            return ReturnResult.error("参数无效");
        }
        long completed = countCompletedByFileId(fileId);
        if (completed != expectedChunks) {
            return ReturnResult.ok(false);
        }
        long total = count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, fileId));
        return ReturnResult.ok(total == expectedChunks);
    }
}
