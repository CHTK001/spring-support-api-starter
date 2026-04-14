package com.chua.starter.music.service;

import com.chua.starter.music.config.MusicConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "music.real", matches = "true")
@SpringBootTest(
        classes = MusicRealApiSmokeTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.root=WARN",
                "logging.level.com.chua.starter.music=INFO",
                "music.http.connect-timeout-millis=8000",
                "music.http.read-timeout-millis=12000",
                "plugin.captcha.enable=false",
                "plugin.optional.enable=false",
                "plugin.api.uniform=false",
                "plugin.api.encode.enable=false",
                "plugin.api.decode.enable=false",
                "knife4j.enable=false",
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        })
class MusicRealApiSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSmokeTestLiveMusicApis() throws Exception {
        JsonNode sourcesResponse = getJson("/v1/music/sources");
        JsonNode sources = sourcesResponse.path("data");
        assertThat(sources.isArray()).isTrue();
        assertThat(sources).isNotEmpty();

        JsonNode aggregateSearch = getJson("/v1/music/search?source=all&keyword=%E5%91%A8%E6%9D%B0%E4%BC%A6&page=1&pageSize=5");
        assertThat(aggregateSearch.path("data").path("tracks").isArray()).isTrue();
        assertThat(aggregateSearch.path("data").path("tracks").size()).isGreaterThan(0);

        List<String> failures = new ArrayList<>();
        Map<String, String> report = new LinkedHashMap<>();
        for (JsonNode sourceNode : sources) {
            String source = sourceNode.path("code").asText();
            Optional<JsonNode> track = findTrack(source);
            if (track.isEmpty()) {
                failures.add("source=%s search returned no playable track".formatted(source));
                continue;
            }

            String trackId = track.get().path("trackId").asText();
            JsonNode trackDetailResponse = getJson("/v1/music/track/%s/%s".formatted(
                    source, UriUtils.encodePathSegment(trackId, StandardCharsets.UTF_8)));
            JsonNode trackDetail = trackDetailResponse.path("data");
            String streamUrl = trackDetail.path("streamUrl").asText();
            if (streamUrl == null || streamUrl.isBlank()) {
                failures.add("source=%s track detail missing streamUrl for %s".formatted(source, trackId));
                continue;
            }

            ResponseEntity<byte[]> streamResponse = stream(source, trackId);
            if (!streamResponse.getStatusCode().is2xxSuccessful() || streamResponse.getBody() == null || streamResponse.getBody().length == 0) {
                failures.add("source=%s stream proxy failed for %s with status %s".formatted(
                        source, trackId, streamResponse.getStatusCode()));
                continue;
            }

            Optional<JsonNode> playlist = findPlaylist(source);
            if (playlist.isEmpty()) {
                if ("bd".equals(source)) {
                    report.put(source, "search/detail/stream ok, playlist skipped because no configured Bodian playlist ids");
                    continue;
                }
                failures.add("source=%s playlist lookup returned no data".formatted(source));
                continue;
            }

            String playlistId = playlist.get().path("playlistId").asText();
            JsonNode playlistDetailResponse = getJson("/v1/music/playlist/%s/%s".formatted(
                    source, UriUtils.encodePathSegment(playlistId, StandardCharsets.UTF_8)));
            JsonNode playlistDetail = playlistDetailResponse.path("data");
            if (!playlistDetail.path("tracks").isArray() || playlistDetail.path("tracks").isEmpty()) {
                failures.add("source=%s playlist detail returned empty tracks for %s".formatted(source, playlistId));
                continue;
            }
            report.put(source, "search/detail/stream/playlist ok");
        }

        report.forEach((source, summary) -> System.out.println("[music-smoke] " + source + " -> " + summary));
        assertThat(failures).isEmpty();
    }

    private Optional<JsonNode> findTrack(String source) throws Exception {
        for (String keyword : keywordsFor(source)) {
            JsonNode response = getJson("/v1/music/search?source=%s&keyword=%s&page=1&pageSize=3".formatted(
                    source, encodeQuery(keyword)));
            JsonNode tracks = response.path("data").path("tracks");
            if (tracks.isArray() && !tracks.isEmpty()) {
                return Optional.of(tracks.get(0));
            }
        }
        return Optional.empty();
    }

    private Optional<JsonNode> findPlaylist(String source) throws Exception {
        for (String keyword : keywordsFor(source)) {
            JsonNode response = getJson("/v1/music/search/playlists?source=%s&keyword=%s&page=1&pageSize=3".formatted(
                    source, encodeQuery(keyword)));
            JsonNode playlists = response.path("data").path("playlists");
            if (playlists.isArray() && !playlists.isEmpty()) {
                return Optional.of(playlists.get(0));
            }
        }

        JsonNode categoryResponse = getJson("/v1/music/playlist/category?source=%s&page=1&pageSize=3".formatted(source));
        JsonNode playlists = categoryResponse.path("data").path("playlists");
        if (playlists.isArray() && !playlists.isEmpty()) {
            return Optional.of(playlists.get(0));
        }
        return Optional.empty();
    }

    private ResponseEntity<byte[]> stream(String source, String trackId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RANGE, "bytes=0-1023");
        return restTemplate.exchange(
                url("/v1/music/track/%s/%s/stream".formatted(source, UriUtils.encodePathSegment(trackId, StandardCharsets.UTF_8))),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class);
    }

    private JsonNode getJson(String path) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(url(path), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotBlank();
        return objectMapper.readTree(response.getBody());
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private String encodeQuery(String value) {
        return UriUtils.encodeQueryParam(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private List<String> keywordsFor(String source) {
        if ("demo".equals(source)) {
            return List.of("night", "starlit", "");
        }
        if ("git".equals(source)) {
            return List.of("", "music", "unknown");
        }
        if ("bd".equals(source)) {
            return List.of("周杰伦", "林俊杰", "陈奕迅");
        }
        return List.of("周杰伦", "林俊杰", "陈奕迅");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "com.chua.starter.common.support.configuration.CommonConfiguration",
            "com.chua.starter.common.support.configuration.ProjectConfiguration",
            "com.chua.starter.common.support.api.configuration.ApiConfiguration",
            "com.chua.starter.common.support.api.configuration.ApiFilterConfiguration",
            "com.chua.starter.common.support.configuration.CaptchaConfiguration",
            "com.chua.starter.common.support.configuration.security.RequestFingerprintAutoConfiguration",
            "com.chua.starter.common.support.cache.CacheConfiguration",
            "com.chua.starter.common.support.cache.CacheProxyCachingConfiguration",
            "com.chua.starter.common.support.configuration.AutoControllerRegistrar",
            "com.chua.starter.common.support.configuration.TypeConverterRegisterConfiguration",
            "com.chua.starter.common.support.configuration.ResourceWebMvcConfigurer",
            "com.chua.starter.common.support.configuration.MessageConverterWebMvcConfigurer",
            "com.chua.starter.swagger.support.Knife4jRunner",
            "com.chua.starter.swagger.support.Knife4jWebMvcConfigurer"
    })
    @org.springframework.context.annotation.Import(MusicConfiguration.class)
    static class TestApplication {
    }
}
