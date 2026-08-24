-- ACME 账户支持多证书服务商（acme4j 标准 ACME / aliyun CAS）
-- 新增 provider_type 字段标识账户使用的证书服务商，默认 acme（ACME 协议）

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `monitor_acme_account`
  ADD COLUMN `acme_account_provider_type` varchar(32) NOT NULL DEFAULT 'acme' COMMENT '证书服务商类型(acme=ACME协议/acme4j、cas=阿里云CAS)'
  AFTER `acme_account_server`;