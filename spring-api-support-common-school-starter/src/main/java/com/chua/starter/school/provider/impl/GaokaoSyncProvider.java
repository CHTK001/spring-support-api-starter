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
        // 同步学校基本信息
        // 同步学校专业信息
        // 同步招生计划
        // 同步历年分数
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                boolean result = true;
                syncSchoolInfo(config);
                return result;
            } catch (Exception e) {
                log.error("同步所有学校数据异常: {}", e.getMessage(), e);
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
        if (config.getSchoolSyncConfigSyncSyncType() == SyncType.NEW) {
            if (null != selectOne) {
                return;
            }
            if (null != selectOne) {
                schoolInfo = selectOne;
            }
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
            schoolMajorMapper.delete(
                    Wrappers.<SchoolMajor>lambdaQuery()
                            .eq(SchoolMajor::getSchoolCode, schoolInfo.getSchoolCode())
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
            schoolAdmissionPlan.setSchoolAdmissionPlanNumber(itemDTO.getNum());
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
            schoolMajor1.setSchoolMajorRuanKeRank(specialDTO1.getRuankeRank());
            schoolMajor1.setSchoolMajorXueKeRank(specialDTO1.getXuekeRank());
            schoolMajor1.setSchoolMajorXueKeRankScore(specialDTO1.getXuekeRankScore());

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
            gaokaoRank.getData().forEach(item -> {
                SchoolRank schoolRank = new SchoolRank();
                schoolRank.setSchoolRankValue(item.getRank());
                schoolRank.setSchoolRankType(item.getRankType());
                schoolRank.setSchoolRankName(item.getRankName());
                schoolRank.setSchoolCode(schoolInfo.getSchoolCode());
                schoolRankMapper.insert(schoolRank);
            });
        } catch (Exception ignored) {
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
        return getAllSchoolIds();
    }

    @Override
    public List<SchoolInfo> searchSchools(String schoolName) {
        return null;
    }

    /**
     * 获取所有学校ID
     *
     * @return 学校ID列表
     */
    public List<String> getAllSchoolIds() {
        return null;
    }
} 