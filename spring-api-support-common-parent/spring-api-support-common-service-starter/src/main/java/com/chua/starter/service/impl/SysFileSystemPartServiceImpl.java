package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.mapper.SysFileSystemPartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文件系统分片服务实现类
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileSystemPartServiceImpl extends ServiceImpl<SysFileSystemPartMapper, SysFileSystemPart> implements SysFileSystemPartService {

}
