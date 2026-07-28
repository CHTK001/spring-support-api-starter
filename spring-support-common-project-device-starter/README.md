# spring-support-common-project-device-starter

通用项目设备模块，负责设备商户、设备同步与预览编排

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-support-common-project-device-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 功能概览

| 类/接口 | 说明 |
|---------|------|
| `DeviceConfiguration` | 独立设备模块自动配置。 |
| `DeviceChannelController` | 独立设备通道接口。 |
| `DeviceController` | 独立设备接口。 |
| `DeviceTemplateController` | 独立设备模板接口。 |
| `SysDeviceEventController` | 独立设备事件接口。 |
| `SysDeviceMerchantController` | 独立设备商户接口。 |
| `DeviceMerchantMapper` | DeviceMerchantMapper |
| `SysDeviceCameraRecordMapper` | SysDeviceCameraRecordMapper |
| `SysDeviceCardEventMapper` | SysDeviceCardEventMapper |
| `SysDeviceChannelMapper` | SysDeviceChannelMapper |
| ... | 共 27 个类 |

---

## 配置说明

本模块为零配置模块，引入依赖后即可使用。

---

## 依赖关系

```
spring-support-common-project-device-starter
├── spring-api-support-common-system-starter
├── spring-api-support-common-entity-starter
├── spring-api-support-common-configuration-starter
├── spring-support-project-starter
├── utils-support-hikvision-starter
├── utils-support-dahua-starter
```