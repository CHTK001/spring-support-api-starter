package com.chua.starter.video.test.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.impl.GuanYingResourceProvider;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 视频 Provider 测试控制器
 *
 * @author CH
 */
@RestController
@RequestMapping("/api/video-test")
public class VideoTestController {

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "MV") String type,
            @RequestParam(defaultValue = "1") int page) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", keyword);
        result.put("type", type);
        result.put("page", page);

        try {
            // 创建 mock VideoSource
            VideoSource videoSource = new VideoSource();
            videoSource.setVideoSourceId(1);
            videoSource.setVideoSourceName("pkmp4测试");
            videoSource.setVideoSourcePlatform("pkmp4");

            // 直接实例化 provider
            GuanYingResourceProvider provider = new GuanYingResourceProvider(videoSource);

            VideoSearch search = new VideoSearch();
            search.setKeyword(keyword);
            search.setVideoType(type);
            search.setPage(page);
            search.setPageSize(20);

            long start = System.currentTimeMillis();
            ReturnPageResult<VideoInfoResult> pageResult = provider.searchResource(search);
            long cost = System.currentTimeMillis() - start;

            result.put("success", true);
            result.put("costMs", cost);
            List<VideoInfoResult> list = pageResult.data() != null ? pageResult.data().getData() : null;
            result.put("total", list != null ? list.size() : 0);
            result.put("list", list != null ? list : Collections.emptyList());
            result.put("msg", pageResult.msg());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("list", Collections.emptyList());
        }

        return result;
    }

    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam String url) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            VideoSource videoSource = new VideoSource();
            videoSource.setVideoSourceId(1);
            videoSource.setVideoSourceName("pkmp4测试");
            videoSource.setVideoSourcePlatform("pkmp4");

            GuanYingResourceProvider provider = new GuanYingResourceProvider(videoSource);
            VideoInfoResult detail = provider.getVideoDetail(url);

            result.put("success", true);
            result.put("data", detail);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/providers")
    public Map<String, Object> listProviders() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> providers = new ArrayList<>();
        providers.add("pkmp4");
        result.put("providers", providers);
        return result;
    }
}
