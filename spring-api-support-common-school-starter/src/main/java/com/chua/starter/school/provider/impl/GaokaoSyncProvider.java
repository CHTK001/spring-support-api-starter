package com.chua.starter.school.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.entity.SchoolMajor;
import com.chua.starter.school.entity.SchoolSyncConfig;
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
                        schoolInfo.setId(existingSchool.getId());
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
            schoolInfo.setLogoUrl(logoElement.attr("src"));
        }

        // 设置高考网链接
        schoolInfo.setGaokaoUrl(GAOKAO_BASE_URL + schoolId);

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
                        schoolInfo.setProvince(provinceCityParts[0] + "省");
                        schoolInfo.setCity(provinceCityParts[1]);
                    } else {
                        schoolInfo.setCity(provinceCity);
                    }
                    if (parts.length > 1) {
                        schoolInfo.setDistrict(parts[1].trim());
                    }
                } else {
                    schoolInfo.setProvince(location);
                }
            } else if (text.contains("隶属于")) {
                schoolInfo.setSubordinateTo(text.replace("隶属于：", "").trim());
            } else if (text.contains("学历层次")) {
                schoolInfo.setEducationLevel(text.replace("学历层次：", "").trim());
            } else if (text.contains("招办电话")) {
                schoolInfo.setPhone(text.replace("招办电话：", "").trim());
            } else if (text.contains("学校网址")) {
                schoolInfo.setWebsiteUrl(text.replace("学校网址：", "").trim());
            } else if (text.contains("招生网址")) {
                schoolInfo.setAdmissionUrl(text.replace("招生网址：", "").trim());
            } else if (text.contains("创建时间")) {
                schoolInfo.setFoundingYear(text.replace("创建时间：", "").trim());
            }
        }

        // 获取是否是985/211/双一流
        Element tagElement = document.selectFirst(".tag-list");
        if (tagElement != null) {
            String tagText = tagElement.text();
            schoolInfo.setIs985(tagText.contains("985") ? 1 : 0);
            schoolInfo.setIs211(tagText.contains("211") ? 1 : 0);
            schoolInfo.setIsDoubleFirst(tagText.contains("双一流") ? 1 : 0);
        }

        // 获取学校介绍
        Element introElement = document.selectFirst(".school-intro");
        if (introElement != null) {
            schoolInfo.setIntroduction(introElement.text().trim());
        }

        // 获取学校特色
        Element featuresElement = document.selectFirst(".schoolFeature");
        if (featuresElement != null) {
            schoolInfo.setFeatures(featuresElement.text().trim());
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
            return transactionTemplate.execute(status -> {
                try {
                    // 保存专业信息
                    int savedCount = 0;
                    int updatedCount = 0;

                    for (SchoolMajor major : majors) {
                        // 查找是否已存在该专业
                        Map<String, Object> queryMap = new HashMap<>();
                        queryMap.put("school_id", major.getSchoolId());
                        queryMap.put("major_code", major.getMajorCode());
                        SchoolMajor existingMajor = schoolMajorMapper.selectByMap(queryMap).stream().findFirst().orElse(null);

                        if (existingMajor != null) {
                            major.setId(existingMajor.getId());
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
            });

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
            major.setSchoolId(schoolInfo.getId());
            major.setSchoolCode(schoolInfo.getSchoolCode());

            // 获取专业名称
            Element nameElement = majorElement.selectFirst(".special-list-item-name");
            if (nameElement != null) {
                major.setMajorName(nameElement.text().trim());

                // 从专业名称链接中提取专业代码
                Element linkElement = nameElement.selectFirst("a");
                if (linkElement != null) {
                    String href = linkElement.attr("href");
                    if (href.contains("/special/")) {
                        String[] parts = href.split("/special/");
                        if (parts.length > 1) {
                            major.setMajorCode(parts[1]);
                            major.setDetailUrl(href);
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
                    major.setMajorCategory(parts[0].trim());
                    major.setMajorSubject(parts[1].trim());
                } else {
                    major.setMajorCategory(subjectText);
                }
            }

            // 获取授予学位
            Element degreeElement = majorElement.selectFirst(".degree");
            if (degreeElement != null) {
                major.setDegree(degreeElement.text().trim());
            }

            // 获取学制
            Element durationElement = majorElement.selectFirst(".year");
            if (durationElement != null) {
                major.setStudyDuration(durationElement.text().trim());
            }

            // 获取教育层次
            major.setEducationLevel(schoolInfo.getEducationLevel());

            // 设置排名
            major.setRanking(ranking++);

            // 获取是否为特色/重点专业标记
            Elements tagElements = majorElement.select(".tag");
            for (Element tagElement : tagElements) {
                String tagText = tagElement.text().trim();
                if (tagText.contains("特色专业")) {
                    major.setIsFeatured(1);
                }
                if (tagText.contains("重点专业")) {
                    major.setIsKeyMajor(1);
                }
            }

            // 获取核心课程和就业方向
            Elements descElements = majorElement.select(".desc");
            if (descElements.size() >= 1) {
                major.setCoreCourses(descElements.get(0).text().trim());
            }
            if (descElements.size() >= 2) {
                major.setCareerProspects(descElements.get(1).text().trim());
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
            if (StringUtils.hasText(config.getSyncCookie())) {
                headers.put("Cookie", config.getSyncCookie());
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
        // 这部分将在后续实现
        log.info("同步招生计划功能尚未实现");
        return false;
    }

    @Override
    public boolean syncHistoricalScores(String schoolId, SchoolSyncConfig config) {
        // 这部分将在后续实现
        log.info("同步历年分数功能尚未实现");
        return false;
    }

    @Override
    public boolean syncAllSchoolData(String schoolId, SchoolSyncConfig config) {
        // 使用TransactionTemplate替代@Transactional注解
        return transactionTemplate.execute(status -> {
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
        });
    }
} 