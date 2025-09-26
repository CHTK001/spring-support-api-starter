package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.date.DateTime;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.QyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.project.service.support.hikvision.yunmou.consumer.MessageHandler;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.CardHistory;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;
import com.chua.starter.system.entity.SysDeviceCardEvent;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import org.apache.commons.lang3.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chua.starter.project.service.support.hikvision.HiYunMouClient.MINOR_MAP;

/**
 * 资源工厂
 *
 * @author CH
 */
public class YunMouResourceFactory extends ResourceFactory {
    private final HikvisionClient client;
    private final SysProject sysProject;

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
     * 获取门禁历史数据
     *
     * @param request 请求
     * @return 获取组织机构接口
     */
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + client.getToken());
        String s = client.executePost("/api/v1/mq/consumer/group3", new JSONObject(), headers, String.class);
        JSONObject jsonObject = JSON.parseObject(s, JSONObject.class);
        String consumerId = jsonObject.getString("consumerId");
        if (ObjectUtils.isEmpty(consumerId)) {
            return false;
        }
        long startTimeMillis = System.currentTimeMillis();
        long maxMillis = 5 * 60 * 1000L;
        String message = client.executePostForm("/api/v1/mq/consumer/messages?autoCommit=true&consumerId=%s".formatted(consumerId), new JSONObject(), headers, String.class);
        while (true) {
            message = client.executePostForm("/api/v1/mq/consumer/messages?autoCommit=true&consumerId=%s".formatted(consumerId), new JSONObject(), headers, String.class);
            List<YunMouCardEventMessage> yunMouCardEventMessages = JSON.parseArray(message, YunMouCardEventMessage.class);
            registerMessage(yunMouCardEventMessages, client);
            if (System.currentTimeMillis() - startTimeMillis >= maxMillis) {
                break;
            }
            ThreadUtils.sleepSecondsQuietly(10);
        }
        return true;
    }

    private void registerMessage(List<YunMouCardEventMessage> yunMouCardEventMessages, HikvisionClient client) {
        if (null == yunMouCardEventMessages) {
            return;
        }
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
