package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.docker.support.callback.DockerResultCallback;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.callback.StatusUpdatingWebSocketDockerPullCallback;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.starter.monitor.entity.SystemSoftRegistry;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftImageMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.mapper.SystemSoftRecordMapper;
import com.chua.starter.monitor.mapper.SystemSoftRegistryMapper;
import com.chua.starter.monitor.service.DockerCallbackService;
import com.chua.starter.monitor.service.SystemSoftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 系统软件管理服务实现类
 *
 * @author CH
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftServiceImpl extends ServiceImpl<SystemSoftMapper, SystemSoft> implements SystemSoftService {

    private final SystemSoftMapper systemSoftMapper;
    private final SystemSoftRegistryMapper systemSoftRegistryMapper;
    private final SystemSoftImageMapper systemSoftImageMapper;
    private final SystemSoftRecordMapper systemSoftRecordMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SocketSessionTemplate socketSessionTemplate;
    @Autowired
    @Lazy
    private DockerCallbackService dockerCallbackService;

    @Override
    public ReturnResult<SystemSoft> getBySoftCode(String softCode) {
        log.debug("根据软件代码查询软件信息 - softCode: {}", softCode);

        try {
            if (!StringUtils.hasText(softCode)) {
                return ReturnResult.error("软件代码不能为空");
            }

            SystemSoft systemSoft = systemSoftMapper.selectBySoftCode(softCode);
            return ReturnResult.ok(systemSoft);

        } catch (Exception e) {
            log.error("根据软件代码查询软件信息异常 - softCode: {}", softCode, e);
            return ReturnResult.error("查询软件信息异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> selectSoftList(String softCode, String softName, String category, Integer status) {
        log.debug("条件查询软件列表 - softCode: {}, softName: {}, category: {}, status: {}",
                softCode, softName, category, status);

        try {
            LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(softCode), SystemSoft::getSystemSoftCode, softCode)
                    .like(StringUtils.hasText(softName), SystemSoft::getSystemSoftName, softName)
                    .eq(StringUtils.hasText(category), SystemSoft::getSystemSoftCategory, category)
                    .eq(Objects.nonNull(status), SystemSoft::getSystemSoftStatus, status)
                    .orderByDesc(SystemSoft::getCreateTime);

            List<SystemSoft> list = systemSoftMapper.selectList(wrapper);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("条件查询软件列表异常", e);
            return ReturnResult.error("查询软件列表异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SystemSoft> saveOrUpdateForSoft(SystemSoft systemSoft) {
        log.debug("保存或更新软件信息 - softCode: {}", systemSoft.getSystemSoftCode());

        try {
            // 数据验证
            ReturnResult<String> validateResult = validateSoftware(systemSoft);
            if (!validateResult.isOk()) {
                return ReturnResult.error(validateResult.getMsg());
            }

            // 设置默认值
            if (systemSoft.getSystemSoftStatus() == null) {
                systemSoft.setSystemSoftStatus(1);
            }
            if (systemSoft.getSystemSoftIsOfficial() == null) {
                systemSoft.setSystemSoftIsOfficial(0);
            }

            LocalDateTime now = LocalDateTime.now();
            if (systemSoft.getSystemSoftId() == null) {
                systemSoft.setCreateTime(now);
                systemSoftMapper.insert(systemSoft);
            } else {
                systemSoft.setUpdateTime(now);
                systemSoftMapper.updateById(systemSoft);
            }

            return ReturnResult.ok(systemSoft, "操作成功");

        } catch (Exception e) {
            log.error("保存或更新软件信息异常 - softCode: {}", systemSoft.getSystemSoftCode(), e);
            return ReturnResult.error("操作异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> removeById(Integer softId) {
        log.debug("删除软件 - softId: {}", softId);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }

            // 检查软件是否存在
            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            // 检查是否有关联的镜像或容器
            // TODO: 添加关联检查逻辑

            int result = systemSoftMapper.deleteById(softId);
            return ReturnResult.ok(result > 0, result > 0 ? "删除成功" : "删除失败");

        } catch (Exception e) {
            log.error("删除软件异常 - softId: {}", softId, e);
            return ReturnResult.error("删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> removeByIds(List<Integer> softIds) {
        log.debug("批量删除软件 - softIds: {}", softIds);

        try {
            if (softIds == null || softIds.isEmpty()) {
                return ReturnResult.error("软件ID列表不能为空");
            }

            int result = systemSoftMapper.deleteBatchIds(softIds);
            return ReturnResult.ok(result > 0, "批量删除完成，共删除" + result + "条记录");

        } catch (Exception e) {
            log.error("批量删除软件异常 - softIds: {}", softIds, e);
            return ReturnResult.error("批量删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> syncFromRegistry(Integer registryId) {
        log.info("从仓库同步软件信息 - registryId: {}", registryId);

        try {
            SystemSoftRegistry registry;
            if (registryId == null) {
                // 获取默认仓库
                registry = systemSoftRegistryMapper.selectDefaultRegistry();
                if (registry == null) {
                    return ReturnResult.error("未设置默认仓库");
                }
            } else {
                registry = systemSoftRegistryMapper.selectById(registryId);
                if (registry == null) {
                    return ReturnResult.error("仓库不存在");
                }
            }

            if (registry.getSystemSoftRegistryStatus() != 1) {
                return ReturnResult.error("仓库未启用");
            }
            if (registry.getSystemSoftRegistrySupportSync() == null || registry.getSystemSoftRegistrySupportSync() != 1) {
                return ReturnResult.error("该仓库不支持同步");
            }

            // 使用 Docker Registry V2 标准接口进行同步（优先），必要时可在后续扩展不同仓库类型的专属实现
            int syncCount = doRegistryV2Sync(registry);

            log.info("同步软件信息完成 - 同步数量: {}", syncCount);
            return ReturnResult.ok(syncCount, "同步成功，共同步" + syncCount + "个软件");

        } catch (Exception e) {
            log.error("从仓库同步软件信息异常 - registryId: {}", registryId, e);
            return ReturnResult.error("同步异常: " + e.getMessage());
        }
    }

    /**
     * 通过 Docker Registry V2 API 同步仓库中的镜像仓库列表到系统软件库
     *
     * <p>实现要点：
     * 1) 访问 {registryUrl}/v2/_catalog?n=1000 获取仓库列表（必要时根据 Link 分页继续拉取）
     * 2) 解析 JSON 中的 repositories 字段
     * 3) 将每个仓库映射为一条 SystemSoft 记录（存在则更新，不存在则新增）
     * 4) 对于非 DockerHub 的私有仓库，systemSoftDockerImage 使用 "host/repo" 形式；DockerHub 则使用 "namespace/repo"
     *
     * 注意：
     * - 本实现仅同步仓库列表，不同步标签；标签后续由其它接口按需获取
     * - 解析采用轻量字符串解析，避免引入额外依赖
     *
     * @param registry 仓库配置
     * @return 成功新增或更新的软件数量
     * @author CH
     * @since 2025/10/18
     * @version 1.0.0
     */
    private int doRegistryV2Sync(SystemSoftRegistry registry) {
        String baseUrl = trimTrailingSlash(registry.getSystemSoftRegistryUrl());
        String username = registry.getSystemSoftRegistryUsername();
        String password = registry.getSystemSoftRegistryPassword();
        String type = registry.getSystemSoftRegistryType();

        int total = 0;
        String nextUrl = baseUrl + "/v2/_catalog?n=1000";
        while (StringUtils.hasText(nextUrl)) {
            try {
                HttpResponse resp = httpGet(nextUrl, username, password, registry.getSystemSoftRegistryTimeout());
                if (resp == null || resp.code < 200 || resp.code >= 400) {
                    log.warn("拉取仓库列表失败: httpCode={}, url={}", resp != null ? resp.code : -1, nextUrl);
                    break;
                }
                List<String> repositories = parseRepositories(resp.body);
                if (repositories.isEmpty()) {
                    log.debug("未解析到仓库列表, url={}", nextUrl);
                }
                for (String repo : repositories) {
                    // 计算镜像名称：
                    String imageName = buildImageName(type, baseUrl, repo);
                    // 计算软件名/代码
                    String softName = repo.contains("/") ? repo.substring(repo.lastIndexOf('/') + 1) : repo;
                    String softCode = repo.replace('/', '_');

                    // 已存在则更新，不存在则新增
                    SystemSoft existed = systemSoftMapper.selectByDockerImage(imageName);
                    LocalDateTime now = LocalDateTime.now();
                    if (existed == null) {
                        SystemSoft soft = new SystemSoft();
                        soft.setSystemSoftName(softName);
                        soft.setSystemSoftCode(softCode);
                        soft.setSystemSoftDockerImage(imageName);
                        soft.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                        soft.setSystemSoftStatus(1);
                        soft.setSystemSoftIsOfficial(isDockerHubOfficial(type, repo) ? 1 : 0);
                        soft.setCreateTime(now);
                        soft.setUpdateTime(now);
                        systemSoftMapper.insert(soft);
                        total++;
                    } else {
                        existed.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                        existed.setUpdateTime(now);
                        // 保持名称/代码与仓库一致（如需）
                        if (!StringUtils.hasText(existed.getSystemSoftName())) {
                            existed.setSystemSoftName(softName);
                        }
                        if (!StringUtils.hasText(existed.getSystemSoftCode())) {
                            existed.setSystemSoftCode(softCode);
                        }
                        if (existed.getSystemSoftStatus() == null) {
                            existed.setSystemSoftStatus(1);
                        }
                        systemSoftMapper.updateById(existed);
                        total++;
                    }
                }

                // 处理分页：Docker Registry V2 可通过 Link 头部提供下一页
                String link = resp.link;
                if (!StringUtils.hasText(link)) {
                    break;
                }
                String parsedNext = parseNextLink(link, baseUrl);
                nextUrl = parsedNext;
            } catch (Exception ex) {
                log.error("同步仓库分页数据失败, url={}, err={}", nextUrl, ex.getMessage());
                break;
            }
        }
        return total;
    }

    /**
     * 是否为 DockerHub 官方镜像（namespace 为 library）
     */
    private boolean isDockerHubOfficial(String type, String repo) {
        if (!StringUtils.hasText(type)) {
            return false;
        }
        return "docker_hub".equalsIgnoreCase(type) && repo.startsWith("library/");
    }

    /**
     * 构建镜像名称：
     * - 对于 docker_hub：保持 "namespace/repo" 格式（如 library/nginx）
     * - 其它类型：使用 "host/repo"（从 registryUrl 提取 host[:port]）
     */
    private String buildImageName(String type, String baseUrl, String repo) {
        if ("docker_hub".equalsIgnoreCase(type)) {
            return repo;
        }
        try {
            java.net.URL u = new java.net.URL(baseUrl);
            String host = u.getHost();
            int port = u.getPort();
            String hostPort = port > 0 ? host + ":" + port : host;
            return hostPort + "/" + repo;
        } catch (Exception ignore) {
            return repo;
        }
    }

    /**
     * 解析 Docker Registry V2 _catalog 响应中的 repositories 字段
     * 预期 JSON：{"repositories":["a","b"]}
     */
    private List<String> parseRepositories(String body) {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        if (!StringUtils.hasText(body)) {
            return list;
        }
        int key = body.indexOf("\"repositories\"");
        if (key < 0) {
            return list;
        }
        int lb = body.indexOf('[', key);
        int rb = body.indexOf(']', lb + 1);
        if (lb < 0 || rb < 0 || rb <= lb) {
            return list;
        }
        String arr = body.substring(lb + 1, rb).trim();
        if (arr.isEmpty()) {
            return list;
        }
        // 简单字符串数组解析：按逗号分割，去除引号与空格
        int idx = 0;
        while (idx < arr.length()) {
            // 跳过空白与逗号
            char c = arr.charAt(idx);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == ',') {
                idx++;
                continue;
            }
            if (c == '"') {
                int end = arr.indexOf('"', idx + 1);
                if (end > idx + 1) {
                    String v = arr.substring(idx + 1, end);
                    if (StringUtils.hasText(v)) {
                        list.add(v);
                    }
                    idx = end + 1;
                } else {
                    break;
                }
            } else {
                // 非严格 JSON（容错）：读取到下一个逗号
                int end = arr.indexOf(',', idx + 1);
                String v = end > 0 ? arr.substring(idx, end) : arr.substring(idx);
                v = v.trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);
                }
                if (StringUtils.hasText(v)) {
                    list.add(v);
                }
                idx = end > 0 ? end + 1 : arr.length();
            }
        }
        return list;
    }

    /**
     * 解析 Link 头的 next URL。
     * 形如：<https://registry/v2/_catalog?last=xxx&n=1000>; rel="next"
     */
    private String parseNextLink(String link, String baseUrl) {
        try {
            // 仅提取尖括号内的 URL
            int lb = link.indexOf('<');
            int rb = link.indexOf('>');
            if (lb >= 0 && rb > lb) {
                String url = link.substring(lb + 1, rb);
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return url;
                }
                // 相对路径情况

                if (url.startsWith("/")) {
                    return trimTrailingSlash(baseUrl) + url;
                }
                return trimTrailingSlash(baseUrl) + "/" + url;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * GET 请求简易封装
     */
    private HttpResponse httpGet(String url, String username, String password, Integer timeoutSeconds) {
        java.net.HttpURLConnection conn = null;
        try {
            java.net.URL u = new java.net.URL(url);
            conn = (java.net.HttpURLConnection) u.openConnection();
            int timeoutMs = (timeoutSeconds == null || timeoutSeconds <= 0) ? 5000 : timeoutSeconds * 1000;
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                String basic = username + ":" + password;
                String enc = java.util.Base64.getEncoder().encodeToString(basic.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + enc);
            }
            int code = conn.getResponseCode();
            String link = conn.getHeaderField("Link");
            java.io.InputStream is = code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream();
            String body = readStream(is);
            return new HttpResponse(code, body, link);
        } catch (Exception e) {
            log.warn("HTTP GET 失败: url={}, err={}", url, e.getMessage());
            return new HttpResponse(-1, null, null);
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Exception ignore) {}
            }
        }
    }

    private String readStream(java.io.InputStream is) {
        if (is == null) { return null; }
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String trimTrailingSlash(String s) {
        if (!StringUtils.hasText(s)) { return s; }
        if (s.endsWith("/")) { return s.substring(0, s.length() - 1); }
        return s;
    }

    private static class HttpResponse {
        final int code;
        final String body;
        final String link;
        HttpResponse(int code, String body, String link) {
            this.code = code;
            this.body = body;
            this.link = link;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SystemSoft> syncSingleSoftware(Integer registryId, String softwareName) {
        log.info("同步单个软件信息 - registryId: {}, softwareName: {}", registryId, softwareName);

        try {
            if (!StringUtils.hasText(softwareName)) {
                return ReturnResult.error("软件名称不能为空");
            }

            SystemSoftRegistry registry = systemSoftRegistryMapper.selectById(registryId);
            if (registry == null) {
                return ReturnResult.error("仓库不存在");
            }

            // TODO: 实现具体的单个软件同步逻辑
            // 这里暂时返回空结果
            
            return ReturnResult.ok(null, "同步成功");

        } catch (Exception e) {
            log.error("同步单个软件信息异常 - registryId: {}, softwareName: {}", registryId, softwareName, e);
            return ReturnResult.error("同步异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> installSoftware(Integer softId, List<Integer> serverIds, String imageTag) {
        log.info("安装软件 - softId: {}, serverIds: {}, imageTag: {}", softId, serverIds, imageTag);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            if (!StringUtils.hasText(imageTag)) {
                imageTag = "latest";
            }

            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            if (!StringUtils.hasText(systemSoft.getSystemSoftDockerImage())) {
                return ReturnResult.error("软件未配置Docker镜像");
            }

            // 对每个服务器执行安装
            for (Integer serverId : serverIds) {
                // 创建镜像记录
                SystemSoftImage image = new SystemSoftImage();
                image.setSystemSoftId(softId);
                image.setSystemSoftImageServerId(serverId);
                image.setSystemSoftImageName(systemSoft.getSystemSoftDockerImage());
                image.setSystemSoftImageTag(imageTag);
                image.setCreateTime(LocalDateTime.now());
                image.setUpdateTime(LocalDateTime.now());
                systemSoftImageMapper.insert(image);
            }

            return ReturnResult.ok(true, "安装成功");

        } catch (Exception e) {
            log.error("安装软件异常 - softId: {}, serverIds: {}, imageTag: {}", softId, serverIds, imageTag, e);
            return ReturnResult.error("安装异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getAvailableTags(Integer softId) {
        log.debug("获取软件可用标签 - softId: {}", softId);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }

            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            // TODO: 实现从仓库获取标签列表的逻辑
            // 这里暂时返回默认标签
            List<String> tags = List.of("latest", "stable");
            
            return ReturnResult.ok(tags);

        } catch (Exception e) {
            log.error("获取软件可用标签异常 - softId: {}", softId, e);
            return ReturnResult.error("获取标签异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkSoftCodeExists(String softCode, Integer excludeId) {
        log.debug("检查软件代码是否存在 - softCode: {}, excludeId: {}", softCode, excludeId);

        try {
            if (!StringUtils.hasText(softCode)) {
                return ReturnResult.error("软件代码不能为空");
            }

            Long count = systemSoftMapper.checkSoftCodeExists(softCode, excludeId);
            return ReturnResult.ok(count > 0);

        } catch (Exception e) {
            log.error("检查软件代码异常 - softCode: {}, excludeId: {}", softCode, excludeId, e);
            return ReturnResult.error("检查异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdateStatus(List<Integer> softIds, Integer status) {
        log.debug("批量更新软件状态 - softIds: {}, status: {}", softIds, status);

        try {
            if (softIds == null || softIds.isEmpty()) {
                return ReturnResult.error("软件ID列表不能为空");
            }
            if (status == null) {
                return ReturnResult.error("状态不能为空");
            }

            Integer updateCount = systemSoftMapper.batchUpdateStatus(softIds, status);
            return ReturnResult.ok(updateCount > 0, "批量更新成功，共更新" + updateCount + "条记录");

        } catch (Exception e) {
            log.error("批量更新软件状态异常 - softIds: {}, status: {}", softIds, status, e);
            return ReturnResult.error("批量更新异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> searchByKeyword(String keyword) {
        log.debug("根据关键字搜索软件 - keyword: {}", keyword);

        try {
            if (!StringUtils.hasText(keyword)) {
                return ReturnResult.error("关键字不能为空");
            }

            List<SystemSoft> list = systemSoftMapper.searchByKeyword(keyword);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("根据关键字搜索软件异常 - keyword: {}", keyword, e);
            return ReturnResult.error("搜索异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getByCategory(String category) {
        log.debug("根据分类获取软件列表 - category: {}", category);

        try {
            List<SystemSoft> list = systemSoftMapper.selectByCategory(category);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("根据分类获取软件列表异常 - category: {}", category, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getPopularSofts(Integer limit) {
        log.debug("获取热门软件列表 - limit: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            List<SystemSoft> list = systemSoftMapper.selectPopularSofts(limit);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("获取热门软件列表异常 - limit: {}", limit, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getLatestSofts(Integer limit) {
        log.debug("获取最新软件列表 - limit: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            List<SystemSoft> list = systemSoftMapper.selectLatestSofts(limit);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("获取最新软件列表异常 - limit: {}", limit, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    /**
     * 拉取Docker镜像（支持异步回调）
     * <p>
     * 该方法实现了拉取Docker镜像的功能，支持异步回调通知。整个过程包括：
     * 1. 创建Docker客户端连接
     * 2. 获取镜像信息并创建镜像记录
     * 3. 构造DockerPullImage配置对象
     * 4. 创建状态更新WebSocket回调处理器
     * 5. 执行异步镜像拉取操作
     * <p>
     * 该方法使用异步回调机制，在拉取过程中实时更新镜像状态并通过WebSocket推送进度信息。
     * 注意：由于是异步操作，方法返回时拉取可能尚未完成，状态更新在回调中处理。
     *
     * @param softId   软件ID，用于查询软件的Docker镜像信息
     * @param serverId 服务器ID，指定目标服务器
     * @param imageTag 镜像标签，默认为"latest"
     * @param params   拉取参数（保留用于扩展）
     * @return 拉取结果，包含镜像信息或错误信息
     * @see DockerProtocolClient Docker客户端
     * @see DockerPullImage 镜像拉取配置对象
     * @see DockerCallbackService#pullImageWithCallback(DockerProtocolClient, DockerPullImage, DockerResultCallback) 回调服务方法
     * @see StatusUpdatingWebSocketDockerPullCallback 状态更新WebSocket回调处理器
     */
    public ReturnResult<SystemSoftImage> pullDockerImageWithCallback(Integer softId, Integer serverId, String imageTag, java.util.Map<String, Object> params) {
        log.info("拉取Docker镜像(带回调) - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);

        try {
            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取镜像信息
            String imageName = "nginx"; // 这里应该根据softId查询实际的镜像名称

            // 构造完整的镜像名称
            String fullImageName = imageName;
            if (StringUtils.hasText(imageTag)) {
                fullImageName = imageName + ":" + imageTag;
            }

            // 创建镜像记录
            SystemSoftImage image = new SystemSoftImage();
            image.setSystemSoftId(softId);
            image.setSystemSoftImageServerId(serverId);
            image.setSystemSoftImageName(imageName);
            image.setSystemSoftImageTag(imageTag);
            image.setSystemSoftImageFullName(fullImageName);
            image.setSystemSoftImageStatus(SystemSoftImageStatus.PULLING); // 拉取中
            image.setCreateTime(LocalDateTime.now());
            systemSoftImageMapper.insert(image);

            // 创建DockerPullImage配置对象
            DockerPullImage config = DockerPullImage.builder()
                    .imageName(imageName)
                    .tag(imageTag)
                    .build();

            // 创建状态更新WebSocket回调处理器
            var callback = dockerCallbackService.createStatusUpdatingWebSocketPullCallback(OperationType.PULL, fullImageName, image.getSystemSoftImageId());

            // 执行拉取操作（带回调）
            log.info("开始拉取镜像(带回调): {}:{}", imageName, imageTag);
            dockerCallbackService.pullImageWithCallback(dockerClient, config, callback);

            // 注意：由于是异步操作，我们不能立即更新状态为已拉取
            // 状态更新应该在回调中处理

            log.info("拉取镜像请求已发送 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);
            return ReturnResult.ok(image);
        } catch (Exception e) {
            // 更新镜像状态为拉取失败
            try {
                SystemSoftImage image = new SystemSoftImage();
                image.setSystemSoftImageStatus(SystemSoftImageStatus.PULL_FAILED); // 拉取失败
                systemSoftImageMapper.updateById(image);
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为拉取失败时出错: {}", updateEx.getMessage());
            }

            log.error("拉取镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("拉取镜像失败: " + e.getMessage());
        }
    }

    /**
     * 验证软件信息
     *
     * @param systemSoft 软件信息
     * @return 验证结果
     */
    private ReturnResult<String> validateSoftware(SystemSoft systemSoft) {
        if (systemSoft == null) {
            return ReturnResult.error("软件信息不能为空");
        }

        if (!StringUtils.hasText(systemSoft.getSystemSoftCode())) {
            return ReturnResult.error("软件代码不能为空");
        }

        if (!StringUtils.hasText(systemSoft.getSystemSoftName())) {
            return ReturnResult.error("软件名称不能为空");
        }

        // 检查软件代码是否重复
        if (systemSoft.getSystemSoftId() == null) {
            // 新增时检查
            SystemSoft existing = systemSoftMapper.selectBySoftCode(systemSoft.getSystemSoftCode());
            if (existing != null) {
                return ReturnResult.error("软件代码已存在");
            }
        } else {
            // 更新时检查
            Long count = systemSoftMapper.checkSoftCodeExists(systemSoft.getSystemSoftCode(), systemSoft.getSystemSoftId());
            if (count > 0) {
                return ReturnResult.error("软件代码已被其他软件使用");
            }
        }

        return ReturnResult.ok("验证通过");
    }
}