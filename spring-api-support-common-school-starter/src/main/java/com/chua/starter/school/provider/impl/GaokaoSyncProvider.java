package com.chua.starter.school.provider.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.starter.school.dto.*;
import com.chua.starter.school.entity.*;
import com.chua.starter.school.enums.SyncType;
import com.chua.starter.school.mapper.*;
import com.chua.starter.school.provider.SchoolSyncProvider;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高考网同步提供者实现
 *
 * @author CH
 */
@Slf4j
@Service
@Spi("gaokao")
public class GaokaoSyncProvider implements SchoolSyncProvider {

    /**
     * 高考网基础URL
     */
    private static final String GAOKAO_BASE_URL = "https://www.gaokao.cn/school/";

    /**
     * 学校详情URL：后缀为空，即学校主页
     */
    private static final String SCHOOL_INFO_URL_SUFFIX = "";

    /**
     * 学校专业URL后缀
     */
    private static final String SCHOOL_MAJOR_URL_SUFFIX = "/speciality";

    /**
     * 学校招生计划URL后缀
     */
    private static final String SCHOOL_PLAN_URL_SUFFIX = "/provinceline";

    /**
     * 学校分数线URL后缀
     */
    private static final String SCHOOL_SCORE_URL_SUFFIX = "/provincescore";

    static final Integer[] PROVIDES = new Integer[]{11, 12, 13, 21, 31, 32, 33, 34, 35, 36, 37, 41, 42, 43, 44, 50, 51, 61};
    static final Integer[] YEARS = new Integer[]{2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029, 2030};
    @Autowired
    private SchoolInfoMapper schoolInfoMapper;

    @Autowired
    private SchoolDictMapper schoolDictMapper;
    @Autowired
    private SchoolRankMapper schoolRankMapper;

    @Autowired
    private SchoolMajorMapper schoolMajorMapper;

    @Autowired
    private SchoolAdmissionPlanMapper schoolAdmissionPlanMapper;

    @Autowired
    private SchoolHistoricalScoreMapper schoolHistoricalScoreMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public String getType() {
        return "gaokao";
    }

    @Override
    public boolean syncAllSchoolData(SchoolSyncConfig config) {
        // 使用TransactionTemplate替代@Transactional注解
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 同步所有学校基本信息
                syncSchoolInfo(config);
                
                // 获取所有学校ID
                List<SchoolInfo> allSchools = schoolInfoMapper.selectList(Wrappers.emptyWrapper());
                if (allSchools == null || allSchools.isEmpty()) {
                    log.warn("未找到任何学校信息");
                    return true;
                }
                
                // 对每个学校进行专业、招生计划和历年分数的同步
                for (SchoolInfo school : allSchools) {
                    try {
                        // 同步学校专业信息
                        registerMajor(school);
                        
                        // 同步招生计划
                        registerPlan(school);
                        
                        // 同步历年分数
                        syncHistoricalScores(school);
                    } catch (Exception e) {
                        log.error("同步学校数据异常: {}, {}", school.getSchoolCode(), e.getMessage());
                        // 继续处理下一个学校，不中断整体流程
                    }
                }
                
                return true;
            } catch (Exception e) {
                log.error("同步所有学校数据异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    /**
     * 同步单个学校的所有数据
     *
     * @param schoolCode 学校代码
     * @param config 同步配置
     * @return 是否同步成功
     */
    public boolean syncAllSchoolDataByCode(String schoolCode, SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 同步学校基本信息
                boolean infoResult = syncSchoolInfoByCode(schoolCode, config);
                if (!infoResult) {
                    log.error("同步学校基本信息失败: {}", schoolCode);
                    status.setRollbackOnly();
                    return false;
                }
                
                // 同步学校专业信息
                boolean majorResult = syncSchoolMajorByCode(schoolCode, config);
                if (!majorResult) {
                    log.warn("同步学校专业信息失败: {}", schoolCode);
                    // 不中断整体流程
                }
                
                // 同步学校招生计划
                boolean planResult = syncSchoolAdmissionPlanByCode(schoolCode, config);
                if (!planResult) {
                    log.warn("同步学校招生计划失败: {}", schoolCode);
                    // 不中断整体流程
                }
                
                // 同步学校历年分数
                boolean scoreResult = syncSchoolHistoricalScoreByCode(schoolCode, config);
                if (!scoreResult) {
                    log.warn("同步学校历年分数失败: {}", schoolCode);
                    // 不中断整体流程
                }
                
                return true;
            } catch (Exception e) {
                log.error("同步学校所有数据异常: {}, {}", schoolCode, e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    /**
     * 同步学校历年分数线
     *
     * @param schoolInfo 学校信息
     */
    private void syncHistoricalScores(SchoolInfo schoolInfo) {
        try {
            // 清除旧数据
            schoolHistoricalScoreMapper.delete(
                    Wrappers.<SchoolHistoricalScore>lambdaQuery()
                            .eq(SchoolHistoricalScore::getSchoolHistoricalScoreSchoolCode, schoolInfo.getSchoolCode())
            );
            
            // 遍历省份和年份
            for (Integer province : PROVIDES) {
                for (Integer year : YEARS) {
                    try {
                        // 获取分数线数据
                        String url = "https://api.zjzw.cn/web/api/?local_batch_id=&local_province_id=" + province + 
                                "&local_type_id=&page=1&school_id=" + schoolInfo.getSchoolCode() + 
                                "&size=10&uri=apidata/api/gkv3/score/school&year=" + year + 
                                "&signsafe=" + IdUtils.createUlid();
                        
                        // 调用API获取分数线数据并保存
                        GaokaoScore gaokaoScore = HttpClient
                                .get(url)
                                .newInvoker()
                                .execute()
                                .content(GaokaoScore.class);
                                
                        if (gaokaoScore != null && gaokaoScore.getData() != null && gaokaoScore.getData().getItem() != null) {
                            List<GaokaoScore.ItemDTO> item1 = gaokaoScore.getData().getItem();
                            for (GaokaoScore.ItemDTO item : item1) {
                                SchoolHistoricalScore score = new SchoolHistoricalScore();
                                score.setSchoolHistoricalScoreSchoolId(schoolInfo.getSchoolId());
                                score.setSchoolHistoricalScoreSchoolCode(schoolInfo.getSchoolCode());
                                score.setSchoolHistoricalScoreYear(Converter.createInteger(item.getYear()));
                                score.setSchoolHistoricalScoreProvince(province + Strings.repeat("0", 12 - String.valueOf(province).length()));
                                score.setSchoolHistoricalScoreSubjectType(item.getLocalTypeName());
                                score.setSchoolHistoricalScoreBatch(item.getLocalBatchName());
                                score.setSchoolHistoricalScoreAdmissionType(item.getZslxName());
                                score.setSchoolHistoricalScoreMaxScore(Converter.createInteger(item.getMax()));
                                score.setSchoolHistoricalScoreMinScore(Converter.createInteger(item.getMin()));
                                score.setSchoolHistoricalScoreAvgScore(Converter.createInteger(item.getAverage()));
                                score.setSchoolHistoricalScoreAdmissionCount(Converter.createInteger(item.getCount()));
                                score.setSchoolHistoricalScoreProvincialLine(Converter.createInteger(item.getProvinceScore()));
                                score.setSchoolHistoricalScoreLowestRank(Converter.createInteger(item.getMinSection()));
                                
                                // 如果有专业信息，设置专业相关字段
                                if (!Strings.isNullOrEmpty(item.getLevel3Name())) {
                                    // 查找对应的专业
                                    SchoolMajor major = schoolMajorMapper.selectOne(
                                            Wrappers.<SchoolMajor>lambdaQuery()
                                                    .eq(SchoolMajor::getSchoolCode, schoolInfo.getSchoolCode())
                                                    .eq(SchoolMajor::getSchoolMajorName, item.getLevel3Name())
                                    );
                                    
                                    if (major != null) {
                                        score.setSchoolHistoricalScoreMajorId(major.getSchoolMajorId());
                                        score.setSchoolHistoricalScoreMajorCode(major.getSchoolMajorCode());
                                    }
                                }
                                
                                schoolHistoricalScoreMapper.insert(score);
                            }
                        }
                    } catch (Exception e) {
                        log.error("同步学校历年分数异常: {}, 省份: {}, 年份: {}, {}", 
                                schoolInfo.getSchoolCode(), province, year, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("同步学校历年分数异常: {}, {}", schoolInfo.getSchoolCode(), e.getMessage());
        }
    }
    
    /**
     * 根据学校代码获取学校信息
     *
     * @param schoolCode 学校代码
     * @return 学校信息
     */
    private GaokaoSchoolInfo.DataDTO.ItemDTO getSchoolItemByCode(String schoolCode) {
        AtomicInteger page = new AtomicInteger(1);
        while (true) {
            try {
                GaokaoSchoolInfo schoolInfo = HttpClient
                        .get("https://api.zjzw.cn/web/api/?keyword=&page=" + page.get() + "&province_id=&ranktype=&request_type=1&size=20&type=&uri=apidata/api/gkv3/school/lists&signsafe=" + IdUtils.createUlid())
                        .newInvoker()
                        .execute()
                        .content(GaokaoSchoolInfo.class);

                if (null == schoolInfo || null == schoolInfo.getData() || null == schoolInfo.getData().getItem()) {
                    break;
                }
                
                // 查找匹配的学校
                for (GaokaoSchoolInfo.DataDTO.ItemDTO item : schoolInfo.getData().getItem()) {
                    if (String.valueOf(item.getSchoolId()).equals(schoolCode)) {
                        return item;
                    }
                }
                
                page.incrementAndGet();
                
                // 防止无限循环，设置最大页数限制
                if (page.get() > 100) {
                    break;
                }
            } catch (Exception e) {
                log.error("获取学校信息异常: {}, {}", schoolCode, e.getMessage());
                break;
            }
        }
        return null;
    }
    
    @Override
    public boolean syncSchoolInfoByCode(String schoolCode, SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 根据schoolCode获取学校信息
                GaokaoSchoolInfo.DataDTO.ItemDTO schoolItem = getSchoolItemByCode(schoolCode);
                if (schoolItem == null) {
                    log.error("未找到学校信息: {}", schoolCode);
                    return false;
                }
                
                // 注册学校信息
                registerSchoolInfo(schoolItem, config);
                return true;
            } catch (Exception e) {
                log.error("同步学校信息异常: {}, {}", schoolCode, e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    @Override
    public boolean syncSchoolMajorByCode(String schoolCode, SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 获取学校信息
                SchoolInfo schoolInfo = schoolInfoMapper.selectOne(
                        Wrappers.<SchoolInfo>lambdaQuery()
                                .eq(SchoolInfo::getSchoolCode, schoolCode)
                );
                
                if (schoolInfo == null) {
                    log.error("未找到学校信息: {}", schoolCode);
                    return false;
                }
                
                // 同步学校专业信息
                registerMajor(schoolInfo);
                return true;
            } catch (Exception e) {
                log.error("同步学校专业异常: {}, {}", schoolCode, e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    @Override
    public boolean syncSchoolAdmissionPlanByCode(String schoolCode, SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 获取学校信息
                SchoolInfo schoolInfo = schoolInfoMapper.selectOne(
                        Wrappers.<SchoolInfo>lambdaQuery()
                                .eq(SchoolInfo::getSchoolCode, schoolCode)
                );
                
                if (schoolInfo == null) {
                    log.error("未找到学校信息: {}", schoolCode);
                    return false;
                }
                
                // 同步学校招生计划
                registerPlan(schoolInfo);
                return true;
            } catch (Exception e) {
                log.error("同步学校招生计划异常: {}, {}", schoolCode, e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    @Override
    public boolean syncSchoolHistoricalScoreByCode(String schoolCode, SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 获取学校信息
                SchoolInfo schoolInfo = schoolInfoMapper.selectOne(
                        Wrappers.<SchoolInfo>lambdaQuery()
                                .eq(SchoolInfo::getSchoolCode, schoolCode)
                );
                
                if (schoolInfo == null) {
                    log.error("未找到学校信息: {}", schoolCode);
                    return false;
                }
                
                // 同步学校历年分数线
                syncHistoricalScores(schoolInfo);
                return true;
            } catch (Exception e) {
                log.error("同步学校历年分数异常: {}, {}", schoolCode, e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    @Override
    public boolean syncAllMajors(SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 获取所有学校
                List<SchoolInfo> schoolInfoList = schoolInfoMapper.selectList(Wrappers.emptyWrapper());
                
                for (SchoolInfo schoolInfo : schoolInfoList) {
                    try {
                        registerMajor(schoolInfo);
                    } catch (Exception e) {
                        log.error("同步学校专业异常: {}, {}", schoolInfo.getSchoolCode(), e.getMessage());
                        // 继续处理下一个学校，不中断整体流程
                    }
                }
                
                return true;
            } catch (Exception e) {
                log.error("同步所有专业异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }

    private void syncSchoolInfo(SchoolSyncConfig config) {
        //https://api.zjzw.cn/web/api/?keyword=&page=3&province_id=&ranktype=&request_type=1&size=20&type=&uri=apidata/api/gkv3/school/lists&signsafe=dea67c187d0f4cb7630b966337f24860
        AtomicInteger page = new AtomicInteger(1);
        while (true) {
            GaokaoSchoolInfo schoolInfo = HttpClient
                    .get("https://api.zjzw.cn/web/api/?keyword=&page=" + page.get() + "&province_id=&ranktype=&request_type=1&size=20&type=&uri=apidata/api/gkv3/school/lists&signsafe=" + IdUtils.createUlid())
                    .newInvoker()
                    .execute()
                    .content(GaokaoSchoolInfo.class);

            if (null == schoolInfo || null == schoolInfo.getData() || null == schoolInfo.getData().getItem()) {
                break;
            }
            registerSchoolInfo(schoolInfo.getData().getItem(), config);
            page.incrementAndGet();
        }
    }

    private void registerSchoolInfo(List<GaokaoSchoolInfo.DataDTO.ItemDTO> item, SchoolSyncConfig config) {
        for (GaokaoSchoolInfo.DataDTO.ItemDTO itemDTO : item) {
            registerSchoolInfo(itemDTO, config);
        }
    }

    private void registerSchoolInfo(GaokaoSchoolInfo.DataDTO.ItemDTO itemDTO, SchoolSyncConfig config) {
        SchoolInfo schoolInfo = new SchoolInfo();
        SchoolInfo selectOne = schoolInfoMapper.selectOne(
                Wrappers.<SchoolInfo>lambdaQuery()
                        .eq(SchoolInfo::getSchoolCode, String.valueOf(itemDTO.getSchoolId()))
        );
        if (config.getSchoolSyncConfigSyncType() == SyncType.NEW) {
            if (null != selectOne) {
                return;
            }
        } else if (null != selectOne) {
            schoolInfo = selectOne;
        }
        schoolInfo.setSchoolLogoUrl("https://static-data.gaokao.cn/upload/logo/" + schoolInfo.getSchoolCode() + ".jpg");
        schoolInfo.setSchoolCode(String.valueOf(itemDTO.getSchoolId()));
        schoolInfo.setSchoolProvinceId(itemDTO.getProvinceId());
        schoolInfo.setSchoolCityId(itemDTO.getCityId());
        schoolInfo.setSchoolDistrictId(itemDTO.getCountyId());
        schoolInfo.setSchoolName(itemDTO.getHightitle());
        schoolInfo.setSchoolRank(Converter.createInteger(itemDTO.getRank()));
        schoolInfo.setSchoolType(itemDTO.getType());
        schoolInfo.setSchoolIs211(ObjectUtils.equals(itemDTO.getF211(), 1) ? 1 : 0);
        schoolInfo.setSchoolIs985(ObjectUtils.equals(itemDTO.getF985(), 1) ? 1 : 0);
        schoolInfo.setSchoolDual(itemDTO.getDualClass());
        schoolInfo.setSchoolEducationLevel(itemDTO.getLevel());
        schoolInfo.setSchoolEducationNative(itemDTO.getNature());
        schoolInfo.setSchoolSubordinateTo(itemDTO.getBelong());
        schoolInfo.setSchoolAdmission(Converter.createInteger(itemDTO.getAdmissions()));
        schoolInfo.setSchoolViewMonth(Converter.createInteger(itemDTO.getViewMonth()));
        schoolInfo.setSchoolTag(itemDTO.getTagName());
        schoolInfo.setSchoolViewTotal(Converter.createInteger(itemDTO.getViewTotalNumber()));
        register(itemDTO.getNature(), itemDTO.getNatureName(), "nature");
        //210000000000
        register(itemDTO.getType(), itemDTO.getTypeName(), "type");
        register(itemDTO.getLevel(), itemDTO.getLevelName(), "level");
        register(itemDTO.getDualClass(), itemDTO.getDualClassName(), "dual");
        register(itemDTO.getProvinceId() + Strings.repeat("0", 12 - itemDTO.getProvinceId().length()), itemDTO.getProvinceName(), "province");
        register(itemDTO.getCountyId() + Strings.repeat("0", 12 - itemDTO.getCountyId().length()), itemDTO.getCountyName(), "county");
        register(itemDTO.getCityId() + Strings.repeat("0", 12 - itemDTO.getCityId().length()), itemDTO.getCityName(), "city");
        registerRank(schoolInfo);
        registerDetail(schoolInfo);
        registerMajor(schoolInfo);
        registerPlan(schoolInfo);
        if (null == schoolInfo.getSchoolId()) {
            schoolInfoMapper.insert(schoolInfo);
            return;
        }
        schoolInfoMapper.updateById(schoolInfo);

    }

    private void registerPlan(SchoolInfo schoolInfo) {
        try {
            schoolAdmissionPlanMapper.delete(
                    Wrappers.<SchoolAdmissionPlan>lambdaQuery()
                            .eq(SchoolAdmissionPlan::getSchoolAdmissionPlanSchoolCode, schoolInfo.getSchoolCode())
            );
        } catch (Exception ignored) {
        }
        //https://api.zjzw.cn/web/api/?like_spname=&local_batch_id=14&local_province_id=11&local_type_id=3&page=1&school_id=3486&sg_xuanke=&size=10&special_group=&uri=apidata/api/gkv3/plan/school&year=2024&signsafe=64d22b09f8d7056413355616de19dcd1
        for (int i = 0; i < PROVIDES.length; i++) {
            Integer provide = PROVIDES[i];
            for (int j = 0; j < YEARS.length; j++) {
                Integer year = YEARS[j];
                try {
                    registerPlan(schoolInfo, provide, year);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void registerPlan(SchoolInfo schoolInfo, Integer provide, Integer year) {
        GaokaoPlan gaokaoPlan = HttpClient
                .get("https://api.zjzw.cn/web/api/?like_spname=&local_batch_id=14&local_province_id=" + provide + "&local_type_id=3&page=1&school_id=" + schoolInfo.getSchoolCode() + "&sg_xuanke=&size=10&special_group=&uri=apidata/api/gkv3/plan/school&year=" + year + "&signsafe=64d22b09f8d7056413355616de19dcd1")
                .newInvoker()
                .execute()
                .content(GaokaoPlan.class);
        GaokaoPlan.DataDTO data = gaokaoPlan.getData();
        if (null == data) {
            return;
        }

        for (GaokaoPlan.DataDTO.ItemDTO itemDTO : data.getItem()) {
            SchoolAdmissionPlan schoolAdmissionPlan = new SchoolAdmissionPlan();
            schoolAdmissionPlan.setSchoolAdmissionPlanSchoolId(schoolInfo.getSchoolId());
            schoolAdmissionPlan.setSchoolAdmissionPlanSchoolCode(schoolInfo.getSchoolCode());
            schoolAdmissionPlan.setSchoolAdmissionPlanNumber(Converter.createInteger(itemDTO.getNum()));
            schoolAdmissionPlan.setSchoolAdmissionPlanYear(year);
            String provideStr = provide + "";
            schoolAdmissionPlan.setSchoolAdmissionPlanProvince(provideStr + Strings.repeat("0", 12 - provideStr.length()));
            schoolAdmissionPlan.setSchoolAdmissionPlanTuition(Converter.convertIfNecessary(itemDTO.getTuition(), Double.class));
            schoolAdmissionPlan.setSchoolAdmissionPlanLength(Converter.convertIfNecessary(itemDTO.getLength(), Integer.class));
            schoolAdmissionPlan.setSchoolAdmissionPlanSubjectRequirements(itemDTO.getInfo());
            schoolAdmissionPlan.setSchoolAdmissionPlanName(itemDTO.getSpname());
            schoolAdmissionPlan.setSchoolAdmissionPlanRemark(itemDTO.getSpName());
            schoolAdmissionPlan.setSchoolAdmissionPlanMajorName(itemDTO.getLevel3Name());
            schoolAdmissionPlan.setSchoolAdmissionPlanBatch(itemDTO.getLocalBatchName());

            schoolAdmissionPlanMapper.insert(schoolAdmissionPlan);
        }
    }

    private void registerMajor(SchoolInfo schoolInfo) {
        //https://static-data.gaokao.cn/www/2.0/school/140/pc_special.json?a=www.gaokao.cn
        GaokaoMajor gaokaoMajor = HttpClient
                .get("https://static-data.gaokao.cn/www/2.0/school/" + schoolInfo.getSchoolCode() + "/pc_special.json?a=www.gaokao.cn")
                .newInvoker()
                .execute()
                .content(GaokaoMajor.class);
        GaokaoMajor.DataDTO data = gaokaoMajor.getData();
        if (null == data) {
            return;
        }
        schoolMajorMapper.delete(
                Wrappers.<SchoolMajor>lambdaQuery()
                        .eq(SchoolMajor::getSchoolCode, schoolInfo.getSchoolCode())
        );
        for (GaokaoMajor.DataDTO.SpecialDTO specialDTO : data.getSpecial()) {
            SchoolMajor schoolMajor = new SchoolMajor();
            schoolMajor.setSchoolMajorCode(specialDTO.getCode());
            schoolMajor.setSchoolMajorName(specialDTO.getName());
            schoolMajor.setSchoolCode(schoolInfo.getSchoolCode());
            schoolMajorMapper.insert(schoolMajor);

            List<GaokaoMajor.DataDTO.SpecialDTO.SpecialDTO1> special = specialDTO.getSpecial();
            registerMajor(schoolMajor, special);
        }

    }

    private void registerMajor(SchoolMajor schoolMajor, List<GaokaoMajor.DataDTO.SpecialDTO.SpecialDTO1> special) {
        for (GaokaoMajor.DataDTO.SpecialDTO.SpecialDTO1 specialDTO1 : special) {
            SchoolMajor schoolMajor1 = new SchoolMajor();
            schoolMajor1.setSchoolMajorParentCode(schoolMajor.getSchoolMajorCode());
            schoolMajor1.setSchoolMajorName(specialDTO1.getSpecialName());
            schoolMajor1.setSchoolMajorType(specialDTO1.getSpecialType());
            schoolMajor1.setSchoolCode(schoolMajor.getSchoolCode());
            schoolMajor1.setSchoolMajorLevel(specialDTO1.getTypeName());
            schoolMajor1.setSchoolMajorYear(specialDTO1.getYear());
            schoolMajor1.setSchoolMajorCode(specialDTO1.getLevel3Code());
            schoolMajor1.setSchoolMajorCategoryName(specialDTO1.getLevel2Name());
            schoolMajor1.setSchoolMajorCategoryLabel(schoolMajor.getSchoolMajorName());
            schoolMajor1.setSchoolMajorCategoryCode(specialDTO1.getLevel2Code());
            schoolMajor1.setSchoolMajorRuanKeLevel(specialDTO1.getRuankeLevel());
            schoolMajor1.setSchoolMajorRuanKeRank(Converter.createInteger(specialDTO1.getRuankeRank()));
            schoolMajor1.setSchoolMajorXueKeRank(Converter.createInteger(specialDTO1.getXuekeRank()));
            schoolMajor1.setSchoolMajorXueKeRankScore(Converter.createInteger(specialDTO1.getXuekeRankScore()));

            schoolMajorMapper.insert(schoolMajor1);
        }
    }

    private void registerDetail(SchoolInfo schoolInfo) {
        //https://static-data.gaokao.cn/www/2.0/school/140/info.json?a=www.gaokao.cn
        try {
            GaokaoDetail gaokaoDetail = HttpClient
                    .get("https://static-data.gaokao.cn/www/2.0/school/" + schoolInfo.getSchoolCode() + "/info.json?a=www.gaokao.cn")
                    .newInvoker()
                    .execute()
                    .content(GaokaoDetail.class);
            GaokaoDetail.DataDTO data = gaokaoDetail.getData();
            if (null == data) {
                return;
            }
            schoolInfo.setSchoolAddress(data.getAddress());
            schoolInfo.setSchoolCreateTime(data.getCreateDate());
            schoolInfo.setSchoolContent(data.getContent());
            schoolInfo.setSchoolEmail(data.getEmail());
            schoolInfo.setSchoolPhone(data.getPhone());
            schoolInfo.setSchoolPostCode(data.getPostcode());
            schoolInfo.setSchoolNumDoctor(Converter.createInteger(data.getNumDoctor()));
            schoolInfo.setSchoolNumMaster(Converter.createInteger(data.getNumMaster()));
            schoolInfo.setSchoolNumSubject(Converter.createInteger(data.getNumSubject()));
            schoolInfo.setSchoolWebsiteUrl(data.getSchoolSite());
            schoolInfo.setSchoolFeatures(Joiner.on(",").join(data.getYkFeature()));
        } catch (Exception ignored) {
        }
    }

    private void registerRank(SchoolInfo schoolInfo) {
        try {
            schoolRankMapper.delete(
                    Wrappers.<SchoolRank>lambdaQuery()
                            .eq(SchoolRank::getSchoolCode, schoolInfo.getSchoolCode())
            );
            GaokaoRank gaokaoRank = HttpClient
                    .get("https://static-data.gaokao.cn/www/2.0/school/" + schoolInfo.getSchoolCode() + "/rank.json?a=www.gaokao.cn")
                    .newInvoker()
                    .execute()
                    .content(GaokaoRank.class);
                    
            if (gaokaoRank == null || gaokaoRank.getData() == null) {
                log.warn("获取学校排名信息失败: {}", schoolInfo.getSchoolCode());
                return;
            }
            
            gaokaoRank.getData().forEach(item -> {
                try {
                    SchoolRank schoolRank = new SchoolRank();
                    // 转换rank字段，确保类型匹配
                    schoolRank.setSchoolRankValue(Converter.createInteger(item.getRank()));
                    schoolRank.setSchoolRankType(item.getRankType());
                    schoolRank.setSchoolRankName(item.getRankName());
                    schoolRank.setSchoolCode(schoolInfo.getSchoolCode());
                    
                    // 设置新增字段的默认值
                    // 从rankName中提取年份信息，如"2023年软科排名"
                    String rankName = item.getRankName();
                    if (rankName != null && rankName.matches(".*\\d{4}.*")) {
                        // 尝试从名称中提取年份
                        String yearStr = rankName.replaceAll(".*?(\\d{4}).*", "$1");
                        try {
                            schoolRank.setSchoolRankYear(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            // 提取失败时使用当前年份
                            schoolRank.setSchoolRankYear(java.time.Year.now().getValue());
                        }
                    } else {
                        // 默认使用当前年份
                        schoolRank.setSchoolRankYear(java.time.Year.now().getValue());
                    }
                    
                    // 从rankType和rankName中提取排名机构信息
                    if (item.getRankType() != null && item.getRankType().contains("软科")) {
                        schoolRank.setSchoolRankInstitution("软科");
                    } else if (item.getRankName() != null) {
                        if (item.getRankName().contains("QS")) {
                            schoolRank.setSchoolRankInstitution("QS");
                        } else if (item.getRankName().contains("校友会")) {
                            schoolRank.setSchoolRankInstitution("校友会");
                        } else if (item.getRankName().contains("软科")) {
                            schoolRank.setSchoolRankInstitution("软科");
                        } else if (item.getRankName().contains("泰晤士")) {
                            schoolRank.setSchoolRankInstitution("泰晤士高等教育");
                        } else {
                            schoolRank.setSchoolRankInstitution("其他");
                        }
                    }
                    
                    // 设置排名得分（如果有）
                    // 某些排名可能在master字段中包含得分信息
                    if (item.getMaster() != null && !item.getMaster().isEmpty()) {
                        try {
                            // 尝试从master字段提取得分信息
                            // 得分通常是一个浮点数，比如"得分：85.6分"
                            if (item.getMaster().matches(".*\\d+(\\.\\d+)?.*")) {
                                String scoreStr = item.getMaster().replaceAll(".*?(\\d+(\\.\\d+)?).*", "$1");
                                schoolRank.setSchoolRankScore(Double.parseDouble(scoreStr));
                            }
                        } catch (Exception e) {
                            log.debug("解析排名得分失败: {}", item.getMaster());
                        }
                    }
                    
                    // 尝试计算排名变化
                    // 排名变化信息可能在数据源中没有直接提供，这里可以通过比对历史数据来计算
                    // 这里提供一个基本框架，实际实现可能需要查询上一年的排名数据
                    try {
                        // 查询上一年的排名数据
                        SchoolRank lastYearRank = schoolRankMapper.selectOne(
                                Wrappers.<SchoolRank>lambdaQuery()
                                        .eq(SchoolRank::getSchoolCode, schoolInfo.getSchoolCode())
                                        .eq(SchoolRank::getSchoolRankType, item.getRankType())
                                        .eq(SchoolRank::getSchoolRankYear, schoolRank.getSchoolRankYear() - 1)
                        );
                        
                        if (lastYearRank != null && lastYearRank.getSchoolRankValue() != null && 
                                schoolRank.getSchoolRankValue() != null) {
                            // 计算排名变化：正数表示排名上升（变好），负数表示排名下降（变差）
                            // 注意：排名数字越小表示排名越好
                            int lastYearValue = lastYearRank.getSchoolRankValue();
                            int currentValue = schoolRank.getSchoolRankValue();
                            int change = lastYearValue - currentValue;
                            schoolRank.setSchoolRankChange(change);
                        }
                    } catch (Exception e) {
                        log.debug("计算排名变化失败: {}", e.getMessage());
                    }
                    
                    // 增加可能的备注信息
                    if (item.getMaster() != null && !item.getMaster().isEmpty()) {
                        schoolRank.setSchoolRankRemark(item.getMaster());
                    }
                    
                    // 设置删除标志
                    schoolRank.setSchoolRankDeleted(0);
                    
                    schoolRankMapper.insert(schoolRank);
                } catch (Exception e) {
                    log.error("保存学校排名信息异常: {}, {}", schoolInfo.getSchoolCode(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("获取学校排名信息异常: {}, {}", schoolInfo.getSchoolCode(), e.getMessage());
        }
    }

    private void register(String nature, String natureName, String type) {
        try {
            SchoolDict schoolDict = new SchoolDict();
            if (schoolDictMapper.exists(
                    Wrappers.<SchoolDict>lambdaQuery()
                            .eq(SchoolDict::getSchoolDictCode, nature)
                            .eq(SchoolDict::getSchoolDictType, type)
            )) {
                return;
            }
            schoolDict.setSchoolDictCode(nature);
            schoolDict.setSchoolDictName(natureName);
            schoolDict.setSchoolDictType(type);
            schoolDictMapper.insert(schoolDict);
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<String> listSchoolIds() {
        try {
            List<SchoolInfo> allSchools = schoolInfoMapper.selectList(Wrappers.emptyWrapper());
            if (allSchools == null || allSchools.isEmpty()) {
                return List.of();
            }
            
            return allSchools.stream()
                    .map(SchoolInfo::getSchoolCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .toList();
        } catch (Exception e) {
            log.error("获取所有学校ID异常: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<SchoolInfo> searchSchools(String schoolName) {
        try {
            if (schoolName == null || schoolName.trim().isEmpty()) {
                return List.of();
            }
            
            return schoolInfoMapper.selectList(
                    Wrappers.<SchoolInfo>lambdaQuery()
                            .like(SchoolInfo::getSchoolName, schoolName)
            );
        } catch (Exception e) {
            log.error("搜索学校信息异常: {}, {}", schoolName, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public boolean getAllSpecialties(SchoolSyncConfig config) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                log.info("开始同步所有专业信息");
                AtomicInteger page = new AtomicInteger(1);
                int pageSize = 20;
                int totalProcessed = 0;
                
                while (true) {
                    // 构建API请求URL
                    String url = "https://api.zjzw.cn/web/api/?keyword=&page=" + page.get() + 
                            "&size=" + pageSize + 
                            "&sort=&uri=apidata/api/gkv3/special/lists&signsafe=" + IdUtils.createUlid();
                    
                    // 调用API获取专业数据
                    GaokaoSpecialty specialtyData = HttpClient
                            .get(url)
                            .newInvoker()
                            .execute()
                            .content(GaokaoSpecialty.class);
                    
                    // 检查返回数据是否有效
                    if (specialtyData == null || specialtyData.getData() == null || 
                            specialtyData.getData().getItem() == null || 
                            specialtyData.getData().getItem().isEmpty()) {
                        log.info("没有更多专业数据，结束同步");
                        break;
                    }
                    
                    // 处理当前页的专业数据
                    List<GaokaoSpecialty.ItemDTO> specialties = specialtyData.getData().getItem();
                    processSpecialties(specialties);
                    
                    totalProcessed += specialties.size();
                    log.info("已处理{}个专业数据", totalProcessed);
                    
                    // 检查是否还有下一页
                    if (specialties.size() < pageSize) {
                        log.info("已到达最后一页，结束同步");
                        break;
                    }
                    
                    // 继续处理下一页
                    page.incrementAndGet();
                }
                
                log.info("所有专业信息同步完成，共处理{}个专业", totalProcessed);
                return true;
            } catch (Exception e) {
                log.error("同步所有专业信息异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }
    
    /**
     * 处理专业数据列表
     *
     * @param specialties 专业数据列表
     */
    private void processSpecialties(List<GaokaoSpecialty.ItemDTO> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            return;
        }
        
        for (GaokaoSpecialty.ItemDTO specialty : specialties) {
            try {
                // 创建专业对象并保存到数据库
                SchoolMajor schoolMajor = new SchoolMajor();
                schoolMajor.setSchoolMajorCode(specialty.getSpecialCode());
                schoolMajor.setSchoolMajorName(specialty.getSpecialName());
                schoolMajor.setSchoolMajorType(specialty.getSpecialType());
                schoolMajor.setSchoolMajorTypeName(specialty.getTypeName());
                schoolMajor.setSchoolMajorLevel(specialty.getDegree());
                schoolMajor.setSchoolMajorCategoryCode(specialty.getLevel2Code());
                schoolMajor.setSchoolMajorCategoryName(specialty.getLevel2Name());
                schoolMajor.setSchoolMajorParentCode(specialty.getLevel1Code());
                schoolMajor.setSchoolMajorCategoryLabel(specialty.getLevel1Name());
                schoolMajor.setSchoolMajorYear(String.valueOf(specialty.getLimitYear()));
                schoolMajor.setSchoolMajorContent(specialty.getWhatMajor());
                schoolMajor.setSchoolMajorCourse(specialty.getCourse());
                schoolMajor.setSchoolMajorEmployment(specialty.getWhatWork());
                
                // 检查数据库中是否已存在该专业
                SchoolMajor existingMajor = schoolMajorMapper.selectOne(
                        Wrappers.<SchoolMajor>lambdaQuery()
                                .eq(SchoolMajor::getSchoolMajorCode, schoolMajor.getSchoolMajorCode())
                                .isNull(SchoolMajor::getSchoolCode) // 通用专业没有关联学校
                );
                
                if (existingMajor == null) {
                    // 新增专业
                    schoolMajorMapper.insert(schoolMajor);
                } else {
                    // 更新现有专业
                    schoolMajor.setSchoolMajorId(existingMajor.getSchoolMajorId());
                    schoolMajorMapper.updateById(schoolMajor);
                }
            } catch (Exception e) {
                log.error("处理专业数据异常: {}, {}", specialty.getSpecialName(), e.getMessage());
                // 继续处理下一个专业，不中断整体流程
            }
        }
    }
}