package com.chua.starter.monitor.configuration;

import com.chua.common.support.ai.rag.GlobalSettingProvider;
import com.chua.common.support.spi.annotations.Spi;

import java.util.List;

/**
* 运行时 (runtime) 全局设置提供者 SPI。
* <p>
* 注册 AI 端口开关等运行时配置项到 sys_AI_全局_setting 表。
* </p>
*
* @author CH
* @since 4.0.0
 */
@Spi("runtime")
public class RuntimeGlobalSettingProvider implements GlobalSettingProvider {

    @Override
    public String group() {
        return "runtime";
    }

    @Override
    public String groupName() {
        return "运行时配置";
    }

    @Override
    public List<SettingItem> getItems() {
        return List.of(
                new SettingItem("enabled", "true", "BOOLEAN", "启用聊天能力", 1),
                new SettingItem("appKeyEnabled", "true", "BOOLEAN", "AppKey 校验（开启 REST 能力时建议开启）", 2),
                new SettingItem("openaiRestEnabled", "true", "BOOLEAN", "开启 OpenAI RESTful 端口 (/v1/chat/completions)", 3),
                new SettingItem("claudeRestEnabled", "true", "BOOLEAN", "开启 Claude RESTful 端口", 4),
                new SettingItem("textEmbedEnabled", "true", "BOOLEAN", "开启 Text Embed 端口 (/v1/embeddings)", 5),
                new SettingItem("default_model", "", "STRING", "默认模型", 10),
                new SettingItem("compression_model", "", "STRING", "压缩模型", 11)
        );
    }
}