package com.chua.starter.school.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.starter.school.entity.*;
import com.chua.starter.school.mapper.SchoolAdmissionPlanMapper;
import com.chua.starter.school.mapper.SchoolHistoricalScoreMapper;
import com.chua.starter.school.mapper.SchoolInfoMapper;
import com.chua.starter.school.mapper.SchoolMajorMapper;
import com.chua.starter.school.provider.SchoolSyncProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private SchoolInfoMapper schoolInfoMapper;

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

    /**
     * 同步学校信息
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    @Override
    public boolean syncSchoolInfo(String schoolId, SchoolSyncConfig config) {
        try {
            String url = GAOKAO_BASE_URL + schoolId + SCHOOL_INFO_URL_SUFFIX;
            log.info("开始同步学校信息: {}", url);

            Document document = getDocument(url, config);
            if (document == null) {
                return false;
            }

            SchoolInfo schoolInfo = parseSchoolInfo(document, schoolId);

            // 使用TransactionTemplate替代@Transactional注解
            // 查找是否已存在该学校信息
            // TransactionTemplate遇到异常会自动回滚
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 查找是否已存在该学校信息
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("school_code", schoolInfo.getSchoolCode());
                    SchoolInfo existingSchool = schoolInfoMapper.selectByMap(queryMap).stream().findFirst().orElse(null);

                    if (existingSchool != null) {
                        schoolInfo.setSchoolId(existingSchool.getSchoolId());
                        schoolInfo.setUpdateTime(LocalDateTime.now());
                        schoolInfoMapper.updateById(schoolInfo);
                        log.info("更新学校信息成功: {}", schoolInfo.getSchoolName());
                    } else {
                        schoolInfo.setCreateTime(LocalDateTime.now());
                        schoolInfo.setUpdateTime(LocalDateTime.now());
                        schoolInfoMapper.insert(schoolInfo);
                        log.info("新增学校信息成功: {}", schoolInfo.getSchoolName());
                    }

                    return true;
                } catch (Exception e) {
                    // TransactionTemplate遇到异常会自动回滚
                    log.error("保存学校信息失败: {}", e.getMessage(), e);
                    status.setRollbackOnly();
                    return false;
                }
            }));
        } catch (Exception e) {
            log.error("同步学校信息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解析学校信息
     *
     * @param document HTML文档
     * @param schoolId 高考网学校ID
     * @return 学校信息对象
     */
    private SchoolInfo parseSchoolInfo(Document document, String schoolId) {
        SchoolInfo schoolInfo = new SchoolInfo();

        // 设置学校代码为高考网ID
        schoolInfo.setSchoolCode(schoolId);

        // 获取学校名称和Logo
        Element schoolHeader = document.selectFirst(".header-schoolname");
        if (schoolHeader != null) {
            schoolInfo.setSchoolName(schoolHeader.text());
        }

        Element logoElement = document.selectFirst(".header-logo img");
        if (logoElement != null) {
            schoolInfo.setSchoolLogoUrl(logoElement.attr("src"));
        }

        // 设置高考网链接
        schoolInfo.setSchoolGaokaoUrl(GAOKAO_BASE_URL + schoolId);

        // 获取学校基本信息
        Elements basicInfoItems = document.select(".intro li");
        for (Element item : basicInfoItems) {
            String text = item.text().trim();
            if (text.contains("院校类型")) {
                schoolInfo.setSchoolType(text.replace("院校类型：", "").trim());
            } else if (text.contains("所在地")) {
                String location = text.replace("所在地：", "").trim();
                if (location.contains("省") && location.contains("市")) {
                    String[] parts = location.split("市");
                    String provinceCity = parts[0] + "市";
                    String[] provinceCityParts = provinceCity.split("省");
                    if (provinceCityParts.length > 1) {
                        schoolInfo.setSchoolProvince(provinceCityParts[0] + "省");
                        schoolInfo.setSchoolCity(provinceCityParts[1]);
                    } else {
                        schoolInfo.setSchoolCity(provinceCity);
                    }
                    if (parts.length > 1) {
                        schoolInfo.setSchoolDistrict(parts[1].trim());
                    }
                } else {
                    schoolInfo.setSchoolProvince(location);
                }
            } else if (text.contains("隶属于")) {
                schoolInfo.setSchoolSubordinateTo(text.replace("隶属于：", "").trim());
            } else if (text.contains("学历层次")) {
                schoolInfo.setSchoolEducationLevel(text.replace("学历层次：", "").trim());
            } else if (text.contains("招办电话")) {
                schoolInfo.setSchoolPhone(text.replace("招办电话：", "").trim());
            } else if (text.contains("学校网址")) {
                schoolInfo.setSchoolWebsiteUrl(text.replace("学校网址：", "").trim());
            } else if (text.contains("招生网址")) {
                schoolInfo.setSchoolAdmissionUrl(text.replace("招生网址：", "").trim());
            } else if (text.contains("创建时间")) {
                schoolInfo.setSchoolFoundingYear(text.replace("创建时间：", "").trim());
            }
        }

        // 获取是否是985/211/双一流
        Element tagElement = document.selectFirst(".tag-list");
        if (tagElement != null) {
            String tagText = tagElement.text();
            schoolInfo.setSchoolIs985(tagText.contains("985") ? 1 : 0);
            schoolInfo.setSchoolIs211(tagText.contains("211") ? 1 : 0);
            schoolInfo.setSchoolIsDoubleFirst(tagText.contains("双一流") ? 1 : 0);
        }

        // 获取学校介绍
        Element introElement = document.selectFirst(".school-intro");
        if (introElement != null) {
            schoolInfo.setSchoolIntroduction(introElement.text().trim());
        }

        // 获取学校特色
        Element featuresElement = document.selectFirst(".schoolFeature");
        if (featuresElement != null) {
            schoolInfo.setSchoolFeatures(featuresElement.text().trim());
        }

        return schoolInfo;
    }

    /**
     * 同步学校专业信息
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    @Override
    public boolean syncSchoolMajors(String schoolId, SchoolSyncConfig config) {
        try {
            String url = GAOKAO_BASE_URL + schoolId + SCHOOL_MAJOR_URL_SUFFIX;
            log.info("开始同步学校专业信息: {}", url);

            Document document = getDocument(url, config);
            if (document == null) {
                return false;
            }

            // 获取学校ID
            SchoolInfo schoolInfo = getSchoolInfoByCode(schoolId);
            if (schoolInfo == null) {
                log.error("学校信息不存在，请先同步学校基本信息");
                return false;
            }

            // 解析专业信息
            List<SchoolMajor> majors = parseSchoolMajors(document, schoolInfo);

            // 使用TransactionTemplate替代@Transactional注解
            // 保存专业信息
            // 查找是否已存在该专业
            // TransactionTemplate遇到异常会自动回滚
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 保存专业信息
                    int savedCount = 0;
                    int updatedCount = 0;

                    for (SchoolMajor major : majors) {
                        // 查找是否已存在该专业
                        Map<String, Object> queryMap = new HashMap<>();
                        queryMap.put("school_major_school_id", major.getSchoolMajorSchoolId());
                        queryMap.put("school_major_code", major.getSchoolMajorCode());
                        SchoolMajor existingMajor = schoolMajorMapper.selectByMap(queryMap).stream().findFirst().orElse(null);

                        if (existingMajor != null) {
                            major.setSchoolMajorId(existingMajor.getSchoolMajorId());
                            major.setUpdateTime(LocalDateTime.now());
                            schoolMajorMapper.updateById(major);
                            updatedCount++;
                        } else {
                            major.setCreateTime(LocalDateTime.now());
                            major.setUpdateTime(LocalDateTime.now());
                            schoolMajorMapper.insert(major);
                            savedCount++;
                        }
                    }

                    log.info("学校[{}]专业同步完成，新增: {}，更新: {}", schoolInfo.getSchoolName(), savedCount, updatedCount);
                    return true;
                } catch (Exception e) {
                    // TransactionTemplate遇到异常会自动回滚
                    log.error("保存专业信息失败: {}", e.getMessage(), e);
                    status.setRollbackOnly();
                    return false;
                }
            }));

        } catch (Exception e) {
            log.error("同步学校专业信息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据学校代码获取学校信息
     *
     * @param schoolCode 学校代码
     * @return 学校信息
     */
    private SchoolInfo getSchoolInfoByCode(String schoolCode) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("school_code", schoolCode);
        return schoolInfoMapper.selectByMap(queryMap).stream().findFirst().orElse(null);
    }

    /**
     * 解析学校专业信息
     *
     * @param document   HTML文档
     * @param schoolInfo 学校信息
     * @return 专业列表
     */
    private List<SchoolMajor> parseSchoolMajors(Document document, SchoolInfo schoolInfo) {
        List<SchoolMajor> majors = new ArrayList<>();

        // 获取专业列表
        Elements majorElements = document.select(".special-list-item");
        int ranking = 1;

        for (Element majorElement : majorElements) {
            SchoolMajor major = new SchoolMajor();

            // 设置学校相关信息
            major.setSchoolMajorSchoolId(schoolInfo.getSchoolId());
            major.setSchoolMajorSchoolCode(schoolInfo.getSchoolCode());

            // 获取专业名称
            Element nameElement = majorElement.selectFirst(".special-list-item-name");
            if (nameElement != null) {
                major.setSchoolMajorName(nameElement.text().trim());

                // 从专业名称链接中提取专业代码
                Element linkElement = nameElement.selectFirst("a");
                if (linkElement != null) {
                    String href = linkElement.attr("href");
                    if (href.contains("/special/")) {
                        String[] parts = href.split("/special/");
                        if (parts.length > 1) {
                            major.setSchoolMajorCode(parts[1]);
                            major.setSchoolMajorDetailUrl(href);
                        }
                    }
                }
            }

            // 获取专业学科和类别
            Element subjectElement = majorElement.selectFirst(".type");
            if (subjectElement != null) {
                String subjectText = subjectElement.text().trim();
                String[] parts = subjectText.split("·");
                if (parts.length > 1) {
                    major.setSchoolMajorCategory(parts[0].trim());
                    major.setSchoolMajorSubject(parts[1].trim());
                } else {
                    major.setSchoolMajorCategory(subjectText);
                }
            }

            // 获取授予学位
            Element degreeElement = majorElement.selectFirst(".degree");
            if (degreeElement != null) {
                major.setSchoolMajorDegree(degreeElement.text().trim());
            }

            // 获取学制
            Element durationElement = majorElement.selectFirst(".year");
            if (durationElement != null) {
                major.setSchoolMajorStudyDuration(durationElement.text().trim());
            }

            // 获取教育层次
            major.setSchoolMajorEducationLevel(schoolInfo.getSchoolEducationLevel());

            // 设置排名
            major.setSchoolMajorRanking(ranking++);

            // 获取是否为特色/重点专业标记
            Elements tagElements = majorElement.select(".tag");
            for (Element tagElement : tagElements) {
                String tagText = tagElement.text().trim();
                if (tagText.contains("特色专业")) {
                    major.setSchoolMajorIsFeatured(1);
                }
                if (tagText.contains("重点专业")) {
                    major.setSchoolMajorIsKeyMajor(1);
                }
            }

            // 获取核心课程和就业方向
            Elements descElements = majorElement.select(".desc");
            if (descElements.size() >= 1) {
                major.setSchoolMajorCoreCourses(descElements.get(0).text().trim());
            }
            if (descElements.size() >= 2) {
                major.setSchoolMajorCareerProspects(descElements.get(1).text().trim());
            }

            majors.add(major);
        }

        return majors;
    }

    /**
     * 获取页面文档
     *
     * @param url    请求URL
     * @param config 同步配置
     * @return HTML文档
     */
    private Document getDocument(String url, SchoolSyncConfig config) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

            // 如果配置了同步Cookie，则添加到请求头
            if (StringUtils.hasText(config.getSchoolSyncConfigCookie())) {
                headers.put("Cookie", config.getSchoolSyncConfigCookie());
            }

            // 发送请求
            return Jsoup.connect(url)
                    .headers(headers)
                    .timeout(30000)
                    .get();

        } catch (IOException e) {
            log.error("获取页面文档异常: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean syncAdmissionPlans(String schoolId, SchoolSyncConfig config) {
        try {
            String url = GAOKAO_BASE_URL + schoolId + SCHOOL_PLAN_URL_SUFFIX;
            log.info("开始同步学校招生计划信息: {}", url);

            Document document = getDocument(url, config);
            if (document == null) {
                return false;
            }

            // 获取学校信息
            SchoolInfo schoolInfo = getSchoolInfoByCode(schoolId);
            if (schoolInfo == null) {
                log.error("学校信息不存在，请先同步学校基本信息");
                return false;
            }

            // 解析招生计划信息
            List<SchoolAdmissionPlan> plans = parseAdmissionPlans(document, schoolInfo);

            // 使用TransactionTemplate保存招生计划
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 保存招生计划信息
                    int savedCount = 0;
                    int updatedCount = 0;

                    for (SchoolAdmissionPlan plan : plans) {
                        // 查找是否已存在该招生计划
                        Map<String, Object> queryMap = new HashMap<>();
                        queryMap.put("school_admission_plan_school_id", plan.getSchoolAdmissionPlanSchoolId());
                        queryMap.put("school_admission_plan_year", plan.getSchoolAdmissionPlanYear());
                        queryMap.put("school_admission_plan_province", plan.getSchoolAdmissionPlanProvince());
                        queryMap.put("school_admission_plan_major_id", plan.getSchoolAdmissionPlanMajorId());
                        SchoolAdmissionPlan existingPlan = schoolAdmissionPlanMapper.selectByMap(queryMap).stream().findFirst().orElse(null);

                        if (existingPlan != null) {
                            plan.setSchoolAdmissionPlanId(existingPlan.getSchoolAdmissionPlanId());
                            plan.setUpdateTime(LocalDateTime.now());
                            schoolAdmissionPlanMapper.updateById(plan);
                            updatedCount++;
                        } else {
                            plan.setCreateTime(LocalDateTime.now());
                            plan.setUpdateTime(LocalDateTime.now());
                            schoolAdmissionPlanMapper.insert(plan);
                            savedCount++;
                        }
                    }

                    log.info("学校[{}]招生计划同步完成，新增: {}，更新: {}", schoolInfo.getSchoolName(), savedCount, updatedCount);
                    return true;
                } catch (Exception e) {
                    log.error("保存招生计划信息失败: {}", e.getMessage(), e);
                    status.setRollbackOnly();
                    return false;
                }
            }));

        } catch (Exception e) {
            log.error("同步学校招生计划信息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean syncHistoricalScores(String schoolId, SchoolSyncConfig config) {
        try {
            String url = GAOKAO_BASE_URL + schoolId + SCHOOL_SCORE_URL_SUFFIX;
            log.info("开始同步学校历年分数信息: {}", url);

            Document document = getDocument(url, config);
            if (document == null) {
                return false;
            }

            // 获取学校信息
            SchoolInfo schoolInfo = getSchoolInfoByCode(schoolId);
            if (schoolInfo == null) {
                log.error("学校信息不存在，请先同步学校基本信息");
                return false;
            }

            // 解析历年分数信息
            List<SchoolHistoricalScore> scores = parseHistoricalScores(document, schoolInfo);

            // 使用TransactionTemplate保存历年分数
            return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 保存历年分数信息
                    int savedCount = 0;
                    int updatedCount = 0;

                    for (SchoolHistoricalScore score : scores) {
                        // 查找是否已存在该分数记录
                        Map<String, Object> queryMap = new HashMap<>();
                        queryMap.put("school_historical_score_school_id", score.getSchoolHistoricalScoreSchoolId());
                        queryMap.put("school_historical_score_year", score.getSchoolHistoricalScoreYear());
                        queryMap.put("school_historical_score_province", score.getSchoolHistoricalScoreProvince());
                        queryMap.put("school_historical_score_subject_type", score.getSchoolHistoricalScoreSubjectType());
                        queryMap.put("school_historical_score_batch", score.getSchoolHistoricalScoreBatch());
                        SchoolHistoricalScore existingScore = schoolHistoricalScoreMapper.selectByMap(queryMap).stream().findFirst().orElse(null);

                        if (existingScore != null) {
                            score.setSchoolHistoricalScoreId(existingScore.getSchoolHistoricalScoreId());
                            score.setUpdateTime(LocalDateTime.now());
                            schoolHistoricalScoreMapper.updateById(score);
                            updatedCount++;
                        } else {
                            score.setCreateTime(LocalDateTime.now());
                            score.setUpdateTime(LocalDateTime.now());
                            schoolHistoricalScoreMapper.insert(score);
                            savedCount++;
                        }
                    }

                    log.info("学校[{}]历年分数同步完成，新增: {}，更新: {}", schoolInfo.getSchoolName(), savedCount, updatedCount);
                    return true;
                } catch (Exception e) {
                    log.error("保存历年分数信息失败: {}", e.getMessage(), e);
                    status.setRollbackOnly();
                    return false;
                }
            }));

        } catch (Exception e) {
            log.error("同步学校历年分数信息异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解析招生计划信息
     *
     * @param document   HTML文档
     * @param schoolInfo 学校信息
     * @return 招生计划列表
     */
    private List<SchoolAdmissionPlan> parseAdmissionPlans(Document document, SchoolInfo schoolInfo) {
        List<SchoolAdmissionPlan> plans = new ArrayList<>();

        // 获取招生计划表格
        Elements planRows = document.select(".plan-table tbody tr");
        for (Element row : planRows) {
            SchoolAdmissionPlan plan = new SchoolAdmissionPlan();

            // 设置学校相关信息
            plan.setSchoolAdmissionPlanSchoolId(schoolInfo.getSchoolId());

            // 获取年份
            Element yearElement = row.selectFirst(".year");
            if (yearElement != null) {
                plan.setSchoolAdmissionPlanYear(Integer.parseInt(yearElement.text().trim()));
            }

            // 获取省份
            Element provinceElement = row.selectFirst(".province");
            if (provinceElement != null) {
                plan.setSchoolAdmissionPlanProvince(provinceElement.text().trim());
            }

            // 获取批次
            Element batchElement = row.selectFirst(".batch");
            if (batchElement != null) {
                plan.setSchoolAdmissionPlanBatch(batchElement.text().trim());
            }

            // 获取科类
            Element typeElement = row.selectFirst(".type");
            if (typeElement != null) {
                plan.setSchoolAdmissionPlanAdmissionType(typeElement.text().trim());
            }

            // 获取专业名称
            Element majorElement = row.selectFirst(".major");
            if (majorElement != null) {
                plan.setSchoolAdmissionPlanMajorCode(majorElement.text().trim());
                // 从链接中获取专业ID
                Element majorLink = majorElement.selectFirst("a");
                if (majorLink != null) {
                    String href = majorLink.attr("href");
                    if (href.contains("/special/")) {
                        String[] parts = href.split("/special/");
                        if (parts.length > 1) {
                            plan.setSchoolAdmissionPlanMajorId(parts[1]);
                        }
                    }
                }
            }

            // 获取计划人数
            Element quotaElement = row.selectFirst(".quota");
            if (quotaElement != null) {
                String quotaText = quotaElement.text().trim();
                if (quotaText.matches("\\d+")) {
                    plan.setSchoolAdmissionPlanPlanCount(Integer.parseInt(quotaText));
                }
            }

            // 获取学费
            Element feeElement = row.selectFirst(".fee");
            if (feeElement != null) {
                String feeText = feeElement.text().trim().replaceAll("[^\\d.]", "");
                if (!feeText.isEmpty()) {
                    plan.setSchoolAdmissionPlanTuition(Double.parseDouble(feeText));
                }
            }

            // 获取备注
            Element remarkElement = row.selectFirst(".remark");
            if (remarkElement != null) {
                plan.setSchoolAdmissionPlanRemark(remarkElement.text().trim());
            }

            plans.add(plan);
        }

        return plans;
    }

    /**
     * 解析历年分数信息
     *
     * @param document   HTML文档
     * @param schoolInfo 学校信息
     * @return 历年分数列表
     */
    private List<SchoolHistoricalScore> parseHistoricalScores(Document document, SchoolInfo schoolInfo) {
        List<SchoolHistoricalScore> scores = new ArrayList<>();

        // 获取分数表格
        Elements scoreRows = document.select(".score-table tbody tr");
        for (Element row : scoreRows) {
            SchoolHistoricalScore score = new SchoolHistoricalScore();

            // 设置学校相关信息
            score.setSchoolHistoricalScoreSchoolId(schoolInfo.getSchoolId());
            score.setSchoolHistoricalScoreSchoolCode(schoolInfo.getSchoolCode());

            // 获取年份
            Element yearElement = row.selectFirst(".year");
            if (yearElement != null) {
                score.setSchoolHistoricalScoreYear(Integer.parseInt(yearElement.text().trim()));
            }

            // 获取省份
            Element provinceElement = row.selectFirst(".province");
            if (provinceElement != null) {
                score.setSchoolHistoricalScoreProvince(provinceElement.text().trim());
            }

            // 获取批次
            Element batchElement = row.selectFirst(".batch");
            if (batchElement != null) {
                score.setSchoolHistoricalScoreBatch(batchElement.text().trim());
            }

            // 获取科类
            Element typeElement = row.selectFirst(".type");
            if (typeElement != null) {
                score.setSchoolHistoricalScoreSubjectType(typeElement.text().trim());
            }

            // 获取最高分
            Element maxScoreElement = row.selectFirst(".max-score");
            if (maxScoreElement != null) {
                String maxScoreText = maxScoreElement.text().trim();
                if (maxScoreText.matches("\\d+")) {
                    score.setSchoolHistoricalScoreMaxScore(Integer.parseInt(maxScoreText));
                }
            }

            // 获取最低分
            Element minScoreElement = row.selectFirst(".min-score");
            if (minScoreElement != null) {
                String minScoreText = minScoreElement.text().trim();
                if (minScoreText.matches("\\d+")) {
                    score.setSchoolHistoricalScoreMinScore(Integer.parseInt(minScoreText));
                }
            }

            // 获取平均分
            Element avgScoreElement = row.selectFirst(".avg-score");
            if (avgScoreElement != null) {
                String avgScoreText = avgScoreElement.text().trim();
                if (avgScoreText.matches("\\d+")) {
                    score.setSchoolHistoricalScoreAvgScore(Integer.parseInt(avgScoreText));
                }
            }

            // 获取录取人数
            Element admittedElement = row.selectFirst(".admitted");
            if (admittedElement != null) {
                String admittedText = admittedElement.text().trim();
                if (admittedText.matches("\\d+")) {
                    score.setSchoolHistoricalScoreAdmissionCount(Integer.parseInt(admittedText));
                }
            }

            // 获取投档人数
            Element appliedElement = row.selectFirst(".applied");
            if (appliedElement != null) {
                String appliedText = appliedElement.text().trim();
                if (appliedText.matches("\\d+")) {
                    score.setSchoolHistoricalScoreAppliedCount(Integer.parseInt(appliedText));
                }
            }

            scores.add(score);
        }

        return scores;
    }

    @Override
    public boolean syncAllSchoolData(String schoolId, SchoolSyncConfig config) {
        // 使用TransactionTemplate替代@Transactional注解
        // 同步学校基本信息
        // 同步学校专业信息
        // 同步招生计划
        // 同步历年分数
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                boolean result = true;

                // 同步学校基本信息
                result = result && syncSchoolInfo(schoolId, config);

                // 同步学校专业信息
                result = result && syncSchoolMajors(schoolId, config);

                // 同步招生计划
                result = result && syncAdmissionPlans(schoolId, config);

                // 同步历年分数
                result = result && syncHistoricalScores(schoolId, config);

                return result;
            } catch (Exception e) {
                log.error("同步所有学校数据异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        }));
    }

    @Override
    public List<String> listSchoolIds() {
        return getAllSchoolIds();
    }

    @Override
    public List<SchoolInfo> searchSchools(String schoolName) {
        try {
            // 构建搜索URL
            String searchUrl = GAOKAO_BASE_URL + "search?keyword=" + schoolName;
            log.info("开始搜索学校: {}", searchUrl);

            Document document = getDocument(searchUrl, null);
            if (document == null) {
                return List.of();
            }

            List<SchoolInfo> schools = new ArrayList<>();

            // 解析搜索结果
            Elements schoolElements = document.select(".school-list-item");
            for (Element schoolElement : schoolElements) {
                SchoolInfo schoolInfo = new SchoolInfo();

                // 获取学校名称和链接
                Element nameElement = schoolElement.selectFirst(".school-name");
                if (nameElement != null) {
                    schoolInfo.setSchoolName(nameElement.text().trim());
                    Element linkElement = nameElement.selectFirst("a");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href.contains("/school/")) {
                            String[] parts = href.split("/school/");
                            if (parts.length > 1) {
                                schoolInfo.setSchoolCode(parts[1]);
                                schoolInfo.setSchoolGaokaoUrl(GAOKAO_BASE_URL + parts[1]);
                            }
                        }
                    }
                }

                // 获取学校Logo
                Element logoElement = schoolElement.selectFirst(".school-logo img");
                if (logoElement != null) {
                    schoolInfo.setSchoolLogoUrl(logoElement.attr("src"));
                }

                // 获取学校类型
                Element typeElement = schoolElement.selectFirst(".school-type");
                if (typeElement != null) {
                    schoolInfo.setSchoolType(typeElement.text().trim());
                }

                // 获取学校所在地
                Element locationElement = schoolElement.selectFirst(".school-location");
                if (locationElement != null) {
                    String location = locationElement.text().trim();
                    if (location.contains("省") && location.contains("市")) {
                        String[] parts = location.split("市");
                        String provinceCity = parts[0] + "市";
                        String[] provinceCityParts = provinceCity.split("省");
                        if (provinceCityParts.length > 1) {
                            schoolInfo.setSchoolProvince(provinceCityParts[0] + "省");
                            schoolInfo.setSchoolCity(provinceCityParts[1]);
                        } else {
                            schoolInfo.setSchoolCity(provinceCity);
                        }
                        if (parts.length > 1) {
                            schoolInfo.setSchoolDistrict(parts[1].trim());
                        }
                    } else {
                        schoolInfo.setSchoolProvince(location);
                    }
                }

                // 获取学校标签（985/211/双一流）
                Elements tagElements = schoolElement.select(".school-tag");
                for (Element tagElement : tagElements) {
                    String tagText = tagElement.text().trim();
                    if (tagText.contains("985")) {
                        schoolInfo.setSchoolIs985(1);
                    }
                    if (tagText.contains("211")) {
                        schoolInfo.setSchoolIs211(1);
                    }
                    if (tagText.contains("双一流")) {
                        schoolInfo.setSchoolIsDoubleFirst(1);
                    }
                }

                schools.add(schoolInfo);
            }

            return schools;
        } catch (Exception e) {
            log.error("搜索学校异常: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取所有学校ID
     *
     * @return 学校ID列表
     */
    public List<String> getAllSchoolIds() {
        try {
            List<String> schoolIds = new ArrayList<>();
            int page = 1;
            boolean hasMore = true;

            while (hasMore) {
                String url = GAOKAO_BASE_URL + "search?page=" + page;
                log.info("开始获取第{}页学校列表: {}", page, url);

                Document document = getDocument(url, null);
                if (document == null) {
                    break;
                }

                Elements schoolElements = document.select(".school-list-item");
                if (schoolElements.isEmpty()) {
                    hasMore = false;
                    continue;
                }

                for (Element schoolElement : schoolElements) {
                    Element linkElement = schoolElement.selectFirst(".school-name a");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href.contains("/school/")) {
                            String[] parts = href.split("/school/");
                            if (parts.length > 1) {
                                schoolIds.add(parts[1]);
                            }
                        }
                    }
                }

                page++;
                // 添加延迟，避免请求过快
                Thread.sleep(1000);
            }

            log.info("共获取到{}所学校的ID", schoolIds.size());
            return schoolIds;
        } catch (Exception e) {
            log.error("获取所有学校ID异常: {}", e.getMessage(), e);
            return List.of();
        }
    }
} 