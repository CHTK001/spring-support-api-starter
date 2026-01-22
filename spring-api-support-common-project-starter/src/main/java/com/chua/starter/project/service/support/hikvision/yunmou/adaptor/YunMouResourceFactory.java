package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.time.date.DateTime;
import com.chua.common.support.time.date.DateUtils;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.ThreadUtils;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.QyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.project.service.support.hikvision.yunmou.consumer.MessageHandler;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.CardHistory;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;
import com.chua.starter.entity.project.SysDeviceCardEvent;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import org.apache.commons.lang3.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.chua.starter.project.service.support.hikvision.HiYunMouClient.MINOR_MAP;

/**
 * 资源工厂
 *
 * @author CH
 * @since 1.0
 */
public class YunMouResourceFactory extends ResourceFactory {
    private final HikvisionClient client;
    private final SysProject sysProject;
    
    /**
     * 同步运行状态：true-运行中，false-已停止
     */
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);
    
    /**
     * 同步暂停状态：true-已暂停，false-未暂停
     */
    private final AtomicBoolean syncPaused = new AtomicBoolean(false);

    public YunMouResourceFactory(HikvisionClient client, SysProject sysProject) {
        super(client);
        this.client = client;
        this.sysProject = sysProject;
    }

    @Override
    public ZyxxFactory getZyxxFactory() {
        return new YunMouZyxxFactory(client, sysProject);
    }

    @Override
    public YyxxFactory getPeopleFactory() {
        return new YunMouYyxxFactory(client, sysProject);
    }

    @Override
    public QyxxFactory getRegionInfoFactory() {
        return new YunMouQyxxFactory(client, sysProject);
    }

    @Override
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        String url = "/api/v1/open/accessControl/accessEvents/actions/list" + "?pageNo=" + request.getPage() +
                "&pageSize=" + request.getPageSize() +
                "&deviceSerial=" + request.getSysDeviceSerialNumber() +
                "&major=5" +
                "&major=75" +
                "&startTime=" + URLEncoder.encode(DateTime.of(ObjectUtils.defaultIfNull(
                request.getStartTime(),
                LocalDateTime.now().minusDays(7)
        )).toFormat("yyyy-MM-dd'T'HH:mm:ss+08:00"), StandardCharsets.UTF_8) +
                "&endTime=" + URLEncoder.encode(DateTime.of(ObjectUtils.defaultIfNull(
                request.getStartTime(),
                LocalDateTime.now()
        )).toFormat("yyyy-MM-dd'T'HH:mm:ss+08:00"), StandardCharsets.UTF_8);
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + client.getToken());
        String s = client.executeGet(url, headers, String.class);
        CardHistory cardHistory = JSON.parseObject(s, CardHistory.class);
        Page<SysDeviceCardEvent> objectPage = new Page<>();
        objectPage.setTotal(cardHistory.getTotal());
        objectPage.setRecords(cardHistory.getRows().stream().map(rowsDTO -> {
            SysDeviceCardEvent sysDeviceCardEvent = new SysDeviceCardEvent();
            try {
                sysDeviceCardEvent.setSysDeviceCardEventTime(DateTime.of(DateUtils.parseDate(
                        rowsDTO.getTime(), "yyyy-MM-dd'T'HH:mm:ss+08:00"
                )).toLocalDateTime());
            } catch (ParseException ignored) {
            }
            sysDeviceCardEvent.setSysDeviceCardEventPersonInvolved(rowsDTO.getEmployeeNo());
            sysDeviceCardEvent.setSysDeviceCardEventMajorName("门禁事件");
            sysDeviceCardEvent.setSysDeviceCardEventMinorName(MINOR_MAP.get(rowsDTO.getMinor()));
            sysDeviceCardEvent.setSysDeviceCardEventMessageId(String.valueOf(rowsDTO.getSerialNo()));
            return sysDeviceCardEvent;
        }).sorted(Comparator.comparing(SysDeviceCardEvent::getSysDeviceCardEventTime).reversed()).collect(Collectors.toList()));
        objectPage.setCurrent(request.getPage());
        objectPage.setSize(request.getPageSize());
        objectPage.setPages(cardHistory.getTotal() / request.getPageSize());
        return objectPage;
    }

    /**
     * 暂停同步
     * 
     * @return 是否成功暂停
     */
    public boolean pauseSync() {
        if (!syncRunning.get()) {
            return false;
        }
        syncPaused.set(true);
        return true;
    }
    
    /**
     * 恢复同步
     * 
     * @return 是否成功恢复
     */
    public boolean resumeSync() {
        if (!syncRunning.get()) {
            return false;
        }
        syncPaused.set(false);
        return true;
    }
    
    /**
     * 停止同步
     * 
     * @return 是否成功停止
     */
    public boolean stopSync() {
        if (!syncRunning.get()) {
            return false;
        }
        syncRunning.set(false);
        syncPaused.set(false);
        return true;
    }
    
    /**
     * 获取同步状态
     * 
     * @return 同步状态信息
     */
    public Map<String, Boolean> getSyncStatus() {
        Map<String, Boolean> status = new HashMap<>(2);
        status.put("running", syncRunning.get());
        status.put("paused", syncPaused.get());
        return status;
    }
    
    /**
     * 获取门禁历史数据（支持暂停和停止）
     *
     * @param request 请求参数
     * @return 是否成功同步
     */
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        // 检查是否已经在运行
        if (syncRunning.get()) {
            return false;
        }
        
        // 设置运行状态
        syncRunning.set(true);
        syncPaused.set(false);
        
        try {
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Authorization", "Bearer " + client.getToken());
            
            // 创建消费者
            String s = client.executePost("/api/v1/mq/consumer/group3", new JSONObject(), headers, String.class);
            JSONObject jsonObject = JSON.parseObject(s, JSONObject.class);
            String consumerId = jsonObject.getString("consumerId");
            
            if (ObjectUtils.isEmpty(consumerId)) {
                return false;
            }
            
            long startTimeMillis = System.currentTimeMillis();
            long maxMillis = 5 * 60 * 1000L;
            
            // 循环同步消息
            while (syncRunning.get()) {
                // 检查是否暂停
                if (syncPaused.get()) {
                    ThreadUtils.sleepSecondsQuietly(1);
                    continue;
                }
                
                // 检查是否超时
                if (System.currentTimeMillis() - startTimeMillis >= maxMillis) {
                    break;
                }
                
                try {
                    // 获取消息
                    String message = client.executePostForm(
                        "/api/v1/mq/consumer/messages?autoCommit=true&consumerId=%s".formatted(consumerId), 
                        new JSONObject(), 
                        headers, 
                        String.class
                    );
                    
                    List<YunMouCardEventMessage> yunMouCardEventMessages = JSON.parseArray(message, YunMouCardEventMessage.class);
                    registerMessage(yunMouCardEventMessages, client);
                } catch (Exception e) {
                    // 记录异常但继续运行
                    continue;
                }
                
                // 等待10秒后继续
                ThreadUtils.sleepSecondsQuietly(10);
            }
            
            return true;
        } finally {
            // 确保清理状态
            syncRunning.set(false);
            syncPaused.set(false);
        }
    }

    private void registerMessage(List<YunMouCardEventMessage> yunMouCardEventMessages, HikvisionClient client) {
        Thread.ofVirtual().start(() -> {
            for (YunMouCardEventMessage yunMouCardEventMessage : yunMouCardEventMessages) {
                MessageHandler messageHandler = ServiceProvider.of(MessageHandler.class).getNewExtension(yunMouCardEventMessage.getMsgType());
                if (null == messageHandler) {
                    continue;
                }
                try {
                    messageHandler.handle(yunMouCardEventMessage, client);
                } catch (Exception ignored) {
                }
            }
        });
    }
}
