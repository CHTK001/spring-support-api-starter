package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysSfc;
import com.chua.starter.system.entity.SysSfcInstall;
import com.chua.starter.system.mapper.SysSfcInstallMapper;
import com.chua.starter.system.mapper.SysSfcMapper;
import com.chua.starter.system.service.SysSfcService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/10/24
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysSfcServiceImpl extends ServiceImpl<SysSfcMapper, SysSfc> implements SysSfcService{

    final TransactionTemplate transactionTemplate;
    final SysSfcInstallMapper sysSfcInstallMapper;

    @Override
    public IPage<SysSfc> pageForUserId(Query<SysSfc> query, SysSfc sysSfc, Set<String> roles, String userId) {
        return baseMapper.selectPageForSfc(query.createPage(), sysSfc, userId);
    }

    @Override
    public ReturnResult<Boolean> uploadSfc(MultipartFile multipartFile, SysSfc sysSfc) {
        if(null == multipartFile) {
            return ReturnResult.error("请上传组件");
        }
        Integer sysSfcId = sysSfc.getSysSfcId();
        File file = new File("./sfc/" + sysSfcId);
        File filePath = new File(file, StringUtils.defaultString(multipartFile.getOriginalFilename(), multipartFile.getName()));

        return transactionTemplate.execute(stats -> {
            sysSfc.setSysSfcPath(filePath.getPath());
            baseMapper.updateById(sysSfc);
            FileUtils.forceDeleteDirectory(file);
            FileUtils.mkParentDirs(file);
            try (InputStream is = multipartFile.getInputStream()){
                FileUtils.writeTo(is, filePath);
            } catch (IOException e) {
                return ReturnResult.error("请重新上传");
            }

            return ReturnResult.of(true);
        });
    }

    @Override
    public ReturnResult<SysSfc> save(SysSfc sysSfc, Set<String> roles) {
        if(!AuthConstant.hasAdmin(roles)) {
            return ReturnResult.error("无权限新增组件");
        }
        sysSfc.setSysSfcStatus(1);
        baseMapper.insert(sysSfc);
        return ReturnResult.of(sysSfc);
    }

    @Override
    public ReturnResult<Boolean> updateById(SysSfc sysSfc, Set<String> roles) {
        if(!AuthConstant.hasAdmin(roles)) {
            return ReturnResult.error("无权限修改组件");
        }
        return ReturnResult.of(baseMapper.updateById(sysSfc) > 0);
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysSfcId, Set<String> roles) {
        if(!AuthConstant.hasAdmin(roles)) {
            return ReturnResult.error("无权限删除组件");
        }

        if(baseMapper.deleteById(sysSfcId) > 0) {
            File file = new File("./sfc/" + sysSfcId);
            try {
                FileUtils.forceDeleteDirectory(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ReturnResult.of(true);
        }
        return ReturnResult.of(false);
    }

    @Override
    public ReturnResult<Boolean> install(SysSfc sysSfc, Integer userId) {
        if(sysSfcInstallMapper.exists(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getSysSfcId, sysSfc.getSysSfcId())
                .eq(SysSfcInstall::getUserId, userId)
        )) {
            return ReturnResult.error(StringUtils.format("组件{}已安装", sysSfc.getSysSfcName()));
        }

        return transactionTemplate.execute(stats -> {
            SysSfcInstall sysSfcInstall = new SysSfcInstall();
            sysSfcInstall.setSysSfcId(sysSfc.getSysSfcId());
            sysSfcInstall.setUserId(userId);

            if(sysSfcInstallMapper.insert(sysSfcInstall) > 0) {
                return ReturnResult.of(true);
            }

            return ReturnResult.error("安装失败");
        });
    }

    @Override
    public ReturnResult<Boolean> uninstall(SysSfc sysSfc, Integer userId) {
        if(!sysSfcInstallMapper.exists(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getSysSfcId, sysSfc.getSysSfcId())
                .eq(SysSfcInstall::getUserId, userId)
        )) {
            return ReturnResult.error(StringUtils.format("组件{}未安装", sysSfc.getSysSfcName()));
        }

        return transactionTemplate.execute(stats -> {
            if(sysSfcInstallMapper.delete(Wrappers.<SysSfcInstall>lambdaQuery()
                    .eq(SysSfcInstall::getSysSfcId, sysSfc.getSysSfcId())
                    .eq(SysSfcInstall::getUserId, userId)
            ) > 0) {
                return ReturnResult.of(true);
            }

            return ReturnResult.error("卸载失败");
        });
    }

    @Override
    public ReturnResult<String> get(Long sysSfcId, Set<String> roles, String userId) {
        SysSfc sysSfc = baseMapper.selectById(sysSfcId);
        if(sysSfc.getSysSfcType() == 0 && StringUtils.isBlank(sysSfc.getSysSfcPath())) {
            return ReturnResult.error("组件不存在");
        }

        Integer sysSfcType = sysSfc.getSysSfcType();
        if(null == sysSfcType) {
            sysSfcType = 0;
        }

        if(sysSfcType == 1) {
            return ReturnResult.of(sysSfc.getSysSfcContent());
        }

        String sysSfcProxy = sysSfc.getSysSfcProxy();
        if(StringUtils.isNotBlank(sysSfcProxy)) {
            return ReturnResult.of(HttpClient
                    .get()
                    .url(UrlUtils.normalize(sysSfcProxy, sysSfc.getSysSfcPath()))
                    .newInvoker()
                    .execute()
                    .content(String.class)
            );
        }

        File newFile = new File( sysSfc.getSysSfcPath());
        if(!newFile.exists()) {
            return ReturnResult.error("组件不存在");
        }

        try (FileInputStream fis = new FileInputStream(newFile)) {
            return ReturnResult.of(IoUtils.toString(fis, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReturnResult<List<SysSfc>> mine(String sysSfcCategory, String userId) {
        return ReturnResult.of(baseMapper.mine(sysSfcCategory, userId));
    }
}

