package com.chua.starter.datalake.collection;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.model.CollectData;
import com.chua.datalake.support.spi.collection.Collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 天气数据采集器 — 定时从 Open-Meteo API 拉取天气数据
 * <p>通过定时任务周期性地获取气象数据，并推送到管线系统。</p>
 *
 * @author CH
 */
@Spi("weather-collector")
public class WeatherCollector implements Collector {

    private static final String API_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=39.9042&longitude=116.4074" +
        "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
        "&timezone=Asia/Shanghai";

    /** 默认采集间隔：600 秒（10 分钟） */
    private static final int DEFAULT_INTERVAL = 600;

    private volatile Consumer<CollectData> handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;

    @Override
    public String protocol() {
        return "HTTP";
    }

    @Override
    public int defaultPort() {
        return 0;
    }

    @Override
    public void start(int port) {
        if (!running.compareAndSet(false, true)) return;
        int interval = port > 0 ? port : DEFAULT_INTERVAL;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "weather-collector");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::fetchAndDispatch, 0, interval, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public void setHandler(Consumer<CollectData> handler) {
        this.handler = handler;
    }

    @Override
    public Consumer<CollectData> getHandler() {
        return handler;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void fetchAndDispatch() {
        try {
            String json = fetchWeather();
            if (json != null) {
                collect("weather", json, "open-meteo", null, null);
            }
        } catch (Exception ignored) {
            // 采集失败，下一轮重试
        }
    }

    private String fetchWeather() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}