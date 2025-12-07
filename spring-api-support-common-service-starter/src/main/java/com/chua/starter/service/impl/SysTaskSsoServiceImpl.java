package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.binary.ByteSource;
import com.chua.common.support.binary.ByteSourceArray;
import com.chua.common.support.binary.ByteSourceFile;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.FileStorage;
import com.chua.common.support.oss.extension.NamingResolver;
import com.chua.common.support.oss.metadata.MapMetadata;
import com.chua.common.support.oss.metadata.Metadata;
import com.chua.common.support.oss.request.PutObjectRequest;
import com.chua.common.support.oss.result.PutObjectResult;
import com.chua.common.support.oss.setting.BucketSetting;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.service.properties.SsoProperties;
import com.chua.starter.service.entity.SysTaskSso;
import com.chua.starter.service.entity.SysTaskSsoItem;
import com.chua.starter.service.mapper.SysTaskSsoMapper;
import com.chua.starter.service.pojo.sso.*;
import com.chua.starter.service.SysTaskSsoItemService;
import com.chua.starter.service.SysTaskSsoService;
import com.chua.starter.service.task.SysSsoTaskTaskRegistration;
import com.chua.starter.service.task.SysTaskSsoWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * 分片
 * @since 2024/12/4
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysTaskSsoServiceImpl extends ServiceImpl<SysTaskSsoMapper, SysTaskSso> implements SysTaskSsoService {

    private final Environment environment;
    private static final long CHUNK_SIZE = 1024 * 1024 * 100L;
    private final TransactionTemplate transactionTemplate;
    private final SysTaskSsoItemService sysTaskSsoItemService;
    private final SsoProperties ssoProperties;
    private final SysSsoTaskTaskRegistration sysSsoTaskTaskRegistration;

    @Override
    public ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, MultipartFile multipartFile) {
        BucketSetting bucketSetting = createSetting(request, multipartFile.getOriginalFilename());

        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(multipartFile.getInputStream()))
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            return ReturnResult.error(e);
        }
    }

    @Override
    public ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, File file) {
        BucketSetting bucketSetting = createSetting(request, file.getName());

        try (FileStorage fileStorage = FileStorage.createStorage(request.getFileBucket(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceFile(file))
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(file.getName())))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            return ReturnResult.error(e);
        }
    }

    @Override
    public ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, String fileName, byte[] bytes) {
        BucketSetting bucketSetting = createSetting(request, fileName);
        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(bytes))
                    .fileName(StringUtils.defaultString(request.getFileName(), fileName))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            return ReturnResult.error(e);
        }
    }

    /**
     * 创建设置
     *
     * @param request          请求
     * @param originalFilename
     * @return 设置
     */
    private BucketSetting createSetting(SysFileUploadRequest request, String originalFilename) {
        return BucketSetting.builder()
                .bucket(request.getFileBucket())
                .nameResolver(new NamingResolver() {
                    @Override
                    public String transform(Metadata metadata, ByteSource byteSource) {
                        return StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(originalFilename));
                    }
                })
                .accessKeyId(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase()+ ".app-id"))
                .accessKeySecret(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase()+ ".app-secret"))
                .charset(StandardCharsets.UTF_8)
                .endpoint(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase()+ ".endpoint", "oss/system"))
                .region(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase()+ ".region"))
                .build();
    }

    @Override
    public ReturnResult<SysFileUploadTaskResponse> createTask(SysFileUploadTaskRequest request) {
        long fileSize = request.getFileSize();
        int chuck =  (int) Math.ceil(((double)fileSize / CHUNK_SIZE));
        String taskUid = IdUtils.createUlid();
        SysTaskSso sysTaskSso = new SysTaskSso();
        sysTaskSso.setSysTaskSsoTid(taskUid);
        sysTaskSso.setSysTaskSsoName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(request.getFileType())));
        sysTaskSso.setSysTaskSsoNumber(chuck);
        sysTaskSso.setSysTaskSsoStatus(0);
        sysTaskSso.setSysTaskSsoSummary(request.getFileSummary());
        sysTaskSso.setSysTaskSsoTotal(fileSize);
        sysTaskSso.setSysTaskSsoMd5(request.getFileMd5());
        sysTaskSso.setSysTaskSsoType(request.getSsoType());
        sysTaskSso.setSysTaskBucket(request.getFileBucket());

        return transactionTemplate.execute(status -> {
            save(sysTaskSso);
            List<SysTaskSsoItem> sysTaskSsoItems = new ArrayList<>(chuck);
            for (int i = 0; i < chuck; i++) {
                SysTaskSsoItem sysTaskSsoItem = new SysTaskSsoItem();
                sysTaskSsoItem.setSysTaskSsoItemSeq(i + 1);
                sysTaskSsoItem.setSysTaskSsoItemMd5(request.getFileMd5());
                sysTaskSsoItem.setSysTaskSsoItemStatus(0);
                sysTaskSsoItem.setSysTaskSsoId(sysTaskSso.getSysTaskSsoId());

                sysTaskSsoItems.add(sysTaskSsoItem);
            }

            sysTaskSsoItemService.saveBatch(sysTaskSsoItems);
            SysFileUploadTaskResponse response = new SysFileUploadTaskResponse();
            response.setTaskId(taskUid);
            response.setSetSysTaskSsoNumber(chuck);
            response.setSysTaskSsoChuck(CHUNK_SIZE);
            response.setUrl(UrlUtils.normalize(request.getFileBucket(), sysTaskSso.getSysTaskSsoName()));
            return ReturnResult.ok(response);
        });
    }

    @Override
    public ReturnResult<SysFileUploadTaskPartResponse> uploadTask(SysFileUploadTaskPartRequest request, MultipartFile multipartFile) {
        SysTaskSso sysTaskSso = getOne(Wrappers.<SysTaskSso>lambdaQuery()
                .eq(SysTaskSso::getSysTaskSsoTid, request.getTaskId())
        );

        if(null == sysTaskSso) {
            return ReturnResult.error("任务不存在");
        }

        if(sysTaskSso.getSysTaskSsoStatus() == 2) {
            return ReturnResult.error("任务已上传");
        }

        SysTaskSsoItem sysTaskSsoItem = sysTaskSsoItemService.getOne(Wrappers.<SysTaskSsoItem>lambdaQuery()
                .eq(SysTaskSsoItem::getSysTaskSsoId, sysTaskSso.getSysTaskSsoId())
                .eq(SysTaskSsoItem::getSysTaskSsoItemSeq, request.getTaskSeq() + 1)
        );

        if(null == sysTaskSsoItem) {
            return ReturnResult.error("分片不存在");
        }

        if(sysTaskSsoItem.getSysTaskSsoItemStatus() == 2) {
            return ReturnResult.error("分片已上传");
        }

        if(sysTaskSsoItem.getSysTaskSsoItemStatus() == 1) {
            return ReturnResult.error("分片正在上传");
        }

        sysTaskSsoItem.setSysTaskSsoItemStatus(2);
        sysTaskSsoItem.setSysTaskSsoItemMd5(request.getFileMd5());
        sysTaskSsoItem.setSysTaskSsoItemLocalPath(FileUtils.normalize(ssoProperties.getTempPath(), request.getTaskId(), request.getTaskSeq() +""));
        try {
            sysTaskSsoItem.transferFrom(multipartFile);
        } catch (Exception e) {
            return ReturnResult.error("分片新增失败, 请重试!");
        }
        sysTaskSsoItemService.updateById(sysTaskSsoItem);
        checkFullFile(sysTaskSso);
        return ReturnResult.ok(new SysFileUploadTaskPartResponse());
    }

    private void checkFullFile(SysTaskSso sysTaskSso) {
        List<SysTaskSsoItem> list = sysTaskSsoItemService.list(Wrappers.<SysTaskSsoItem>lambdaQuery()
                .eq(SysTaskSsoItem::getSysTaskSsoId, sysTaskSso.getSysTaskSsoId())
                .eq(SysTaskSsoItem::getSysTaskSsoItemStatus, 2));

        if(list.size() != sysTaskSso.getSysTaskSsoNumber()) {
            return;
        }

        sysTaskSso.setSysTaskSsoStatus(2);
        baseMapper.updateById(sysTaskSso);
        sysSsoTaskTaskRegistration.register(new SysTaskSsoWrapper(sysTaskSso, this));
    }
}
