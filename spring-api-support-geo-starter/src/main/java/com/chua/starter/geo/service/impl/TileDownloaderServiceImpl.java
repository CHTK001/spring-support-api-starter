package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.config.GeoProperties;
import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.TileSource;
import com.chua.starter.geo.service.TileCacheService;
import com.chua.starter.geo.service.TileDownloaderService;
import com.chua.starter.geo.util.TileMathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.Proxy;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 瓦片下载服务实现，使用 Virtual Thread + Semaphore 控制并发。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TileDownloaderServiceImpl implements TileDownloaderService {

    private final GeoProperties properties;
    private final TileCacheService cacheService;
    private static final int TILE_SIZE = 256;

    @Override
    public DownloadResult downloadTiles(TileSource source, Bounds bounds, int zoom, String proxy, boolean parallel) {
        int[] tl = TileMathUtils.toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = TileMathUtils.toTile(bounds.getSouth(), bounds.getEast(), zoom);
        int rowWidth = br[0] - tl[0] + 1;
        int total = rowWidth * (br[1] - tl[1] + 1);

        if (total == 0) {
            return new DownloadResult(Collections.emptyMap(), 0, 0, 0);
        }

        int concurrency = Math.min(properties.getTile().getMaxConcurrency(), total);
        Semaphore sem = new Semaphore(concurrency);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        Map<String, byte[]> results = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(total);

        for (int idx = 0; idx < total; idx++) {
            final int i = idx;
            executor.submit(() -> {
                try {
                    sem.acquire();
                    int x = tl[0] + (i / rowWidth);
                    int y = tl[1] + (i % rowWidth);
                    String key = zoom + "_" + x + "_" + y;
                    String url = buildTileUrl(source, x, y, zoom);

                    if (properties.getTile().isCacheEnabled()) {
                        byte[] cached = cacheService.get(key);
                        if (cached != null && cached.length > 0) {
                            results.put(key, cached);
                            completed.incrementAndGet();
                            latch.countDown();
                            return;
                        }
                    }

                    byte[] data = doDownload(url, source, proxy);
                    if (data != null && data.length > 0) {
                        if (properties.getTile().isCacheEnabled()) {
                            cacheService.put(key, data);
                        }
                        results.put(key, data);
                        completed.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.warn("[{}] 瓦片下载异常: x={} y={} z={}: {}", source.getId(),
                            tl[0] + (i / rowWidth), tl[1] + (i % rowWidth), zoom, e.getMessage());
                } finally {
                    sem.release();
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("下载任务被中断");
        }
        executor.shutdown();

        log.info("[{}] 瓦片下载完成: {}/{} 成功, {} 失败", source.getId(),
                completed.get(), total, failed.get());
        return new DownloadResult(results, total, completed.get(), failed.get());
    }

    private byte[] doDownload(String url, TileSource source, String proxy) {
        try {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(properties.getTile().getTimeoutSeconds()))
                    .followRedirects(HttpClient.Redirect.NORMAL);
            if (proxy != null && !proxy.isEmpty() && !url.contains("tianditu")) {
                URI proxyUri = new URI(proxy);
                builder.proxy(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        return List.of(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort())));
                    }

                    @Override
                    public void connectFailed(URI uri, java.net.SocketAddress sa, IOException ie) {
                    }
                });
            }
            HttpClient client = builder.build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GeoD-Web/1.0")
                    .header("Accept", "image/*")
                    .timeout(Duration.ofSeconds(properties.getTile().getTimeoutSeconds()))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body() != null) {
                return response.body();
            }
            log.warn("[{}] HTTP {} for {}", source.getId(), response.statusCode(), url);
            return null;
        } catch (Exception e) {
            log.warn("[{}] 下载失败 {}: {}", source.getId(), url, e.getMessage());
            return null;
        }
    }

    private String buildTileUrl(TileSource source, int x, int y, int z) {
        String url = source.getUrl();
        if (source.getSubdomains() != null && !source.getSubdomains().isEmpty()) {
            int idx = (x + y + z) % source.getSubdomains().size();
            url = url.replace("{s}", source.getSubdomains().get(idx));
        }
        url = url.replace("{x}", String.valueOf(x));
        url = url.replace("{y}", String.valueOf(y));
        url = url.replace("{z}", String.valueOf(z));
        return url;
    }
}
