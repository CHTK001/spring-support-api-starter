package com.chua.spider.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.spider.support.dto.SpiderDefinitionSaveRequest;
import com.chua.spider.support.entity.SpiderDataRecord;
import com.chua.spider.support.entity.SpiderDefinition;
import com.chua.spider.support.entity.SpiderExecutionRecord;
import com.chua.spider.support.mapper.SpiderDataRecordMapper;
import com.chua.spider.support.mapper.SpiderDefinitionMapper;
import com.chua.spider.support.mapper.SpiderExecutionRecordMapper;
import com.chua.spider.support.service.SpiderCenterService;
import com.chua.spider.support.vo.SpiderDataRecordVo;
import com.chua.spider.support.vo.SpiderDefinitionVo;
import com.chua.spider.support.vo.SpiderExecutionRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 爬虫中心服务实现。
 *
 * <p>基于 MyBatis-Plus 提供爬虫定义、采集数据、执行记录的分页查询与增删改。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
public class SpiderCenterServiceImpl implements SpiderCenterService {

    /**
     * 爬虫定义 Mapper
     */
    private final SpiderDefinitionMapper definitionMapper;

    /**
     * 采集数据记录 Mapper
     */
    private final SpiderDataRecordMapper dataRecordMapper;

    /**
     * 爬虫执行记录 Mapper
     */
    private final SpiderExecutionRecordMapper executionRecordMapper;

    /**
     * 构造服务实现。
     *
     * @param definitionMapper      爬虫定义 Mapper
     * @param dataRecordMapper      采集数据记录 Mapper
     * @param executionRecordMapper 爬虫执行记录 Mapper
     */
    public SpiderCenterServiceImpl(SpiderDefinitionMapper definitionMapper,
                                   SpiderDataRecordMapper dataRecordMapper,
                                   SpiderExecutionRecordMapper executionRecordMapper) {
        this.definitionMapper = definitionMapper;
        this.dataRecordMapper = dataRecordMapper;
        this.executionRecordMapper = executionRecordMapper;
    }

    @Override
    public Page pageDefinitions(int pageNo, int pageSize, String keyword) {
        Page<SpiderDefinition> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SpiderDefinition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SpiderDefinition::getSpiderCode, keyword)
                    .or()
                    .like(SpiderDefinition::getSpiderName, keyword));
        }
        wrapper.orderByDesc(SpiderDefinition::getUpdateTime);

        Page<SpiderDefinition> result = definitionMapper.selectPage(page, wrapper);
        Page<SpiderDefinitionVo> voPage = new Page<>(pageNo, pageSize, result.getTotal());
        List<SpiderDefinitionVo> vos = result.getRecords().stream()
                .map(SpiderDefinitionVo::from)
                .collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public SpiderDefinitionVo getDefinition(String spiderCode) {
        LambdaQueryWrapper<SpiderDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpiderDefinition::getSpiderCode, spiderCode);
        return SpiderDefinitionVo.from(definitionMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpiderDefinitionVo saveDefinition(SpiderDefinitionSaveRequest req) {
        SpiderDefinition existing = findByCode(req.getSpiderCode());
        SpiderDefinition target = existing == null ? new SpiderDefinition() : existing;
        BeanUtils.copyProperties(req, target);
        if (existing == null) {
            definitionMapper.insert(target);
        } else {
            definitionMapper.updateById(target);
        }
        log.info("保存爬虫定义: code={}, depth={}, maxPages={}",
                target.getSpiderCode(), target.getSpiderMaxDepth(), target.getSpiderMaxPages());
        return SpiderDefinitionVo.from(target);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDefinition(String spiderCode) {
        SpiderDefinition existed = findByCode(spiderCode);
        if (existed == null) {
            return false;
        }
        int rows = definitionMapper.deleteById(existed.getSpiderId());
        return rows > 0;
    }

    @Override
    public boolean toggleStatus(String spiderCode, Integer newStatus) {
        SpiderDefinition existed = findByCode(spiderCode);
        if (existed == null) {
            return false;
        }
        existed.setSpiderStatus(newStatus);
        int rows = definitionMapper.updateById(existed);
        return rows > 0;
    }

    @Override
    public Page pageDataRecords(int pageNo, int pageSize, String spiderDefinitionCode, String spiderExecutionNo, Integer depth) {
        Page<SpiderDataRecord> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SpiderDataRecord> wrapper = new LambdaQueryWrapper<>();
        if (spiderDefinitionCode != null && !spiderDefinitionCode.isBlank()) {
            wrapper.eq(SpiderDataRecord::getSpiderDefinitionCode, spiderDefinitionCode);
        }
        if (spiderExecutionNo != null && !spiderExecutionNo.isBlank()) {
            wrapper.eq(SpiderDataRecord::getSpiderExecutionNo, spiderExecutionNo);
        }
        if (depth != null) {
            wrapper.eq(SpiderDataRecord::getSpiderDepth, depth);
        }
        wrapper.orderByDesc(SpiderDataRecord::getCreateTime);

        Page<SpiderDataRecord> result = dataRecordMapper.selectPage(page, wrapper);
        Page<SpiderDataRecordVo> voPage = new Page<>(pageNo, pageSize, result.getTotal());
        List<SpiderDataRecordVo> vos = result.getRecords().stream()
                .map(SpiderDataRecordVo::from)
                .collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public Page pageExecutions(int pageNo, int pageSize, String spiderDefinitionCode) {
        Page<SpiderExecutionRecord> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SpiderExecutionRecord> wrapper = new LambdaQueryWrapper<>();
        if (spiderDefinitionCode != null && !spiderDefinitionCode.isBlank()) {
            wrapper.eq(SpiderExecutionRecord::getSpiderDefinitionCode, spiderDefinitionCode);
        }
        wrapper.orderByDesc(SpiderExecutionRecord::getCreateTime);

        Page<SpiderExecutionRecord> result = executionRecordMapper.selectPage(page, wrapper);
        Page<SpiderExecutionRecordVo> voPage = new Page<>(pageNo, pageSize, result.getTotal());
        List<SpiderExecutionRecordVo> vos = result.getRecords().stream()
                .map(SpiderExecutionRecordVo::from)
                .collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    /**
     * 根据编码查询爬虫定义。
     *
     * @param spiderCode 爬虫编码
     * @return 爬虫定义实体，不存在时返回 null
     */
    private SpiderDefinition findByCode(String spiderCode) {
        if (spiderCode == null || spiderCode.isBlank()) {
            return null;
        }
        LambdaQueryWrapper<SpiderDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpiderDefinition::getSpiderCode, spiderCode);
        wrapper.last("LIMIT 1");
        return definitionMapper.selectOne(wrapper);
    }
}
