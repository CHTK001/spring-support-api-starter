/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80016 (8.0.16)
 Source Host           : 172.16.0.40:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 80016 (8.0.16)
 File Encoding         : 65001

 Date: 27/03/2025 14:40:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_ai_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_module`;
CREATE TABLE `sys_ai_module`  (
  `sys_ai_module_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_ai_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `sys_ai_module_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型编码',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `sys_ai_module_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `sys_ai_module_status` int(11) NULL DEFAULT NULL COMMENT '是否禁用;0:禁用, 1:可用',
  `sys_ai_module_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_ai_module_manufacturers` int(11) NULL DEFAULT NULL COMMENT '厂商(字典表1)',
  `sys_ai_module_role_setting` int(11) NULL DEFAULT NULL COMMENT '是否存在角色设置;0:不存在',
  `sys_ai_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型类型',
  `sys_ai_module_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型单独地址',
  `sys_ai_module_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型版本',
  `sys_ai_module_vlm` int(11) NULL DEFAULT NULL COMMENT '是否是vlm模型;0:否',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_module_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ai模型分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ai_module
-- ----------------------------
INSERT INTO `sys_ai_module` VALUES (1, 'Spark Lite', 'Spark Lite', 6, NULL, 1, NULL, 10, 0, 'LLM', NULL, 'lite', 0, 'sa', 1, '2025-02-07 16:38:03', '2025-02-08 11:31:17', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (2, 'deepseek-chat', 'deepseek-chat', 7, NULL, 1, 0, 21, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-08 15:20:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (3, 'moonshot-v1-8k', 'moonshot-v1-8k', 8, NULL, 1, 0, 22, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-08 16:40:10', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (4, 'deepseek-r1:1.5b', 'deepseek-r1:1.5b', 9, NULL, 1, 0, 23, 1, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-10 10:17:12', '2025-02-10 11:17:10', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (5, 'gpt-4o-mini', 'gpt-4o-mini', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-17 09:35:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (6, 'bot-20250220142651-gh6tp', 'bot-20250220142651-gh6tp', 12, NULL, 1, 0, 26, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-20 14:43:43', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (7, 'wanx-v1', 'wanx-v1', 13, NULL, 1, 0, 2, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-02-26 11:07:15', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (8, 'black-forest-labs/FLUX.1-schnell', 'black-forest-labs/FLUX.1-schnell', 14, 'FLUX.1 [schnell] 是一个 120 亿参数的 Rectified Flow Transformer 模型，能够根据文本描述生成图像。该模型采用潜在对抗扩散蒸馏技术训练，可以在1到4步内生成高质量图像。它具有最先进的输出质量和具有竞争力的提示跟随能力，匹配封闭源替代方案的性能。该模型在 Apache 2.0 许可下发布，可用于个人、科学和商业目的', 1, 0, 28, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-27 16:54:44', '2025-02-28 08:39:31', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (9, 'stabilityai/stable-diffusion-xl-base-1.0', 'stabilityai/stable-diffusion-xl-base-1.0', 14, 'Stable Diffusion XL（SDXL）是一个基于潜在扩散的文本到图像生成模型，使用了两个固定的预训练文本编码器（OpenCLIP-ViT/G 和 CLIP-ViT/L）。SDXL 由基础模型和精炼模型组成，可以单独使用基础模型或结合两者以获得更好的效果。该模型在生成艺术作品、设计和创意工具应用等方面表现出色，但仅限于研究目的使用', 1, 0, 28, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 10:10:11', '2025-02-28 10:11:41', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (10, 'stabilityai/stable-diffusion-3-5-large', 'stabilityai/stable-diffusion-3-5-large', 14, 'Stable Diffusion 3.5 Large 是 Stable Diffusion 系列中最强大的基础模型，拥有 80 亿参数。该模型具有卓越的图像质量和更好的提示遵循能力，特别适合在 1 兆像素分辨率下进行专业用途', 1, 2, 28, 0, 'VINCENT', NULL, 'v3.5', 0, 'sa', 1, '2025-02-28 10:44:14', '2025-02-28 10:45:09', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (11, 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 15, 'DeepSeek-R1-Distill-Qwen-7B 是基于 Qwen2.5-Math-7B 通过知识蒸馏得到的模型。该模型使用 DeepSeek-R1 生成的 80 万个精选样本进行微调，展现出优秀的推理能力。在多个基准测试中表现出色，其中在 MATH-500 上达到了 92.8% 的准确率，在 AIME 2024 上达到了 55.5% 的通过率，在 CodeForces 上获得了 1189 的评分，作为 7B 规模的模型展示了较强的数学和编程能力\n', 1, 1, 28, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 11:30:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (12, 'Qwen/Qwen2.5-Coder-7B-Instruct', 'Qwen/Qwen2.5-Coder-7B-Instruct', 15, 'Qwen2.5-Coder-7B-Instruct 是阿里云发布的代码特定大语言模型系列的最新版本。该模型在 Qwen2.5 的基础上，通过 5.5 万亿个 tokens 的训练，显著提升了代码生成、推理和修复能力。它不仅增强了编码能力，还保持了数学和通用能力的优势。模型为代码智能体等实际应用提供了更全面的基础', 1, 1, 28, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 13:43:38', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (13, 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 14, 'DeepSeek-R1-Distill-Qwen-7B 是基于 Qwen2.5-Math-7B 通过知识蒸馏得到的模型。该模型使用 DeepSeek-R1 生成的 80 万个精选样本进行微调，展现出优秀的推理能力。在多个基准测试中表现出色，其中在 MATH-500 上达到了 92.8% 的准确率，在 AIME 2024 上达到了 55.5% 的通过率，在 CodeForces 上获得了 1189 的评分，作为 7B 规模的模型展示了较强的数学和编程能力', 1, 1, 28, 0, 'LLM', 'https://api.siliconflow.cn/v1/chat/completions', 'v1.0', 0, 'sa', 1, '2025-02-28 15:21:14', '2025-02-28 15:21:58', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (14, 'cogview-3-flash', 'cogview-3-flash', 17, 'CogView-3-Flash是智谱AI推出的免费图像生成模型，能够根据用户指令生成符合要求且美学评分更高的图像。CogView-3-Flash主要应用于艺术创作、设计参考、游戏开发、虚拟现实等领域，帮助用户快速实现从文本到图像的转换需求。', 1, 1, 30, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 15:31:07', '2025-02-28 15:44:18', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (15, 'glm-4-flash', 'glm-4-flash', 17, NULL, 1, 1, 30, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 15:53:45', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (16, 'Kwai-Kolors/Kolors', 'Kwai-Kolors/Kolors', 14, 'Kolors 是由快手 Kolors 团队开发的基于潜在扩散的大规模文本到图像生成模型。该模型通过数十亿文本-图像对的训练，在视觉质量、复杂语义准确性以及中英文字符渲染方面展现出显著优势。它不仅支持中英文输入，在理解和生成中文特定内容方面也表现出色', 1, 0, 28, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-03-01 17:47:46', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (17, 'moonshotai/moonlight-16b-a3b-instruct:free', 'moonshotai/moonlight-16b-a3b-instruct:free', 16, 'Moonlight-16B-A3B-Instruct 是由 Moonshot AI 开发的一个具有 160 亿参数的混合专家（MoE）语言模型。它针对指令跟随任务进行了优化，每次推理有 30 亿激活参数。该模型在英语、编码、数学和中文基准测试中，在每 FLOP 的性能方面推进了帕累托前沿。它在性能上优于类似的模型，如 Llama3-3B 和 Deepseek-v2-Lite，同时通过与 Hugging Face 集成以及与流行的推理引擎（如 vLLM12）兼容，保持了高效的部署能力。', 1, 1, 29, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 19:19:15', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (18, 'cognitivecomputations/dolphin3.0-r1-mistral-24b:free', 'cognitivecomputations/dolphin3.0-r1-mistral-24b:free', 16, 'Dolphin 3.0 R1 是 Dolphin 系列指令微调模型的下一代产品。旨在成为终极通用本地模型，支持编程、数学、智能代理、函数调用和一般用例。R1 版本经过了 3 个周期的训练，使用来自 Dolphin-R1 数据集的 80 万个推理轨迹进行推理。', 1, 0, 29, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 19:25:55', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (19, 'deepseek-r1', 'deepseek-r1', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:30:36', '2025-03-01 21:30:58', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (20, 'deepseek-r1-distill-qwen-32b', 'deepseek-r1-distill-qwen-32b', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:35:34', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (21, 'deepseek-v3', 'deepseek-v3', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:37:42', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (22, '@cf/meta/llama-3.1-8b-instruct', '@cf/meta/llama-3.1-8b-instruct', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 22:36:47', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (23, '@cf/deepseek-ai/deepseek-math-7b-instruct', '@cf/deepseek-ai/deepseek-math-7b-instruct', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 22:54:41', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (24, '@cf/stabilityai/stable-diffusion-xl-base-1.0', '@cf/stabilityai/stable-diffusion-xl-base-1.0', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/stabilityai/stable-diffusion-xl-base-1.0', NULL, 0, 'sa', 1, '2025-03-01 23:20:09', '2025-03-02 00:37:09', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (25, '@cf/black-forest-labs/flux-1-schnell', '@cf/black-forest-labs/flux-1-schnell', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/black-forest-labs/flux-1-schnell', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (26, '@cf/lykon/dreamshaper-8-lcm', '@cf/lykon/dreamshaper-8-lcm', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/lykon/dreamshaper-8-lcm', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (27, '@cf/bytedance/stable-diffusion-xl-lightning', '@cf/bytedance/stable-diffusion-xl-lightning', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/bytedance/stable-diffusion-xl-lightning', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (28, '@cf/runwayml/stable-diffusion-v1-5-img2img', '@cf/runwayml/stable-diffusion-v1-5-img2img', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/runwayml/stable-diffusion-v1-5-img2img', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (29, '@cf/qwen/qwen1.5-14b-chat-awq', '@cf/qwen/qwen1.5-14b-chat-awq', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-02 03:03:52', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (30, 'glm-4v-flash', 'glm-4v-flash', 17, NULL, 1, 0, 30, 0, 'LLM', NULL, NULL, 1, 'sa', 1, '2025-03-02 12:35:46', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (31, 'cogvideox-flash', 'cogvideox-flash', 17, NULL, 1, 1, 30, 0, 'VIDEO', NULL, NULL, 0, 'sa', 1, '2025-03-04 14:35:25', '2025-03-04 15:16:15', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (32, 'hunyuan-lite', 'hunyuan-lite', 19, NULL, 1, 0, 34, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-07 15:57:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (33, 'TextToImageLite', 'TextToImageLite', 19, NULL, 1, 1, 34, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-03-07 16:30:43', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (34, 'HunYuanImage', 'HunYuanImage', 19, NULL, 1, 1, 34, 0, 'VINCENT', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (35, '佐糖', '佐糖', 21, NULL, 1, 0, 38, 0, 'RESOLUTION', NULL, NULL, 0, 'sa', 1, '2025-03-20 13:52:27', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (36, '佐糖', '佐糖', 21, NULL, 1, 0, 38, 0, 'COLORIZATION', NULL, 'v1.0.0', 0, 'sa', 1, '2025-03-21 10:35:11', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_ai_vincent_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_setting`;
CREATE TABLE `sys_ai_vincent_setting`  (
  `sys_ai_vincent_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int(11) NULL DEFAULT NULL COMMENT '所属模块模型ID',
  `sys_ai_vincent_support_async` int(11) NULL DEFAULT NULL COMMENT '是否支持异步任务;0:不支持',
  `sys_ai_vincent_support_ref_image` int(11) NULL DEFAULT NULL COMMENT '支持模板',
  `sys_ai_vincent_support_audio` int(11) NULL DEFAULT NULL COMMENT '支持ai音效',
  `sys_ai_vincent_supported_style` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的风格',
  `sys_ai_vincent_supported_number` int(11) NULL DEFAULT NULL COMMENT '支持输出张数',
  `sys_ai_vincent_supported_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的尺寸',
  `sys_ai_vincent_supported_max_size` int(11) NULL DEFAULT NULL COMMENT '支持的最大尺寸',
  `sys_ai_vincent_supported_fps` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的FPS',
  `sys_ai_vincent_supported_quality` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的视频质量',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图模型参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ai_vincent_setting
-- ----------------------------
INSERT INTO `sys_ai_vincent_setting` VALUES (1, 7, 1, 1, NULL, '<auto>,<photography>,<portrait>,<3d cartoon>,<anime>,<oil painting>,<watercolor>,<sketch>,<chinese painting>,<flat illustration>', 2, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-26 11:07:15', '2025-02-26 13:17:00', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (2, 8, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-28 08:41:43', '2025-02-28 08:49:03', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (3, 9, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-28 10:21:17', '2025-02-28 10:27:56', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (4, 10, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-02-28 10:45:02', '2025-02-28 10:48:08', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (5, 14, 0, 0, NULL, '', 1, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-02-28 15:31:27', '2025-03-18 14:22:12', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (6, 16, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-03-01 17:48:04', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (7, 24, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-01 23:20:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (8, 25, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:04:06', '2025-03-02 02:08:14', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (9, 27, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:51:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (10, 26, 0, NULL, NULL, '<auto>', NULL, '1024*1024', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:52:04', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (11, 28, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:58:13', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (12, 31, 1, 1, 1, '<auto>', 1, '1024*1024', NULL, '', '', 'sa', 1, '2025-03-04 15:14:35', '2025-03-04 15:15:21', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (13, 33, 1, 0, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-07 16:36:31', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (14, 34, 1, 0, NULL, NULL, 1, '1024*1024', NULL, NULL, NULL, 'sa', 1, '2025-03-07 20:21:12', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (15, 35, NULL, NULL, NULL, NULL, NULL, '2,4', 2048, NULL, NULL, 'sa', 1, '2025-03-21 09:46:39', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_ai_vincent_style
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_style`;
CREATE TABLE `sys_ai_vincent_style`  (
  `sys_ai_vincent_style_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int(11) NULL DEFAULT NULL COMMENT '所属模型',
  `sys_ai_vincent_style_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格名称',
  `sys_ai_vincent_style_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格编码',
  `sys_ai_vincent_style_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格地址',
  PRIMARY KEY (`sys_ai_vincent_style_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图风格' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ai_vincent_style
-- ----------------------------
INSERT INTO `sys_ai_vincent_style` VALUES (1, 34, '日漫动画', 'riman', 'https://qcloudimg.tencent-cloud.cn/image/document/1899ad3c41b71cbf67564e5e9a3de27d.png');
INSERT INTO `sys_ai_vincent_style` VALUES (2, 34, '水墨画', 'shuimo', 'https://qcloudimg.tencent-cloud.cn/image/document/567818d24ede579ebb47a90dd8749f39.png');
INSERT INTO `sys_ai_vincent_style` VALUES (3, 34, '莫奈', 'minai', 'https://qcloudimg.tencent-cloud.cn/image/document/60f02a8510ed6d55475608f25d6bdc83.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (4, 34, '扁平插画', 'bianping', 'https://qcloudimg.tencent-cloud.cn/image/document/57dc6b150d8695479203f4887783f1d6.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (5, 34, '像素插画', 'xiangsu', 'https://qcloudimg.tencent-cloud.cn/image/document/74be9d474c6ba9e8f37468d76f40a4ce.png');
INSERT INTO `sys_ai_vincent_style` VALUES (6, 34, '儿童绘本', 'ertonghuiben', 'https://qcloudimg.tencent-cloud.cn/image/document/28dc717b5f32e810bf53dc10f02948d7.png');
INSERT INTO `sys_ai_vincent_style` VALUES (7, 34, '3D 渲染', '3dxuanran', 'https://qcloudimg.tencent-cloud.cn/image/document/19fa3321dacd17cf7689fd47f8629005.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (8, 34, '漫画', 'manhua', 'https://qcloudimg.tencent-cloud.cn/image/document/869e471bf04942eb8e0d36da0a4556bf.png');
INSERT INTO `sys_ai_vincent_style` VALUES (9, 34, '黑白漫画', 'heibaimanhua', 'https://qcloudimg.tencent-cloud.cn/image/document/120f20965200a8bd2f838d1a7fe26d9a.png');
INSERT INTO `sys_ai_vincent_style` VALUES (10, 34, '写实', 'xieshi', 'https://qcloudimg.tencent-cloud.cn/image/document/19c066e3ee8f25069b8834a93f337df3.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (11, 34, '动漫', 'dongman', 'https://qcloudimg.tencent-cloud.cn/image/document/20271b79b5a5aa8ff49ec07bb488edc9.png');
INSERT INTO `sys_ai_vincent_style` VALUES (12, 34, '毕加索', 'bijiasuo', 'https://qcloudimg.tencent-cloud.cn/image/document/dc45004da5225aebb89af48fadc8731c.png');
INSERT INTO `sys_ai_vincent_style` VALUES (13, 34, '赛博朋克', 'saibopengke', 'https://qcloudimg.tencent-cloud.cn/image/document/3e633ceff357c72a1b07db07199669c6.png');
INSERT INTO `sys_ai_vincent_style` VALUES (14, 34, '油画', 'youhua', 'https://qcloudimg.tencent-cloud.cn/image/document/ee58c19eb93298d4175c1d0f88490a94.png');
INSERT INTO `sys_ai_vincent_style` VALUES (15, 34, '马赛克', 'masaike', 'https://qcloudimg.tencent-cloud.cn/image/document/112378e0f4ee1d78c9f306daa8fbff85.png');
INSERT INTO `sys_ai_vincent_style` VALUES (16, 34, '青花瓷', 'qinghuaci', 'https://qcloudimg.tencent-cloud.cn/image/document/d82963705b50cb90ee66bd060177c71b.png');
INSERT INTO `sys_ai_vincent_style` VALUES (17, 34, '新年剪纸画', 'xinnianjianzhi', 'https://qcloudimg.tencent-cloud.cn/image/document/324589129d651b05d23ca336be075cd7.png');
INSERT INTO `sys_ai_vincent_style` VALUES (18, 34, '新年花艺', 'xinnianhuayi', 'https://qcloudimg.tencent-cloud.cn/image/document/92e1acb426ef3268a6b17255d1d6d12b.png');
INSERT INTO `sys_ai_vincent_style` VALUES (19, 7, '默认', '<auto>', 'https://img.alicdn.com/imgextra/i4/O1CN01RzKicz1W0YWzYkWcK_!!6000000002726-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (20, 7, '水彩', '<watercolor>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (21, 7, '摄影', '<photography>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (22, 7, '人像写真', '<portrait>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (23, 7, '油画', '<oil painting>', 'https://img.alicdn.com/imgextra/i4/O1CN01wuUOm31wLmtuwafhG_!!6000000006292-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (24, 7, '中国画', '<chinese painting>', 'https://img.alicdn.com/imgextra/i3/O1CN01tN84xj1oI6nEhfmL1_!!6000000005201-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (25, 7, '扁平插画', '<flat illustration>', 'https://img.alicdn.com/imgextra/i2/O1CN01XMadUe29wy3eouOgg_!!6000000008133-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (26, 7, '二次元', '<anime>', 'https://img.alicdn.com/imgextra/i4/O1CN01RIdXyT1Qoa62pyko6_!!6000000002023-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (27, 7, '素描', '<sketch>', 'https://img.alicdn.com/imgextra/i1/O1CN017OBYOc1YWjaZbcRpu_!!6000000003067-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (28, 7, '3D卡通', '<3d cartoon>', 'https://img.alicdn.com/imgextra/i1/O1CN01W1aZZv1vqBZYXLLrn_!!6000000006223-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (29, 33, '默认', '000', 'https://img.alicdn.com/imgextra/i4/O1CN01RzKicz1W0YWzYkWcK_!!6000000002726-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (30, 33, '水墨画', '101', 'https://img.alicdn.com/imgextra/i4/O1CN01wuUOm31wLmtuwafhG_!!6000000006292-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (31, 33, '概念艺术', '102', 'https://n.sinaimg.cn/sinacn11/720/w1920h1200/20180821/fe95-hhzsnea0733811.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (32, 33, '油画1', '103', 'https://qcloudimg.tencent-cloud.cn/image/document/ee58c19eb93298d4175c1d0f88490a94.png');
INSERT INTO `sys_ai_vincent_style` VALUES (33, 33, '油画2（梵高）', '118', 'https://5b0988e595225.cdn.sohucs.com/images/20180806/db2a2d66d4bc4924b28b8b8541f5bdf3.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (34, 33, '水彩画', '104', 'https://img.lucydraw.com/data/upload/d/file/qita/shuicaihua/2015-09-11/1441930227892565.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (35, 33, '像素画', '105', 'https://img.zcool.cn/community/01958b5c9b0fa9a801214168ab22f7.jpg?x-oss-process=image/auto-orient,1/resize,m_lfit,w_1280,limit_1/sharpen,100');
INSERT INTO `sys_ai_vincent_style` VALUES (36, 33, '厚涂风格', '106', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.wXRGpsVC77SQlqwDAS7W0AHaMr?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (37, 33, '插图', '107', 'https://img.zcool.cn/community/031crpgdb64vh4s9c28me0s3839.png');
INSERT INTO `sys_ai_vincent_style` VALUES (38, 33, '剪纸风格', '108', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.1AAnOtU3pli62YtDhrkSlQHaJ1?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (39, 33, '印象派1（莫奈）', '109', 'https://n.sinaimg.cn/sinacn11/75/w2048h1227/20180402/18be-fyssmme6384393.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (40, 33, '印象派2', '119', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.kj_tF2vSCBW074ywVVkLbQHaF6?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (41, 33, '2.5D', '110', NULL);
INSERT INTO `sys_ai_vincent_style` VALUES (42, 33, '古典肖像画', '111', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.xIpdK_hc45tsFmKOPD5WZAHaKa?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (43, 33, '黑白素描画', '112', 'https://c-ssl.duitang.com/uploads/item/201705/28/20170528024853_rz5te.jpeg');
INSERT INTO `sys_ai_vincent_style` VALUES (44, 33, '赛博朋克', '113', 'https://qcloudimg.tencent-cloud.cn/image/document/3e633ceff357c72a1b07db07199669c6.png');
INSERT INTO `sys_ai_vincent_style` VALUES (45, 33, '科幻风格', '114', 'https://img.cgmol.com/excellentwork/20190514/164066_20190514110224xsedrgxjpo9tpq5.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (46, 33, '暗黑风格', '115', 'https://img.3dmgame.com/UploadFiles/201108/20110802091135913.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (47, 33, '3D', '116', 'https://qcloudimg.tencent-cloud.cn/image/document/19fa3321dacd17cf7689fd47f8629005.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (48, 33, '蒸汽波', '117', 'https://pic2.zhimg.com/v2-21e09b4d3f1d2fb50f3d1d63bbef6bbc_720w.jpg?source=172ae18b');
INSERT INTO `sys_ai_vincent_style` VALUES (49, 33, '日系动漫', '201', 'https://qcloudimg.tencent-cloud.cn/image/document/1899ad3c41b71cbf67564e5e9a3de27d.png');
INSERT INTO `sys_ai_vincent_style` VALUES (50, 33, '怪兽风格', '202', 'https://img.redocn.com/sheji/20230908/shouhuifenggeguaiwuchatu_13096310.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (51, 33, '唯美古风', '203', 'https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1287230597,3924216573&fm=173&app=25&f=JPEG?w=640&h=1139&s=71B432D19110E3C610BDACD8030090B3');
INSERT INTO `sys_ai_vincent_style` VALUES (52, 33, '复古动漫', '204', 'https://c-ssl.duitang.com/uploads/item/202002/04/20200204083412_A5dKK.jpeg');
INSERT INTO `sys_ai_vincent_style` VALUES (53, 33, '游戏卡通手绘', '301', 'https://img.tukuppt.com/png_preview/00/24/81/BfoO7Ynm9x.jpg!/fw/780');
INSERT INTO `sys_ai_vincent_style` VALUES (54, 33, '通用写实风格', '401', 'https://qcloudimg.tencent-cloud.cn/image/document/19c066e3ee8f25069b8834a93f337df3.jpg');

-- ----------------------------
-- Table structure for sys_ai_vincent_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_task`;
CREATE TABLE `sys_ai_vincent_task`  (
  `sys_ai_vincent_task_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_ai_vincent_task_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求ID',
  `sys_ai_vincent_task_status` int(11) NULL DEFAULT NULL COMMENT '状态;0:未完成; 1: 完成; 2:失败',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `sys_ai_module_id` int(11) NULL DEFAULT NULL COMMENT '模型id',
  `sys_ai_vincent_task_condition` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '参数',
  `sys_ai_vincent_task_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片地址',
  `sys_ai_vincent_task_local_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片本地地址',
  `sys_ai_vincent_task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `sys_ai_vincent_task_request_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 258 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ai_vincent_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_vincent_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_template`;
CREATE TABLE `sys_ai_vincent_template`  (
  `sys_ai_vincent_template_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int(11) NULL DEFAULT NULL,
  `sys_ai_vincent_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_ai_vincent_template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `sys_ai_vincent_template_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `sys_ai_vincent_template_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_ai_vincent_template_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_ai_vincent_template_model_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型ID',
  `sys_ai_vincent_template_model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `sys_ai_vincent_template_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `sys_ai_vincent_template_train_model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '训练模型',
  `sys_ai_vincent_template_recommend_prompt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正向关键词',
  `sys_ai_vincent_template_resolutions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分辨率',
  `sys_ai_vincent_template_word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ai_vincent_template
-- ----------------------------
INSERT INTO `sys_ai_vincent_template` VALUES (1, 7, '四格漫画', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (2, 7, '情侣头像', 'qinglvtouxiang', 'https://wanx.alicdn.com/model/qinglvtouxiang.png', '擅长清新可爱的情侣头像', 2, '303-2.1-t2i-plus', 'qinglvtouxiang', 'combining_images', 'wanx2.1-t2i-plus', '针织娃娃相对而望，甜蜜的情侣头像, 一对白色毛毡小鸟站在树枝上，轻声细语, 一对情侣戴动物帽子相互微笑，黄色卡通背景, 两只毛毡小狗在绿草地上欢快地追逐嬉戏, 一对白兔子耳朵缠绕在一起，构成情侣头像', '1440*720', '描述你想要的头像内容与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (3, 7, '图标印花', 'tubiaoyinhua', 'https://wanx.alicdn.com/model/tubiaoyinhua.png', '擅长设计和展示印花效果', 3, '302-2.1-t2i-plus', 'tubiaoyinhua', 'combining_images', 'wanx2.1-t2i-plus', '面包店的菱形logo应用在招牌上, 健身房的logo设计应用于健身T恤, 花店的logo展示在漂亮的橱窗中, 茶馆的logo应用在精美的茶杯上, 服装店的logo别致地印在衣物标签上', '1248*832', '描述你希望设计的Logo印花样式与贴图效果', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (4, 7, '电影分镜', 'dianyingfenjing', 'https://wanx.alicdn.com/model/dianyingfenjing.png', '擅长电影质感画面', 4, '300-2.1-t2i-plus', 'dianyingfenjing', 'combining_images', 'wanx2.1-t2i-plus', '两个人在雪地里一起堆雪人，欢乐无比。, 蒙古草原上的骏马飞驰，场景壮丽。, 圣诞老人点燃烟花，场面充满欢乐。, 火箭从发射升空，壮观地进入太空。, 两人在篝火旁载歌载舞，气氛热烈。', '1072*1424', '描述你希望展现的电影故事主题与画面内容', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (5, 7, '新年请神', 'qingshen', 'https://wanx.alicdn.com/model/qingshen.png', '擅长人像', 5, '304-2.1-t2i-plus', 'qingshen', 'style', 'wanx2.1-t2i-plus', '一个女人，双手托着笔记本电脑, 一个女孩捧着鲜花, 一个女孩拿着一杯珍珠奶茶, 一个女孩拿着红包, 一个女孩捧着榴莲，背景里飘着榴莲', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (9, 7, '四格漫画2', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', 'sa', 1, '2025-03-04 11:08:14', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_bank_bins
-- ----------------------------
DROP TABLE IF EXISTS `sys_bank_bins`;
CREATE TABLE `sys_bank_bins`  (
  `sys_blank_bin_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_blank_bin_bank_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `sys_blank_bin_bank_name_en` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_card_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_card_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_bin` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `sys_blank_bin_n_length` tinyint(4) NULL DEFAULT NULL,
  `sys_blank_bin_bin_length` tinyint(1) NULL DEFAULT NULL,
  `sys_blank_bin_issue_id` int(11) NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_blank_bin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2379 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '银行BIN对照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_bank_bins
-- ----------------------------
INSERT INTO `sys_bank_bins` VALUES (1, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621096', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621098', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (3, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622150', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (4, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622151', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (5, '邮政储蓄银行', 'PSBC', '绿卡专用卡', '绿卡专用卡', '622181', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (6, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622188', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (7, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622199', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (8, '邮政储蓄银行', 'PSBC', '绿卡(银联卡)', '绿卡(银联卡)', '955100', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (9, '邮政储蓄银行', 'PSBC', '绿卡VIP卡', '绿卡VIP卡', '621095', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (10, '邮政储蓄银行', 'PSBC', '银联标准卡', '银联标准卡', '620062', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (11, '邮政储蓄银行', 'PSBC', '中职学生资助卡', '中职学生资助卡', '621285', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (12, '邮政储蓄银行', 'PSBC', 'IC绿卡通VIP卡', 'IC绿卡通VIP卡', '621798', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (13, '邮政储蓄银行', 'PSBC', 'IC绿卡通', 'IC绿卡通', '621799', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (14, '邮政储蓄银行', 'PSBC', 'IC联名卡', 'IC联名卡', '621797', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (15, '邮政储蓄银行', 'PSBC', 'IC预付费卡', 'IC预付费卡', '620529', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (16, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215049', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (17, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215050', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (18, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215051', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (19, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218850', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (20, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218851', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (21, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218849', 19, 8, 1004900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (22, '邮政储蓄银行', 'PSBC', '武警军人保障卡', '武警军人保障卡', '621622', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (23, '邮政储蓄银行', 'PSBC', '福农卡', '福农卡', '621599', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (24, '邮政储蓄银行', 'PSBC', '普通高中学生资助卡', '普通高中学生资助卡', '621674', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (25, '邮政储蓄银行', 'PSBC', '中国旅游卡（普卡）', '中国旅游卡（普卡）', '623218', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (26, '邮政储蓄银行', 'PSBC', '中国旅游卡（金卡）', '中国旅游卡（金卡）', '623219', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (27, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370246', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (28, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370248', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (29, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370249', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (30, '中国工商银行', 'ICBC', '牡丹VISA卡(单位卡)', '牡丹VISA卡(单位卡)', '427010', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (31, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427018', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (32, '中国工商银行', 'ICBC', '牡丹VISA卡(单位卡)', '牡丹VISA卡(单位卡)', '427019', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (33, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427020', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (34, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427029', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (35, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427030', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (36, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427039', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (37, '中国工商银行', 'ICBC', '牡丹运通卡普通卡', '牡丹运通卡普通卡', '370247', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (38, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '438125', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (39, '中国工商银行', 'ICBC', '牡丹VISA白金卡', '牡丹VISA白金卡', '438126', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (40, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451804', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (41, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451810', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (42, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451811', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (43, '中国工商银行', 'ICBC', '牡丹信用卡(银联卡)', '牡丹信用卡(银联卡)', '45806', 16, 5, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (44, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '458071', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (45, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489734', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (46, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489735', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (47, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489736', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (48, '中国工商银行', 'ICBC', '牡丹万事达国际借记卡', '牡丹万事达国际借记卡', '510529', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (49, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427062', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (50, '中国工商银行', 'ICBC', '海航信用卡', '海航信用卡', '524091', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (51, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427064', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (52, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '530970', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (53, '中国工商银行', 'ICBC', '牡丹信用卡(银联卡)', '牡丹信用卡(银联卡)', '53098', 16, 5, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (54, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '530990', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (55, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '558360', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (56, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620200', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (57, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (58, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (59, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620403', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (60, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (61, '中国工商银行', 'ICBC', '牡丹万事达白金卡', '牡丹万事达白金卡', '524047', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (62, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620406', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (63, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620407', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (64, '中国工商银行', 'ICBC', '海航信用卡个人普卡', '海航信用卡个人普卡', '525498', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (65, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620409', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (66, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620410', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (67, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620411', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (68, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620412', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (69, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (70, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620503', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (71, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620405', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (72, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620408', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (73, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620512', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (74, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (75, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (76, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620607', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (77, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620611', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (78, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620612', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (79, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620704', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (80, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620706', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (81, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620707', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (82, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620708', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (83, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620709', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (84, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620710', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (85, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620609', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (86, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620712', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (87, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620713', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (88, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620714', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (89, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620802', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (90, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620711', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (91, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (92, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620905', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (93, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621001', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (94, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620902', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (95, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621103', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (96, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621105', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (97, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621106', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (98, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621107', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (99, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621102', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (100, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621203', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (101, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621204', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (102, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621205', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (103, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621206', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (104, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621207', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (105, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621208', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (106, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621209', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (107, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621210', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (108, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (109, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621303', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (110, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621202', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (111, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621305', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (112, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621306', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (113, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621307', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (114, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621309', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (115, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621311', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (116, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621313', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (117, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621211', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (118, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621315', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (119, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621304', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (120, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (121, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (122, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621405', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (123, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621406', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (124, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621407', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (125, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621408', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (126, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621409', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (127, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621410', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (128, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (129, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621317', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (130, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621511', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (131, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (132, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621603', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (133, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (134, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621605', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (135, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621608', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (136, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621609', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (137, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621610', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (138, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621611', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (139, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621612', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (140, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621613', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (141, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621614', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (142, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621615', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (143, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621616', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (144, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621617', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (145, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621607', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (146, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621606', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (147, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621804', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (148, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621807', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (149, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621813', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (150, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621814', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (151, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621817', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (152, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621901', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (153, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (154, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621905', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (155, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621906', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (156, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621907', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (157, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621908', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (158, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621909', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (159, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621910', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (160, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621911', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (161, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621912', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (162, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621913', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (163, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621915', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (164, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622002', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (165, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621903', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (166, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622004', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (167, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622005', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (168, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622006', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (169, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622007', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (170, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622008', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (171, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622010', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (172, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622011', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (173, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622012', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (174, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621914', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (175, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622015', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (176, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622016', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (177, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622003', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (178, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622018', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (179, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622019', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (180, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622020', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (181, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622102', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (182, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622103', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (183, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622104', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (184, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622105', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (185, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622013', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (186, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622111', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (187, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622114', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (188, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '622200', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (189, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622017', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (190, '中国工商银行', 'ICBC', 'E时代卡', 'E时代卡', '622202', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (191, '中国工商银行', 'ICBC', 'E时代卡', 'E时代卡', '622203', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (192, '中国工商银行', 'ICBC', '理财金卡', '理财金卡', '622208', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (193, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622210', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (194, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622211', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (195, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622212', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (196, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622213', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (197, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622214', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (198, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622110', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (199, '中国工商银行', 'ICBC', '准贷记卡(商普)', '准贷记卡(商普)', '622220', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (200, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622223', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (201, '中国工商银行', 'ICBC', '准贷记卡(商金)', '准贷记卡(商金)', '622225', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (202, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622229', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (203, '中国工商银行', 'ICBC', '贷记卡(个普)', '贷记卡(个普)', '622230', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (204, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622231', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (205, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622232', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (206, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622233', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (207, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622234', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (208, '中国工商银行', 'ICBC', '贷记卡(个金)', '贷记卡(个金)', '622235', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (209, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622237', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (210, '中国工商银行', 'ICBC', '准贷记卡(个金)', '准贷记卡(个金)', '622215', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (211, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622239', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (212, '中国工商银行', 'ICBC', '贷记卡(商普)', '贷记卡(商普)', '622240', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (213, '中国工商银行', 'ICBC', '贷记卡(商金)', '贷记卡(商金)', '622245', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (214, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622224', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (215, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622303', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (216, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622304', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (217, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622305', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (218, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622306', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (219, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622307', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (220, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622308', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (221, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622309', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (222, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622238', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (223, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622314', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (224, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622315', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (225, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622317', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (226, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (227, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (228, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622403', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (229, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (230, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622313', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (231, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622504', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (232, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622505', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (233, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622509', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (234, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622513', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (235, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622517', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (236, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (237, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (238, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622605', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (239, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622606', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (240, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622510', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (241, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622703', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (242, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622715', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (243, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622806', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (244, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622902', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (245, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622903', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (246, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622706', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (247, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623002', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (248, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623006', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (249, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623008', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (250, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623011', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (251, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623012', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (252, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (253, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623015', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (254, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623100', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (255, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623202', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (256, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623301', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (257, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623400', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (258, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623500', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (259, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (260, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623803', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (261, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623901', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (262, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623014', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (263, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624100', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (264, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624200', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (265, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624301', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (266, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (267, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451804', 16, 8, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (268, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451810', 16, 8, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (269, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451811', 16, 8, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (270, '中国工商银行', 'ICBC', '牡丹信用卡', '牡丹信用卡', '6245806', 16, 7, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (271, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62458071', 16, 8, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (272, '中国工商银行', 'ICBC', '牡丹信用卡', '牡丹信用卡', '6253098', 16, 7, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (273, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623700', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (274, '中国工商银行', 'ICBC', '中央预算单位公务卡', '中央预算单位公务卡', '628288', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (275, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624000', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (276, '中国工商银行', 'ICBC', '牡丹灵通卡(银联卡)', '牡丹灵通卡(银联卡)', '9558', 19, 4, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (277, '中国工商银行', 'ICBC', '财政预算单位公务卡', '财政预算单位公务卡', '628286', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (278, '中国工商银行', 'ICBC', '牡丹卡白金卡', '牡丹卡白金卡', '622206', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (279, '中国工商银行', 'ICBC', '牡丹卡普卡', '牡丹卡普卡', '621225', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (280, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '526836', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (281, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '513685', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (282, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '543098', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (283, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '458441', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (284, '中国工商银行', 'ICBC', '银联标准卡', '银联标准卡', '620058', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (285, '中国工商银行', 'ICBC', '中职学生资助卡', '中职学生资助卡', '621281', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (286, '中国工商银行', 'ICBC', '专用信用消费卡', '专用信用消费卡', '622246', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (287, '中国工商银行', 'ICBC', '牡丹社会保障卡', '牡丹社会保障卡', '900000', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (288, '中国工商银行', 'ICBC', '牡丹东航联名卡', '牡丹东航联名卡', '544210', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (289, '中国工商银行', 'ICBC', '牡丹东航联名卡', '牡丹东航联名卡', '548943', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (290, '中国工商银行', 'ICBC', '牡丹运通白金卡', '牡丹运通白金卡', '370267', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (291, '中国工商银行', 'ICBC', '福农灵通卡', '福农灵通卡', '621558', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (292, '中国工商银行', 'ICBC', '福农灵通卡', '福农灵通卡', '621559', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (293, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621722', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (294, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621723', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (295, '中国工商银行', 'ICBC', '中国旅行卡', '中国旅行卡', '620086', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (296, '中国工商银行', 'ICBC', '牡丹卡普卡', '牡丹卡普卡', '621226', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (297, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '402791', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (298, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '427028', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (299, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '427038', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (300, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '548259', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (301, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356879', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (302, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356880', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (303, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356881', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (304, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356882', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (305, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '528856', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (306, '中国工商银行', 'ICBC', '武警军人保障卡', '武警军人保障卡', '621618', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (307, '中国工商银行', 'ICBC', '预付芯片卡', '预付芯片卡', '620516', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (308, '中国工商银行', 'ICBC', '理财金账户金卡', '理财金账户金卡', '621227', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (309, '中国工商银行', 'ICBC', '工银财富卡', '工银财富卡', '621288', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (310, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621721', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (311, '中国工商银行', 'ICBC', '牡丹宁波市民卡', '牡丹宁波市民卡', '900010', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (312, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625330', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (313, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625331', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (314, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625332', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (315, '中国工商银行', 'ICBC', '借记卡', '借记卡', '623062', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (316, '中国工商银行', 'ICBC', '借贷合一卡', '借贷合一卡', '622236', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (317, '中国工商银行', 'ICBC', '牡丹百夫长信用卡', '牡丹百夫长信用卡', '374738', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (318, '中国工商银行', 'ICBC', '牡丹百夫长信用卡', '牡丹百夫长信用卡', '374739', 15, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (319, '中国工商银行', 'ICBC', '普通高中学生资助卡', '普通高中学生资助卡', '621670', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (320, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '524374', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (321, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '550213', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (322, '中国工商银行布鲁塞尔分行', 'ICBC', '贷记卡', '贷记卡', '625929', 16, 6, 1020056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (323, '中国工商银行布鲁塞尔分行', 'ICBC', '借记卡', '借记卡', '621376', 16, 6, 1020056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (324, '中国工商银行布鲁塞尔分行', 'ICBC', '预付卡', '预付卡', '620054', 16, 6, 1020056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (325, '中国工商银行布鲁塞尔分行', 'ICBC', '预付卡', '预付卡', '620142', 16, 6, 1020056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (326, '中国工商银行（巴西）', 'ICBC', '借记卡', '借记卡', '621423', 16, 6, 1020076, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (327, '中国工商银行（巴西）', 'ICBC', '贷记卡', '贷记卡', '625927', 16, 6, 1020076, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (328, '中国工商银行金边分行', 'ICBC', '借记卡', '借记卡', '621428', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (329, '中国工商银行金边分行', 'ICBC', '信用卡', '信用卡', '625939', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (330, '中国工商银行金边分行', 'ICBC', '借记卡', '借记卡', '621434', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (331, '中国工商银行金边分行', 'ICBC', '信用卡', '信用卡', '625987', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (332, '中国工商银行加拿大分行', 'ICBC', '贷记卡', '贷记卡', '625930', 16, 6, 1020124, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (333, '中国工商银行加拿大分行', 'ICBC', '借记卡', '借记卡', '621761', 16, 6, 1020124, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (334, '中国工商银行加拿大分行', 'ICBC', '借记卡', '借记卡', '621749', 16, 6, 1020124, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (335, '中国工商银行加拿大分行', 'ICBC', '预付卡', '预付卡', '620184', 16, 6, 1020124, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (336, '中国工商银行巴黎分行', 'ICBC', '借记卡', '借记卡', '621300', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (337, '中国工商银行巴黎分行', 'ICBC', '借记卡', '借记卡', '621378', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (338, '中国工商银行巴黎分行', 'ICBC', '贷记卡', '贷记卡', '625114', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (339, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '622159', 16, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (340, '中国工商银行法兰克福分行', 'ICBC', '借记卡', '借记卡', '621720', 19, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (341, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625021', 16, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (342, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625022', 16, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (343, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625932', 16, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (344, '中国工商银行法兰克福分行', 'ICBC', '借记卡', '借记卡', '621379', 19, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (345, '中国工商银行法兰克福分行', 'ICBC', '预付卡', '预付卡', '620114', 19, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (346, '中国工商银行法兰克福分行', 'ICBC', '预付卡', '预付卡', '620146', 19, 6, 1020276, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (347, '中国工商银行(亚洲)', 'ICBC', 'ICBC(Asia) Credit', 'ICBC(Asia) Credit', '622889', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (348, '中国工商银行(亚洲)', 'ICBC', 'ICBC Credit Card', 'ICBC Credit Card', '625900', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (349, '中国工商银行(亚洲)', 'ICBC', 'CNYEasylinkCard', 'CNYEasylinkCard', '622944', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (350, '中国工商银行(亚洲)', 'ICBC', 'EliteClubATMCard', 'EliteClubATMCard', '622949', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (351, '中国工商银行(亚洲)', 'ICBC', '港币信用卡', '港币信用卡', '625915', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (352, '中国工商银行(亚洲)', 'ICBC', '港币信用卡', '港币信用卡', '625916', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (353, '中国工商银行(亚洲)', 'ICBC', '工银亚洲预付卡', '工银亚洲预付卡', '620030', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (354, '中国工商银行(亚洲)', 'ICBC', '预付卡', '预付卡', '620050', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (355, '中国工商银行印尼分行', 'ICBC', '印尼盾复合卡', '印尼盾复合卡', '622171', 16, 6, 1020360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (356, '中国工商银行印尼分行', 'ICBC', '借记卡', '借记卡', '621240', 19, 6, 1020360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (357, '中国工商银行印尼分行', 'ICBC', '借记卡', '借记卡', '621724', 19, 6, 1020360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (358, '中国工商银行印尼分行', 'ICBC', '贷记卡', '贷记卡', '625931', 16, 6, 1020360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (359, '中国工商银行（印度尼西亚）', 'ICBC', '借记卡', '借记卡', '621762', 19, 6, 1020360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (360, '中国工商银行米兰分行', 'ICBC', '贷记卡', '贷记卡', '625113', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (361, '中国工商银行米兰分行', 'ICBC', '借记卡', '借记卡', '621371', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (362, '中国工商银行米兰分行', 'ICBC', '预付卡', '预付卡', '620143', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (363, '中国工商银行米兰分行', 'ICBC', '预付卡', '预付卡', '620149', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (364, '工行东京分行', 'ICBC', '工行东京借记卡', '工行东京借记卡', '621730', 16, 6, 1020392, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (365, '中国工商银行阿拉木图子行', 'ICBC', '贷记卡', '贷记卡', '625928', 16, 6, 1020398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (366, '中国工商银行阿拉木图子行', 'ICBC', '借记卡', '借记卡', '621414', 19, 6, 1020398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (367, '中国工商银行阿拉木图子行', 'ICBC', '贷记卡', '贷记卡', '625914', 16, 6, 1020398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (368, '中国工商银行阿拉木图子行', 'ICBC', '借记卡', '借记卡', '621375', 19, 6, 1020398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (369, '中国工商银行阿拉木图子行', 'ICBC', '预付卡', '预付卡', '620187', 19, 6, 1020398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (370, '工行首尔', 'ICBC', '借记卡', '借记卡', '621734', 16, 6, 1020410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (371, '中国工商银行万象分行', 'ICBC', '借记卡', '借记卡', '621433', 16, 6, 1020418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (372, '中国工商银行万象分行', 'ICBC', '贷记卡', '贷记卡', '625986', 16, 6, 1020418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (373, '中国工商银行卢森堡分行', 'ICBC', '借记卡', '借记卡', '621370', 16, 6, 1020442, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (374, '中国工商银行卢森堡分行', 'ICBC', '贷记卡', '贷记卡', '625925', 16, 6, 1020442, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (375, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622926', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (376, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622927', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (377, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622928', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (378, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622929', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (379, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622930', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (380, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622931', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (381, '中国工商银行澳门分行', 'ICBC', '借记卡', '借记卡', '621733', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (382, '中国工商银行澳门分行', 'ICBC', '借记卡', '借记卡', '621732', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (383, '中国工商银行澳门分行', 'ICBC', '预付卡', '预付卡', '620124', 16, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (384, '中国工商银行澳门分行', 'ICBC', '预付卡', '预付卡', '620183', 16, 6, 1020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (385, '工行马来西亚', 'ICBC', '贷记卡', '贷记卡', '625921', 16, 6, 1020458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (386, '工行马来西亚', 'ICBC', '借记卡', '借记卡', '621764', 16, 6, 1020458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (387, '中国工商银行阿姆斯特丹', 'ICBC', '贷记卡', '贷记卡', '625926', 16, 6, 1020528, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (388, '中国工商银行阿姆斯特丹', 'ICBC', '借记卡', '借记卡', '621372', 19, 6, 1020528, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (389, '中国工商银行卡拉奇分行', 'ICBC', '借记卡', '借记卡', '621464', 16, 6, 1020586, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (390, '中国工商银行卡拉奇分行', 'ICBC', '贷记卡', '贷记卡', '625942', 16, 6, 1020586, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (391, '中国工商银行新加坡分行', 'ICBC', '贷记卡', '贷记卡', '622158', 16, 6, 1020702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (392, '中国工商银行新加坡分行', 'ICBC', '贷记卡', '贷记卡', '625917', 16, 6, 1020702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (393, '中国工商银行新加坡分行', 'ICBC', '借记卡', '借记卡', '621765', 16, 6, 1020702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (394, '中国工商银行新加坡分行', 'ICBC', '预付卡', '预付卡', '620094', 16, 6, 1020702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (395, '中国工商银行新加坡分行', 'ICBC', '预付卡', '预付卡', '620186', 16, 6, 1020702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (396, '工行河内', 'ICBC', '贷记卡', '贷记卡', '625922', 16, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (397, '工银河内', 'ICBC', '借记卡', '借记卡', '621369', 19, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (398, '工银河内', 'ICBC', '工银越南盾借记卡', '工银越南盾借记卡', '621763', 19, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (399, '工银河内', 'ICBC', '工银越南盾信用卡', '工银越南盾信用卡', '625934', 16, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (400, '工银河内', 'ICBC', '预付卡', '预付卡', '620046', 19, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (401, '中国工商银行马德里分行', 'ICBC', '借记卡', '借记卡', '621750', 16, 6, 1020724, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (402, '中国工商银行马德里分行', 'ICBC', '贷记卡', '贷记卡', '625933', 16, 6, 1020724, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (403, '中国工商银行马德里分行', 'ICBC', '借记卡', '借记卡', '621377', 16, 6, 1020724, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (404, '中国工商银行马德里分行', 'ICBC', '预付卡', '预付卡', '620148', 16, 6, 1020724, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (405, '中国工商银行马德里分行', 'ICBC', '预付卡', '预付卡', '620185', 16, 6, 1020724, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (406, '工银泰国', 'ICBC', '贷记卡', '贷记卡', '625920', 16, 6, 1020764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (407, '工银泰国', 'ICBC', '借记卡', '借记卡', '621367', 16, 6, 1020764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (408, '中国工商银行伦敦子行', 'ICBC', '贷记卡', '贷记卡', '625924', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (409, '中国工商银行伦敦子行', 'ICBC', '借记卡', '借记卡', '621374', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (410, '中国工商银行伦敦子行', 'ICBC', '工银伦敦借记卡', '工银伦敦借记卡', '621731', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (411, '中国工商银行伦敦子行', 'ICBC', '借记卡', '借记卡', '621781', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (412, '农业银行', 'ABC', '金穗借记卡', '金穗借记卡', '103', 19, 3, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (413, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '403361', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (414, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404117', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (415, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404118', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (416, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404119', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (417, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404120', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (418, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404121', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (419, '农业银行', 'ABC', 'VISA白金卡', 'VISA白金卡', '463758', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (420, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '519412', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (421, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '519413', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (422, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '520082', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (423, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '520083', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (424, '农业银行', 'ABC', '金穗信用卡(银联卡)', '金穗信用卡(银联卡)', '53591', 16, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (425, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '552599', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (426, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '558730', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (427, '农业银行', 'ABC', '金穗信用卡(银联卡)', '金穗信用卡(银联卡)', '49102', 16, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (428, '农业银行', 'ABC', '万事达白金卡', '万事达白金卡', '514027', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (429, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '622836', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (430, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '622837', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (431, '农业银行', 'ABC', '金穗惠农卡', '金穗惠农卡', '622841', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (432, '农业银行', 'ABC', '金穗旅游卡(银联卡)', '金穗旅游卡(银联卡)', '622824', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (433, '农业银行', 'ABC', '复合介质金穗通宝卡', '复合介质金穗通宝卡', '622826', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (434, '农业银行', 'ABC', '金穗公务卡', '金穗公务卡', '628268', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (435, '农业银行', 'ABC', '金穗信用卡', '金穗信用卡', '6349102', 16, 7, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (436, '农业银行', 'ABC', '金穗信用卡', '金穗信用卡', '6353591', 16, 7, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (437, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95595', 19, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (438, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95596', 19, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (439, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95597', 19, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (440, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95598', 19, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (441, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95599', 19, 5, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (442, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622848', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (443, '农业银行', 'ABC', '银联标准卡', '银联标准卡', '620059', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (444, '农业银行', 'ABC', '中职学生资助卡', '中职学生资助卡', '621282', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (445, '农业银行', 'ABC', '退役金卡', '退役金卡', '622828', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (446, '农业银行', 'ABC', '银联IC卡金卡', '银联IC卡金卡', '625996', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (447, '农业银行', 'ABC', '银联IC卡白金卡', '银联IC卡白金卡', '625998', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (448, '农业银行', 'ABC', '金穗社保卡(银联卡)', '金穗社保卡(银联卡)', '622823', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (449, '农业银行', 'ABC', '专用惠农卡', '专用惠农卡', '621336', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (450, '农业银行', 'ABC', '武警军人保障卡', '武警军人保障卡', '621619', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (451, '农业银行', 'ABC', '金穗校园卡(银联卡)', '金穗校园卡(银联卡)', '622821', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (452, '农业银行', 'ABC', '金穗星座卡(银联卡)', '金穗星座卡(银联卡)', '622822', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (453, '农业银行', 'ABC', '金穗青年卡(银联卡)', '金穗青年卡(银联卡)', '622825', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (454, '农业银行', 'ABC', '金穗海通卡', '金穗海通卡', '622827', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (455, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622845', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (456, '农业银行', 'ABC', '金穗通宝钻石卡', '金穗通宝钻石卡', '622849', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (457, '农业银行', 'ABC', '掌尚钱包', '掌尚钱包', '623018', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (458, '农业银行', 'ABC', '银联预算单位公务卡金卡', '银联预算单位公务卡金卡', '625997', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (459, '农业银行', 'ABC', '中国旅游卡', '中国旅游卡', '623206', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (460, '农业银行', 'ABC', '普通高中学生资助卡', '普通高中学生资助卡', '621671', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (461, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622840', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (462, '农业银行', 'ABC', '金穗通宝银卡', '金穗通宝银卡', '622843', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (463, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622844', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (464, '农业银行', 'ABC', '金穗通宝卡', '金穗通宝卡', '622846', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (465, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622847', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (466, '农业银行', 'ABC', '金穗通商卡', '金穗通商卡', '622820', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (467, '农业银行', 'ABC', '金穗通商卡', '金穗通商卡', '622830', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (468, '农业银行', 'ABC', '银联白金卡', '银联白金卡', '622838', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (469, '农业银行', 'ABC', '中国旅游卡', '中国旅游卡', '625336', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (470, '农业银行', 'ABC', 'IC普卡', 'IC普卡', '625826', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (471, '农业银行', 'ABC', 'IC金卡', 'IC金卡', '625827', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (472, '农业银行', 'ABC', '欧元卡', '欧元卡', '544243', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (473, '农业银行', 'ABC', '澳元卡', '澳元卡', '548478', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (474, '农业银行', 'ABC', '银联IC公务卡', '银联IC公务卡', '628269', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (475, '宁波市农业银行', '(NULL)', '市民卡B卡', '市民卡B卡', '620501', 19, 6, 1033320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (476, '中国银行', 'BOC', '联名卡', '联名卡', '621660', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (477, '中国银行', 'BOC', '个人普卡', '个人普卡', '621661', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (478, '中国银行', 'BOC', '个人金卡', '个人金卡', '621662', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (479, '中国银行', 'BOC', '员工普卡', '员工普卡', '621663', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (480, '中国银行', 'BOC', '员工金卡', '员工金卡', '621665', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (481, '中国银行', 'BOC', '理财普卡', '理财普卡', '621667', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (482, '中国银行', 'BOC', '理财金卡', '理财金卡', '621668', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (483, '中国银行', 'BOC', '理财银卡', '理财银卡', '621669', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (484, '中国银行', 'BOC', '理财白金卡', '理财白金卡', '621666', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (485, '中国银行', 'BOC', '中行金融IC卡白金卡', '中行金融IC卡白金卡', '625908', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (486, '中国银行', 'BOC', '中行金融IC卡普卡', '中行金融IC卡普卡', '625910', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (487, '中国银行', 'BOC', '中行金融IC卡金卡', '中行金融IC卡金卡', '625909', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (488, '中国银行', 'BOC', '中银JCB卡金卡', '中银JCB卡金卡', '356833', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (489, '中国银行', 'BOC', '中银JCB卡普卡', '中银JCB卡普卡', '356835', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (490, '中国银行', 'BOC', '员工普卡', '员工普卡', '409665', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (491, '中国银行', 'BOC', '个人普卡', '个人普卡', '409666', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (492, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409668', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (493, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409669', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (494, '中国银行', 'BOC', '个人白金卡', '个人白金卡', '409670', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (495, '中国银行', 'BOC', '中银威士信用卡', '中银威士信用卡', '409671', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (496, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '409672', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (497, '中国银行', 'BOC', '长城电子借记卡', '长城电子借记卡', '456351', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (498, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512315', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (499, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512316', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (500, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512411', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (501, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512412', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (502, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '514957', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (503, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409667', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (504, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518378', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (505, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518379', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (506, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518474', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (507, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518475', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (508, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518476', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (509, '中国银行', 'BOC', '中银奥运信用卡', '中银奥运信用卡', '438088', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (510, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '524865', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (511, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '525745', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (512, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '525746', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (513, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '547766', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (514, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '552742', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (515, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '553131', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (516, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '558868', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (517, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '514958', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (518, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622752', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (519, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622753', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (520, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622755', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (521, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '524864', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (522, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622757', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (523, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622758', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (524, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622759', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (525, '中国银行', 'BOC', '银联单币贷记卡', '银联单币贷记卡', '622760', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (526, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622761', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (527, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622762', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (528, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622763', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (529, '中国银行', 'BOC', '长城电子借记卡', '长城电子借记卡', '601382', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (530, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622756', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (531, '中国银行', 'BOC', '银联标准公务卡', '银联标准公务卡', '628388', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (532, '中国银行', 'BOC', '一卡双账户普卡', '一卡双账户普卡', '621256', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (533, '中国银行', 'BOC', '财互通卡', '财互通卡', '621212', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (534, '中国银行', 'BOC', '电子现金卡', '电子现金卡', '620514', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (535, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622754', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (536, '中国银行', 'BOC', '长城单位信用卡普卡', '长城单位信用卡普卡', '622764', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (537, '中国银行', 'BOC', '中银女性主题信用卡', '中银女性主题信用卡', '518377', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (538, '中国银行', 'BOC', '长城单位信用卡金卡', '长城单位信用卡金卡', '622765', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (539, '中国银行', 'BOC', '白金卡', '白金卡', '622788', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (540, '中国银行', 'BOC', '中职学生资助卡', '中职学生资助卡', '621283', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (541, '中国银行', 'BOC', '银联标准卡', '银联标准卡', '620061', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (542, '中国银行', 'BOC', '金融IC卡', '金融IC卡', '621725', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (543, '中国银行', 'BOC', '长城社会保障卡', '长城社会保障卡', '620040', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (544, '中国银行', 'BOC', '世界卡', '世界卡', '558869', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (545, '中国银行', 'BOC', '社保联名卡', '社保联名卡', '621330', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (546, '中国银行', 'BOC', '社保联名卡', '社保联名卡', '621331', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (547, '中国银行', 'BOC', '医保联名卡', '医保联名卡', '621332', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (548, '中国银行', 'BOC', '医保联名卡', '医保联名卡', '621333', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (549, '中国银行', 'BOC', '公司借记卡', '公司借记卡', '621297', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (550, '中国银行', 'BOC', '银联美运顶级卡', '银联美运顶级卡', '377677', 15, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (551, '中国银行', 'BOC', '长城福农借记卡金卡', '长城福农借记卡金卡', '621568', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (552, '中国银行', 'BOC', '长城福农借记卡普卡', '长城福农借记卡普卡', '621569', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (553, '中国银行', 'BOC', '中行金融IC卡普卡', '中行金融IC卡普卡', '625905', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (554, '中国银行', 'BOC', '中行金融IC卡金卡', '中行金融IC卡金卡', '625906', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (555, '中国银行', 'BOC', '中行金融IC卡白金卡', '中行金融IC卡白金卡', '625907', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (556, '中国银行', 'BOC', '长城银联公务IC卡白金卡', '长城银联公务IC卡白金卡', '628313', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (557, '中国银行', 'BOC', '中银旅游信用卡', '中银旅游信用卡', '625333', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (558, '中国银行', 'BOC', '普通高中学生资助卡', '普通高中学生资助卡', '621672', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (559, '中国银行', 'BOC', '长城银联公务IC卡金卡', '长城银联公务IC卡金卡', '628312', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (560, '中国银行', 'BOC', '中国旅游卡', '中国旅游卡', '623208', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (561, '中国银行', 'BOC', '武警军人保障卡', '武警军人保障卡', '621620', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (562, '中国银行', 'BOC', '社保联名借记IC卡', '社保联名借记IC卡', '621756', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (563, '中国银行', 'BOC', '社保联名借记IC卡', '社保联名借记IC卡', '621757', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (564, '中国银行', 'BOC', '医保联名借记IC卡', '医保联名借记IC卡', '621758', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (565, '中国银行', 'BOC', '医保联名借记IC卡', '医保联名借记IC卡', '621759', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (566, '中国银行', 'BOC', '借记IC个人普卡', '借记IC个人普卡', '621785', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (567, '中国银行', 'BOC', '借记IC个人金卡', '借记IC个人金卡', '621786', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (568, '中国银行', 'BOC', '借记IC个人普卡', '借记IC个人普卡', '621787', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (569, '中国银行', 'BOC', '借记IC白金卡', '借记IC白金卡', '621788', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (570, '中国银行', 'BOC', '借记IC钻石卡', '借记IC钻石卡', '621789', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (571, '中国银行', 'BOC', '借记IC联名卡', '借记IC联名卡', '621790', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (572, '中国银行（澳大利亚）', 'BOC', '预付卡', '预付卡', '620025', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (573, '中国银行（澳大利亚）', 'BOC', '预付卡', '预付卡', '620026', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (574, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621293', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (575, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621294', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (576, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621342', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (577, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621343', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (578, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621364', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (579, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621394', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (580, '中国银行金边分行', 'BOC', '借记卡', '借记卡', '621648', 16, 6, 1040116, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (581, '中国银行雅加达分行', 'BOC', '借记卡', '借记卡', '621248', 16, 6, 1040360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (582, '中银东京分行', 'BOC', '借记卡普卡', '借记卡普卡', '621215', 16, 6, 1040392, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (583, '中国银行首尔分行', 'BOC', '借记卡', '借记卡', '621249', 16, 6, 1040410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (584, '中国银行澳门分行', 'BOC', '银联港币卡', '银联港币卡', '622273', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (585, '中国银行澳门分行', 'BOC', '银联澳门币卡', '银联澳门币卡', '622274', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (586, '中国银行澳门分行', 'BOC', '人民币信用卡', '人民币信用卡', '622750', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (587, '中国银行澳门分行', 'BOC', '人民币信用卡', '人民币信用卡', '622751', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (588, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622771', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (589, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622772', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (590, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622770', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (591, '中国银行澳门分行', 'BOC', '中银银联双币商务卡', '中银银联双币商务卡', '625145', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (592, '中国银行澳门分行', 'BOC', '预付卡', '预付卡', '620531', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (593, '中国银行澳门分行', 'BOC', '澳门中国银行银联预付卡', '澳门中国银行银联预付卡', '620210', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (594, '中国银行澳门分行', 'BOC', '澳门中国银行银联预付卡', '澳门中国银行银联预付卡', '620211', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (595, '中国银行澳门分行', 'BOC', '熊猫卡', '熊猫卡', '622479', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (596, '中国银行澳门分行', 'BOC', '财富卡', '财富卡', '622480', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (597, '中国银行(马来西亚)', 'BOC', '预付卡', '预付卡', '620019', 16, 6, 1040458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (598, '中国银行(马来西亚)', 'BOC', '预付卡', '预付卡', '620035', 16, 6, 1040458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (599, '中国银行马尼拉分行', 'BOC', '双币种借记卡', '双币种借记卡', '621231', 16, 6, 1040608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (600, '中行新加坡分行', 'BOC', 'BOCCUPPLATINUMCARD', 'BOCCUPPLATINUMCARD', '622789', 16, 6, 1040702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (601, '中国银行胡志明分行', 'BOC', '借记卡', '借记卡', '621638', 16, 6, 1040704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (602, '中国银行曼谷分行', 'BOC', '借记卡', '借记卡', '621334', 16, 6, 1040764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (603, '中国银行曼谷分行', 'BOC', '长城信用卡环球通', '长城信用卡环球通', '625140', 16, 6, 1040764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (604, '中国银行曼谷分行', 'BOC', '借记卡', '借记卡', '621395', 16, 6, 1040764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (605, '中国银行', 'BOC', '长城宁波市民卡', '长城宁波市民卡', '620513', 19, 6, 1043320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (606, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5453242', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (607, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5491031', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (608, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5544033', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (609, '建设银行', 'CCB', '龙卡准贷记卡', '龙卡准贷记卡', '622725', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (610, '建设银行', 'CCB', '龙卡准贷记卡金卡', '龙卡准贷记卡金卡', '622728', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (611, '建设银行', 'CCB', '中职学生资助卡', '中职学生资助卡', '621284', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (612, '建设银行', 'CCB', '乐当家银卡VISA', '乐当家银卡VISA', '421349', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (613, '建设银行', 'CCB', '乐当家金卡VISA', '乐当家金卡VISA', '434061', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (614, '建设银行', 'CCB', '乐当家白金卡', '乐当家白金卡', '434062', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (615, '建设银行', 'CCB', '龙卡普通卡VISA', '龙卡普通卡VISA', '436728', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (616, '建设银行', 'CCB', '龙卡储蓄卡', '龙卡储蓄卡', '436742', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (617, '建设银行', 'CCB', 'VISA准贷记卡(银联卡)', 'VISA准贷记卡(银联卡)', '453242', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (618, '建设银行', 'CCB', 'VISA准贷记金卡', 'VISA准贷记金卡', '491031', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (619, '建设银行', 'CCB', '乐当家', '乐当家', '524094', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (620, '建设银行', 'CCB', '乐当家', '乐当家', '526410', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (621, '建设银行', 'CCB', 'MASTER准贷记卡', 'MASTER准贷记卡', '53242', 16, 5, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (622, '建设银行', 'CCB', '乐当家', '乐当家', '53243', 16, 5, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (623, '建设银行', 'CCB', '准贷记金卡', '准贷记金卡', '544033', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (624, '建设银行', 'CCB', '乐当家白金卡', '乐当家白金卡', '552245', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (625, '建设银行', 'CCB', '金融复合IC卡', '金融复合IC卡', '589970', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (626, '建设银行', 'CCB', '银联标准卡', '银联标准卡', '620060', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (627, '建设银行', 'CCB', '银联理财钻石卡', '银联理财钻石卡', '621080', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (628, '建设银行', 'CCB', '金融IC卡', '金融IC卡', '621081', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (629, '建设银行', 'CCB', '建行陆港通龙卡', '建行陆港通龙卡', '621082', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (630, '建设银行', 'CCB', '理财白金卡', '理财白金卡', '621466', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (631, '建设银行', 'CCB', '社保IC卡', '社保IC卡', '621467', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (632, '建设银行', 'CCB', '财富卡私人银行卡', '财富卡私人银行卡', '621488', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (633, '建设银行', 'CCB', '理财金卡', '理财金卡', '621499', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (634, '建设银行', 'CCB', '福农卡', '福农卡', '621598', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (635, '建设银行', 'CCB', '武警军人保障卡', '武警军人保障卡', '621621', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (636, '建设银行', 'CCB', '龙卡通', '龙卡通', '621700', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (637, '建设银行', 'CCB', '银联储蓄卡', '银联储蓄卡', '622280', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (638, '建设银行', 'CCB', '龙卡储蓄卡(银联卡)', '龙卡储蓄卡(银联卡)', '622700', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (639, '建设银行', 'CCB', '准贷记卡', '准贷记卡', '622707', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (640, '建设银行', 'CCB', '理财白金卡', '理财白金卡', '622966', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (641, '建设银行', 'CCB', '理财金卡', '理财金卡', '622988', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (642, '建设银行', 'CCB', '准贷记卡普卡', '准贷记卡普卡', '625955', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (643, '建设银行', 'CCB', '准贷记卡金卡', '准贷记卡金卡', '625956', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (644, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '553242', 18, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (645, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '625362', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (646, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '625363', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (647, '建设银行', 'CCB', '龙卡IC公务卡', '龙卡IC公务卡', '628316', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (648, '建设银行', 'CCB', '龙卡IC公务卡', '龙卡IC公务卡', '628317', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (649, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '623211', 19, 6, 1050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (650, '建设银行', 'CCB', '龙卡储蓄卡', '龙卡储蓄卡', '436742193', 19, 9, 1053930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (651, '建设银行', 'CCB', '银联储蓄卡', '银联储蓄卡', '622280193', 19, 9, 1053930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (652, '建设银行', 'CCB', '龙卡JCB金卡', '龙卡JCB金卡', '356896', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (653, '建设银行', 'CCB', '龙卡JCB白金卡', '龙卡JCB白金卡', '356899', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (654, '建设银行', 'CCB', '龙卡JCB普卡', '龙卡JCB普卡', '356895', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (655, '建设银行', 'CCB', '龙卡贷记卡公司卡', '龙卡贷记卡公司卡', '436718', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (656, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '436738', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (657, '建设银行', 'CCB', '龙卡国际普通卡VISA', '龙卡国际普通卡VISA', '436745', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (658, '建设银行', 'CCB', '龙卡国际金卡VISA', '龙卡国际金卡VISA', '436748', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (659, '建设银行', 'CCB', 'VISA白金信用卡', 'VISA白金信用卡', '489592', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (660, '建设银行', 'CCB', '龙卡国际白金卡', '龙卡国际白金卡', '531693', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (661, '建设银行', 'CCB', '龙卡国际普通卡MASTER', '龙卡国际普通卡MASTER', '532450', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (662, '建设银行', 'CCB', '龙卡国际金卡MASTER', '龙卡国际金卡MASTER', '532458', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (663, '建设银行', 'CCB', '龙卡万事达金卡', '龙卡万事达金卡', '544887', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (664, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '552801', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (665, '建设银行', 'CCB', '龙卡万事达白金卡', '龙卡万事达白金卡', '557080', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (666, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '558895', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (667, '建设银行', 'CCB', '龙卡万事达信用卡', '龙卡万事达信用卡', '559051', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (668, '建设银行', 'CCB', '龙卡人民币信用卡', '龙卡人民币信用卡', '622166', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (669, '建设银行', 'CCB', '龙卡人民币信用金卡', '龙卡人民币信用金卡', '622168', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (670, '建设银行', 'CCB', '龙卡人民币白金卡', '龙卡人民币白金卡', '622708', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (671, '建设银行', 'CCB', '龙卡IC信用卡普卡', '龙卡IC信用卡普卡', '625964', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (672, '建设银行', 'CCB', '龙卡IC信用卡金卡', '龙卡IC信用卡金卡', '625965', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (673, '建设银行', 'CCB', '龙卡IC信用卡白金卡', '龙卡IC信用卡白金卡', '625966', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (674, '建设银行', 'CCB', '龙卡银联公务卡普卡', '龙卡银联公务卡普卡', '628266', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (675, '建设银行', 'CCB', '龙卡银联公务卡金卡', '龙卡银联公务卡金卡', '628366', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (676, '交通银行', 'COMM', '交行预付卡', '交行预付卡', '620021', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (677, '交通银行', 'COMM', '世博预付IC卡', '世博预付IC卡', '620521', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (678, '交通银行', 'COMM', '太平洋互连卡', '太平洋互连卡', '0', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (679, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '0', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (680, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '0', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (681, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '0', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (682, '交通银行', 'COMM', '太平洋互连卡(银联卡)', '太平洋互连卡(银联卡)', '405512', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (683, '交通银行', 'COMM', '太平洋白金信用卡', '太平洋白金信用卡', '434910', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (684, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '458123', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (685, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '458124', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (686, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '49104', 16, 5, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (687, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '520169', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (688, '交通银行', 'COMM', '太平洋白金信用卡', '太平洋白金信用卡', '522964', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (689, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '53783', 16, 5, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (690, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '552853', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (691, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '601428', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (692, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622250', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (693, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622251', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (694, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '521899', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (695, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622254', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (696, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622255', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (697, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622256', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (698, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622257', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (699, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622258', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (700, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622259', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (701, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622253', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (702, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622261', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (703, '交通银行', 'COMM', '太平洋MORE卡', '太平洋MORE卡', '622284', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (704, '交通银行', 'COMM', '白金卡', '白金卡', '622656', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (705, '交通银行', 'COMM', '交通银行公务卡普卡', '交通银行公务卡普卡', '628216', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (706, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622252', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (707, '交通银行', 'COMM', '太平洋互连卡', '太平洋互连卡', '66405512', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (708, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '6649104', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (709, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622260', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (710, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '66601428', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (711, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955590', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (712, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955591', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (713, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955592', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (714, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955593', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (715, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '6653783', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (716, '交通银行', 'COMM', '交通银行公务卡金卡', '交通银行公务卡金卡', '628218', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (717, '交通银行', 'COMM', '交银IC卡', '交银IC卡', '622262', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (718, '交通银行香港分行', 'COMM', '银联人民币卡', '银联人民币卡', '621002', 19, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (719, '交通银行香港分行', 'COMM', '交通银行港币借记卡', '交通银行港币借记卡', '621069', 19, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (720, '交通银行香港分行', 'COMM', '港币礼物卡', '港币礼物卡', '620013', 16, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (721, '交通银行香港分行', 'COMM', '双币种信用卡', '双币种信用卡', '625028', 16, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (722, '交通银行香港分行', 'COMM', '双币种信用卡', '双币种信用卡', '625029', 16, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (723, '交通银行香港分行', 'COMM', '双币卡', '双币卡', '621436', 19, 6, 3010344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (724, '交通银行澳门分行', 'COMM', '银联借记卡', '银联借记卡', '621335', 19, 6, 3010446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (725, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433670', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (726, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433680', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (727, '中信银行', 'CITIC', '中信国际借记卡', '中信国际借记卡', '442729', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (728, '中信银行', 'CITIC', '中信国际借记卡', '中信国际借记卡', '442730', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (729, '中信银行', 'CITIC', '中国旅行卡', '中国旅行卡', '620082', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (730, '中信银行', 'CITIC', '中信借记卡(银联卡)', '中信借记卡(银联卡)', '622690', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (731, '中信银行', 'CITIC', '中信借记卡(银联卡)', '中信借记卡(银联卡)', '622691', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (732, '中信银行', 'CITIC', '中信贵宾卡(银联卡)', '中信贵宾卡(银联卡)', '622692', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (733, '中信银行', 'CITIC', '中信理财宝金卡', '中信理财宝金卡', '622696', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (734, '中信银行', 'CITIC', '中信理财宝白金卡', '中信理财宝白金卡', '622698', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (735, '中信银行', 'CITIC', '中信钻石卡', '中信钻石卡', '622998', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (736, '中信银行', 'CITIC', '中信钻石卡', '中信钻石卡', '622999', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (737, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433671', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (738, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968807', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (739, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968808', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (740, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968809', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (741, '中信银行', 'CITIC', '借记卡', '借记卡', '621771', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (742, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621767', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (743, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621768', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (744, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621770', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (745, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621772', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (746, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621773', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (747, '中国光大银行', 'CEB', '阳光卡', '阳光卡', '303', 16, 3, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (748, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '356837', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (749, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '356838', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (750, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '486497', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (751, '中国光大银行', 'CEB', '阳光旅行卡', '阳光旅行卡', '620085', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (752, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622660', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (753, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622662', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (754, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622663', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (755, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622664', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (756, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622665', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (757, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622666', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (758, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622667', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (759, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622669', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (760, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622670', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (761, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622671', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (762, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622672', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (763, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622668', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (764, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622661', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (765, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622674', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (766, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '90030', 16, 5, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (767, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622673', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (768, '中国光大银行', 'CEB', '借记卡普卡', '借记卡普卡', '620518', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (769, '中国光大银行', 'CEB', '社会保障IC卡', '社会保障IC卡', '621489', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (770, '中国光大银行', 'CEB', 'IC借记卡普卡', 'IC借记卡普卡', '621492', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (771, '中国光大银行', 'CEB', '手机支付卡', '手机支付卡', '620535', 19, 6, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (772, '华夏银行', 'HXBANK', '华夏卡(银联卡)', '华夏卡(银联卡)', '622630', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (773, '华夏银行', 'HXBANK', '华夏至尊金卡(银联卡)', '华夏至尊金卡(银联卡)', '622631', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (774, '华夏银行', 'HXBANK', '华夏丽人卡(银联卡)', '华夏丽人卡(银联卡)', '622632', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (775, '华夏银行', 'HXBANK', '华夏万通卡', '华夏万通卡', '622633', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (776, '华夏银行', 'HXBANK', '华夏卡(银联卡)', '华夏卡(银联卡)', '999999', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (777, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '621222', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (778, '华夏银行', 'HXBANK', '华夏普卡', '华夏普卡', '623020', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (779, '华夏银行', 'HXBANK', '华夏金卡', '华夏金卡', '623021', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (780, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '623022', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (781, '华夏银行', 'HXBANK', '华夏钻石卡', '华夏钻石卡', '623023', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (782, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622615', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (783, '中国民生银行', 'CMBC', '民生银联借记卡－金卡', '民生银联借记卡－金卡', '622616', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (784, '中国民生银行', 'CMBC', '钻石卡', '钻石卡', '622618', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (785, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622622', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (786, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622617', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (787, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622619', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (788, '中国民生银行', 'CMBC', '民生借记卡', '民生借记卡', '415599', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (789, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '421393', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (790, '中国民生银行', 'CMBC', '民生国际卡(银卡)', '民生国际卡(银卡)', '421865', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (791, '中国民生银行', 'CMBC', '民生国际卡(欧元卡)', '民生国际卡(欧元卡)', '427570', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (792, '中国民生银行', 'CMBC', '民生国际卡(澳元卡)', '民生国际卡(澳元卡)', '427571', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (793, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '472067', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (794, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '472068', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (795, '中国民生银行', 'CMBC', '薪资理财卡', '薪资理财卡', '622620', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (796, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545392', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (797, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545393', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (798, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545431', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (799, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545447', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (800, '中国民生银行', 'CMBC', '民生JCB信用卡', '民生JCB信用卡', '356859', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (801, '中国民生银行', 'CMBC', '民生JCB金卡', '民生JCB金卡', '356857', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (802, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '407405', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (803, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421869', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (804, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421870', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (805, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421871', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (806, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '512466', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (807, '中国民生银行', 'CMBC', '民生JCB普卡', '民生JCB普卡', '356856', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (808, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '528948', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (809, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '552288', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (810, '中国民生银行', 'CMBC', '民生信用卡(银联卡)', '民生信用卡(银联卡)', '622600', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (811, '中国民生银行', 'CMBC', '民生信用卡(银联卡)', '民生信用卡(银联卡)', '622601', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (812, '中国民生银行', 'CMBC', '民生银联白金信用卡', '民生银联白金信用卡', '622602', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (813, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '517636', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (814, '中国民生银行', 'CMBC', '民生银联个人白金卡', '民生银联个人白金卡', '622621', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (815, '中国民生银行', 'CMBC', '公务卡金卡', '公务卡金卡', '628258', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (816, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '556610', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (817, '中国民生银行', 'CMBC', '民生银联商务信用卡', '民生银联商务信用卡', '622603', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (818, '中国民生银行', 'CMBC', '民VISA无限卡', '民VISA无限卡', '464580', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (819, '中国民生银行', 'CMBC', '民生VISA商务白金卡', '民生VISA商务白金卡', '464581', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (820, '中国民生银行', 'CMBC', '民生万事达钛金卡', '民生万事达钛金卡', '523952', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (821, '中国民生银行', 'CMBC', '民生万事达世界卡', '民生万事达世界卡', '545217', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (822, '中国民生银行', 'CMBC', '民生万事达白金公务卡', '民生万事达白金公务卡', '553161', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (823, '中国民生银行', 'CMBC', '民生JCB白金卡', '民生JCB白金卡', '356858', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (824, '中国民生银行', 'CMBC', '银联标准金卡', '银联标准金卡', '622623', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (825, '中国民生银行', 'CMBC', '银联芯片金卡', '银联芯片金卡', '625912', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (826, '中国民生银行', 'CMBC', '银联芯片白金卡', '银联芯片白金卡', '625913', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (827, '中国民生银行', 'CMBC', '银联芯片普卡', '银联芯片普卡', '625911', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (828, '中国民生银行', 'CMBC', '民生运通双币标准信用卡白金卡', '民生运通双币标准信用卡白金卡', '377155', 15, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (829, '中国民生银行', 'CMBC', '民生运通双币信用卡普卡', '民生运通双币信用卡普卡', '377152', 15, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (830, '中国民生银行', 'CMBC', '民生运通双币信用卡金卡', '民生运通双币信用卡金卡', '377153', 15, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (831, '中国民生银行', 'CMBC', '民生运通双币信用卡钻石卡', '民生运通双币信用卡钻石卡', '377158', 15, 6, 3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (832, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '406365', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (833, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '406366', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (834, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '428911', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (835, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436768', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (836, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436769', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (837, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436770', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (838, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '487013', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (839, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491032', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (840, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491033', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (841, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491034', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (842, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491035', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (843, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491036', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (844, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491037', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (845, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491038', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (846, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436771', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (847, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '518364', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (848, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '520152', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (849, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '520382', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (850, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '541709', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (851, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '541710', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (852, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '548844', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (853, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '552794', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (854, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '493427', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (855, '广东发展银行', 'GDB', '广发银联标准金卡', '广发银联标准金卡', '622555', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (856, '广东发展银行', 'GDB', '广发银联标准普卡', '广发银联标准普卡', '622556', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (857, '广东发展银行', 'GDB', '广发银联标准真情金卡', '广发银联标准真情金卡', '622557', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (858, '广东发展银行', 'GDB', '广发银联标准白金卡', '广发银联标准白金卡', '622558', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (859, '广东发展银行', 'GDB', '广发银联标准真情普卡', '广发银联标准真情普卡', '622559', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (860, '广东发展银行', 'GDB', '广发真情白金卡', '广发真情白金卡', '622560', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (861, '广东发展银行', 'GDB', '广发理财通卡', '广发理财通卡', '622568', 19, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (862, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '528931', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (863, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '685800', 19, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (864, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858000', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (865, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858001', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (866, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858009', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (867, '广东发展银行', 'GDB', '广发理财通(银联卡)', '广发理财通(银联卡)', '9111', 19, 4, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (868, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '558894', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (869, '广东发展银行', 'GDB', '银联标准金卡', '银联标准金卡', '625072', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (870, '广东发展银行', 'GDB', '银联标准普卡', '银联标准普卡', '625071', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (871, '广东发展银行', 'GDB', '银联公务金卡', '银联公务金卡', '628260', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (872, '广东发展银行', 'GDB', '银联公务普卡', '银联公务普卡', '628259', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (873, '广东发展银行', 'GDB', '理财通卡', '理财通卡', '621462', 19, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (874, '广东发展银行', 'GDB', '银联真情普卡', '银联真情普卡', '625805', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (875, '广东发展银行', 'GDB', '银联真情金卡', '银联真情金卡', '625806', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (876, '广东发展银行', 'GDB', '银联真情白金卡', '银联真情白金卡', '625807', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (877, '广东发展银行', 'GDB', '银联标准普卡', '银联标准普卡', '625808', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (878, '广东发展银行', 'GDB', '银联标准金卡', '银联标准金卡', '625809', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (879, '广东发展银行', 'GDB', '银联标准白金卡', '银联标准白金卡', '625810', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (880, '深圳发展银行', '(NULL)', '沃尔玛百分卡', '沃尔玛百分卡', '435744', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (881, '深圳发展银行', '(NULL)', '沃尔玛百分卡', '沃尔玛百分卡', '435745', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (882, '深圳发展银行', '(NULL)', 'VISA白金卡', 'VISA白金卡', '483536', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (883, '深圳发展银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622525', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (884, '深圳发展银行', '(NULL)', '人民币信用卡普卡', '人民币信用卡普卡', '622526', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (885, '深圳发展银行', '(NULL)', '发展信用卡(银联卡)', '发展信用卡(银联卡)', '998801', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (886, '深圳发展银行', '(NULL)', '发展信用卡(银联卡)', '发展信用卡(银联卡)', '998802', 16, 6, 3070000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (887, '深圳发展银行', '(NULL)', '发展借记卡', '发展借记卡', '412963', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (888, '深圳发展银行', '(NULL)', '国际借记卡', '国际借记卡', '415752', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (889, '深圳发展银行', '(NULL)', '国际借记卡', '国际借记卡', '415753', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (890, '深圳发展银行', '(NULL)', '聚财卡金卡', '聚财卡金卡', '622535', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (891, '深圳发展银行', '(NULL)', '聚财卡VIP金卡', '聚财卡VIP金卡', '622536', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (892, '深圳发展银行', '(NULL)', '发展卡(银联卡)', '发展卡(银联卡)', '622538', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (893, '深圳发展银行', '(NULL)', '聚财卡白金卡和钻石卡', '聚财卡白金卡和钻石卡', '622539', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (894, '深圳发展银行', '(NULL)', '发展借记卡(银联卡)', '发展借记卡(银联卡)', '998800', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (895, '深圳发展银行', '(NULL)', '发展借记卡', '发展借记卡', '412962', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (896, '深圳发展银行', '(NULL)', '聚财卡钻石卡', '聚财卡钻石卡', '622983', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (897, '深圳发展银行', '(NULL)', '公益预付卡', '公益预付卡', '620010', 16, 6, 3070010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (898, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356885', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (899, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356886', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (900, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356887', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (901, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356888', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (902, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356890', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (903, '招商银行', 'CMB', '两地一卡通', '两地一卡通', '402658', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (904, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '410062', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (905, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '439188', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (906, '招商银行', 'CMB', 'VISA商务信用卡', 'VISA商务信用卡', '439227', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (907, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '468203', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (908, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '479228', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (909, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '479229', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (910, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '512425', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (911, '招商银行', 'CMB', '世纪金花联名信用卡', '世纪金花联名信用卡', '521302', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (912, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '524011', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (913, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356889', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (914, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545620', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (915, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545621', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (916, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545947', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (917, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545948', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (918, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '552534', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (919, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '552587', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (920, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622575', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (921, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622576', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (922, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622577', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (923, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622578', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (924, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622579', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (925, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '622580', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (926, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545619', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (927, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622581', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (928, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622582', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (929, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '622588', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (930, '招商银行', 'CMB', '公司卡(银联卡)', '公司卡(银联卡)', '622598', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (931, '招商银行', 'CMB', '金卡', '金卡', '622609', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (932, '招商银行', 'CMB', '招行一卡通', '招行一卡通', '690755', 15, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (933, '招商银行', 'CMB', '招行一卡通', '招行一卡通', '690755', 18, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (934, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '95555', 16, 5, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (935, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545623', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (936, '招商银行', 'CMB', '金葵花卡', '金葵花卡', '621286', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (937, '招商银行', 'CMB', '电子现金卡', '电子现金卡', '620520', 19, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (938, '招商银行', 'CMB', '银联IC普卡', '银联IC普卡', '621483', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (939, '招商银行', 'CMB', '银联IC金卡', '银联IC金卡', '621485', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (940, '招商银行', 'CMB', '银联金葵花IC卡', '银联金葵花IC卡', '621486', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (941, '招商银行', 'CMB', 'IC公务卡', 'IC公务卡', '628290', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (942, '招商银行', 'CMB', '美国运通绿卡', '美国运通绿卡', '370285', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (943, '招商银行', 'CMB', '美国运通金卡', '美国运通金卡', '370286', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (944, '招商银行', 'CMB', '美国运通商务绿卡', '美国运通商务绿卡', '370287', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (945, '招商银行', 'CMB', '美国运通商务金卡', '美国运通商务金卡', '370289', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (946, '招商银行', 'CMB', 'VISA信用卡', 'VISA信用卡', '439225', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (947, '招商银行', 'CMB', 'MASTER信用卡', 'MASTER信用卡', '518710', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (948, '招商银行', 'CMB', 'MASTER信用金卡', 'MASTER信用金卡', '518718', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (949, '招商银行', 'CMB', '银联标准公务卡(金卡)', '银联标准公务卡(金卡)', '628362', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (950, '招商银行', 'CMB', 'VISA信用卡', 'VISA信用卡', '439226', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (951, '招商银行', 'CMB', '银联标准财政公务卡', '银联标准财政公务卡', '628262', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (952, '招商银行', 'CMB', '芯片IC信用卡', '芯片IC信用卡', '625802', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (953, '招商银行', 'CMB', '芯片IC信用卡', '芯片IC信用卡', '625803', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (954, '招商银行', 'CMB', '香港一卡通', '香港一卡通', '621299', 16, 6, 3080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (955, '兴业银行', 'CIB', '兴业智能卡', '兴业智能卡', '438589', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (956, '兴业银行', 'CIB', '兴业卡', '兴业卡', '90592', 16, 5, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (957, '兴业银行', 'CIB', '兴业卡(银联卡)', '兴业卡(银联卡)', '966666', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (958, '兴业银行', 'CIB', '兴业卡(银联标准卡)', '兴业卡(银联标准卡)', '622909', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (959, '兴业银行', 'CIB', '兴业智能卡(银联卡)', '兴业智能卡(银联卡)', '438588', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (960, '兴业银行', 'CIB', '兴业自然人生理财卡', '兴业自然人生理财卡', '622908', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (961, '兴业银行', 'CIB', 'visa标准双币个人普卡', 'visa标准双币个人普卡', '461982', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (962, '兴业银行', 'CIB', 'VISA商务普卡', 'VISA商务普卡', '486493', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (963, '兴业银行', 'CIB', 'VISA商务金卡', 'VISA商务金卡', '486494', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (964, '兴业银行', 'CIB', 'VISA运动白金信用卡', 'VISA运动白金信用卡', '486861', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (965, '兴业银行', 'CIB', '万事达信用卡(银联卡)', '万事达信用卡(银联卡)', '523036', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (966, '兴业银行', 'CIB', 'VISA信用卡(银联卡)', 'VISA信用卡(银联卡)', '451289', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (967, '兴业银行', 'CIB', '加菲猫信用卡', '加菲猫信用卡', '527414', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (968, '兴业银行', 'CIB', '个人白金卡', '个人白金卡', '528057', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (969, '兴业银行', 'CIB', '银联信用卡(银联卡)', '银联信用卡(银联卡)', '622901', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (970, '兴业银行', 'CIB', '银联信用卡(银联卡)', '银联信用卡(银联卡)', '622902', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (971, '兴业银行', 'CIB', '银联白金信用卡', '银联白金信用卡', '622922', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (972, '兴业银行', 'CIB', '银联标准公务卡', '银联标准公务卡', '628212', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (973, '兴业银行', 'CIB', 'VISA信用卡(银联卡)', 'VISA信用卡(银联卡)', '451290', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (974, '兴业银行', 'CIB', '万事达信用卡(银联卡)', '万事达信用卡(银联卡)', '524070', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (975, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625084', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (976, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625085', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (977, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625086', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (978, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625087', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (979, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '548738', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (980, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '549633', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (981, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '552398', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (982, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625082', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (983, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625083', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (984, '兴业银行', 'CIB', '兴业芯片普卡', '兴业芯片普卡', '625960', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (985, '兴业银行', 'CIB', '兴业芯片金卡', '兴业芯片金卡', '625961', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (986, '兴业银行', 'CIB', '兴业芯片白金卡', '兴业芯片白金卡', '625962', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (987, '兴业银行', 'CIB', '兴业芯片钻石卡', '兴业芯片钻石卡', '625963', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (988, '浦东发展银行', 'SPDB', '浦发JCB金卡', '浦发JCB金卡', '356851', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (989, '浦东发展银行', 'SPDB', '浦发JCB白金卡', '浦发JCB白金卡', '356852', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (990, '浦东发展银行', 'SPDB', '信用卡VISA普通', '信用卡VISA普通', '404738', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (991, '浦东发展银行', 'SPDB', '信用卡VISA金卡', '信用卡VISA金卡', '404739', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (992, '浦东发展银行', 'SPDB', '浦发银行VISA年青卡', '浦发银行VISA年青卡', '456418', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (993, '浦东发展银行', 'SPDB', 'VISA白金信用卡', 'VISA白金信用卡', '498451', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (994, '浦东发展银行', 'SPDB', '浦发万事达白金卡', '浦发万事达白金卡', '515672', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (995, '浦东发展银行', 'SPDB', '浦发JCB普卡', '浦发JCB普卡', '356850', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (996, '浦东发展银行', 'SPDB', '浦发万事达金卡', '浦发万事达金卡', '517650', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (997, '浦东发展银行', 'SPDB', '浦发万事达普卡', '浦发万事达普卡', '525998', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (998, '浦东发展银行', 'SPDB', '浦发单币卡', '浦发单币卡', '622177', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (999, '浦东发展银行', 'SPDB', '浦发银联单币麦兜普卡', '浦发银联单币麦兜普卡', '622277', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1000, '浦东发展银行', 'SPDB', '东方轻松理财卡', '东方轻松理财卡', '622516', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1001, '浦东发展银行', 'SPDB', '东方-轻松理财卡普卡', '东方-轻松理财卡普卡', '622517', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1002, '浦东发展银行', 'SPDB', '东方轻松理财卡', '东方轻松理财卡', '622518', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1003, '浦东发展银行', 'SPDB', '东方轻松理财智业金卡', '东方轻松理财智业金卡', '622520', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1004, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622521', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1005, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622522', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1006, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622523', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1007, '浦东发展银行', 'SPDB', '公务卡金卡', '公务卡金卡', '628222', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1008, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84301', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1009, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84336', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1010, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '622500', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1011, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84373', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1012, '浦东发展银行', 'SPDB', '公务卡普卡', '公务卡普卡', '628221', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1013, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84385', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1014, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84390', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1015, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87000', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1016, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87010', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1017, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87030', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1018, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87040', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1019, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84380', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1020, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '984301', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1021, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '984303', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1022, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84361', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1023, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87050', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1024, '浦东发展银行', 'SPDB', '浦发单币卡', '浦发单币卡', '622176', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1025, '浦东发展银行', 'SPDB', '浦发联名信用卡', '浦发联名信用卡', '622276', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1026, '浦东发展银行', 'SPDB', '浦发银联白金卡', '浦发银联白金卡', '622228', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1027, '浦东发展银行', 'SPDB', '轻松理财普卡', '轻松理财普卡', '621352', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1028, '浦东发展银行', 'SPDB', '轻松理财金卡（复合卡）', '轻松理财金卡（复合卡）', '621793', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1029, '浦东发展银行', 'SPDB', '轻松理财白金卡（复合卡）', '轻松理财白金卡（复合卡）', '621795', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1030, '浦东发展银行', 'SPDB', '轻松理财钻石卡（复合卡）', '轻松理财钻石卡（复合卡）', '621796', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1031, '浦东发展银行', 'SPDB', '移动联名卡', '移动联名卡', '621351', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1032, '浦东发展银行', 'SPDB', '轻松理财消贷易卡', '轻松理财消贷易卡', '621390', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1033, '浦东发展银行', 'SPDB', '轻松理财普卡（复合卡）', '轻松理财普卡（复合卡）', '621792', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1034, '浦东发展银行', 'SPDB', '贷记卡', '贷记卡', '625957', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1035, '浦东发展银行', 'SPDB', '贷记卡', '贷记卡', '625958', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1036, '浦东发展银行', 'SPDB', '东方借记卡（复合卡）', '东方借记卡（复合卡）', '621791', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1037, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84342', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1038, '浦东发展银行', 'SPDB', '电子现金卡（IC卡）', '电子现金卡（IC卡）', '620530', 19, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1039, '浦东发展银行', 'SPDB', '移动浦发联名卡', '移动浦发联名卡', '625993', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1040, '浦东发展银行', 'SPDB', '东方-标准准贷记卡', '东方-标准准贷记卡', '622519', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1041, '浦东发展银行', 'SPDB', '中国移动浦发银行联名手机卡', '中国移动浦发银行联名手机卡', '625831', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1042, '恒丰银行', 'EGBANK', '九州IC卡', '九州IC卡', '623078', 19, 6, 3110000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1043, '恒丰银行', 'EGBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '622384', 17, 6, 3114560, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1044, '恒丰银行', 'EGBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '940034', 17, 6, 3114560, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1045, '天津市商业银行', '(NULL)', '津卡', '津卡', '6091201', 18, 7, 3131100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1046, '天津市商业银行', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940015', 18, 6, 3131100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1047, '齐鲁银行', 'QLBANK', '齐鲁卡(银联卡)', '齐鲁卡(银联卡)', '940008', 19, 6, 3134500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1048, '齐鲁银行', 'QLBANK', '齐鲁卡(银联卡)', '齐鲁卡(银联卡)', '622379', 19, 6, 3134510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1049, '烟台商业银行', '(NULL)', '金通卡', '金通卡', '622886', 16, 6, 3134560, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1050, '潍坊银行', 'BANKWF', '鸢都卡(银联卡)', '鸢都卡(银联卡)', '622391', 16, 6, 3134580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1051, '潍坊银行', 'BANKWF', '鸳都卡(银联卡)', '鸳都卡(银联卡)', '940072', 16, 6, 3134580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1052, '临沂商业银行', '(NULL)', '沂蒙卡(银联卡)', '沂蒙卡(银联卡)', '622359', 19, 6, 3134730, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1053, '临沂商业银行', '(NULL)', '沂蒙卡(银联卡)', '沂蒙卡(银联卡)', '940066', 19, 6, 3134730, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1054, '日照市商业银行', '(NULL)', '黄海卡', '黄海卡', '622857', 19, 6, 3134732, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1055, '日照市商业银行', '(NULL)', '黄海卡(银联卡)', '黄海卡(银联卡)', '940065', 19, 6, 3134732, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1056, '浙商银行', 'CZBANK', '商卡', '商卡', '621019', 19, 6, 3160000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1057, '浙商银行', 'CZBANK', '商卡', '商卡', '622309', 19, 6, 3160000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1058, '浙商银行', 'CZBANK', '商卡', '商卡', '6223091100', 19, 10, 3161100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1059, '浙商银行', 'CZBANK', '商卡', '商卡', '6223092900', 19, 10, 3162900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1060, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093310', 19, 10, 3163310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1061, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093320', 19, 10, 3163320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1062, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093330', 19, 10, 3163330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1063, '浙商银行', 'CZBANK', '商卡', '商卡', '6223093370', 19, 10, 3163370, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1064, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093380', 19, 10, 3163380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1065, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223096510', 19, 10, 3166510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1066, '浙商银行', 'CZBANK', '商卡', '商卡', '6223097910', 19, 10, 3167910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1067, '渤海银行', 'BOHAIB', '浩瀚金卡', '浩瀚金卡', '621268', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1068, '渤海银行', 'BOHAIB', '渤海银行公司借记卡', '渤海银行公司借记卡', '622684', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1069, '渤海银行', 'BOHAIB', '渤海银行借记卡', '渤海银行借记卡', '622884', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1070, '渤海银行', 'BOHAIB', '金融IC卡', '金融IC卡', '621453', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1071, '花旗银行(中国)有限公司', '(NULL)', '借记卡普卡', '借记卡普卡', '621062', 16, 6, 3190001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1072, '花旗银行(中国)有限公司', '(NULL)', '借记卡高端卡', '借记卡高端卡', '621063', 16, 6, 3190001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1073, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625076', 16, 6, 3190002, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1074, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625077', 16, 6, 3190002, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1075, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625074', 16, 6, 3190003, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1076, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625075', 16, 6, 3190003, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1077, '东亚银行', 'HKBEA', '紫荆卡', '紫荆卡', '622933', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1078, '东亚银行', 'HKBEA', '显卓理财卡', '显卓理财卡', '622938', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1079, '东亚银行', 'HKBEA', '借记卡', '借记卡', '623031', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1080, '汇丰银(中国)有限公司', '(NULL)', '汇丰中国卓越理财卡', '汇丰中国卓越理财卡', '622946', 16, 6, 3210000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1081, '渣打银行中国有限公司', '(NULL)', '渣打银行智通借记卡', '渣打银行智通借记卡', '622942', 16, 6, 3220000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1082, '渣打银行中国有限公司', '(NULL)', '渣打银行白金借记卡', '渣打银行白金借记卡', '622994', 16, 6, 3220000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1083, '星展银行', '(NULL)', '星展银行借记卡', '星展银行借记卡', '621016', 19, 6, 3240000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1084, '星展银行', '(NULL)', '星展银行借记卡', '星展银行借记卡', '621015', 19, 6, 3240000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1085, '恒生银行', '(NULL)', '恒生通财卡', '恒生通财卡', '622950', 16, 6, 3260000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1086, '恒生银行', '(NULL)', '恒生优越通财卡', '恒生优越通财卡', '622951', 16, 6, 3260000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1087, '友利银行(中国)有限公司', '(NULL)', '友利借记卡', '友利借记卡', '621060', 19, 6, 3270000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1088, '新韩银行', '(NULL)', '新韩卡', '新韩卡', '621072', 19, 6, 3280000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1089, '韩亚银行（中国）', '(NULL)', '韩亚卡', '韩亚卡', '621201', 16, 6, 3290000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1090, '华侨银行（中国）', '(NULL)', '卓锦借记卡', '卓锦借记卡', '621077', 16, 6, 3300000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1091, '永亨银行（中国）有限公司', '(NULL)', '永亨卡', '永亨卡', '621298', 18, 6, 3310000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1092, '南洋商业银行（中国）', '(NULL)', '借记卡', '借记卡', '621213', 19, 6, 3320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1093, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621289', 19, 6, 3320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1094, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621290', 19, 6, 3320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1095, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621291', 19, 6, 3320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1096, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621292', 19, 6, 3320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1097, '法国兴业银行（中国）', '(NULL)', '法兴标准借记卡', '法兴标准借记卡', '621245', 16, 6, 3330001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1098, '大华银行（中国）', '(NULL)', '尊享理财卡', '尊享理财卡', '621328', 19, 6, 3340000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1099, '大新银行（中国）', '(NULL)', '借记卡', '借记卡', '621277', 16, 6, 3350000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1100, '企业银行（中国）', '(NULL)', '瑞卡', '瑞卡', '621651', 19, 6, 3360000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1101, '上海银行', 'SHBANK', '单位借记卡', '单位借记卡', '621050', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1102, '上海银行', 'SHBANK', '综合保险卡', '综合保险卡', '622172', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1103, '上海银行', 'SHBANK', '申卡社保副卡(有折)', '申卡社保副卡(有折)', '622985', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1104, '上海银行', 'SHBANK', '申卡社保副卡(无折)', '申卡社保副卡(无折)', '622987', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1105, '上海银行', 'SHBANK', '首发纪念版IC卡', '首发纪念版IC卡', '620522', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1106, '上海银行', 'SHBANK', '白金IC借记卡', '白金IC借记卡', '622267', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1107, '上海银行', 'SHBANK', '慧通白金卡(配折)', '慧通白金卡(配折)', '622278', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1108, '上海银行', 'SHBANK', '慧通白金卡(不配折)', '慧通白金卡(不配折)', '622279', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1109, '上海银行', 'SHBANK', '申卡(银联卡)', '申卡(银联卡)', '622468', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1110, '上海银行', 'SHBANK', '申卡借记卡', '申卡借记卡', '622892', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1111, '上海银行', 'SHBANK', '银联申卡(银联卡)', '银联申卡(银联卡)', '940021', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1112, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '356827', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1113, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '356828', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1114, '上海银行', 'SHBANK', 'J分期付款信用卡', 'J分期付款信用卡', '356830', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1115, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '402673', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1116, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '402674', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1117, '上海银行', 'SHBANK', '上海申卡IC', '上海申卡IC', '438600', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1118, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '486466', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1119, '上海银行', 'SHBANK', '申卡贷记卡普通卡', '申卡贷记卡普通卡', '519498', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1120, '上海银行', 'SHBANK', '申卡贷记卡金卡', '申卡贷记卡金卡', '520131', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1121, '上海银行', 'SHBANK', '万事达白金卡', '万事达白金卡', '524031', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1122, '上海银行', 'SHBANK', '万事达星运卡', '万事达星运卡', '548838', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1123, '上海银行', 'SHBANK', '申卡贷记卡金卡', '申卡贷记卡金卡', '622148', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1124, '上海银行', 'SHBANK', '申卡贷记卡普通卡', '申卡贷记卡普通卡', '622149', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1125, '上海银行', 'SHBANK', '安融卡', '安融卡', '622268', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1126, '上海银行', 'SHBANK', '分期付款信用卡', '分期付款信用卡', '356829', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1127, '上海银行', 'SHBANK', '信用卡', '信用卡', '622300', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1128, '上海银行', 'SHBANK', '个人公务卡', '个人公务卡', '628230', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1129, '上海银行', 'SHBANK', '安融卡', '安融卡', '622269', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1130, '上海银行', 'SHBANK', '上海银行银联白金卡', '上海银行银联白金卡', '625099', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1131, '上海银行', 'SHBANK', '贷记IC卡', '贷记IC卡', '625953', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1132, '厦门银行', '(NULL)', '银鹭借记卡(银联卡)', '银鹭借记卡(银联卡)', '622393', 16, 6, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1133, '厦门银行', '(NULL)', '银鹭卡', '银鹭卡', '6886592', 18, 7, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1134, '厦门银行', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940023', 16, 6, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1135, '厦门银行', '(NULL)', '凤凰花卡', '凤凰花卡', '623019', 19, 6, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1136, '厦门银行', '(NULL)', '凤凰花卡', '凤凰花卡', '621600', 19, 6, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1137, '北京银行', 'BJBANK', '京卡借记卡', '京卡借记卡', '421317', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1138, '北京银行', 'BJBANK', '京卡贵宾白金卡', '京卡贵宾白金卡', '422161', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1139, '北京银行', 'BJBANK', '京卡(银联卡)', '京卡(银联卡)', '602969', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1140, '北京银行', 'BJBANK', '京卡贵宾金卡', '京卡贵宾金卡', '422160', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1141, '北京银行', 'BJBANK', '京卡借记卡', '京卡借记卡', '621030', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1142, '北京银行', 'BJBANK', '京卡', '京卡', '621420', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1143, '北京银行', 'BJBANK', '京卡', '京卡', '621468', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1144, '福建海峡银行', 'FJHXBC', '榕城卡(银联卡)', '榕城卡(银联卡)', '622388', 16, 6, 4053910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1145, '福建海峡银行', 'FJHXBC', '福州市民卡', '福州市民卡', '621267', 18, 6, 4053910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1146, '福建海峡银行', 'FJHXBC', '福州市民卡', '福州市民卡', '620043', 18, 6, 4053910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1147, '福建海峡银行', 'FJHXBC', '海福卡（IC卡）', '海福卡（IC卡）', '623063', 18, 6, 4053910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1148, '吉林银行', 'JLBANK', '君子兰一卡通(银联卡)', '君子兰一卡通(银联卡)', '622865', 19, 6, 4062410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1149, '吉林银行', 'JLBANK', '君子兰卡(银联卡)', '君子兰卡(银联卡)', '940012', 16, 6, 4062410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1150, '吉林银行', 'JLBANK', '长白山金融IC卡', '长白山金融IC卡', '623131', 19, 6, 4062410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1151, '吉林银行', 'JLBANK', '信用卡', '信用卡', '622178', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1152, '吉林银行', 'JLBANK', '信用卡', '信用卡', '622179', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1153, '吉林银行', 'JLBANK', '公务卡', '公务卡', '628358', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1154, '镇江市商业银行', '(NULL)', '金山灵通卡(银联卡)', '金山灵通卡(银联卡)', '622394', 16, 6, 4073140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1155, '镇江市商业银行', '(NULL)', '金山灵通卡(银联卡)', '金山灵通卡(银联卡)', '940025', 16, 6, 4073140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1156, '宁波银行', 'NBBANK', '银联标准卡', '银联标准卡', '621279', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1157, '宁波银行', 'NBBANK', '汇通借记卡', '汇通借记卡', '622281', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1158, '宁波银行', 'NBBANK', '汇通卡(银联卡)', '汇通卡(银联卡)', '622316', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1159, '宁波银行', 'NBBANK', '明州卡', '明州卡', '940022', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1160, '宁波银行', 'NBBANK', '汇通借记卡', '汇通借记卡', '621418', 19, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1161, '宁波银行', 'NBBANK', '汇通国际卡银联双币卡', '汇通国际卡银联双币卡', '512431', 16, 6, 4083329, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1162, '宁波银行', 'NBBANK', '汇通国际卡银联双币卡', '汇通国际卡银联双币卡', '520194', 16, 6, 4083329, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1163, '宁波银行', 'NBBANK', '汇通贷记卡', '汇通贷记卡', '622282', 16, 6, 4083329, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1164, '宁波银行', 'NBBANK', '汇通卡(银联卡)', '汇通卡(银联卡)', '622318', 16, 6, 4083329, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1165, '平安银行', 'SPABANK', '新磁条借记卡', '新磁条借记卡', '621626', 19, 6, 4100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1166, '平安银行', 'SPABANK', '平安银行IC借记卡', '平安银行IC借记卡', '623058', 19, 6, 4100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1167, '平安银行', 'SPABANK', '万事顺卡', '万事顺卡', '602907', 16, 6, 4105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1168, '平安银行', 'SPABANK', '平安银行借记卡', '平安银行借记卡', '622986', 16, 6, 4105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1169, '平安银行', 'SPABANK', '平安银行借记卡', '平安银行借记卡', '622989', 16, 6, 4105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1170, '平安银行', 'SPABANK', '万事顺借记卡', '万事顺借记卡', '622298', 16, 6, 4105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1171, '焦作市商业银行', '(NULL)', '月季借记卡(银联卡)', '月季借记卡(银联卡)', '622338', 19, 6, 4115010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1172, '焦作市商业银行', '(NULL)', '月季城市通(银联卡)', '月季城市通(银联卡)', '940032', 16, 6, 4115010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1173, '温州银行', 'WZCB', '金鹿卡', '金鹿卡', '621977', 16, 6, 4123330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1174, '广州银行', 'GCB', '羊城借记卡', '羊城借记卡', '603445', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1175, '广州银行', 'GCB', '羊城借记卡(银联卡)', '羊城借记卡(银联卡)', '622467', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1176, '广州银行', 'GCB', '羊城借记卡(银联卡)', '羊城借记卡(银联卡)', '940016', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1177, '广州银行', 'GCB', '金融IC借记卡', '金融IC借记卡', '621463', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1178, '汉口银行', 'HKB', '九通卡(银联卡)', '九通卡(银联卡)', '990027', 18, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1179, '汉口银行', 'HKB', '九通卡', '九通卡', '622325', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1180, '汉口银行', 'HKB', '借记卡', '借记卡', '623105', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1181, '汉口银行', 'HKB', '借记卡', '借记卡', '623029', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1182, '龙江银行', 'DAQINGB', '金鹤卡', '金鹤卡', '622475', 16, 6, 4162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1183, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '621244', 16, 6, 4170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1184, '盛京银行', 'SJBANK', '玫瑰IC卡', '玫瑰IC卡', '623108', 16, 6, 4170001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1185, '盛京银行', 'SJBANK', '玫瑰IC卡', '玫瑰IC卡', '623081', 16, 6, 4170001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1186, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '566666', 18, 6, 4172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1187, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '622455', 19, 6, 4172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1188, '盛京银行', 'SJBANK', '玫瑰卡(银联卡)', '玫瑰卡(银联卡)', '940039', 19, 6, 4172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1189, '盛京银行', 'SJBANK', '玫瑰卡(银联卡)', '玫瑰卡(银联卡)', '622466', 16, 6, 4172211, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1190, '盛京银行', 'SJBANK', '盛京银行公务卡', '盛京银行公务卡', '628285', 16, 6, 4172211, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1191, '洛阳银行', '(NULL)', '都市一卡通(银联卡)', '都市一卡通(银联卡)', '622420', 17, 6, 4184930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1192, '洛阳银行', '(NULL)', '都市一卡通(银联卡)', '都市一卡通(银联卡)', '940041', 17, 6, 4184930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1193, '辽阳银行', '(NULL)', '新兴卡(银联卡)', '新兴卡(银联卡)', '622399', 17, 6, 4192310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1194, '辽阳银行股份有限公司', '(NULL)', '新兴卡(银联卡)', '新兴卡(银联卡)', '940043', 17, 6, 4192310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1195, '大连银行', 'DLB', '北方明珠卡', '北方明珠卡', '603708', 17, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1196, '大连银行', 'DLB', '人民币借记卡', '人民币借记卡', '622993', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1197, '大连银行', 'DLB', '大连市社会保障卡', '大连市社会保障卡', '623069', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1198, '大连银行', 'DLB', '金融IC借记卡', '金融IC借记卡', '623070', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1199, '大连银行', 'DLB', '借记IC卡', '借记IC卡', '623172', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1200, '大连银行', 'DLB', '借记IC卡', '借记IC卡', '623173', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1201, '大连银行', 'DLB', '大连市商业银行贷记卡', '大连市商业银行贷记卡', '622383', 16, 6, 4202221, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1202, '大连银行', 'DLB', '大连市商业银行贷记卡', '大连市商业银行贷记卡', '622385', 16, 6, 4202221, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1203, '大连银行', 'DLB', '银联标准公务卡', '银联标准公务卡', '628299', 16, 6, 4202221, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1204, '苏州市商业银行', '(NULL)', '姑苏卡', '姑苏卡', '603506', 19, 6, 4213050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1205, '河北银行', 'BHB', '如意借记卡(银联卡)', '如意借记卡(银联卡)', '622498', 19, 6, 4221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1206, '河北银行', 'BHB', '如意借记卡(银联卡)', '如意借记卡(银联卡)', '622499', 19, 6, 4221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1207, '河北银行', 'BHB', '借记IC卡', '借记IC卡', '623000', 19, 6, 4221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1208, '河北银行', 'BHB', '如意卡(银联卡)', '如意卡(银联卡)', '940046', 19, 6, 4221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1209, '杭州商业银行', '(NULL)', '西湖卡', '西湖卡', '603367', 18, 6, 4233310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1210, '杭州商业银行', '(NULL)', '西湖卡', '西湖卡', '622878', 18, 6, 4233310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1211, '南京银行', 'NJCB', '梅花贷记卡(银联卡)', '梅花贷记卡(银联卡)', '622303', 16, 6, 4240001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1212, '南京银行', 'NJCB', '梅花信用卡公务卡', '梅花信用卡公务卡', '628242', 16, 6, 4240001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1213, '南京银行', 'NJCB', '梅花信用卡商务卡', '梅花信用卡商务卡', '622595', 16, 6, 4240001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1214, '南京银行', 'NJCB', '梅花借记卡(银联卡)', '梅花借记卡(银联卡)', '622305', 16, 6, 4243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1215, '南京银行', 'NJCB', '白金卡', '白金卡', '621259', 16, 6, 4243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1216, '南京银行', 'NJCB', '商务卡', '商务卡', '622596', 16, 6, 4243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1217, '东莞市商业银行', '(NULL)', '万顺通卡(银联卡)', '万顺通卡(银联卡)', '622333', 16, 6, 4256020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1218, '东莞市商业银行', '(NULL)', '万顺通卡(银联卡)', '万顺通卡(银联卡)', '940050', 16, 6, 4256020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1219, '东莞市商业银行', '(NULL)', '万顺通借记卡', '万顺通借记卡', '621439', 19, 6, 4256020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1220, '东莞市商业银行', '(NULL)', '社会保障卡', '社会保障卡', '623010', 19, 6, 4256020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1221, '金华银行', 'JHBANK', '双龙借记卡', '双龙借记卡', '622449', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1222, '金华银行', 'JHBANK', '双龙卡(银联卡)', '双龙卡(银联卡)', '940051', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1223, '金华银行', 'JHBANK', '双龙贷记卡(银联卡)', '双龙贷记卡(银联卡)', '622450', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1224, '金华银行', 'JHBANK', '公务卡', '公务卡', '628204', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1225, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲贷记卡(银联卡)', '雪莲贷记卡(银联卡)', '622476', 16, 6, 4270001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1226, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记IC卡', '雪莲借记IC卡', '621751', 19, 6, 4270001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1227, '乌鲁木齐市商业银行', 'URMQCCB', '乌鲁木齐市公务卡', '乌鲁木齐市公务卡', '628278', 16, 6, 4270001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1228, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记卡(银联卡)', '雪莲借记卡(银联卡)', '622143', 19, 6, 4278810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1229, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲卡(银联卡)', '雪莲卡(银联卡)', '940001', 19, 6, 4278810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1230, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记IC卡', '雪莲借记IC卡', '621754', 19, 6, 4278810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1231, '绍兴银行', 'SXCB', '兰花卡(银联卡)', '兰花卡(银联卡)', '622486', 16, 6, 4283370, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1232, '绍兴银行', 'SXCB', '兰花卡', '兰花卡', '603602', 18, 6, 4283370, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1233, '绍兴银行', 'SXCB', '兰花IC借记卡', '兰花IC借记卡', '623026', 16, 6, 4283371, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1234, '绍兴银行', 'SXCB', '社保IC借记卡', '社保IC借记卡', '623086', 16, 6, 4283371, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1235, '绍兴银行', 'SXCB', '兰花公务卡', '兰花公务卡', '628291', 16, 6, 4283379, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1236, '成都商业银行', '(NULL)', '锦程卡金卡', '锦程卡金卡', '622152', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1237, '成都商业银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622154', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1238, '成都商业银行', '(NULL)', '锦程力诚联名卡', '锦程力诚联名卡', '622996', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1239, '成都商业银行', '(NULL)', '锦程力诚联名卡', '锦程力诚联名卡', '622997', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1240, '成都商业银行', '(NULL)', '锦程卡(银联卡)', '锦程卡(银联卡)', '940027', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1241, '成都商业银行', '(NULL)', '锦程卡定活一卡通金卡', '锦程卡定活一卡通金卡', '622153', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1242, '成都商业银行', '(NULL)', '锦程卡(银联卡)', '锦程卡(银联卡)', '622135', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1243, '成都商业银行', '(NULL)', '芙蓉锦程天府通卡', '芙蓉锦程天府通卡', '621482', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1244, '成都商业银行', '(NULL)', '芙蓉锦程福农卡', '芙蓉锦程福农卡', '621532', 19, 6, 4296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1245, '抚顺银行', 'FSCB', '绿叶卡(银联卡)', '绿叶卡(银联卡)', '622442', 17, 6, 4302240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1246, '抚顺银行', 'FSCB', '启运卡', '启运卡', '622442', 19, 6, 4302240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1247, '抚顺银行', 'FSCB', '绿叶卡(银联卡)', '绿叶卡(银联卡)', '940053', 18, 6, 4302240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1248, '抚顺银行', 'FSCB', '借记IC卡', '借记IC卡', '623099', 19, 6, 4302248, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1249, '宜昌市商业银行', '(NULL)', '三峡卡(银联卡)', '三峡卡(银联卡)', '940055', 17, 6, 4325210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1250, '宜昌市商业银行', '(NULL)', '信用卡(银联卡)', '信用卡(银联卡)', '622397', 16, 6, 4325261, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1251, '葫芦岛市商业银行', '(NULL)', '一通卡', '一通卡', '622398', 16, 6, 4332350, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1252, '葫芦岛市商业银行', '(NULL)', '一卡通(银联卡)', '一卡通(银联卡)', '940054', 16, 6, 4332350, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1253, '天津市商业银行', '(NULL)', '津卡', '津卡', '622331', 18, 6, 4341100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1254, '天津市商业银行', '(NULL)', '津卡贷记卡(银联卡)', '津卡贷记卡(银联卡)', '622426', 16, 6, 4341100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1255, '天津银行', 'TCCB', '商务卡', '商务卡', '628205', 16, 6, 4341101, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1256, '郑州银行', 'ZZBANK', '世纪一卡通(银联卡)', '世纪一卡通(银联卡)', '622421', 19, 6, 4354910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1257, '郑州银行', 'ZZBANK', '世纪一卡通', '世纪一卡通', '940056', 17, 6, 4354910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1258, '郑州银行', 'ZZBANK', '世纪一卡通', '世纪一卡通', '96828', 16, 5, 4354910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1259, '宁夏银行', 'NXBANK', '宁夏银行公务卡', '宁夏银行公务卡', '628214', 16, 6, 4360010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1260, '宁夏银行', 'NXBANK', '宁夏银行福农贷记卡', '宁夏银行福农贷记卡', '625529', 16, 6, 4360010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1261, '宁夏银行', 'NXBANK', '如意卡(银联卡)', '如意卡(银联卡)', '622428', 16, 6, 4368710, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1262, '宁夏银行', 'NXBANK', '宁夏银行福农借记卡', '宁夏银行福农借记卡', '621529', 19, 6, 4368710, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1263, '宁夏银行', 'NXBANK', '如意借记卡', '如意借记卡', '622429', 19, 6, 4368710, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1264, '宁夏银行', 'NXBANK', '如意IC卡', '如意IC卡', '621417', 19, 6, 4368719, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1265, '宁夏银行', 'NXBANK', '宁夏银行如意借记卡', '宁夏银行如意借记卡', '623089', 19, 6, 4368719, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1266, '宁夏银行', 'NXBANK', '中国旅游卡', '中国旅游卡', '623200', 19, 6, 4368719, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1267, '珠海华润银行', '(NULL)', '万事顺卡', '万事顺卡', '622363', 19, 6, 4375850, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1268, '珠海华润银行', '(NULL)', '万事顺卡(银联卡)', '万事顺卡(银联卡)', '940048', 19, 6, 4375850, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1269, '齐商银行', 'ZBCB', '金达借记卡(银联卡)', '金达借记卡(银联卡)', '622311', 17, 6, 4384530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1270, '齐商银行', 'ZBCB', '金达卡(银联卡)', '金达卡(银联卡)', '940057', 17, 6, 4384530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1271, '锦州银行', 'BOJZ', '7777卡', '7777卡', '622990', 17, 6, 4392270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1272, '锦州银行', 'BOJZ', '万通卡(银联卡)', '万通卡(银联卡)', '940003', 17, 6, 4392270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1273, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '622877', 19, 6, 4403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1274, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '622879', 19, 6, 4403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1275, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '603601', 17, 6, 4403610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1276, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622137', 17, 6, 4403620, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1277, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622327', 17, 6, 4403650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1278, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622340', 17, 6, 4403660, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1279, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622366', 17, 6, 4403680, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1280, '重庆银行', 'CQBANK', '长江卡', '长江卡', '9896', 16, 4, 4416530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1281, '重庆银行', 'CQBANK', '长江卡(银联卡)', '长江卡(银联卡)', '622134', 16, 6, 4416530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1282, '重庆银行', 'CQBANK', '长江卡(银联卡)', '长江卡(银联卡)', '940018', 16, 6, 4416530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1283, '重庆银行', 'CQBANK', '长江卡', '长江卡', '623016', 16, 6, 4416900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1284, '哈尔滨银行', '(NULL)', '福农借记卡', '福农借记卡', '621577', 19, 6, 4422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1285, '哈尔滨银行', '(NULL)', '丁香一卡通(银联卡)', '丁香一卡通(银联卡)', '940049', 18, 6, 4422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1286, '哈尔滨银行', '(NULL)', '丁香卡', '丁香卡', '622425', 19, 6, 4422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1287, '哈尔滨银行', '(NULL)', '丁香借记卡(银联卡)', '丁香借记卡(银联卡)', '622425', 17, 6, 4422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1288, '贵阳银行', '(NULL)', '甲秀银联借记卡', '甲秀银联借记卡', '622133', 19, 6, 4437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1289, '贵阳银行', '(NULL)', '甲秀卡', '甲秀卡', '888', 16, 3, 4437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1290, '贵阳银行', '(NULL)', '一卡通', '一卡通', '621735', 19, 6, 4437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1291, '西安银行', 'XABANK', '福瑞卡', '福瑞卡', '622136', 18, 6, 4447910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1292, '西安银行', 'XABANK', '金丝路卡', '金丝路卡', '622981', 18, 6, 4447910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1293, '无锡市商业银行', '(NULL)', '太湖卡', '太湖卡', '60326500', 16, 8, 4453020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1294, '无锡市商业银行', '(NULL)', '太湖卡', '太湖卡', '60326513', 18, 8, 4453020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1295, '无锡市商业银行', '(NULL)', '太湖金保卡(银联卡)', '太湖金保卡(银联卡)', '622485', 18, 6, 4453020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1296, '丹东银行', 'BODD', '银杏卡(银联卡)', '银杏卡(银联卡)', '622415', 16, 6, 4462260, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1297, '丹东银行', 'BODD', '银杏卡(银联卡)', '银杏卡(银联卡)', '940060', 16, 6, 4462260, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1298, '丹东银行', 'BODD', '借记IC卡', '借记IC卡', '623098', 19, 6, 4462262, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1299, '兰州银行', 'LZYH', '敦煌国际卡(银联卡)', '敦煌国际卡(银联卡)', '622139', 16, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1300, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '940040', 16, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1301, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '621242', 19, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1302, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '621538', 19, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1303, '兰州银行', 'LZYH', '敦煌金融IC卡', '敦煌金融IC卡', '621496', 19, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1304, '南昌银行', 'NCB', '南昌银行借记卡', '南昌银行借记卡', '621269', 16, 6, 4484210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1305, '南昌银行', 'NCB', '金瑞卡', '金瑞卡', '622275', 16, 6, 4484210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1306, '南昌银行', 'NCB', '金瑞卡(银联卡)', '金瑞卡(银联卡)', '940006', 17, 6, 4484210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1307, '晋商银行', 'JSB', '晋龙一卡通', '晋龙一卡通', '621216', 19, 6, 4491610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1308, '晋商银行', 'JSB', '晋龙一卡通', '晋龙一卡通', '622465', 17, 6, 4491610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1309, '晋商银行', 'JSB', '晋龙卡(银联卡)', '晋龙卡(银联卡)', '940031', 17, 6, 4491610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1310, '青岛银行', 'QDCCB', '金桥通卡', '金桥通卡', '621252', 16, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1311, '青岛银行', 'QDCCB', '金桥卡(银联卡)', '金桥卡(银联卡)', '622146', 16, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1312, '青岛银行', 'QDCCB', '金桥卡(银联卡)', '金桥卡(银联卡)', '940061', 16, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1313, '青岛银行', 'QDCCB', '金桥卡', '金桥卡', '621419', 19, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1314, '青岛银行', 'QDCCB', '借记IC卡', '借记IC卡', '623170', 19, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1315, '吉林银行', 'JLBANK', '雾凇卡(银联卡)', '雾凇卡(银联卡)', '622440', 16, 6, 4512420, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1316, '吉林银行', 'JLBANK', '雾凇卡(银联卡)', '雾凇卡(银联卡)', '940047', 16, 6, 4512420, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1317, '南通商业银行', '(NULL)', '金桥卡', '金桥卡', '69580', 18, 5, 4523060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1318, '南通商业银行', '(NULL)', '金桥卡(银联卡)', '金桥卡(银联卡)', '940017', 18, 6, 4523060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1319, '南通商业银行', '(NULL)', '金桥卡(银联卡)', '金桥卡(银联卡)', '622418', 16, 6, 4523060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1320, '九江银行', 'JJBANK', '庐山卡(银联卡)', '庐山卡(银联卡)', '622307', 19, 6, 4544240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1321, '九江银行', 'JJBANK', '庐山卡', '庐山卡', '622162', 19, 6, 4544240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1322, '鞍山银行', 'ASCB', '千山卡(银联卡)', '千山卡(银联卡)', '622413', 16, 6, 4562230, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1323, '鞍山银行', 'ASCB', '千山卡(银联卡)', '千山卡(银联卡)', '940002', 16, 6, 4562230, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1324, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '621237', 19, 6, 4571260, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1325, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '62249802', 19, 8, 4571260, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1326, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '94004602', 19, 8, 4571260, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1327, '秦皇岛市商业银行', '(NULL)', '秦卡-IC卡', '秦卡-IC卡', '623003', 19, 6, 4571261, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1328, '青海银行', 'BOQH', '三江银行卡(银联卡)', '三江银行卡(银联卡)', '622310', 17, 6, 4588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1329, '青海银行', 'BOQH', '三江卡', '三江卡', '940068', 17, 6, 4588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1330, '台州银行', 'TZCB', '大唐贷记卡', '大唐贷记卡', '622321', 16, 6, 4593450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1331, '台州银行', 'TZCB', '大唐准贷记卡', '大唐准贷记卡', '625001', 16, 6, 4593450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1332, '台州银行', 'TZCB', '大唐卡(银联卡)', '大唐卡(银联卡)', '622427', 16, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1333, '台州银行', 'TZCB', '大唐卡', '大唐卡', '940069', 17, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1334, '台州银行', 'TZCB', '借记卡', '借记卡', '623039', 19, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1335, '台州银行', 'TZCB', '公务卡', '公务卡', '628273', 16, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1336, '盐城商行', '(NULL)', '金鹤卡(银联卡)', '金鹤卡(银联卡)', '940070', 16, 6, 4603110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1337, '长沙银行', 'CSCB', '芙蓉卡', '芙蓉卡', '694301', 18, 6, 4615510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1338, '长沙银行', 'CSCB', '芙蓉卡(银联卡)', '芙蓉卡(银联卡)', '940071', 19, 6, 4615510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1339, '长沙银行', 'CSCB', '芙蓉卡(银联卡)', '芙蓉卡(银联卡)', '622368', 19, 6, 4615510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1340, '长沙银行', 'CSCB', '芙蓉金融IC卡', '芙蓉金融IC卡', '621446', 19, 6, 4615510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1341, '长沙银行', 'CSCB', '市民卡', '市民卡', '625901', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1342, '长沙银行', 'CSCB', '芙蓉贷记卡', '芙蓉贷记卡', '622898', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1343, '长沙银行', 'CSCB', '芙蓉贷记卡', '芙蓉贷记卡', '622900', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1344, '长沙银行', 'CSCB', '公务卡钻石卡', '公务卡钻石卡', '628281', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1345, '长沙银行', 'CSCB', '公务卡金卡', '公务卡金卡', '628282', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1346, '长沙银行', 'CSCB', '银联标准钻石卡', '银联标准钻石卡', '622806', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1347, '长沙银行', 'CSCB', '公务卡普卡', '公务卡普卡', '628283', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1348, '长沙银行', 'CSCB', '市民卡', '市民卡', '620519', 19, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1349, '赣州银行', 'GZB', '长征卡', '长征卡', '622967', 19, 6, 4634280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1350, '赣州银行', 'GZB', '长征卡(银联卡)', '长征卡(银联卡)', '940073', 19, 6, 4634280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1351, '泉州银行', '(NULL)', '海峡银联卡(银联卡)', '海峡银联卡(银联卡)', '622370', 19, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1352, '泉州银行', '(NULL)', '海峡储蓄卡', '海峡储蓄卡', '683970', 18, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1353, '泉州银行', '(NULL)', '海峡银联卡(银联卡)', '海峡银联卡(银联卡)', '940074', 18, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1354, '泉州银行', '(NULL)', '海峡卡', '海峡卡', '621437', 19, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1355, '泉州银行', '(NULL)', '公务卡', '公务卡', '628319', 16, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1356, '营口市商业银行', '(NULL)', '辽河一卡通(银联卡)', '辽河一卡通(银联卡)', '622400', 17, 6, 4652280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1357, '昆明商业银行', '(NULL)', '春城卡(银联卡)', '春城卡(银联卡)', '990871', 18, 6, 4667310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1358, '昆明商业银行', '(NULL)', '春城卡(银联卡)', '春城卡(银联卡)', '622308', 17, 6, 4667310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1359, '昆明商业银行', '(NULL)', '富滇IC卡（复合卡）', '富滇IC卡（复合卡）', '621415', 19, 6, 4667310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1360, '阜新银行', 'FXCB', '金通卡(银联卡)', '金通卡(银联卡)', '622126', 18, 6, 4672290, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1361, '嘉兴市商业银行', '(NULL)', '南湖借记卡(银联卡)', '南湖借记卡(银联卡)', '622132', 16, 6, 4703350, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1362, '廊坊银行', 'LANGFB', '白金卡', '白金卡', '621340', 16, 6, 4721460, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1363, '廊坊银行', 'LANGFB', '金卡', '金卡', '621341', 16, 6, 4721460, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1364, '廊坊银行', 'LANGFB', '银星卡(银联卡)', '银星卡(银联卡)', '622140', 16, 6, 4721460, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1365, '泰隆城市信用社', '(NULL)', '泰隆卡(银联卡)', '泰隆卡(银联卡)', '622141', 16, 6, 4733450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1366, '内蒙古银行', 'H3CB', '百灵卡(银联卡)', '百灵卡(银联卡)', '622147', 19, 6, 4741910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1367, '内蒙古银行', 'H3CB', '成吉思汗卡', '成吉思汗卡', '621633', 19, 6, 4741910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1368, '湖州市商业银行', 'HZCCB', '百合卡', '百合卡', '622301', 19, 6, 4753360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1369, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '621266', 19, 6, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1370, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '62249804', 19, 8, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1371, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '94004604', 19, 8, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1372, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '621422', 19, 6, 4761431, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1373, '南宁市商业银行', '(NULL)', '桂花卡(银联卡)', '桂花卡(银联卡)', '622335', 16, 6, 4786110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1374, '包商银行', 'BSB', '雄鹰卡(银联卡)', '雄鹰卡(银联卡)', '622336', 17, 6, 4791920, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1375, '包商银行', 'BSB', '包头市商业银行借记卡', '包头市商业银行借记卡', '622165', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1376, '包商银行', 'BSB', '雄鹰贷记卡', '雄鹰贷记卡', '622315', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1377, '包商银行', 'BSB', '贷记卡', '贷记卡', '625950', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1378, '包商银行', 'BSB', '包商银行内蒙古自治区公务卡', '包商银行内蒙古自治区公务卡', '628295', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1379, '包商银行', 'BSB', '借记卡', '借记卡', '621760', 17, 6, 4791922, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1380, '连云港市商业银行', '(NULL)', '金猴神通借记卡', '金猴神通借记卡', '622337', 16, 6, 4803070, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1381, '威海商业银行', '(NULL)', '通达卡(银联卡)', '通达卡(银联卡)', '622411', 16, 6, 4814650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1382, '威海市商业银行', 'WHCCB', '通达借记IC卡', '通达借记IC卡', '623102', 16, 6, 4814659, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1383, '攀枝花市商业银行', '(NULL)', '攀枝花卡(银联卡)', '攀枝花卡(银联卡)', '622342', 19, 6, 4836560, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1384, '绵阳市商业银行', '(NULL)', '科技城卡(银联卡)', '科技城卡(银联卡)', '622367', 19, 6, 4856590, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1385, '泸州市商业银行', '(NULL)', '酒城卡(银联卡)', '酒城卡(银联卡)', '622392', 19, 6, 4866570, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1386, '大同市商业银行', '(NULL)', '云冈卡(银联卡)', '云冈卡(银联卡)', '622395', 19, 6, 4871620, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1387, '三门峡银行', 'SCCB', '天鹅卡(银联卡)', '天鹅卡(银联卡)', '622441', 16, 6, 4885050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1388, '广东南粤银行', '(NULL)', '南珠卡(银联卡)', '南珠卡(银联卡)', '622448', 16, 6, 4895910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1389, '张家口市商业银行', 'ZJKCCB', '如意借记卡', '如意借记卡', '622982', 19, 6, 4901380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1390, '张家口市商业银行', 'ZJKCCB', '好运IC借记卡', '好运IC借记卡', '621413', 19, 6, 4901381, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1391, '桂林市商业银行', '(NULL)', '漓江卡(银联卡)', '漓江卡(银联卡)', '622856', 17, 6, 4916170, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1392, '龙江银行', 'DAQINGB', '福农借记卡', '福农借记卡', '621037', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1393, '龙江银行', 'DAQINGB', '联名借记卡', '联名借记卡', '621097', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1394, '龙江银行', 'DAQINGB', '福农借记卡', '福农借记卡', '621588', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1395, '龙江银行', 'DAQINGB', '中国旅游卡', '中国旅游卡', '62321601', 19, 8, 4922600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1396, '龙江银行', 'DAQINGB', '玉兔卡(银联卡)', '玉兔卡(银联卡)', '622860', 16, 6, 4922690, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1397, '江苏长江商业银行', '(NULL)', '长江卡', '长江卡', '622870', 16, 6, 4933120, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1398, '徐州市商业银行', '(NULL)', '彭城借记卡(银联卡)', '彭城借记卡(银联卡)', '622866', 16, 6, 4943030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1399, '柳州银行', '(NULL)', '龙城卡', '龙城卡', '622292', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1400, '柳州银行', '(NULL)', '龙城卡VIP卡', '龙城卡VIP卡', '622880', 16, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1401, '柳州银行', '(NULL)', '龙城致富卡', '龙城致富卡', '622881', 16, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1402, '柳州银行', '(NULL)', '龙城卡', '龙城卡', '622291', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1403, '柳州银行', '(NULL)', '龙城IC卡', '龙城IC卡', '621412', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1404, '南充市商业银行', 'CGNB', '熊猫团团卡', '熊猫团团卡', '622897', 19, 6, 4966730, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1405, '莱商银行', 'LSBANK', '银联标准卡', '银联标准卡', '628279', 16, 6, 4974634, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1406, '莱芜银行', '(NULL)', '金凤卡', '金凤卡', '622864', 16, 6, 4974634, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1407, '莱商银行', 'LSBANK', '借记IC卡', '借记IC卡', '621403', 19, 6, 4974790, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1408, '德阳银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622561', 19, 6, 4986580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1409, '德阳银行', '(NULL)', '锦程卡定活一卡通金卡', '锦程卡定活一卡通金卡', '622562', 19, 6, 4986580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1410, '德阳银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622563', 19, 6, 4986580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1411, '唐山市商业银行', '(NULL)', '唐山市城通卡', '唐山市城通卡', '622167', 19, 6, 4991240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1412, '六盘水商行', '(NULL)', '凉都卡', '凉都卡', '622508', 16, 6, 5007020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1413, '曲靖市商业银行', '(NULL)', '珠江源卡', '珠江源卡', '622777', 16, 6, 5027360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1414, '曲靖市商业银行', '(NULL)', '珠江源卡', '珠江源卡', '621497', 16, 6, 5027361, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1415, '晋城银行', '(NULL)', '珠联璧合卡', '珠联璧合卡', '622532', 19, 6, 5031680, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1416, '东莞商行', '(NULL)', '恒通贷记卡', '恒通贷记卡', '622888', 16, 6, 5056020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1417, '温州银行', 'WZCB', '金鹿信用卡', '金鹿信用卡', '622868', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1418, '温州银行', 'WZCB', '金鹿信用卡', '金鹿信用卡', '622899', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1419, '温州银行', 'WZCB', '金鹿公务卡', '金鹿公务卡', '628255', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1420, '汉口银行', 'HKB', '汉口银行贷记卡', '汉口银行贷记卡', '622566', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1421, '汉口银行', 'HKB', '汉口银行贷记卡', '汉口银行贷记卡', '622567', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1422, '汉口银行', 'HKB', '九通香港旅游贷记普卡', '九通香港旅游贷记普卡', '622625', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1423, '汉口银行', 'HKB', '九通香港旅游贷记金卡', '九通香港旅游贷记金卡', '622626', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1424, '汉口银行', 'HKB', '贷记卡', '贷记卡', '625946', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1425, '汉口银行', 'HKB', '九通公务卡', '九通公务卡', '628200', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1426, '江苏银行', 'JSBANK', '聚宝借记卡', '聚宝借记卡', '621076', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1427, '江苏银行', 'JSBANK', '月季卡', '月季卡', '504923', 16, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1428, '江苏银行', 'JSBANK', '紫金卡', '紫金卡', '622173', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1429, '江苏银行', 'JSBANK', '绿扬卡(银联卡)', '绿扬卡(银联卡)', '622422', 16, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1430, '江苏银行', 'JSBANK', '月季卡(银联卡)', '月季卡(银联卡)', '622447', 16, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1431, '江苏银行', 'JSBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '622131', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1432, '江苏银行', 'JSBANK', '月季卡(银联卡)', '月季卡(银联卡)', '940076', 16, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1433, '江苏银行', 'JSBANK', '聚宝惠民福农卡', '聚宝惠民福农卡', '621579', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1434, '江苏银行', 'JSBANK', '江苏银行聚宝IC借记卡', '江苏银行聚宝IC借记卡', '622876', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1435, '平安银行', 'SPABANK', '白金信用卡', '白金信用卡', '531659', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1436, '平安银行', 'SPABANK', '白金信用卡', '白金信用卡', '622157', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1437, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '528020', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1438, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '622155', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1439, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '622156', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1440, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '526855', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1441, '平安银行', 'SPABANK', '信用卡', '信用卡', '356869', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1442, '平安银行', 'SPABANK', '信用卡', '信用卡', '356868', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1443, '平安银行', 'SPABANK', '平安中国旅游信用卡', '平安中国旅游信用卡', '625360', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1444, '平安银行', 'SPABANK', '平安中国旅游白金信用卡', '平安中国旅游白金信用卡', '625361', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1445, '平安银行', 'SPABANK', '公务卡', '公务卡', '628296', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1446, '长治市商业银行', '(NULL)', '长治商行银联晋龙卡', '长治商行银联晋龙卡', '622962', 17, 6, 5121660, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1447, '承德市商业银行', '(NULL)', '热河卡', '热河卡', '622936', 19, 6, 5131410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1448, '德州银行', 'DZBANK', '长河借记卡', '长河借记卡', '622937', 19, 6, 5154680, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1449, '遵义市商业银行', 'ZYCBANK', '社保卡', '社保卡', '621460', 19, 6, 5167030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1450, '遵义市商业银行', 'ZYCBANK', '尊卡', '尊卡', '622939', 17, 6, 5167030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1451, '邯郸市商业银行', '(NULL)', '邯银卡', '邯银卡', '622960', 18, 6, 5171270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1452, '安顺市商业银行', '(NULL)', '黄果树福农卡', '黄果树福农卡', '621591', 16, 6, 5197110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1453, '安顺市商业银行', '(NULL)', '黄果树借记卡', '黄果树借记卡', '622961', 16, 6, 5197117, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1454, '江苏银行', 'JSBANK', '紫金信用卡(公务卡)', '紫金信用卡(公务卡)', '628210', 16, 6, 5213000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1455, '江苏银行', 'JSBANK', '紫金信用卡', '紫金信用卡', '622283', 16, 6, 5213000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1456, '江苏银行', 'JSBANK', '天翼联名信用卡', '天翼联名信用卡', '625902', 16, 6, 5213000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1457, '平凉市商业银行', '(NULL)', '广成卡', '广成卡', '621010', 16, 6, 5238333, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1458, '玉溪市商业银行', 'YXCCB', '红塔卡', '红塔卡', '622980', 19, 6, 5247410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1459, '浙江民泰商业银行', 'MTBANK', '金融IC卡', '金融IC卡', '621726', 19, 6, 5253450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1460, '浙江民泰商业银行', 'MTBANK', '民泰借记卡', '民泰借记卡', '621088', 19, 6, 5253450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1461, '浙江民泰商业银行', 'MTBANK', '金融IC卡C卡', '金融IC卡C卡', '620517', 19, 6, 5253450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1462, '浙江民泰商业银行', 'MTBANK', '银联标准普卡金卡', '银联标准普卡金卡', '622740', 16, 6, 5253454, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1463, '浙江民泰商业银行', 'MTBANK', '商惠通', '商惠通', '625036', 16, 6, 5253454, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1464, '上饶市商业银行', '(NULL)', '三清山卡', '三清山卡', '621014', 16, 6, 5264330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1465, '东营银行', '(NULL)', '胜利卡', '胜利卡', '621004', 18, 6, 5274550, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1466, '泰安市商业银行', 'TACCB', '岱宗卡', '岱宗卡', '622972', 19, 6, 5284630, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1467, '泰安市商业银行', 'TACCB', '市民一卡通', '市民一卡通', '623196', 19, 6, 5284630, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1468, '浙江稠州商业银行', 'CZCB', '义卡', '义卡', '621028', 16, 6, 5303380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1469, '浙江稠州商业银行', 'CZCB', '公务卡', '公务卡', '628250', 16, 6, 5303387, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1470, '乌海银行', '(NULL)', '狮城借记卡', '狮城借记卡', '622973', 19, 6, 5311930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1471, '自贡市商业银行', 'ZGCCB', '锦程卡', '锦程卡', '621070', 19, 6, 5326560, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1472, '龙江银行', 'DAQINGB', '万通卡', '万通卡', '622977', 19, 6, 5332740, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1473, '鄂尔多斯银行', 'ORBANK', '天骄卡', '天骄卡', '622978', 19, 6, 5342050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1474, '鄂尔多斯银行', 'ORBANK', '天骄公务卡', '天骄公务卡', '628253', 16, 6, 5342050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1475, '鹤壁银行', '(NULL)', '鹤卡', '鹤卡', '622979', 19, 6, 5354970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1476, '许昌市商业银行', '(NULL)', '连城卡', '连城卡', '621035', 19, 6, 5365030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1477, '济宁银行', 'JNBANK', '儒商卡', '儒商卡', '621200', 19, 6, 5374610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1478, '铁岭银行', '(NULL)', '龙凤卡', '龙凤卡', '621038', 19, 6, 5392330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1479, '乐山市商业银行', 'LSCCB', '大福卡', '大福卡', '621086', 19, 6, 5406650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1480, '长安银行', '(NULL)', '长长卡', '长长卡', '621296', 19, 6, 5417900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1481, '长安银行', '(NULL)', '借记IC卡', '借记IC卡', '621448', 19, 6, 5417900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1482, '宝鸡商行', '(NULL)', '姜炎卡', '姜炎卡', '621044', 19, 6, 5417930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1483, '重庆三峡银行', 'CCQTGB', '财富人生卡', '财富人生卡', '622945', 16, 6, 5426900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1484, '重庆三峡银行', 'CCQTGB', '借记卡', '借记卡', '621755', 16, 6, 5426900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1485, '石嘴山银行', 'SZSBK', '麒麟借记卡', '麒麟借记卡', '622940', 19, 6, 5438720, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1486, '石嘴山银行', 'SZSBK', '麒麟公务卡', '麒麟公务卡', '628355', 16, 6, 5438729, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1487, '盘锦市商业银行', '(NULL)', '鹤卡', '鹤卡', '621089', 19, 6, 5442320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1488, '昆仑银行', 'KLB', '瑞卡', '瑞卡', '621029', 19, 6, 5478820, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1489, '昆仑银行', 'KLB', '金融IC卡', '金融IC卡', '621766', 19, 6, 5478820, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1490, '平顶山银行', 'BOP', '佛泉卡', '佛泉卡', '621071', 19, 6, 5484950, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1491, '朝阳银行', 'BOCY', '鑫鑫通卡', '鑫鑫通卡', '621074', 19, 6, 5492340, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1492, '朝阳银行', 'BOCY', '红山卡', '红山卡', '623030', 19, 6, 5492341, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1493, '宁波东海银行', '(NULL)', '绿叶卡', '绿叶卡', '621345', 18, 6, 5503320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1494, '遂宁市商业银行', '(NULL)', '锦程卡', '锦程卡', '621090', 19, 6, 5516620, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1495, '保定银行', '(NULL)', '直隶卡', '直隶卡', '621091', 19, 6, 5521340, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1496, '邢台银行', 'XTB', '金牛卡', '金牛卡', '621238', 19, 6, 5541310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1497, '凉山州商业银行', '(NULL)', '锦程卡', '锦程卡', '621057', 19, 6, 5556840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1498, '漯河商行', '(NULL)', '福卡', '福卡', '621075', 19, 6, 5565040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1499, '漯河商行', '(NULL)', '福源卡', '福源卡', '623037', 19, 6, 5565040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1500, '达州市商业银行', '(NULL)', '锦程卡', '锦程卡', '621233', 19, 6, 5576750, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1501, '新乡市商业银行', '(NULL)', '新卡', '新卡', '621235', 18, 6, 5584980, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1502, '晋中市商业银行', 'JZBANK', '九州方圆借记卡', '九州方圆借记卡', '621223', 19, 6, 5591750, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1503, '晋中市商业银行', 'JZBANK', '九州方圆卡', '九州方圆卡', '621780', 19, 6, 5591750, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1504, '驻马店市商业银行', '(NULL)', '驿站卡', '驿站卡', '621221', 19, 6, 5605110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1505, '衡水市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621239', 19, 6, 5611480, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1506, '周口银行', 'BOZK', '如愿卡', '如愿卡', '621271', 19, 6, 5625080, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1507, '阳泉市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621272', 16, 6, 5631650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1508, '阳泉市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621738', 16, 6, 5631650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1509, '宜宾市商业银行', 'YBCCB', '锦程卡', '锦程卡', '621273', 19, 6, 5646710, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1510, '库尔勒市商业银行', 'KORLABANK', '孔雀胡杨卡', '孔雀胡杨卡', '621263', 18, 6, 5658880, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1511, '雅安市商业银行', '(NULL)', '锦城卡', '锦城卡', '621325', 19, 6, 5666770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1512, '商丘商行', '(NULL)', '百汇卡', '百汇卡', '621337', 19, 6, 5675060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1513, '安阳银行', 'AYCB', '安鼎卡', '安鼎卡', '621327', 19, 6, 5684960, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1514, '信阳银行', 'XYBANK', '信阳卡', '信阳卡', '621753', 19, 6, 5695150, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1515, '华融湘江银行', 'HRXJB', '华融卡', '华融卡', '621366', 19, 6, 5705500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1516, '华融湘江银行', 'HRXJB', '华融卡', '华融卡', '621388', 19, 6, 5705500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1517, '营口沿海银行', '(NULL)', '祥云借记卡', '祥云借记卡', '621348', 19, 6, 5722280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1518, '景德镇商业银行', '(NULL)', '瓷都卡', '瓷都卡', '621359', 18, 6, 5734220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1519, '湖北银行', 'HBC', '金牛卡', '金牛卡', '621217', 18, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1520, '湖北银行', 'HBC', '汉江卡', '汉江卡', '622959', 19, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1521, '湖北银行', 'HBC', '借记卡', '借记卡', '621270', 18, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1522, '湖北银行', 'HBC', '三峡卡', '三峡卡', '622396', 17, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1523, '湖北银行', 'HBC', '至尊卡', '至尊卡', '622511', 17, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1524, '湖北银行', 'HBC', '金融IC卡', '金融IC卡', '623076', 19, 6, 5755201, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1525, '西藏银行', '(NULL)', '借记IC卡', '借记IC卡', '621391', 19, 6, 5767700, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1526, '新疆汇和银行', '(NULL)', '汇和卡', '汇和卡', '621339', 19, 6, 5778981, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1527, '广东华兴银行', '(NULL)', '借记卡', '借记卡', '621469', 19, 6, 5785800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1528, '广东华兴银行', '(NULL)', '华兴银联公司卡', '华兴银联公司卡', '621625', 19, 6, 5785800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1529, '濮阳银行', '(NULL)', '龙翔卡', '龙翔卡', '621601', 19, 6, 5795020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1530, '宁波通商银行', '(NULL)', '借记卡', '借记卡', '621655', 19, 6, 5803320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1531, '甘肃银行', '(NULL)', '神舟兴陇借记卡', '神舟兴陇借记卡', '621636', 19, 6, 5818200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1532, '平安银行', 'SPABANK', '一账通借贷合一钻石卡', '一账通借贷合一钻石卡', '627069', 16, 6, 6105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1533, '平安银行', 'SPABANK', '一账通借贷合一白金卡', '一账通借贷合一白金卡', '627068', 16, 6, 6105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1534, '平安银行', 'SPABANK', '一账通借贷合一卡普卡', '一账通借贷合一卡普卡', '627066', 16, 6, 6105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1535, '平安银行', 'SPABANK', '一账通借贷合一卡金卡', '一账通借贷合一卡金卡', '627067', 16, 6, 6105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1536, '盛京银行', 'SJBANK', '医保卡', '医保卡', '622955', 16, 6, 804172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1537, '上海农商银行', '(NULL)', '如意卡(银联卡)', '如意卡(银联卡)', '622478', 16, 6, 14012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1538, '上海农商银行', '(NULL)', '如意卡(银联卡)', '如意卡(银联卡)', '940013', 16, 6, 14012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1539, '上海农商银行', '(NULL)', '鑫通卡', '鑫通卡', '621495', 16, 6, 14012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1540, '昆山农信社', '(NULL)', '江通卡(银联卡)', '江通卡(银联卡)', '622443', 19, 6, 14023052, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1541, '昆山农信社', '(NULL)', '银联汇通卡(银联卡)', '银联汇通卡(银联卡)', '940029', 19, 6, 14023052, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1542, '常熟市农村商业银行', '(NULL)', '粒金贷记卡(银联卡)', '粒金贷记卡(银联卡)', '622462', 16, 6, 14030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1543, '常熟市农村商业银行', '(NULL)', '公务卡', '公务卡', '628272', 16, 6, 14030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1544, '常熟市农村商业银行', '(NULL)', '粒金准贷卡', '粒金准贷卡', '625101', 16, 6, 14030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1545, '常熟农村商业银行', 'CSRCB', '粒金卡', '粒金卡', '603694', 19, 6, 14033055, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1546, '常熟农村商业银行', 'CSRCB', '粒金借记卡(银联卡)', '粒金借记卡(银联卡)', '622323', 19, 6, 14033055, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1547, '常熟农村商业银行', 'CSRCB', '粒金卡(银联卡)', '粒金卡(银联卡)', '9400301', 19, 7, 14033055, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1548, '深圳农村商业银行', 'SRCB', '信通卡(银联卡)', '信通卡(银联卡)', '622128', 16, 6, 14045840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1549, '深圳农村商业银行', 'SRCB', '信通商务卡(银联卡)', '信通商务卡(银联卡)', '622129', 16, 6, 14045840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1550, '深圳农村商业银行', 'SRCB', '信通卡', '信通卡', '623035', 16, 6, 14045840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1551, '广州农村商业银行', '(NULL)', '麒麟卡', '麒麟卡', '909810', 18, 6, 14055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1552, '广州农村商业银行', '(NULL)', '麒麟卡(银联卡)', '麒麟卡(银联卡)', '940035', 18, 6, 14055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1553, '广州农村商业银行', '(NULL)', '福农太阳卡', '福农太阳卡', '621522', 18, 6, 14055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1554, '广州农村商业银行', '(NULL)', '麒麟储蓄卡', '麒麟储蓄卡', '622439', 18, 6, 14055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1555, '广东南海农村商业银行', '(NULL)', '盛通卡', '盛通卡', '622271', 18, 6, 14075882, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1556, '广东南海农村商业银行', '(NULL)', '盛通卡(银联卡)', '盛通卡(银联卡)', '940037', 18, 6, 14075882, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1557, '佛山顺德农村商业银行', '(NULL)', '恒通卡(银联卡)', '恒通卡(银联卡)', '940038', 16, 6, 14085883, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1558, '佛山顺德农村商业银行', '(NULL)', '恒通卡', '恒通卡', '985262', 16, 6, 14085883, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1559, '佛山顺德农村商业银行', '(NULL)', '恒通卡(银联卡)', '恒通卡(银联卡)', '622322', 16, 6, 14085883, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1560, '昆明农联社', '(NULL)', '金碧卡', '金碧卡', '0', 18, 6, 14097310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1561, '昆明农联社', '(NULL)', '金碧白金卡', '金碧白金卡', '621017', 19, 6, 14097310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1562, '昆明农联社', '(NULL)', '金碧一卡通(银联卡)', '金碧一卡通(银联卡)', '622369', 16, 6, 14097310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1563, '昆明农联社', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940042', 18, 6, 14097310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1564, '湖北农信社', '(NULL)', '信通卡', '信通卡', '622412', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1565, '湖北农信', '(NULL)', '福农小康卡', '福农小康卡', '621523', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1566, '湖北农信社', '(NULL)', '福卡IC借记卡', '福卡IC借记卡', '623055', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1567, '湖北农信社', '(NULL)', '福卡(VIP卡)', '福卡(VIP卡)', '621013', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1568, '武汉农信', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '940044', 17, 6, 14105210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1569, '徐州市郊农村信用合作联社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622312', 16, 6, 14113030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1570, '江阴农村商业银行', '(NULL)', '暨阳公务卡', '暨阳公务卡', '628381', 16, 6, 14123020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1571, '江阴市农村商业银行', '(NULL)', '合作贷记卡(银联卡)', '合作贷记卡(银联卡)', '622481', 16, 6, 14123020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1572, '江阴农村商业银行', '(NULL)', '合作借记卡', '合作借记卡', '622341', 16, 6, 14123022, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1573, '江阴农村商业银行', '(NULL)', '合作卡(银联卡)', '合作卡(银联卡)', '940058', 16, 6, 14123022, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1574, '重庆农村商业银行股份有限公司', '(NULL)', '信合平安卡', '信合平安卡', '622867', 16, 6, 14136530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1575, '重庆农村商业银行股份有限公司', '(NULL)', '信合希望卡', '信合希望卡', '622885', 16, 6, 14136530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1576, '重庆农村商业银行股份有限公司', '(NULL)', '信合一卡通(银联卡)', '信合一卡通(银联卡)', '940020', 16, 6, 14136530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1577, '重庆农村商业银行', 'CRCBANK', '江渝借记卡VIP卡', '江渝借记卡VIP卡', '621258', 16, 6, 14136900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1578, '重庆农村商业银行', 'CRCBANK', '江渝乡情福农卡', '江渝乡情福农卡', '621528', 16, 6, 14136900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1579, '重庆农村商业银行', 'CRCBANK', '江渝IC借记卡', '江渝IC借记卡', '621465', 16, 6, 14136900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1580, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '900105', 16, 6, 14144500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1581, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '900205', 16, 6, 14144500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1582, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '622319', 16, 6, 14144500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1583, '山东省农村信用社联合社', '(NULL)', '泰山福农卡', '泰山福农卡', '621521', 16, 6, 14144500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1584, '山东省农村信用社联合社', '(NULL)', 'VIP卡', 'VIP卡', '621690', 16, 6, 14144500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1585, '青岛农信', '(NULL)', '信通卡', '信通卡', '62231902', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1586, '青岛农信', '(NULL)', '信通卡', '信通卡', '90010502', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1587, '青岛农信', '(NULL)', '信通卡', '信通卡', '90020502', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1588, '东莞农村商业银行', 'DRCBCL', '信通卡(银联卡)', '信通卡(银联卡)', '622328', 19, 6, 14156020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1589, '东莞农村商业银行', 'DRCBCL', '信通卡(银联卡)', '信通卡(银联卡)', '940062', 19, 6, 14156020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1590, '东莞农村商业银行', 'DRCBCL', '信通信用卡', '信通信用卡', '625288', 16, 6, 14156020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1591, '东莞农村商业银行', 'DRCBCL', '信通借记卡', '信通借记卡', '623038', 19, 6, 14156020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1592, '东莞农村商业银行', 'DRCBCL', '贷记IC卡', '贷记IC卡', '625888', 16, 6, 14156020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1593, '张家港农村商业银行', 'ZRCBANK', '一卡通(银联卡)', '一卡通(银联卡)', '622332', 17, 6, 14163056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1594, '张家港农村商业银行', 'ZRCBANK', '一卡通(银联卡)', '一卡通(银联卡)', '940063', 17, 6, 14163056, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1595, '福建省农村信用社联合社', '(NULL)', '万通(借记)卡', '万通(借记)卡', '622127', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1596, '福建省农村信用社联合社', '(NULL)', '万通(借记)卡', '万通(借记)卡', '622184', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1597, '福建省农村信用社联合社', '(NULL)', '社保卡', '社保卡', '621701', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1598, '福建省农村信用社联合社', '(NULL)', '福建海峡旅游卡', '福建海峡旅游卡', '621251', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1599, '福建省农村信用社联合社', '(NULL)', '福万通福农卡', '福万通福农卡', '621589', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1600, '福建省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623036', 19, 6, 14173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1601, '北京农村商业银行', 'BJRCB', '中国旅行卡', '中国旅行卡', '620088', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1602, '北京农村商业银行', 'BJRCB', '惠通卡', '惠通卡', '621068', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1603, '北京农村商业银行', 'BJRCB', '信通卡', '信通卡', '622138', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1604, '北京农村商业银行', 'BJRCB', '惠通卡', '惠通卡', '621066', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1605, '北京农村商业银行', 'BJRCB', '凤凰福农卡', '凤凰福农卡', '621560', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1606, '北京农商行', '(NULL)', '凤凰福农卡', '凤凰福农卡', '625526', 16, 6, 14181001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1607, '北京农商行', '(NULL)', '凤凰标准卡', '凤凰标准卡', '625186', 16, 6, 14181001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1608, '北京农商行', '(NULL)', '凤凰公务卡', '凤凰公务卡', '628336', 16, 6, 14181001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1609, '天津农村商业银行', '(NULL)', '吉祥商联IC卡', '吉祥商联IC卡', '622531', 19, 6, 14191100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1610, '天津农村商业银行', '(NULL)', '信通借记卡(银联卡)', '信通借记卡(银联卡)', '622329', 19, 6, 14191100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1611, '鄞州农村合作银行', '(NULL)', '蜜蜂借记卡(银联卡)', '蜜蜂借记卡(银联卡)', '622339', 16, 6, 14203320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1612, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂电子钱包(IC)', '蜜蜂电子钱包(IC)', '620500', 16, 6, 14203323, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1613, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂IC借记卡', '蜜蜂IC借记卡', '621024', 16, 6, 14203323, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1614, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂贷记IC卡', '蜜蜂贷记IC卡', '622289', 16, 6, 14203323, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1615, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂贷记卡', '蜜蜂贷记卡', '622389', 16, 6, 14203323, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1616, '宁波鄞州农村合作银行', '(NULL)', '公务卡', '公务卡', '628300', 16, 6, 14203323, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1617, '佛山市三水区农村信用合作社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622343', 19, 6, 14215881, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1618, '成都农村商业银行', '(NULL)', '福农卡', '福农卡', '625516', 16, 6, 14226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1619, '成都农村商业银行', '(NULL)', '福农卡', '福农卡', '621516', 19, 6, 14226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1620, '成都农村商业银行', '(NULL)', '天府借记卡(银联卡)', '天府借记卡(银联卡)', '622345', 19, 6, 14226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1621, '江苏农信社', '(NULL)', '圆鼎卡(银联卡)', '圆鼎卡(银联卡)', '622452', 19, 6, 14243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1622, '江苏农信社', '(NULL)', '圆鼎卡(银联卡)', '圆鼎卡(银联卡)', '622324', 19, 6, 14243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1623, '江苏省农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621578', 19, 6, 14243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1624, '江苏省农村信用社联合社', '(NULL)', '圆鼎借记IC卡', '圆鼎借记IC卡', '623066', 19, 6, 14243001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1625, '江门新会农村商业银行', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622470', 19, 6, 14255890, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1626, '吴江农商行', '(NULL)', '垂虹贷记卡', '垂虹贷记卡', '622648', 16, 6, 14283054, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1627, '吴江农商行', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628248', 16, 6, 14283054, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1628, '吴江农商行', '(NULL)', '垂虹卡(银联卡)', '垂虹卡(银联卡)', '622488', 16, 6, 14283054, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1629, '浙江省农村信用社联合社', 'ZJNX', '丰收贷记卡', '丰收贷记卡', '622288', 16, 6, 14293300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1630, '浙江省农村信用社联合社', 'ZJNX', '丰收卡(银联卡)', '丰收卡(银联卡)', '622858', 19, 6, 14293300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1631, '浙江省农村信用社联合社', 'ZJNX', '丰收小额贷款卡', '丰收小额贷款卡', '621058', 19, 6, 14293300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1632, '浙江省农村信用社联合社', 'ZJNX', '丰收福农卡', '丰收福农卡', '621527', 19, 6, 14293300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1633, '浙江省农村信用社联合社', 'ZJNX', '银联标准公务卡', '银联标准公务卡', '628280', 16, 6, 14293300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1634, '苏州银行', 'BOSZ', '新苏卡(银联卡)', '新苏卡(银联卡)', '622855', 19, 6, 14303050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1635, '苏州银行', 'BOSZ', '新苏卡', '新苏卡', '621461', 19, 6, 14303050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1636, '珠海农村商业银行', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622859', 19, 6, 14315850, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1637, '太仓农村商业银行', '(NULL)', '郑和卡(银联卡)', '郑和卡(银联卡)', '622869', 19, 6, 14333051, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1638, '尧都区农村信用合作社联社', '(NULL)', '天河卡', '天河卡', '622882', 19, 6, 14341770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1639, '贵州省农村信用社联合社', '(NULL)', '信合卡', '信合卡', '622893', 19, 6, 14367000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1640, '贵州省农村信用社联合社', '(NULL)', '信合福农卡', '信合福农卡', '621590', 19, 6, 14367000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1641, '无锡农村商业银行', 'WRCB', '金阿福', '金阿福', '622895', 16, 6, 14373020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1642, '湖南省农村信用社联合社', '(NULL)', '福祥借记卡', '福祥借记卡', '622169', 19, 6, 14385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1643, '湖南省农村信用社联合社', '(NULL)', '福祥卡', '福祥卡', '621519', 19, 6, 14385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1644, '湖南省农村信用社联合社', '(NULL)', '福祥卡', '福祥卡', '621539', 19, 6, 14385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1645, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622681', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1646, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622682', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1647, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622683', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1648, '河南省农村信用社联合社', '(NULL)', '金燕卡', '金燕卡', '622991', 18, 6, 14404900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1649, '河南省农村信用社联合社', '(NULL)', '金燕快货通福农卡', '金燕快货通福农卡', '621585', 18, 6, 14404900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1650, '河南省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623013', 18, 6, 14404900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1651, '河北省农村信用社联合社', '(NULL)', '信通卡', '信通卡', '621021', 19, 6, 14411200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1652, '河北省农村信用社联合社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622358', 19, 6, 14411200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1653, '河北省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623025', 19, 6, 14411200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1654, '陕西省农村信用社联合社', '(NULL)', '陕西信合富秦卡', '陕西信合富秦卡', '622506', 19, 6, 14427900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1655, '陕西省农村信用社联合社', '(NULL)', '富秦家乐福农卡', '富秦家乐福农卡', '621566', 19, 6, 14427900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1656, '陕西省农村信用社联合社', '(NULL)', '富秦卡', '富秦卡', '623027', 19, 6, 14427900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1657, '陕西省农村信用社联合社', '(NULL)', '社会保障卡（陕西信合）', '社会保障卡（陕西信合）', '623028', 19, 6, 14427900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1658, '广西农村信用社联合社', '(NULL)', '桂盛卡', '桂盛卡', '622992', 19, 6, 14436100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1659, '新疆维吾尔自治区农村信用社联合', '(NULL)', '玉卡', '玉卡', '621008', 16, 6, 14448800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1660, '新疆农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621525', 16, 6, 14448800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1661, '吉林农信联合社', '(NULL)', '吉卡', '吉卡', '622935', 19, 6, 14452400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1662, '吉林农信联合社', '(NULL)', '吉林农信银联标准吉卡福农借记卡', '吉林农信银联标准吉卡福农借记卡', '621531', 19, 6, 14452400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1663, '吉林省农村信用社联合社', '(NULL)', '借记IC卡', '借记IC卡', '623181', 19, 6, 14452402, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1664, '黄河农村商业银行', '(NULL)', '黄河卡', '黄河卡', '622947', 19, 6, 14468700, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1665, '黄河农村商业银行', '(NULL)', '黄河富农卡福农卡', '黄河富农卡福农卡', '621561', 19, 6, 14468700, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1666, '安徽省农村信用社联合社', '(NULL)', '金农卡', '金农卡', '622953', 19, 6, 14473600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1667, '安徽省农村信用社联合社', '(NULL)', '金农易贷福农卡', '金农易贷福农卡', '621526', 19, 6, 14473600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1668, '海南省农村信用社联合社', '(NULL)', '大海卡', '大海卡', '621036', 19, 6, 14486400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1669, '海南省农村信用社联合社', '(NULL)', '大海福农卡', '大海福农卡', '621536', 19, 6, 14486400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1670, '海南省农村信用社联合社', '(NULL)', '金融IC借记卡', '金融IC借记卡', '621458', 19, 6, 14486400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1671, '青海省农村信用社联合社', '(NULL)', '紫丁香借记卡', '紫丁香借记卡', '621065', 16, 6, 14498500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1672, '青海省农村信用社联合社', '(NULL)', '紫丁香福农卡', '紫丁香福农卡', '621517', 16, 6, 14498500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1673, '青海省农村信用社联合社', '(NULL)', '青海省公务卡', '青海省公务卡', '628289', 16, 6, 14498501, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1674, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622477', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1675, '广东省农村信用社联合社', 'GDRCC', '信通白金卡', '信通白金卡', '622509', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1676, '广东省农村信用社联合社', 'GDRCC', '信通金卡', '信通金卡', '622510', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1677, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622302', 16, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1678, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622362', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1679, '广东省农村信用社联合社', 'GDRCC', '珠江平安卡', '珠江平安卡', '621018', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1680, '广东省农村信用社联合社', 'GDRCC', '珠江平安福农卡', '珠江平安福农卡', '621518', 19, 6, 14505800, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1681, '内蒙古自治区农村信用社联合式', '(NULL)', '信合金牛卡', '信合金牛卡', '622976', 19, 6, 14511900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1682, '内蒙古自治区农村信用社联合式', '(NULL)', '金牛福农卡', '金牛福农卡', '621533', 19, 6, 14511900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1683, '内蒙古自治区农村信用社联合式', '(NULL)', '白金卡', '白金卡', '621362', 19, 6, 14511900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1684, '四川省农村信用社联合社', '(NULL)', '蜀信卡', '蜀信卡', '621033', 19, 6, 14526500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1685, '四川省农村信用社联合社', '(NULL)', '蜀信贵宾卡', '蜀信贵宾卡', '621099', 19, 6, 14526500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1686, '四川省农村信用社联合社', '(NULL)', '蜀信福农卡', '蜀信福农卡', '621530', 19, 6, 14526500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1687, '四川省农村信用社联合社', '(NULL)', '蜀信卡', '蜀信卡', '621457', 19, 6, 14526500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1688, '四川省农村信用社联合社', '(NULL)', '蜀信社保卡', '蜀信社保卡', '621459', 19, 6, 14526500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1689, '甘肃省农村信用社联合社', '(NULL)', '飞天卡', '飞天卡', '621061', 19, 6, 14538200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1690, '甘肃省农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621520', 19, 6, 14538200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1691, '辽宁省农村信用社联合社', '(NULL)', '金信卡', '金信卡', '621449', 19, 6, 14540001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1692, '辽宁省农村信用社联合社', '(NULL)', '金信卡', '金信卡', '621026', 19, 6, 14542200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1693, '山西省农村信用社联合社', '(NULL)', '关帝银行卡', '关帝银行卡', '622968', 19, 6, 14551600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1694, '山西省农村信用社', '(NULL)', '信合通', '信合通', '621280', 19, 6, 14551600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1695, '山西省农村信用社联合社', '(NULL)', '信合通', '信合通', '621580', 19, 6, 14551600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1696, '天津滨海农村商业银行', '(NULL)', '四海通卡', '四海通卡', '621073', 19, 6, 14561100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1697, '黑龙江省农村信用社联合社', '(NULL)', '鹤卡', '鹤卡', '621228', 19, 6, 14572600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1698, '黑龙江省农村信用社联合社', '(NULL)', '丰收时贷福农卡', '丰收时贷福农卡', '621557', 19, 6, 14572600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1699, '武汉农村商业银行', 'WHRCB', '汉卡', '汉卡', '621361', 19, 6, 14595210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1700, '武汉农村商业银行', 'WHRCB', '汉卡', '汉卡', '623033', 19, 6, 14595210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1701, '江南农村商业银行', '(NULL)', '阳湖卡(银联卡)', '阳湖卡(银联卡)', '622891', 19, 6, 14603040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1702, '江南农村商业银行', '(NULL)', '天天红火卡', '天天红火卡', '621363', 19, 6, 14603040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1703, '大邑交银兴民村镇银行', '(NULL)', '借记卡', '借记卡', '621056801', 19, 9, 15006518, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1704, '湖北嘉鱼吴江村镇银行', '(NULL)', '垂虹卡', '垂虹卡', '622995', 16, 6, 15015363, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1705, '青岛即墨北农商村镇银行', '(NULL)', '凤凰卡', '凤凰卡', '6229756114', 19, 10, 15024521, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1706, '湖北仙桃北农商村镇银行', '(NULL)', '凤凰卡', '凤凰卡', '6229756115', 19, 10, 15025371, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1707, '句容茅山村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105913', 16, 8, 15033142, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1708, '兴化苏南村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105916', 16, 8, 15033161, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1709, '海口苏南村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105915', 16, 8, 15036410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1710, '双流诚民村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105901', 16, 8, 15036512, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1711, '宣汉诚民村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105900', 16, 8, 15036753, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1712, '福建建瓯石狮村镇银行', '(NULL)', '玉竹卡', '玉竹卡', '621053', 19, 6, 15044015, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1713, '恩施常农商村镇银行', '(NULL)', '恩施村镇银行借记卡', '恩施村镇银行借记卡', '621260002', 19, 9, 15055411, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1714, '咸丰常农商村镇银行', '(NULL)', '借记卡', '借记卡', '621260001', 19, 9, 15055416, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1715, '浙江乐清联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092003', 19, 9, 15083333, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1716, '浙江嘉善联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092002', 19, 9, 15083351, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1717, '浙江长兴联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092001', 19, 9, 15083362, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1718, '浙江平湖工银村镇银行', '(NULL)', '金平卡', '金平卡', '621230', 19, 6, 15103352, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1719, '重庆璧山工银村镇银行', '(NULL)', '翡翠卡', '翡翠卡', '621229', 19, 6, 15106919, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1720, '北京密云汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250004', 16, 9, 15111027, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1721, '福建永安汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250003', 16, 9, 15113961, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1722, '湖北随州曾都汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250001', 16, 9, 15115270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1723, '广东恩平汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250005', 16, 9, 15115893, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1724, '重庆大足汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250002', 16, 9, 15116917, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1725, '江苏沭阳东吴村镇银行', '(NULL)', '新苏借记卡', '新苏借记卡', '621241001', 19, 9, 15123181, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1726, '重庆农村商业银行', 'CRCBANK', '银联标准贷记卡', '银联标准贷记卡', '622218', 16, 6, 15136900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1727, '重庆农村商业银行', 'CRCBANK', '公务卡', '公务卡', '628267', 16, 6, 15136900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1728, '鄂尔多斯市东胜蒙银村镇银行', '(NULL)', '龙源腾借记卡', '龙源腾借记卡', '621346003', 19, 9, 15142050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1729, '方大村镇银行', '(NULL)', '胡杨卡神州卡', '胡杨卡神州卡', '621346002', 19, 9, 15142080, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1730, '深圳龙岗鼎业村镇银行', '(NULL)', '鼎业卡', '鼎业卡', '621346001', 19, 9, 15145840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1731, '中山小榄村镇银行', 'XLBANK', '菊卡', '菊卡', '621326763', 18, 9, 15156030, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1732, '江苏邗江民泰村镇银行', '(NULL)', '金荷花借记卡', '金荷花借记卡', '621338001', 19, 9, 15173120, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1733, '天津静海新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353008', 19, 9, 15181123, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1734, '安徽当涂新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353002', 19, 9, 15183651, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1735, '安徽当涂新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353102', 19, 9, 15183651, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1736, '安徽和县新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353005', 19, 9, 15183653, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1737, '望江新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353007', 19, 9, 15183687, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1738, '郎溪新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353003', 19, 9, 15183772, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1739, '郎溪新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353103', 19, 9, 15183772, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1740, '广州番禹新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353001', 19, 9, 15185810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1741, '广州番禹新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353101', 19, 9, 15185810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1742, '沂水中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356004', 19, 9, 15194737, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1743, '谷城中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356007', 19, 9, 15195284, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1744, '老河口中银富登村镇银行', '(NULL)', '中银富登村镇银行借记卡', '中银富登村镇银行借记卡', '621356009', 19, 9, 15195287, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1745, '枣阳中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356008', 19, 9, 15195288, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1746, '京山中银富登村镇银行', '(NULL)', '京山富登借记卡', '京山富登借记卡', '621356002', 19, 9, 15195321, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1747, '蕲春中银富登村镇银行', '(NULL)', '中银富登借记卡', '中银富登借记卡', '621356001', 19, 9, 15195338, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1748, '潜江中银富登村镇银行', '(NULL)', '中银富登村镇银行借记卡', '中银富登村镇银行借记卡', '621356010', 19, 9, 15195375, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1749, '北京顺义银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347002', 16, 9, 15201000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1750, '浙江三门银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347005', 16, 9, 15203457, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1751, '江西赣州银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347003', 16, 9, 15204280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1752, '深圳福田银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347001', 16, 9, 15205840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1753, '重庆渝北银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347006', 16, 9, 15206900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1754, '重庆黔江银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347007', 16, 9, 15206925, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1755, '北京怀柔融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350010', 19, 9, 15211000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1756, '榆树融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350431', 19, 9, 15212411, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1757, '拜泉融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350013', 19, 9, 15212652, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1758, '新安融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350004', 19, 9, 15214933, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1759, '深圳宝安融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350755', 19, 9, 15215840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1760, '海南保亭融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350017', 19, 9, 15216437, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1761, '会宁会师村镇银行', '(NULL)', '会师普惠卡', '会师普惠卡', '621350943', 19, 9, 15218242, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1762, '南阳村镇银行', '(NULL)', '玉都卡', '玉都卡', '621392', 18, 6, 15265130, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1763, '上海松江民生村镇银行', '(NULL)', '借记卡', '借记卡', '621399001', 16, 9, 15272900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1764, '浙江建德湖商村镇银行', '(NULL)', '湖商卡', '湖商卡', '621365001', 19, 9, 15283315, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1765, '晋中市榆次融信村镇银行', '(NULL)', '魏榆卡', '魏榆卡', '621481', 19, 6, 15301750, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1766, '宝生村镇银行', '(NULL)', '宝生村镇银行一卡通', '宝生村镇银行一卡通', '621623001', 19, 9, 15335840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1767, '广州花都稠州村镇银行', '(NULL)', '义卡借记卡', '义卡借记卡', '621627007', 19, 9, 15355810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1768, '文昌国民村镇银行', '(NULL)', '赀业卡', '赀业卡', '621650002', 19, 9, 15386423, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1769, '琼海国民村镇银行', '(NULL)', '椰卡', '椰卡', '621650001', 19, 9, 15386424, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1770, '鹤山珠江村镇银行', '(NULL)', '珠江太阳卡', '珠江太阳卡', '62163101', 18, 8, 15405895, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1771, '大武口石银村镇银行', '(NULL)', '麒麟借记卡', '麒麟借记卡', '621653006', 19, 9, 15438720, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1772, '广元贵商村镇银行', '(NULL)', '利卡', '利卡', '62308299', 19, 8, 15446610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1773, '昆山鹿城村镇银行', '(NULL)', '鹿城卡', '鹿城卡', '621355001', 16, 9, 15483052, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1774, '东营莱商村镇银行', '(NULL)', '绿洲卡', '绿洲卡', '621396', 16, 6, 15514550, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1775, '湖北咸安武农商村镇银行', '(NULL)', '汉卡', '汉卡', '621676001', 19, 9, 15535360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1776, '湖北赤壁武弄商村镇银行', '(NULL)', '汉卡', '汉卡', '621676002', 19, 9, 15535367, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1777, '广州增城长江村镇银行', '(NULL)', '汉卡', '汉卡', '621676003', 19, 9, 15535813, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1778, '济南长清沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682101', 19, 9, 15584510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1779, '济南槐荫沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682102', 19, 9, 15584513, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1780, '泰安沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682106', 19, 9, 15584630, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1781, '宁阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682103', 19, 9, 15584631, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1782, '东平沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682105', 19, 9, 15584633, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1783, '聊城东昌沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682110', 19, 9, 15584710, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1784, '临清沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682111', 19, 9, 15584712, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1785, '阳谷沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682109', 19, 9, 15584713, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1786, '茌平沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682108', 19, 9, 15584715, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1787, '日照沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682107', 19, 9, 15584770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1788, '长沙星沙沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682202', 19, 9, 15585511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1789, '宁乡沪农商行村镇银行', '(NULL)', '借记卡', '借记卡', '621682201', 19, 9, 15585514, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1790, '醴陵沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682203', 19, 9, 15585525, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1791, '衡阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682205', 19, 9, 15585541, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1792, '澧县沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682209', 19, 9, 15585583, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1793, '临澧沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682208', 19, 9, 15585584, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1794, '石门沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682210', 19, 9, 15585586, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1795, '慈利沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682213', 19, 9, 15585591, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1796, '涟源沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682211', 19, 9, 15585623, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1797, '双峰沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682212', 19, 9, 15585624, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1798, '桂阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682207', 19, 9, 15585634, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1799, '永兴沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682206', 19, 9, 15585635, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1800, '嵩明沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682302', 19, 9, 15587317, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1801, '东亚银行', 'HKBEA', 'cup credit card', 'cup credit card', '622372', 16, 6, 25020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1802, '东亚银行', 'HKBEA', '电子网络人民币卡', '电子网络人民币卡', '622365', 17, 6, 25020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1803, '东亚银行', 'HKBEA', '人民币信用卡(银联卡)', '人民币信用卡(银联卡)', '622471', 16, 6, 25020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1804, '东亚银行', 'HKBEA', '银联借记卡', '银联借记卡', '622943', 19, 6, 25020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1805, '东亚银行', 'HKBEA', '人民币信用卡金卡', '人民币信用卡金卡', '622472', 16, 6, 25020344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1806, '东亚银行', 'HKBEA', '银联借记卡', '银联借记卡', '621411', 19, 6, 25020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1807, '花旗银行', '(NULL)', '花旗人民币信用卡', '花旗人民币信用卡', '622371', 16, 6, 25030344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1808, '花旗银行', '(NULL)', '双币卡', '双币卡', '625091', 16, 6, 25030344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1809, '大新银行', '(NULL)', '信用卡(普通卡)', '信用卡(普通卡)', '622293', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1810, '大新银行', '(NULL)', '商务信用卡', '商务信用卡', '622295', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1811, '大新银行', '(NULL)', '商务信用卡', '商务信用卡', '622296', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1812, '大新银行', '(NULL)', '预付卡(普通卡)', '预付卡(普通卡)', '622297', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1813, '大新银行', '(NULL)', '人民币信用卡', '人民币信用卡', '622373', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1814, '大新银行', '(NULL)', '人民币借记卡(银联卡)', '人民币借记卡(银联卡)', '622375', 17, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1815, '大新银行', '(NULL)', '大新人民币信用卡金卡', '大新人民币信用卡金卡', '622451', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1816, '大新银行', '(NULL)', '大新港币信用卡(金卡)', '大新港币信用卡(金卡)', '622294', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1817, '大新银行', '(NULL)', '贷记卡', '贷记卡', '625940', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1818, '大新银行', '(NULL)', '借记卡(银联卡)', '借记卡(银联卡)', '622489', 17, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1819, '永亨银行', '(NULL)', '永亨尊贵理财卡', '永亨尊贵理财卡', '622871', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1820, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622958', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1821, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622963', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1822, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622957', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1823, '永亨银行', '(NULL)', '港币贷记卡', '港币贷记卡', '622798', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1824, '永亨银行', '(NULL)', '永亨银联白金卡', '永亨银联白金卡', '625010', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1825, '建设银行', 'CCB', '人民币信用卡', '人民币信用卡', '622381', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1826, '建设银行', 'CCB', '银联卡', '银联卡', '622675', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1827, '建设银行', 'CCB', '银联卡', '银联卡', '622676', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1828, '建设银行', 'CCB', '银联卡', '银联卡', '622677', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1829, '建设银行', 'CCB', '人民币卡(银联卡)', '人民币卡(银联卡)', '622382', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1830, '建设银行', 'CCB', '借记卡', '借记卡', '621487', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1831, '建设银行', 'CCB', '建行陆港通龙卡', '建行陆港通龙卡', '621083', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1832, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622487', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1833, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622487', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1834, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622490', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1835, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622490', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1836, '星展银行', '(NULL)', '银联银行卡', '银联银行卡', '622491', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1837, '星展银行', '(NULL)', '银联港币银行卡', '银联港币银行卡', '622491', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1838, '星展银行', '(NULL)', '银联银行卡', '银联银行卡', '622492', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1839, '星展银行', '(NULL)', '银联港币银行卡', '银联港币银行卡', '622492', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1840, '星展银行', '(NULL)', '借记卡', '借记卡', '621744', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1841, '星展银行', '(NULL)', '借记卡', '借记卡', '621745', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1842, '星展银行', '(NULL)', '借记卡', '借记卡', '621746', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1843, '星展银行', '(NULL)', '借记卡', '借记卡', '621747', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1844, '上海商业银行', '(NULL)', '上银卡', '上银卡', '621034', 16, 6, 25090344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1845, '上海商业银行', '(NULL)', '人民币信用卡(银联卡)', '人民币信用卡(银联卡)', '622386', 16, 6, 25090344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1846, '上海商业银行', '(NULL)', '上银卡ShacomCard', '上银卡ShacomCard', '622952', 16, 6, 25090344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1847, '永隆银行', '(NULL)', '永隆人民币信用卡', '永隆人民币信用卡', '622387', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1848, '永隆银行', '(NULL)', '永隆人民币信用卡', '永隆人民币信用卡', '622423', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1849, '永隆银行', '(NULL)', '永隆港币卡', '永隆港币卡', '622971', 17, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1850, '永隆银行', '(NULL)', '永隆人民币卡', '永隆人民币卡', '622970', 17, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1851, '永隆银行', '(NULL)', '永隆双币卡', '永隆双币卡', '625062', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1852, '永隆银行', '(NULL)', '永隆双币卡', '永隆双币卡', '625063', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1853, '香港上海汇丰银行', '(NULL)', '人民币卡(银联卡)', '人民币卡(银联卡)', '622360', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1854, '香港上海汇丰银行', '(NULL)', '人民币金卡(银联卡)', '人民币金卡(银联卡)', '622361', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1855, '香港上海汇丰银行', '(NULL)', '银联卡', '银联卡', '625034', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1856, '香港上海汇丰银行', '(NULL)', '汇丰银联双币卡', '汇丰银联双币卡', '625096', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1857, '香港上海汇丰银行', '(NULL)', '汇丰银联双币钻石卡', '汇丰银联双币钻石卡', '625098', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1858, '香港上海汇丰银行', '(NULL)', 'ATMCard', 'ATMCard', '622406', 17, 6, 25130344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1859, '香港上海汇丰银行', '(NULL)', 'ATMCard', 'ATMCard', '622407', 19, 6, 25130344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1860, '香港上海汇丰银行', '(NULL)', '借记卡', '借记卡', '621442', 17, 6, 25130344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1861, '香港上海汇丰银行', '(NULL)', '借记卡', '借记卡', '621443', 19, 6, 25130344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1862, '恒生银行', '(NULL)', '港币贷记白金卡', '港币贷记白金卡', '625026', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1863, '恒生银行', '(NULL)', '港币贷记普卡', '港币贷记普卡', '625024', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1864, '恒生银行', '(NULL)', '恒生人民币信用卡', '恒生人民币信用卡', '622376', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1865, '恒生银行', '(NULL)', '恒生人民币白金卡', '恒生人民币白金卡', '622378', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1866, '恒生银行', '(NULL)', '恒生人民币金卡', '恒生人民币金卡', '622377', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1867, '恒生银行', '(NULL)', '银联人民币钻石商务卡', '银联人民币钻石商务卡', '625092', 16, 6, 25140344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1868, '恒生银行', '(NULL)', '恒生银行港卡借记卡', '恒生银行港卡借记卡', '622409', 19, 6, 25150344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1869, '恒生银行', '(NULL)', '恒生银行港卡借记卡', '恒生银行港卡借记卡', '622410', 17, 6, 25150344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1870, '恒生银行', '(NULL)', '港币借记卡（普卡）', '港币借记卡（普卡）', '621440', 17, 6, 25150344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1871, '恒生银行', '(NULL)', '港币借记卡（金卡）', '港币借记卡（金卡）', '621441', 19, 6, 25150344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1872, '中信嘉华银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622453', 16, 6, 25160344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1873, '中信嘉华银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622456', 16, 6, 25160344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1874, '中信嘉华银行', '(NULL)', '人民币借记卡(银联卡)', '人民币借记卡(银联卡)', '622459', 17, 6, 25160344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1875, '创兴银行', '(NULL)', '银联贺礼卡(创兴银行)', '银联贺礼卡(创兴银行)', '622272', 16, 6, 25170344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1876, '创兴银行', '(NULL)', '港币借记卡', '港币借记卡', '622463', 19, 6, 25170344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1877, '创兴银行', '(NULL)', '人民币提款卡', '人民币提款卡', '621087', 19, 6, 25170344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1878, '创兴银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625008', 16, 6, 25170344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1879, '创兴银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625009', 16, 6, 25170344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1880, '中银信用卡(国际)有限公司', 'BOC', '商务金卡', '商务金卡', '625055', 16, 6, 25180344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1881, '中银信用卡(国际)有限公司', 'BOC', '中银银联双币信用卡', '中银银联双币信用卡', '625040', 16, 6, 25180344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1882, '中银信用卡(国际)有限公司', 'BOC', '中银银联双币信用卡', '中银银联双币信用卡', '625042', 16, 6, 25180344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1883, '中银信用卡(国际)有限公司', 'BOC', '澳门币贷记卡', '澳门币贷记卡', '625141', 16, 6, 25180446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1884, '中银信用卡(国际)有限公司', 'BOC', '澳门币贷记卡', '澳门币贷记卡', '625143', 16, 6, 25180446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1885, '中国银行（香港）', 'BOC', '接触式晶片借记卡', '接触式晶片借记卡', '621741', 19, 6, 25190344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1886, '中国银行（香港）', 'BOC', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623040', 16, 6, 25190344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1887, '中国银行（香港）', 'BOC', '中国银行银联预付卡', '中国银行银联预付卡', '620202', 16, 6, 25190344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1888, '中国银行（香港）', 'BOC', '中国银行银联预付卡', '中国银行银联预付卡', '620203', 16, 6, 25190344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1889, '南洋商业银行', '(NULL)', '银联港币卡', '银联港币卡', '621042', 19, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1890, '南洋商业银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625046', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1891, '南洋商业银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625044', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1892, '南洋商业银行', '(NULL)', '双币商务卡', '双币商务卡', '625058', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1893, '南洋商业银行', '(NULL)', '接触式晶片借记卡', '接触式晶片借记卡', '621743', 19, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1894, '南洋商业银行', '(NULL)', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623041', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1895, '南洋商业银行', '(NULL)', '南洋商业银行银联预付卡', '南洋商业银行银联预付卡', '620208', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1896, '南洋商业银行', '(NULL)', '南洋商业银行银联预付卡', '南洋商业银行银联预付卡', '620209', 16, 6, 25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1897, '集友银行', '(NULL)', '银联港币卡', '银联港币卡', '621043', 19, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1898, '集友银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625048', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1899, '集友银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625053', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1900, '集友银行', '(NULL)', '双币商务卡', '双币商务卡', '625060', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1901, '集友银行', '(NULL)', '接触式晶片借记卡', '接触式晶片借记卡', '621742', 19, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1902, '集友银行', '(NULL)', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623042', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1903, '集友银行', '(NULL)', '集友银行银联预付卡', '集友银行银联预付卡', '620206', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1904, '集友银行', '(NULL)', '集友银行银联预付卡', '集友银行银联预付卡', '620207', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1905, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEONJUSCO银联卡', 'AEONJUSCO银联卡', '622493', 16, 6, 25230344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1906, '大丰银行', '(NULL)', '银联双币白金卡', '银联双币白金卡', '625198', 16, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1907, '大丰银行', '(NULL)', '银联双币金卡', '银联双币金卡', '625196', 16, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1908, '大丰银行', '(NULL)', '港币借记卡', '港币借记卡', '622547', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1909, '大丰银行', '(NULL)', '澳门币借记卡', '澳门币借记卡', '622548', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1910, '大丰银行', '(NULL)', '人民币借记卡', '人民币借记卡', '622546', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1911, '澳门大丰银行', '(NULL)', '中银银联双币商务卡', '中银银联双币商务卡', '625147', 16, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1912, '大丰银行', '(NULL)', '大丰预付卡', '大丰预付卡', '620072', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1913, '大丰银行', '(NULL)', '大丰银行预付卡', '大丰银行预付卡', '620204', 16, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1914, '大丰银行', '(NULL)', '大丰银行预付卡', '大丰银行预付卡', '620205', 16, 6, 25250446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1915, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '621064', 16, 6, 25260344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1916, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '622941', 16, 6, 25260344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1917, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '622974', 16, 6, 25260344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1918, '中国建设银行（澳门）', 'CCB', '扣款卡', '扣款卡', '621084', 16, 6, 25270446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1919, '渣打银行香港有限公司', '(NULL)', '港币借记卡', '港币借记卡', '622948', 16, 6, 25280344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1920, '渣打银行（香港）', '(NULL)', '银联标准卡', '银联标准卡', '621740', 16, 6, 25280344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1921, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622482', 16, 6, 25280344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1922, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622483', 16, 6, 25280344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1923, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622484', 16, 6, 25280344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1924, '中国银盛', '(NULL)', '中国银盛预付卡', '中国银盛预付卡', '620070', 16, 6, 25290344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1925, '中国银盛', '(NULL)', '中国银盛预付卡', '中国银盛预付卡', '620068', 16, 6, 25300344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1926, '中国建设银行（亚洲）', 'CCB', '预付卡', '预付卡', '620107', 16, 6, 25330344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1927, 'Travelex', '(NULL)', 'Travelex港币卡', 'Travelex港币卡', '622495', 16, 6, 26040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1928, 'Travelex', '(NULL)', 'Travelex美元卡', 'Travelex美元卡', '622496', 16, 6, 26040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1929, 'Travelex', '(NULL)', 'CashPassportCounsume', 'CashPassportCounsume', '620152', 16, 6, 26040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1930, 'Travelex', '(NULL)', 'CashPassportCounsume', 'CashPassportCounsume', '620153', 16, 6, 26040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1931, '新加坡大华银行', '(NULL)', 'UOBCUPCARD', 'UOBCUPCARD', '622433', 16, 6, 26070702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1932, '澳门永亨银行', '(NULL)', '人民币卡', '人民币卡', '622861', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1933, '澳门永亨银行', '(NULL)', '港币借记卡', '港币借记卡', '622932', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1934, '澳门永亨银行', '(NULL)', '澳门币借记卡', '澳门币借记卡', '622862', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1935, '澳门永亨银行', '(NULL)', '澳门币贷记卡', '澳门币贷记卡', '622775', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1936, '澳门永亨银行', '(NULL)', '港币贷记卡', '港币贷记卡', '622785', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1937, '日本三井住友卡公司', '(NULL)', 'MITSUISUMITOMOGINREN', 'MITSUISUMITOMOGINREN', '622920', 16, 6, 26110392, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1938, '澳门国际银行', '(NULL)', '人民币卡', '人民币卡', '622434', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1939, '澳门国际银行', '(NULL)', '澳门币卡', '澳门币卡', '622436', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1940, '澳门国际银行', '(NULL)', '港币卡', '港币卡', '622435', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1941, '大西洋银行', '(NULL)', '财运卡', '财运卡', '621232', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1942, '大西洋银行', '(NULL)', '澳门币卡', '澳门币卡', '622432', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1943, '大西洋银行', '(NULL)', '财运卡', '财运卡', '621247', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1944, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60110', 16, 5, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1945, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601100', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1946, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601101', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1947, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112', 16, 5, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1948, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112010', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1949, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112011', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1950, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112012', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1951, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112089', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1952, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601121', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1953, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601123', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1954, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601124', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1955, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601125', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1956, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601126', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1957, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601127', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1958, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601128', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1959, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011290', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1960, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011291', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1961, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011292', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1962, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011293', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1963, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112013', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1964, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011295', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1965, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601122', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1966, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011297', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1967, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112980', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1968, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112981', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1969, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112986', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1970, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112987', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1971, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112988', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1972, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112989', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1973, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112990', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1974, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112991', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1975, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112992', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1976, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112993', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1977, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011294', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1978, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011296', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1979, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112996', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1980, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112997', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1981, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113', 16, 5, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1982, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011300', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1983, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113080', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1984, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113081', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1985, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113089', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1986, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601131', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1987, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601136', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1988, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601137', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1989, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601138', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1990, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011390', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1991, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112995', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1992, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011392', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1993, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011393', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1994, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113940', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1995, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113941', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1996, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113943', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1997, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113944', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1998, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113945', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (1999, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113946', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2000, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113984', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2001, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113985', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2002, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113986', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2003, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113988', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2004, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112994', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2005, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011391', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2006, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60114', 16, 5, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2007, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601140', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2008, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601142', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2009, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601143', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2010, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601144', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2011, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601145', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2012, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601146', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2013, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601147', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2014, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601148', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2015, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601149', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2016, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601174', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2017, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113989', 16, 8, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2018, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601178', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2019, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011399', 16, 7, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2020, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601186', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2021, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601187', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2022, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601188', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2023, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601189', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2024, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60119', 16, 5, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2025, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '644', 16, 3, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2026, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '65', 16, 2, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2027, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6506', 16, 4, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2028, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6507', 16, 4, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2029, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6508', 16, 4, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2030, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601177', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2031, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601179', 16, 6, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2032, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6509', 16, 4, 26290840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2033, '澳门商业银行', '(NULL)', '银联人民币卡', '银联人民币卡', '621253', 19, 6, 26320446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2034, '澳门商业银行', '(NULL)', '银联澳门币卡', '银联澳门币卡', '621254', 19, 6, 26320446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2035, '澳门商业银行', '(NULL)', '银联港币卡', '银联港币卡', '621255', 19, 6, 26320446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2036, '澳门商业银行', '(NULL)', '双币种普卡', '双币种普卡', '625014', 16, 6, 26320446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2037, '澳门商业银行', '(NULL)', '双币种白金卡', '双币种白金卡', '625016', 16, 6, 26320446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2038, '哈萨克斯坦国民储蓄银行', '(NULL)', 'HalykbankClassic', 'HalykbankClassic', '622549', 16, 6, 26330398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2039, '哈萨克斯坦国民储蓄银行', '(NULL)', 'HalykbankGolden', 'HalykbankGolden', '622550', 16, 6, 26330398, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2040, 'Bangkok Bank Pcl', '(NULL)', '贷记卡', '贷记卡', '622354', 16, 6, 26350764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2041, '中国工商银行（澳门）', 'ICBC', '普卡', '普卡', '625017', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2042, '中国工商银行（澳门）', 'ICBC', '金卡', '金卡', '625018', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2043, '中国工商银行（澳门）', 'ICBC', '白金卡', '白金卡', '625019', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2044, '可汗银行', '(NULL)', '借记卡', '借记卡', '621224', 16, 6, 26530496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2045, '可汗银行', '(NULL)', '银联蒙图借记卡', '银联蒙图借记卡', '622954', 16, 6, 26530496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2046, '越南Vietcombank', '(NULL)', '借记卡', '借记卡', '621295', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2047, '越南Vietcombank', '(NULL)', '贷记卡', '贷记卡', '625124', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2048, '越南Vietcombank', '(NULL)', '贷记卡', '贷记卡', '625154', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2049, '蒙古郭勒姆特银行', '(NULL)', 'Golomt Unionpay', 'Golomt Unionpay', '621049', 16, 6, 26620496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2050, '蒙古郭勒姆特银行', '(NULL)', '贷记卡', '贷记卡', '622444', 16, 6, 26620496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2051, '蒙古郭勒姆特银行', '(NULL)', '借记卡', '借记卡', '622414', 16, 6, 26620496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2052, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620011', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2053, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620027', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2054, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620031', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2055, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620039', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2056, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620103', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2057, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620106', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2058, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620120', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2059, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620123', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2060, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620125', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2061, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620220', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2062, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620278', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2063, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620812', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2064, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621006', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2065, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621011', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2066, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621012', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2067, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621020', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2068, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621023', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2069, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621025', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2070, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621027', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2071, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621031', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2072, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620132', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2073, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621039', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2074, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621078', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2075, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621220', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2076, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625003', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2077, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621003', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2078, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625011', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2079, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625012', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2080, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625020', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2081, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625023', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2082, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625025', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2083, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625027', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2084, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625031', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2085, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621032', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2086, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625039', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2087, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625078', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2088, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625079', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2089, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625103', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2090, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625106', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2091, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625006', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2092, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625112', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2093, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625120', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2094, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625123', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2095, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625125', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2096, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625127', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2097, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625131', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2098, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625032', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2099, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625139', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2100, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625178', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2101, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625179', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2102, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625220', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2103, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625320', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2104, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625111', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2105, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625132', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2106, '莫斯科人民储蓄银行', '(NULL)', 'cup-unioncard', 'cup-unioncard', '621040', 16, 6, 26690643, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2107, '丝绸之路银行', '(NULL)', 'Classic/Gold', 'Classic/Gold', '621045', 16, 6, 26700860, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2108, '俄罗斯远东商业银行', '(NULL)', '借记卡', '借记卡', '621264', 16, 6, 26780643, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2109, 'CSC', '(NULL)', '贷记卡', '贷记卡', '622356', 16, 6, 26790422, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2110, 'CSC', '(NULL)', 'CSC借记卡', 'CSC借记卡', '621234', 16, 6, 26790422, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2111, 'Allied Bank', '(NULL)', '贷记卡', '贷记卡', '622145', 16, 6, 26930608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2112, 'Allied Bank', '(NULL)', '贷记卡', '贷记卡', '625013', 16, 6, 26930608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2113, '日本三菱信用卡公司', '(NULL)', '贷记卡', '贷记卡', '622130', 16, 6, 27090392, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2114, 'Baiduri Bank Berhad', '(NULL)', '借记卡', '借记卡', '621257', 16, 6, 27130096, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2115, '越南西贡商业银行', '(NULL)', '借记卡', '借记卡', '621055', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2116, '越南西贡商业银行', '(NULL)', '预付卡', '预付卡', '620009', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2117, '越南西贡商业银行', '(NULL)', '贷记卡', '贷记卡', '625002', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2118, '菲律宾BDO', '(NULL)', '银联卡', '银联卡', '625033', 16, 6, 27240608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2119, '菲律宾BDO', '(NULL)', '银联卡', '银联卡', '625035', 16, 6, 27240608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2120, '菲律宾RCBC', '(NULL)', '贷记卡', '贷记卡', '625007', 16, 6, 27250608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2121, '新加坡星网电子付款私人有限公司', '(NULL)', '预付卡', '预付卡', '620015', 16, 6, 27520702, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2122, 'Royal Bank Open Stoc', '(NULL)', '预付卡', '预付卡', '620024', 16, 6, 27550031, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2123, 'Royal Bank Open Stoc', '(NULL)', '贷记卡', '贷记卡', '625004', 16, 6, 27550031, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2124, 'Royal Bank Open Stoc', '(NULL)', '借记卡', '借记卡', '621344', 19, 6, 27550031, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2125, '乌兹别克斯坦INFINBANK', '(NULL)', '借记卡', '借记卡', '621349', 16, 6, 27650860, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2126, 'BCEL', '(NULL)', '借记卡', '借记卡', '621354', 16, 6, 27710418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2127, '澳门通股份有限公司', '(NULL)', '双币闪付卡', '双币闪付卡', '620532', 16, 6, 28020446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2128, '韩国乐天', '(NULL)', '贷记卡', '贷记卡', '625904', 16, 6, 28030410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2129, '巴基斯坦FAYSAL BANK', '(NULL)', '借记卡', '借记卡', '621645', 16, 6, 28040586, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2130, '俄罗斯ORIENT EXPRESS BA', '(NULL)', '信用卡', '信用卡', '625104', 16, 6, 28450643, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2131, '俄罗斯ORIENT EXPRESS BA', '(NULL)', '借记卡', '借记卡', '621647', 16, 6, 28450643, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2132, 'Mongolia Trade Devel', '(NULL)', '普卡/金卡', '普卡/金卡', '621642', 16, 6, 28530496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2133, 'Krung Thaj Bank Publ', '(NULL)', '借记卡', '借记卡', '621654', 16, 6, 28550764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2134, '韩国KB', '(NULL)', '贷记卡', '贷记卡', '625804', 16, 6, 28590410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2135, '韩国三星卡公司', '(NULL)', '三星卡', '三星卡', '625814', 16, 6, 28660410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2136, '韩国三星卡公司', '(NULL)', '三星卡', '三星卡', '625817', 16, 6, 28660410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2137, 'Commercial Bank of D', '(NULL)', 'PrepaidCard', 'PrepaidCard', '620079', 16, 6, 28790784, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2138, 'Commercial Bank of D', '(NULL)', 'PrepaidCard', 'PrepaidCard', '620091', 16, 6, 28790784, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2139, 'The Bancorp Bank', '(NULL)', 'UnionPay Travel Card', 'UnionPay Travel Card', '620105', 16, 6, 28880840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2140, '巴基斯坦HabibBank', '(NULL)', '借记卡', '借记卡', '621657', 16, 6, 28990586, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2141, 'The Mauritius Commer', '(NULL)', '预付卡', '预付卡', '620129', 16, 6, 29170480, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2142, '中国银行香港有限公司', 'BOC', '人民币信用卡金卡', '人民币信用卡金卡', '622346', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2143, '中国银行香港有限公司', 'BOC', '信用卡普通卡', '信用卡普通卡', '622347', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2144, '中国银行香港有限公司', 'BOC', '中银卡(人民币)', '中银卡(人民币)', '622348', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2145, '南洋商业银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622349', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2146, '南洋商业银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622350', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2147, '集友银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622352', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2148, '集友银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622353', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2149, '集友银行', '(NULL)', '中银卡', '中银卡', '622355', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2150, '中国银行(香港)', '(NULL)', '银联港币借记卡', '银联港币借记卡', '621041', 19, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2151, '南洋商业银行', '(NULL)', '中银卡(人民币)', '中银卡(人民币)', '622351', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2152, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '620048', 16, 6, 48080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2153, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '620515', 16, 6, 48080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2154, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '920000', 16, 6, 48080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2155, '中银通支付', '(NULL)', '安徽合肥通卡', '安徽合肥通卡', '921000', 19, 6, 48080001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2156, '中银通商务支付有限公司', '(NULL)', '铁路卡', '铁路卡', '620038', 19, 6, 48100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2157, '中国邮政储蓄银行', 'PSBC', '银联标准白金卡', '银联标准白金卡', '622812', 16, 6, 61000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2158, '中国邮政储蓄银行', 'PSBC', '银联标准贷记卡', '银联标准贷记卡', '622810', 16, 6, 61000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2159, '中国邮政储蓄银行', 'PSBC', '银联标准贷记卡', '银联标准贷记卡', '622811', 16, 6, 61000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2160, '中国邮政储蓄银行', 'PSBC', '银联标准公务卡', '银联标准公务卡', '628310', 16, 6, 61000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2161, '中国邮政储蓄银行', 'PSBC', '上海购物信用卡', '上海购物信用卡', '625919', 16, 6, 61000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2162, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376968', 15, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2163, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376969', 15, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2164, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '400360', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2165, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403391', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2166, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403392', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2167, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376966', 15, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2168, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404158', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2169, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404159', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2170, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404171', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2171, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404172', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2172, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404173', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2173, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404174', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2174, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404157', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2175, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433667', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2176, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433668', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2177, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433669', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2178, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '514906', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2179, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403393', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2180, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '520108', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2181, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433666', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2182, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '558916', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2183, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622678', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2184, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622679', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2185, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622680', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2186, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622688', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2187, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622689', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2188, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628206', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2189, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '556617', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2190, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628209', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2191, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '518212', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2192, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628208', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2193, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356390', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2194, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356391', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2195, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356392', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2196, '中信银行', 'CITIC', '中信银联IC卡普卡', '中信银联IC卡普卡', '622916', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2197, '中信银行', 'CITIC', '中信银联IC卡金卡', '中信银联IC卡金卡', '622918', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2198, '中信银行', 'CITIC', '中信银联IC卡白金卡', '中信银联IC卡白金卡', '622919', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2199, '中国光大银行', 'CEB', '存贷合一白金卡', '存贷合一白金卡', '622657', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2200, '中国光大银行', 'CEB', '存贷合一卡普卡', '存贷合一卡普卡', '622685', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2201, '中国光大银行', 'CEB', '理财信用卡', '理财信用卡', '622659', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2202, '中国光大银行', 'CEB', '存贷合一钻石卡', '存贷合一钻石卡', '622687', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2203, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625978', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2204, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625980', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2205, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625981', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2206, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625979', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2207, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '356839', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2208, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '356840', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2209, '中国光大银行', 'CEB', '阳光信用卡(银联', '阳光信用卡(银联', '406252', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2210, '中国光大银行', 'CEB', '阳光信用卡(银联', '阳光信用卡(银联', '406254', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2211, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '425862', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2212, '中国光大银行', 'CEB', '阳光白金信用卡', '阳光白金信用卡', '481699', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2213, '中国光大银行', 'CEB', '安邦俱乐部信用卡', '安邦俱乐部信用卡', '524090', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2214, '中国光大银行', 'CEB', '足球锦标赛纪念卡', '足球锦标赛纪念卡', '543159', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2215, '中国光大银行', 'CEB', '光大银行联名公务卡', '光大银行联名公务卡', '622161', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2216, '中国光大银行', 'CEB', '积分卡', '积分卡', '622570', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2217, '中国光大银行', 'CEB', '炎黄卡普卡', '炎黄卡普卡', '622650', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2218, '中国光大银行', 'CEB', '炎黄卡白金卡', '炎黄卡白金卡', '622655', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2219, '中国光大银行', 'CEB', '炎黄卡金卡', '炎黄卡金卡', '622658', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2220, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625975', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2221, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625977', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2222, '中国光大银行', 'CEB', '银联公务卡', '银联公务卡', '628201', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2223, '中国光大银行', 'CEB', '银联公务卡', '银联公务卡', '628202', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2224, '中国光大银行', 'CEB', '银联贷记IC旅游卡', '银联贷记IC旅游卡', '625339', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2225, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625976', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2226, '华夏银行', 'HXBANK', '华夏万事达信用卡', '华夏万事达信用卡', '523959', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2227, '华夏银行', 'HXBANK', '万事达信用卡金卡', '万事达信用卡金卡', '528709', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2228, '华夏银行', 'HXBANK', '万事达普卡', '万事达普卡', '539867', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2229, '华夏银行', 'HXBANK', '万事达普卡', '万事达普卡', '539868', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2230, '华夏银行', 'HXBANK', '华夏信用卡金卡', '华夏信用卡金卡', '622637', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2231, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '622638', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2232, '华夏银行', 'HXBANK', '华夏公务信用卡', '华夏公务信用卡', '628318', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2233, '华夏银行', 'HXBANK', '万事达信用卡金卡', '万事达信用卡金卡', '528708', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2234, '华夏银行', 'HXBANK', '华夏信用卡普卡', '华夏信用卡普卡', '622636', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2235, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625967', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2236, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625968', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2237, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625969', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2238, '浦发银行信用卡中心', '(NULL)', '移动浦发借贷合一联名卡', '移动浦发借贷合一联名卡', '625971', 16, 6, 63100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2239, '浦发银行信用卡中心', '(NULL)', '贷记卡', '贷记卡', '625970', 16, 6, 63100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2240, '浦发银行信用卡中心', '(NULL)', '浦发私人银行信用卡', '浦发私人银行信用卡', '377187', 15, 6, 63100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2241, '东亚银行', 'HKBEA', '东亚银行普卡', '东亚银行普卡', '622265', 16, 6, 63200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2242, '东亚银行', 'HKBEA', '东亚银行金卡', '东亚银行金卡', '622266', 16, 6, 63200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2243, '东亚银行', 'HKBEA', '百家网点纪念版IC贷记卡', '百家网点纪念版IC贷记卡', '625972', 16, 6, 63200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2244, '东亚银行', 'HKBEA', '百家网点纪念版IC贷记卡', '百家网点纪念版IC贷记卡', '625973', 16, 6, 63200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2245, '南洋商业银行', '(NULL)', '银联个人白金信用卡', '银联个人白金信用卡', '625093', 16, 6, 63320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2246, '南洋商业银行', '(NULL)', '银联商务白金信用卡', '银联商务白金信用卡', '625095', 16, 6, 63320000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2247, '北京银行', 'BJBANK', '万事达双币金卡', '万事达双币金卡', '522001', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2248, '北京银行', 'BJBANK', '银联标准贷记卡', '银联标准贷记卡', '622163', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2249, '北京银行', 'BJBANK', '银联标准贷记卡', '银联标准贷记卡', '622853', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2250, '北京银行', 'BJBANK', '银联标准公务卡', '银联标准公务卡', '628203', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2251, '北京银行', 'BJBANK', '北京银行中荷人寿联名卡', '北京银行中荷人寿联名卡', '622851', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2252, '北京银行', 'BJBANK', '尊尚白金卡', '尊尚白金卡', '622852', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2253, '宁波银行', 'NBBANK', '汇通贷记卡', '汇通贷记卡', '625903', 16, 6, 64083300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2254, '宁波银行', 'NBBANK', '汇通白金卡', '汇通白金卡', '622778', 16, 6, 64083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2255, '宁波银行', 'NBBANK', '汇通公务卡', '汇通公务卡', '628207', 16, 6, 64083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2256, '齐鲁银行', 'QLBANK', '泉城公务卡', '泉城公务卡', '628379', 16, 6, 64094510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2257, '广州银行', 'GCB', '银联标准公务卡', '银联标准公务卡', '628367', 16, 6, 64135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2258, '广州银行', 'GCB', '广州银行信用卡', '广州银行信用卡', '625050', 16, 6, 64135810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2259, '龙江银行', 'DAQINGB', '金鹤卡', '金鹤卡', '622644', 16, 6, 64162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2260, '龙江银行', 'DAQINGB', '公务卡', '公务卡', '628333', 16, 6, 64162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2261, '河北银行', 'BHB', '如意贷记卡', '如意贷记卡', '622921', 16, 6, 64221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2262, '河北银行', 'BHB', '如意贷记卡', '如意贷记卡', '628321', 16, 6, 64221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2263, '杭州市商业银行', '(NULL)', '西湖卡IC卡', '西湖卡IC卡', '603367', 19, 6, 64233310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2264, '杭州市商业银行', '(NULL)', '西湖贷记卡', '西湖贷记卡', '622286', 16, 6, 64233311, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2265, '杭州市商业银行', '(NULL)', '西湖贷记卡', '西湖贷记卡', '628236', 16, 6, 64233311, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2266, '南京银行', 'NJCB', '借记IC卡', '借记IC卡', '621777', 16, 6, 64243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2267, '成都市商业银行', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628228', 16, 6, 64296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2268, '成都市商业银行', '(NULL)', '银联标准卡', '银联标准卡', '622813', 16, 6, 64296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2269, '成都市商业银行', '(NULL)', '银联标准卡', '银联标准卡', '622818', 16, 6, 64296510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2270, '临商银行', 'LSBC', '公务卡', '公务卡', '628359', 16, 6, 64314730, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2271, '珠海华润银行', '(NULL)', '公务信用卡', '公务信用卡', '628270', 16, 6, 64375850, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2272, '齐商银行', 'ZBCB', '金达公务卡', '金达公务卡', '628311', 16, 6, 64384530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2273, '锦州银行', 'BOJZ', '公务卡', '公务卡', '628261', 16, 6, 64392270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2274, '徽商银行', 'HSBANK', '银联标准公务卡', '银联标准公务卡', '628251', 16, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2275, '徽商银行', 'HSBANK', '借记卡', '借记卡', '621775', 19, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2276, '徽商银行', 'HSBANK', '贷记卡', '贷记卡', '622651', 16, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2277, '徽商银行', 'HSBANK', '徽商银行中国旅游卡（安徽）', '徽商银行中国旅游卡（安徽）', '623203', 19, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2278, '徽商银行', 'HSBANK', '贷记IC卡', '贷记IC卡', '625828', 16, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2279, '重庆银行', 'CQBANK', '银联标准卡', '银联标准卡', '622613', 16, 6, 64416910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2280, '重庆银行', 'CQBANK', '银联标准公务卡', '银联标准公务卡', '628220', 16, 6, 64416910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2281, '哈尔滨商行', '(NULL)', '丁香贷记卡', '丁香贷记卡', '622809', 16, 6, 64422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2282, '哈尔滨商行', '(NULL)', '哈尔滨银行公务卡', '哈尔滨银行公务卡', '628224', 16, 6, 64422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2283, '哈尔滨银行', '(NULL)', '福农准贷记卡', '福农准贷记卡', '625577', 16, 6, 64422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2284, '哈尔滨银行', '(NULL)', '金融IC借记卡', '金融IC借记卡', '621752', 19, 6, 64422611, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2285, '贵阳银行', '(NULL)', '甲秀公务卡', '甲秀公务卡', '628213', 16, 6, 64437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2286, '兰州银行', 'LZYH', '敦煌公务卡', '敦煌公务卡', '628263', 16, 6, 64478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2287, '南昌银行', 'NCB', '银联标准公务卡', '银联标准公务卡', '628305', 16, 6, 64484210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2288, '青岛银行', 'QDCCB', '公务卡', '公务卡', '628239', 16, 6, 64504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2289, '九江银行', 'JJBANK', '庐山公务卡', '庐山公务卡', '628238', 16, 6, 64544240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2290, '日照银行', '(NULL)', '黄海公务卡', '黄海公务卡', '628257', 16, 6, 64554770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2291, '青海银行', 'BOQH', '三江贷记卡', '三江贷记卡', '622817', 16, 6, 64588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2292, '青海银行', 'BOQH', '三江贷记卡(公务卡)', '三江贷记卡(公务卡)', '628287', 16, 6, 64588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2293, '青海银行', 'BOQH', '三江贷记IC卡', '三江贷记IC卡', '625959', 16, 6, 64588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2294, '青海银行', 'BOQH', '中国旅游卡', '中国旅游卡', '62536601', 16, 8, 64588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2295, '潍坊银行', 'BANKWF', '鸢都公务卡', '鸢都公务卡', '628391', 16, 6, 64624580, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2296, '赣州银行', 'GZB', '长征公务卡', '长征公务卡', '628233', 16, 6, 64634280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2297, '富滇银行', 'FDB', '富滇公务卡', '富滇公务卡', '628231', 16, 6, 64667310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2298, '浙江泰隆商业银行', 'ZJTLCB', '泰隆公务卡(单位卡)', '泰隆公务卡(单位卡)', '628275', 16, 6, 64733450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2299, '浙江泰隆商业银行', 'ZJTLCB', '泰隆尊尚白金卡、钻石卡', '泰隆尊尚白金卡、钻石卡', '622565', 16, 6, 64733450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2300, '浙江泰隆商业银行', 'ZJTLCB', '泰隆信用卡', '泰隆信用卡', '622287', 16, 6, 64733450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2301, '浙江泰隆商业银行', 'ZJTLCB', '融易通', '融易通', '622717', 16, 6, 64733450, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2302, '内蒙古银行', 'H3CB', '银联标准公务卡', '银联标准公务卡', '628252', 16, 6, 64741910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2303, '湖州银行', '(NULL)', '公务卡', '公务卡', '628306', 16, 6, 64753360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2304, '广西北部湾银行', 'BGB', '银联标准公务卡', '银联标准公务卡', '628227', 16, 6, 64786110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2305, '广西北部湾银行', 'BGB', 'IC借记卡', 'IC借记卡', '623001', 16, 6, 64786110, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2306, '威海市商业银行', 'WHCCB', '通达公务卡', '通达公务卡', '628234', 16, 6, 64814650, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2307, '广东南粤银行', '(NULL)', '湛江市民卡', '湛江市民卡', '621727', 19, 6, 64895910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2308, '广东南粤银行', '(NULL)', '公务卡', '公务卡', '628237', 16, 6, 64895919, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2309, '桂林银行', 'GLBANK', '漓江公务卡', '漓江公务卡', '628219', 16, 6, 64916170, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2310, '桂林银行', 'GLBANK', '漓江卡', '漓江卡', '621456', 17, 6, 64916170, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2311, '桂林银行', 'GLBANK', '福农IC卡', '福农IC卡', '621562', 19, 6, 64916170, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2312, '龙江银行', 'DAQINGB', '玉兔贷记卡', '玉兔贷记卡', '622270', 16, 6, 64922690, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2313, '龙江银行', 'DAQINGB', '玉兔贷记卡(公务卡)', '玉兔贷记卡(公务卡)', '628368', 16, 6, 64922690, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2314, '龙江银行', 'DAQINGB', '福农准贷记卡', '福农准贷记卡', '625588', 16, 6, 64922690, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2315, '龙江银行', 'DAQINGB', '联名贷记卡', '联名贷记卡', '625090', 16, 6, 64922690, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2316, '柳州银行', '(NULL)', '龙城公务卡', '龙城公务卡', '628293', 16, 6, 64956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2317, '上海农商银行贷记卡', '(NULL)', '鑫卡', '鑫卡', '622611', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2318, '上海农商银行贷记卡', '(NULL)', '商务卡', '商务卡', '622722', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2319, '上海农商银行贷记卡', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628211', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2320, '上海农商银行贷记卡', '(NULL)', '福农卡', '福农卡', '625500', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2321, '上海农商银行贷记卡', '(NULL)', '鑫通卡', '鑫通卡', '625989', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2322, '广州农村商业银行', '(NULL)', '太阳信用卡', '太阳信用卡', '625080', 16, 6, 65055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2323, '广州农村商业银行', '(NULL)', '公务卡', '公务卡', '628235', 16, 6, 65055810, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2324, '佛山顺德农村商业银行', '(NULL)', '恒通贷记卡（公务卡）', '恒通贷记卡（公务卡）', '628322', 16, 6, 65085883, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2325, '佛山顺德农村商业银行', '(NULL)', '恒通贷记卡', '恒通贷记卡', '625088', 16, 6, 65085883, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2326, '云南省农村信用社', 'YNRCC', '金碧贷记卡', '金碧贷记卡', '622469', 16, 6, 65097300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2327, '云南省农村信用社', 'YNRCC', '金碧公务卡', '金碧公务卡', '628307', 16, 6, 65097300, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2328, '承德银行', 'BOCD', '热河公务卡', '热河公务卡', '628229', 16, 6, 65131410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2329, '德州银行', 'DZBANK', '长河公务卡', '长河公务卡', '628397', 16, 6, 65154680, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2330, '福建省农村信用社联合社', '(NULL)', '万通贷记卡', '万通贷记卡', '628232', 16, 6, 65173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2331, '福建省农村信用社联合社', '(NULL)', '万通贷记卡', '万通贷记卡', '622802', 16, 6, 65173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2332, '福建省农村信用社联合社', '(NULL)', '福建海峡旅游卡', '福建海峡旅游卡', '622290', 16, 6, 65173900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2333, '天津农村商业银行', '(NULL)', '吉祥信用卡', '吉祥信用卡', '622829', 16, 6, 65191100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2334, '天津农村商业银行', '(NULL)', '吉祥信用卡', '吉祥信用卡', '628301', 16, 6, 65191100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2335, '成都农村商业银行', '(NULL)', '天府贷记卡', '天府贷记卡', '622808', 16, 6, 65226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2336, '成都农村商业银行', '(NULL)', '天府公务卡', '天府公务卡', '628308', 16, 6, 65226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2337, '成都农村商业银行', '(NULL)', '天府借记卡', '天府借记卡', '623088', 19, 6, 65226510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2338, '江苏省农村信用社联合社', '(NULL)', '圆鼎贷记卡', '圆鼎贷记卡', '622815', 16, 6, 65243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2339, '江苏省农村信用社联合社', '(NULL)', '圆鼎贷记卡', '圆鼎贷记卡', '622816', 16, 6, 65243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2340, '江苏省农村信用社联合社', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628226', 16, 6, 65243000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2341, '上饶银行', 'SRBANK', '三清山公务卡', '三清山公务卡', '628223', 16, 6, 65264330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2342, '上饶银行', 'SRBANK', '三清山IC卡', '三清山IC卡', '621416', 16, 6, 65264331, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2343, '东营银行', '(NULL)', '财政公务卡', '财政公务卡', '628217', 16, 6, 65274550, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2344, '临汾市尧都区农村信用合作联社', '(NULL)', '天河贷记公务卡', '天河贷记公务卡', '628382', 16, 6, 65341770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2345, '临汾市尧都区农村信用合作联社', '(NULL)', '天河贷记卡', '天河贷记卡', '625158', 16, 6, 65341770, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2346, '无锡农村商业银行', 'WRCB', '银联标准公务卡', '银联标准公务卡', '628369', 16, 6, 65373020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2347, '无锡农村商业银行', 'WRCB', '金阿福贷记卡', '金阿福贷记卡', '622569', 16, 6, 65373020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2348, '湖南农村信用社联合社', '(NULL)', '福祥贷记卡', '福祥贷记卡', '622906', 16, 6, 65385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2349, '湖南农村信用社联合社', '(NULL)', '福祥公务卡', '福祥公务卡', '628386', 16, 6, 65385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2350, '湖南农信', '(NULL)', '福农卡', '福农卡', '625519', 16, 6, 65385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2351, '湖南农信', '(NULL)', '福祥贷记卡（福农卡）', '福祥贷记卡（福农卡）', '625506', 16, 6, 65385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2352, '江西省农村信用社联合社', '(NULL)', '百福福农卡', '百福福农卡', '621592', 16, 6, 65394200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2353, '江西省农村信用社联合社', '(NULL)', '百福公务卡', '百福公务卡', '628392', 16, 6, 65394200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2354, '安徽省农村信用社', 'ARCU', '金农卡', '金农卡', '621778', 19, 6, 65473600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2355, '邢台银行', 'XTB', '金牛市民卡', '金牛市民卡', '620528', 19, 6, 65541310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2356, '商丘市商业银行', '(NULL)', '百汇卡', '百汇卡', '621748', 19, 6, 65675060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2357, '商丘市商业银行', '(NULL)', '公务卡', '公务卡', '628271', 16, 6, 65675061, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2358, '华融湘江银行', 'HRXJB', '华融湘江银行华融公务卡普卡', '华融湘江银行华融公务卡普卡', '628328', 16, 6, 65705500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2359, '北京银行', 'BJBANK', '借记IC卡', '借记IC卡', '623111', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2360, '阜新银行', 'FXCB', '借记IC卡', '借记IC卡', '623166', 18, 6, 4672299, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2361, '衡水市商业银行', '(NULL)', '借记IC卡', '借记IC卡', '623068', 19, 6, 5611481, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2362, '湖南省农村信用社联合社', '(NULL)', '福祥借记IC卡', '福祥借记IC卡', '623090', 19, 6, 14385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2363, '重庆南川石银村镇银行', '(NULL)', '麒麟借记卡', '麒麟借记卡', '621653004', 19, 9, 15436900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2364, 'BC卡公司', '(NULL)', '贷记卡', '贷记卡', '625244', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2365, '澳门BDA', '(NULL)', '汇业卡', '汇业卡', '621274', 19, 6, 27860446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2366, '澳门BDA', '(NULL)', '汇业卡', '汇业卡', '621324', 19, 6, 27860446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2368, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '622180', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2369, '建设银行', 'CCB', NULL, NULL, '623668', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2370, '农业银行', NULL, NULL, NULL, '623052', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2371, '中国工商银行', NULL, NULL, NULL, '621472', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2372, '山西农信社', NULL, NULL, NULL, '623051', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2373, '中国民生银行', NULL, NULL, NULL, '621691', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2374, '邮政储蓄银行', NULL, NULL, NULL, '621582', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2375, '河南农信社', NULL, NULL, NULL, '623059', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2376, '建设银行', 'CCB', NULL, NULL, '623094', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2377, '郑州银行', NULL, NULL, NULL, '623531', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2378, '建设银行', 'CCB', NULL, NULL, '621534', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `sys_dept_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构名称',
  `sys_dept_pid` int(11) NULL DEFAULT NULL COMMENT '父机构',
  `sys_dept_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构链路',
  `sys_dept_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
  `sys_dept_principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人',
  `sys_dept_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人联系方式',
  `sys_dept_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_dept_status` int(11) NULL DEFAULT NULL COMMENT '状态; 是否禁用; 0:启用',
  `sys_dept_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_dept_data_permission` int(11) NULL DEFAULT NULL COMMENT '数据权限类型;1:全部可见;2:本人可见;3:所在部门可见;4:所在部门及子级可见;5:选择的部门可见',
  `sys_dept_data_permission_dept_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据权限可见的部门ID列表',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '一级机构', 0, '0,1', 'ROOT', NULL, NULL, 1, 0, NULL, 5, '4,2,5', 'sa', 1, '2025-02-12 17:23:37', '2025-02-12 17:26:09', 'sa', 1);
INSERT INTO `sys_dept` VALUES (2, '二级机构', 1, '0,1,2', 'l2', NULL, NULL, 1, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-12 17:30:03', '2025-02-13 10:29:52', 'sa', 1);
INSERT INTO `sys_dept` VALUES (4, '二级机构 - 2', 1, '0,1,4', 'l22', NULL, NULL, 0, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-13 14:36:26', '2025-02-13 14:36:26', 'sa', 1);
INSERT INTO `sys_dept` VALUES (5, '三级机构', 2, '0,1,2,5', 'l3', NULL, NULL, 0, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-13 14:36:39', '2025-02-13 14:36:39', 'sa', 1);

-- ----------------------------
-- Table structure for sys_device
-- ----------------------------
DROP TABLE IF EXISTS `sys_device`;
CREATE TABLE `sys_device`  (
  `sys_device_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `sys_device_org_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
  `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
  `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道名称',
  `sys_device_channel_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道号',
  `sys_device_channel_count` int(11) NULL DEFAULT NULL COMMENT '管道数量',
  `sys_device_status` int(11) NULL DEFAULT NULL COMMENT '是否禁用; 0:启用',
  `sys_device_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置',
  `sys_device_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `sys_device_resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源类型',
  `sys_device_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_device_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `sys_device_rtsp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推流地址;存在则使用该地址播放视像头',
  `sys_device_rtsp_webrtc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'rtsp推理webrtc地址; 采用webrtc解析rtsp',
  `sys_device_channels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道',
  `sys_device_owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属者唯一编码',
  `sys_device_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_device_online` int(11) NULL DEFAULT NULL COMMENT '是否在线; 0: 离线； 1:在线',
  `sys_device_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型',
  `sys_device_latitude` decimal(10, 2) NULL DEFAULT NULL COMMENT '纬度',
  `sys_device_longitude` decimal(10, 2) NULL DEFAULT NULL COMMENT '经度',
  `sys_device_net_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网络地址',
  `sys_device_net_physical_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网络地址物理地址',
  `sys_device_validate_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证码',
  `sys_device_main_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主码流编码',
  `sys_device_sub_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子码流编码',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '机构ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 421 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摄像头设备管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device
-- ----------------------------
INSERT INTO `sys_device` VALUES (1, 4, '电梯107.62', '1ffc05b9fcbe42bd913e50da9ac63aca', '320a84af4f0d4cf88eb2306968ccf980', '9楼', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-21 09:06:56', '2023-11-21 09:07:28', 'sa', 1);
INSERT INTO `sys_device` VALUES (2, 4, '行政楼107.1', '20570c6865a24b1c9db4cb55b1f37dca', '67c815b1878043edaf2920cab7662e8e', '1楼', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-15 09:14:27', '2023-11-15 09:20:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (3, 4, '网络机房107.2', 'dcdda67eec8d483282b1bb97a1f0d40f', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:10:54', '2023-11-25 13:15:09', 'sa', 1);
INSERT INTO `sys_device` VALUES (4, 4, '行政楼107.3', '528bfe14b8944374b0c9e42adb95c439', '67c815b1878043edaf2920cab7662e8e', '1楼', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-15 09:14:27', '2023-11-15 09:20:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (5, 4, '行政楼107.4', '2b099b056c844973b6772700e4f0cb96', '67c815b1878043edaf2920cab7662e8e', '1楼', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-15 09:14:27', '2023-11-15 09:20:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (6, 4, '教室1 107.71', '59ce5359a7174eaea69e7774ae7b1fbc', 'e431804a28e949329e367e37a77be93a', '行政楼专业教室10间', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:02', '2023-11-25 13:14:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (7, 4, '2教学楼104.14', '8ac035f4732f456897b8939b11ff949c', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:45', '2023-11-25 13:15:46', 'sa', 1);
INSERT INTO `sys_device` VALUES (8, 4, '教室·2 107.72', 'ea59401a17ce40a1aa02aec01c6329c0', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:02', '2023-11-25 13:14:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (9, 4, '教室3 107.73', '818e4e36db9e4ad48446532815cceba0', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:02', '2023-11-25 13:14:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (10, 4, '教室4 107.74', '1d6fa8c374ed4a07a4e476851f84ad97', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:01', '2023-11-25 13:14:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (11, 4, '教室5 107.75', '320975d2e36744218790b07bb1c22e89', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:01', '2023-11-25 13:14:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (12, 4, '教室6 107.76', '2bb18bd9430044f0a93f7ddda786091d', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:00', '2023-11-25 13:14:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (13, 4, '教室7 107.77', 'd8fc8d02823d4132b57d98016af7115e', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:00', '2023-11-25 13:14:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (14, 4, '教室8 107.78', '373b0839c7a34fd0b11aa2fe1796d747', 'e431804a28e949329e367e37a77be93a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:19:00', '2023-11-25 13:14:56', 'sa', 1);
INSERT INTO `sys_device` VALUES (15, 4, '教室9 107.79', '27e8f19d115148d1921d67ebee397a2d', 'e431804a28e949329e367e37a77be93a', '行政楼专业教室10间', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:23:48', '2023-11-12 17:26:24', 'sa', 1);
INSERT INTO `sys_device` VALUES (16, 4, '教室10 107.80', '3d2d399b4fd4424ea6517fb1c1186e84', 'e431804a28e949329e367e37a77be93a', '行政楼专业教室10间', 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:23:48', '2023-11-12 17:26:35', 'sa', 1);
INSERT INTO `sys_device` VALUES (17, 4, '教学楼103.43', '4e9f6d89c9c04c3d86e1b32fe5d4cd24', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 15:11:59', '2023-11-06 15:12:18', 'sa', 1);
INSERT INTO `sys_device` VALUES (18, 4, '操场西北角108.91', '68f5c0dfdd8d49119c2f82827c005990', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 15:39:14', '2023-11-25 13:15:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (19, 4, '北门108.92', '707a1055ce6749ada39379d2792258e0', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 15:39:14', '2023-11-25 13:15:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (20, 4, '行政楼107.43', '10b8bc2acc1c42a2b663fe621bad57cd', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:03', '2023-11-25 13:15:18', 'sa', 1);
INSERT INTO `sys_device` VALUES (21, 4, '行政楼107.45', '76ac677331184ea196e7a4644fe4e51f', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:03', '2023-11-25 13:15:19', 'sa', 1);
INSERT INTO `sys_device` VALUES (22, 4, '行政楼107.44', '3c4f50e69ba74ad6abc2f84aef1707de', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:21:30', '2023-11-15 09:22:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (23, 4, '行政楼107.46', '23e5cc0d8ec14a3c87d4a4b5246ecda7', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 13:49:19', '2023-11-15 09:23:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (24, 4, '行政楼107.47', 'f28d235f3f794300b08dadfbae1d3be0', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 13:49:19', '2023-11-15 09:23:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (25, 4, '行政楼107.48', '804aefa4e8a743e9aa78a0f8a87fd194', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 13:49:19', '2023-11-15 09:22:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (26, 4, '行政楼107.49', '82c573f21f02431290de21fb21dd9364', '8ea259ac17ac4d31b4bfe35608d88427', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 13:49:18', '2023-11-15 09:22:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (27, 4, 'IPdome', '367521b6f091456680dc24d675663a08', '25b773e5f16848549b77f4b8cba6d85a', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 07:10:13', '2023-11-06 07:10:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (28, 4, 'IPdome', '8baa1e95422c4f6994ee50a1b8c653df', '25b773e5f16848549b77f4b8cba6d85a', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 07:10:13', '2023-11-06 07:10:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (29, 4, '1-2庭院106.100', '279f12afb8764ea6a6114da0e684d39c', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-29 15:35:16', '2023-10-31 07:42:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (30, 4, '1-2庭院106.103', '92a5d74e6c704e31b903e9febd847d0d', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 08:06:56', '2023-10-31 07:42:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (31, 4, '1号顶朝北106.101', 'da882b7026ca414c919017d4cda84784', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-29 15:35:16', '2023-10-31 07:42:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (32, 4, '2-3庭院106.102', '1817ac898dd8412b884b42fa25ad404b', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 08:06:53', '2023-10-31 07:42:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (33, 4, '2-3庭院106.104', 'd82d4aff57c54536b82023aefe58c6d3', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 11:08:08', '2023-10-31 07:42:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (34, 4, '3-4庭院106.105', '311b79058176482ba354c25875c986f5', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-31 07:38:20', '2023-10-31 07:42:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (35, 4, '3-4庭院106.106', '677d5b223a444aefbf56b28bc67a6868', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 15:16:23', '2023-10-30 15:25:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (36, 4, '4号东球106.107', 'dcf12b4a73c04018a743cd3e0e1afecb', 'd374331f1bca426587ae9c51ae922af1', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 15:15:50', '2023-10-30 15:25:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (37, 4, '一（1）班107.18', '3981a66b7295400eabf939f77ab29864', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 07:19:02', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (38, 4, '一（2）班107.20', '780077f114604503aea76c13c411f562', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 08:05:06', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (39, 4, '一（3）班107.27', '7b8ce287d1c546ae9125418cc711299e', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 08:20:42', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (40, 4, '一（4）班107.26', '8b72eae645d2444f80516dac85cdbb69', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 08:36:18', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (41, 4, '一（5）班107.25', 'a2df94aee3a14be2a1e1683f13141a7f', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 09:10:09', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (42, 4, '一（6）班107.19', '229e21dc21694801a2df4aca988d7ad2', '2b511f35e33e4d788dcdcb06260599c7', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 09:43:53', '2023-10-06 09:44:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (43, 4, '行政楼107.60', '04461ee9214148059463f15a48cb5069', '320a84af4f0d4cf88eb2306968ccf980', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 10:07:34', '2023-10-01 10:08:45', 'sa', 1);
INSERT INTO `sys_device` VALUES (44, 4, '行政楼107.56', '25a864b1bfc14b1fa954d34d34f31fd8', '4d7c6257a1c7475b85a38eccfd6feea4', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:00', '2023-11-25 13:15:22', 'sa', 1);
INSERT INTO `sys_device` VALUES (45, 4, '行政楼107.57', 'a981aa2e1a0d45c197d9596afed99b5b', '4d7c6257a1c7475b85a38eccfd6feea4', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:51:47', '2023-10-01 09:52:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (46, 4, '行政楼107.53', '79653eeeffb94b1e99c06b74a0e94fdc', 'ef8bc1ff16ed4d77b58b4511fa00b2db', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:01', '2023-11-25 13:15:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (47, 4, '行政楼107.54', 'faa786a9091b4e05adb840003865c538', 'ef8bc1ff16ed4d77b58b4511fa00b2db', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:46:16', '2023-10-01 09:46:37', 'sa', 1);
INSERT INTO `sys_device` VALUES (48, 4, '行政楼107.50', 'b263a5e27d4748368783e699f4fc530b', '42e80bef18e8476a875dbd03fba35e71', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:02', '2023-11-25 13:15:20', 'sa', 1);
INSERT INTO `sys_device` VALUES (49, 4, '行政楼107.51', '99dd7db317354fdba1316da0f496f8ce', '42e80bef18e8476a875dbd03fba35e71', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:21:53', '2023-10-01 09:46:46', 'sa', 1);
INSERT INTO `sys_device` VALUES (50, 4, '行政楼107.40', '6f2434dbf3bf447cb914c26a0859d969', 'a20a05f4b4d544f9aee4ae1d0a745c24', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:04', '2023-11-25 13:15:18', 'sa', 1);
INSERT INTO `sys_device` VALUES (51, 4, '行政楼107.41', '520d6bfa91ac4495b40e3cf13eebf275', 'a20a05f4b4d544f9aee4ae1d0a745c24', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:20:43', '2023-10-01 09:47:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (52, 4, '行政楼107.37', 'dc6a72ca12464041aef9465d5566b5b0', '25b773e5f16848549b77f4b8cba6d85a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 09:20:14', '2023-10-01 09:47:14', 'sa', 1);
INSERT INTO `sys_device` VALUES (53, 4, '地下室109.1', '8bf48d2ba2394cf994f9dc918b081288', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (54, 4, '地下室109.2', '4117c9f5a3fb47748a1ee5bc0492ca21', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (55, 4, '地下室109.3', 'bf6fd58d4e2c486b9fbfcc86c87d8008', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 14:08:15', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (56, 4, '地下室109.4', 'e0dbceae4c9d495ebd1995cd5127b5d1', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (57, 4, '地下室109.5', 'd346b713d07c45c28b74b453b5fe9c29', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (58, 4, '地下室109.6', '0a9798c7c61742bfa389df9b8a5a9a6d', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (59, 4, '地下室109.7', '3edf675198f047698d096b58d72e2654', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:12', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (60, 4, '地下室109.8', '578d90a8046b4f8c9a7cca3059b7468d', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (61, 4, '地下室109.9', 'd5aac4668a114b688d564efad93b80ff', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (62, 4, '地下室109.10', '2beb2acc3b5d4ca6aaef40ab3bc559b6', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (63, 4, '地下室109.11', '004c9550d95b446999dd02bebbb667c8', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (64, 4, '地下室109.12', 'fa4555a7bc664f929916adc6025f5f1a', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (65, 4, '地下室109.13', '632921199bc749a08dd59e9eebe35670', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 16:31:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (66, 4, '地下室109.14', '0549c6070d7e498cb840c45e7db3c4f2', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (67, 4, '地下室109.15', '2d7668d7c99e47739f41237d866df049', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:50:09', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (68, 4, '地下室109.16', '16c909b831af45a299a5732b25ec4572', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-05 18:18:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (69, 4, '地下室109.17', '43f84312e4fa4a839e3a7b0e94aea439', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-05 18:18:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (70, 4, '地下室109.18', '6aa735a7d22442e98a8ec66e984ce67c', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-05 18:18:11', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (71, 4, '地下室109.19', '7f4dd323d1504570a88afcdf83fdd1e2', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-05 18:18:13', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (72, 4, '地下室109.20', '6a159502e9eb45d0a4c01ef2fc861abf', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-05 18:18:13', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (73, 4, '地下室109.21', '4f6732d981ff4cf2a7bc2a68f86ecc4f', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:04', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (74, 4, '地下室109.22', 'f87b452c574f41bcaf8f6404df79ef4e', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 08:10:04', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (75, 4, '地下室109.23', 'c670bc00f4264d849133edc1162a687f', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (76, 4, '地下室109.24', '35c6e209cf9b4aa987d63270b4abfcf4', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (77, 4, '地下室109.25', 'c5129a7740104073a1d74d5ef80f62e0', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (78, 4, '地下室109.26', 'b7416767869446e1984211c6657a2840', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (79, 4, '地下室109.27', 'ab8722e98ac745fcb2eb9001182e2c1a', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (80, 4, '地下室109.28', '1cc3bc10946545a183db3a5f555c06bc', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:32:26', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (81, 4, '地下室109.29', 'de1eb2544b49411fbead160b7613536d', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (82, 4, '地下室109.30', 'f52aaac46e464751b43e4e694ecc739c', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 15:26:47', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (83, 4, '地下室109.31', '80c7b0a56bf9492b88b52775c269ffb7', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (84, 4, '地下室109.32', 'fe1ec843b3e14ab4b57b2e8000311a48', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (85, 4, '地下室 109.33', '1a3a0aa26ad74376b0c1a8876f09b860', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (86, 4, '地下室109.34', '6a17bb9048e6430f90a6c5ce700b8b9b', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (87, 4, '地下室 109.35', 'f15b02a4c24240b28030daee4727082c', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (88, 4, '地下室 109.36', 'cd345d14952542bfa8b1041d4742ae8f', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (89, 4, '地下室 109.37', '812e30dd701f4ac2b7cc3757472b7efd', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (90, 4, '地下室 109.38', '0d9e2aeab4994babb8a3621379f4cb12', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (91, 4, '地下室 109.39', 'f73270a098a64a7494bf9aa8eedd2ddb', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (92, 4, '地下室 109.40', '3d08406aac964bdaa41e230770043fcc', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:56', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (93, 4, '地下室109.41', 'a9fd611d4f3e4e4f9630c04562dde8cc', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 14:37:16', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (94, 4, '家长等候区109.42', '5379dcd6d2714f348f4d44957d3ec225', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-19 09:25:35', '2023-11-15 09:23:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (95, 4, '地下室 109.43', 'b1aade0fc68e48a1aa0d291c7b89996b', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:48', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (96, 4, '家长等候区109.44', '941252de590742a389d913f85855308f', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-19 09:44:28', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (97, 4, '地下闸机109.45', 'a9f3b4dd02bc4e688d72a6a76a1fa283', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-19 09:08:14', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (98, 4, '地下室109.46', '536361a003ea4294b27d7106b24c0507', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 14:54:33', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (99, 4, '地下室下客区109.47', 'f6538cc4f78f4f9da83b655ed31f941c', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 08:09:52', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (100, 4, '地下室人脸入口109.48', 'd9a503ce7dce4c1aa098fee6a89c735c', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 08:11:34', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (101, 4, '地下室 109.49', '35276fa5e8c1422d8d51dbadc0b0ace4', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:48', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (102, 4, '东地下入口109.50', 'fb5d986cbc8b4c6b9313c777b81662d8', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-19 09:16:40', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (103, 4, '地下室 109.51', 'f5fc6c923c584d50832d5054a1452376', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:48', '2023-09-23 15:39:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (104, 4, '接送处往地上109.52', 'b2f41968cc814897858ef90b2efb8aa2', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-19 09:49:51', '2023-09-23 15:39:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (105, 4, '地下室 109.53', '828738e934ba4db89f6e3b67ca894abe', 'bdd001adfdf4418689c30f49c41fc03b', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 13:52:48', '2023-09-23 15:38:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (106, 4, '室外108.93', '95968500c5354e92bab0da6b7bf545f1', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 15:43:46', '2023-11-06 14:11:50', 'sa', 1);
INSERT INTO `sys_device` VALUES (107, 4, '北门108.94', '028260f35fa84268a1c3ef845c787e66', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 15:39:13', '2023-11-25 13:15:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (108, 4, '操场108.95', 'c992927ce8464539a881b56cecdc4a69', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 10:50:25', '2023-11-25 13:15:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (109, 4, '操场108.96', '7d0ab34440724587954f4d0455b757e5', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 10:53:58', '2023-11-06 14:11:50', 'sa', 1);
INSERT INTO `sys_device` VALUES (110, 4, '室外108.97', 'eadc358889f3496c950d74c5a93f59a1', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 14:03:35', '2023-11-25 13:14:57', 'sa', 1);
INSERT INTO `sys_device` VALUES (111, 4, '室外108.98', '04d66eddf80e4fe992105b869c1eb41d', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-06 14:03:35', '2023-11-25 13:14:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (112, 4, '室外108.1', '9efa936cfb48405fa54c1589dea2b3ff', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-04 08:33:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (113, 4, '室外108.2', '2c21b52f5e9d46eea0c7c82c59e6e688', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-04 08:33:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (114, 4, '室外108.3', 'cb39561170bd49f8a7e93ff2923d73c5', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (115, 4, '室外108.4', '6d17d704904646369d4a0dfcd654e22a', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (116, 4, '室外108.5', 'a2fd95372c6b4623a2e0317618ce0e06', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (117, 4, '室外108.6', 'f19b7aa3086f4e51a49de1a1e4835f75', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (118, 4, '室外108.7', '7da9668bec4041f4b4432eb9ca71ea5a', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 09:48:43', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (119, 4, '室外108.8', '431ab53e5a834f4abed5c8611d10fabc', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (120, 4, '室外108.9', 'b2259b157be643ee90ba2ec3db1fee19', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (121, 4, '室外108.10', 'c6ce9f9f32a84b7bb4fcc9891c6d0188', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (122, 4, '室外108.11', '048e37190f784d3995e73a524db4a9f3', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 09:48:43', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (123, 4, '室外108.12', '7ec232192da346049624221b6e57edd7', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (124, 4, '室外108.13', 'fd8f13880e964f50b4b0270183799612', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (125, 4, '室外108.14', '8fbcf4e633e14259ad97f4ae4319afb2', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:04', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (126, 4, '室外108.15', 'bb98e11ba35e434eb306a04d32e3e3b0', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 09:48:41', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (127, 4, '室外108.16', 'c1cacfe1aeda42f09342d69cd7aa3dd7', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (128, 4, '室外108.17', '46fbed7ce0144007ae776ecee9c5ce5c', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (129, 4, '室外108.18', 'b9bf5b8b537c491e9effb35832fc1aa2', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (130, 4, '室外108.19', 'b5694e7ee6484ea888e1cd8566a7ed00', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (131, 4, '室外108.20', '48cebf4611db48428117b66bb1e098d8', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 09:48:41', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (132, 4, '室外108.21', 'e3e1741f61f9480198731a21a9d282ec', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (133, 4, '室外108.22', '8ee7828304e94b8182c9d14e5a02c6cf', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:51:57', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (134, 4, '室外108.23', '1651bf9f49cc4d17b3b4be0ce0da2ad0', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:03', '2023-11-25 13:15:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (135, 4, '室外108.24', '44f7482aa42c4e8daa171f2a52bc65c2', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:03', '2023-11-25 13:15:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (136, 4, '室外108.25', '95c62dcde7f14245958fc1383bfdb6a5', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:03', '2023-11-25 13:15:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (137, 4, '室外108.26', '2e62b826cfbd453c9f4caeb3f1a6d50d', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 10:07:59', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (138, 4, '室外108.27', '639d54fcc4914f1ea64c4d5484772743', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:02', '2023-11-25 13:15:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (139, 4, '室外108.28', '5119fefa7f584182aa6f00a6a7a76018', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:02', '2023-11-25 13:15:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (140, 4, '室外108.29', 'e518feb0964744a3a94531b972178416', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:00:01', '2023-11-25 13:15:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (141, 4, '室外108.30', '52b4fa09c199433286ec670bf59bd792', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 10:35:33', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (142, 4, '室外108.31', '6538003f661c4337b075d2f8b5b727ae', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-23 10:22:24', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (143, 4, '室外108.32', '2c424402f14c43c3b57d1d39d6a6e3b3', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 10:35:33', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (144, 4, '室外108.33', 'e3c854bffb8749e8be451e94143b3fac', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (145, 4, '室外108.35', '166a8f66f7be4ddc9f41446a18c6bb84', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (146, 4, '室外108.34', 'f11336b3cb5944f49098a821d8e0d50f', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-04 08:34:01', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (147, 4, '室外108.36', 'a3704b7788aa437da22ee6eff221fe3e', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (148, 4, '室外108.37', '583a8f93c57c48ce8d7e2e11cd36ce44', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (149, 4, '室外108.38', '8d655e6445734b3ba0a5b3b7ffebaa7c', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-04 08:34:01', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (150, 4, '室外108.39', 'c9b74139dc314bb4989b4e03ff0040d1', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (151, 4, '室外108.40', '88a22ae5dbb8450784052d491953f709', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (152, 4, '室外108.41', '7809689e403749ce89f71c3808dae2b2', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (153, 4, '室外108.42', 'ca0566d697a843a49a6679cf8383152e', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-17 17:52:00', '2023-11-06 14:11:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (154, 4, '厨房102.59', '220da77161134218943b07490d33a3f3', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (155, 4, '教学楼102.1', '90fc5264b3574ad09ace90802398e3ea', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:45', '2023-11-25 13:16:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (156, 4, '教学楼102.10', 'bc805ac3ec6a4216a911ecf205784f49', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:43', '2023-11-25 13:16:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (157, 4, '教学楼102.11', 'fd391518a2e4474a86f60c59db87cfad', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:42', '2023-11-25 13:16:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (158, 4, '教学楼102.12', 'a9db66b56905490a9d8d823b14ea9a34', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:42', '2023-11-25 13:16:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (159, 4, '教学楼102.13', '7d6e246a7f0b499da9d1ab6adfef7a07', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:42', '2023-11-25 13:16:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (160, 4, '教学楼102.14', '988a719511d6461cacc6c4e1607d6d70', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-02 10:07:43', '2023-11-25 13:15:00', 'sa', 1);
INSERT INTO `sys_device` VALUES (161, 4, '教学楼102.15', '4571b1b60e0d47fe9e5cb39727924cb2', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:36', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (162, 4, '教学楼102.16', '33c93ae99cfa4334b164b5f08522b96e', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-02 14:38:32', '2023-11-25 13:14:59', 'sa', 1);
INSERT INTO `sys_device` VALUES (163, 4, '教学楼102.17', '7c6ed5f11b324eab9346bb38e8a186a3', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-02 11:21:44', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (164, 4, '教学楼102.18', '752a50b212344dd78490c1241980e8a5', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:36', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (165, 4, '教学楼102.19', '7107420e4a5540e6b29b995d0cd3dfa6', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:36', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (166, 4, '教学楼102.2', '9a73bc6497d64ca88bce25e5f5c69178', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-03 14:12:10', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (167, 4, '教学楼102.20', '22199d7d18e84c9bad3a747beb846b0c', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:36', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (168, 4, '教学楼102.21', 'b37233bcb35843f89578ac2da4f05f73', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:10:44', '2023-11-25 13:14:57', 'sa', 1);
INSERT INTO `sys_device` VALUES (169, 4, '校门108.43', '652ac0ee3db144809704331a527d9f16', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:57', '2023-11-25 13:15:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (170, 4, '校门108.44', '4baf8f2af06049e0a306b801c2c001e6', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:57', '2023-11-25 13:15:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (171, 4, '行政楼107.5', '09558446eb1e47b39e0015e313f54cb9', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:14', '2023-11-25 13:15:09', 'sa', 1);
INSERT INTO `sys_device` VALUES (172, 4, '室外108.46', 'b56d4b0fe88f4d18be59691afdba1d30', '4002633ca7cc466988a91560bbb6a9cc', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 10:49:01', '2023-11-25 13:15:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (173, 4, '行政楼107.6', '59cdce2b8db546b3950a04733d02b5e1', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:14', '2023-11-25 13:15:09', 'sa', 1);
INSERT INTO `sys_device` VALUES (174, 4, 'IPdome', '12ff674334f044bf93a5a125ca2b5727', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-11 17:26:03', '2023-11-15 09:21:15', 'sa', 1);
INSERT INTO `sys_device` VALUES (175, 4, '行政楼107.8', 'f808d2d22a094e1296d5037ea866593e', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 09:34:07', '2023-11-15 09:21:14', 'sa', 1);
INSERT INTO `sys_device` VALUES (176, 4, '行政楼107.52', '901a6b58fa77403f87cd5ee6c7646960', '42e80bef18e8476a875dbd03fba35e71', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:01', '2023-11-25 13:15:20', 'sa', 1);
INSERT INTO `sys_device` VALUES (177, 4, '行政楼107.38', '6f63f8cbf8d64be99445b003d877ece0', '25b773e5f16848549b77f4b8cba6d85a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:04', '2023-11-25 13:15:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (178, 4, '行政楼107.9', '1944cfd1df3a4b05a3b73d5ebde04bf3', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:13', '2023-11-25 13:15:10', 'sa', 1);
INSERT INTO `sys_device` VALUES (179, 4, '行政楼107.39', '90fdca024d8a4aa094c1c1eb49b7d016', '25b773e5f16848549b77f4b8cba6d85a', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:04', '2023-11-25 13:15:17', 'sa', 1);
INSERT INTO `sys_device` VALUES (180, 4, '行政楼107.10', '1ebc6ddd1f7a4cfcb74671fe52fe8841', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:13', '2023-11-25 13:15:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (181, 4, '行政楼107.11', 'a8fcf874acee4bdbae6a9675a44d750a', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:13', '2023-11-25 13:15:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (182, 4, '行政楼107.55', '6ab47a50d9734b27a768d8ee8481ae0b', 'ef8bc1ff16ed4d77b58b4511fa00b2db', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:01', '2023-11-25 13:15:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (183, 4, '行政楼107.12', '1e7a040d68b74636b6d1410097dd1ba3', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:12', '2023-11-25 13:15:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (184, 4, '监控室107.13', '69e4a53507aa4f549584ce861abbfa67', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:12', '2023-11-25 13:15:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (185, 4, '消控室107.70', '5b188d07f4c4443aa09e5b49df2f01b6', '67c815b1878043edaf2920cab7662e8e', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:56:50', '2023-11-25 13:15:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (186, 4, '行政楼107.59', '894b607fd7b841d58b659fb8c0d24687', '320a84af4f0d4cf88eb2306968ccf980', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:00', '2023-11-25 13:15:22', 'sa', 1);
INSERT INTO `sys_device` VALUES (187, 4, '行政楼107.61', '334278b6f6174bd197963d9f0177f7fd', '320a84af4f0d4cf88eb2306968ccf980', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:59', '2023-11-25 13:15:23', 'sa', 1);
INSERT INTO `sys_device` VALUES (188, 4, '行政楼107.21', 'c1faf3fe18e54a71841cb3c4378f154d', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:10', '2023-11-25 13:15:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (189, 4, '教学楼103.1', '6761d762796841e881adc3117a73cc64', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:01', '2023-11-25 13:15:50', 'sa', 1);
INSERT INTO `sys_device` VALUES (190, 4, '行政楼107.22', '83cce300a1c343d48aea8d4351379776', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:10', '2023-11-25 13:15:13', 'sa', 1);
INSERT INTO `sys_device` VALUES (191, 4, '教学楼103.2', 'f84cd4a5fba54fa9a3e5da221491ec9e', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:00', '2023-11-25 13:15:51', 'sa', 1);
INSERT INTO `sys_device` VALUES (192, 4, '教学楼103.3', '3a060f011c6f409a8a4611cd9ea3225d', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:00', '2023-11-25 13:15:51', 'sa', 1);
INSERT INTO `sys_device` VALUES (193, 4, '教学楼103.4', 'b95ec4e43c3b4bfb805297cda200b10c', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:59', '2023-11-25 13:15:51', 'sa', 1);
INSERT INTO `sys_device` VALUES (194, 4, '教学楼103.5', 'a0e38acb1b16431a9304ffc738e5341a', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:59', '2023-11-25 13:15:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (195, 4, '教学楼103.6', '31634d6ae2cb4ae687c970dad73116bb', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:58', '2023-11-25 13:15:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (196, 4, '教学楼103.7', '9131d467ed564d7e93b5169a0b50821f', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 15:02:36', '2023-11-01 18:44:30', 'sa', 1);
INSERT INTO `sys_device` VALUES (197, 4, '行政楼107.42', 'c6428d60f6684785bf5a1cdd16680257', 'a20a05f4b4d544f9aee4ae1d0a745c24', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:04', '2023-11-25 13:15:18', 'sa', 1);
INSERT INTO `sys_device` VALUES (198, 4, '4教学楼105.1', '32ab19a105684657af67d341b71c5961', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:33', '2023-11-25 13:15:32', 'sa', 1);
INSERT INTO `sys_device` VALUES (199, 4, '教学楼103.9', '05877eb7997c4963912d513a97579b30', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:58', '2023-11-25 13:15:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (200, 4, '4教学楼105.2', 'e7b80e16835745588e093cea09101ff1', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:33', '2023-11-25 13:15:33', 'sa', 1);
INSERT INTO `sys_device` VALUES (201, 4, '4教学楼104.1', '2653ac41fcf6419b93902436d717a799', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:50', '2023-11-25 13:15:41', 'sa', 1);
INSERT INTO `sys_device` VALUES (202, 4, '教学楼103.10', '257087b16bc3415486b490b71060458e', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:57', '2023-11-25 13:15:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (203, 4, '教学楼103.11', '7dab5e829e414267895c743891197f99', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 17:16:01', '2023-11-01 18:44:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (204, 4, '行政楼107.58', 'b95d1ae35f7746e1abc8836e78674c7d', '4d7c6257a1c7475b85a38eccfd6feea4', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:00', '2023-11-25 13:15:22', 'sa', 1);
INSERT INTO `sys_device` VALUES (205, 4, '4教学楼105.3', '201e7ee17fa0476bba6309722bda2985', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:32', '2023-11-25 13:15:33', 'sa', 1);
INSERT INTO `sys_device` VALUES (206, 4, '4教学楼105.4', '6b950b7d2b03478abafb2828fd64e32b', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:32', '2023-11-25 13:15:33', 'sa', 1);
INSERT INTO `sys_device` VALUES (207, 4, '4教学楼104.2', 'e445cfbbb9524c8b95c2188c14d943bc', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:50', '2023-11-25 13:15:41', 'sa', 1);
INSERT INTO `sys_device` VALUES (208, 4, '教学楼103.12', 'ba2e46052cd14f1e9b45e63f9c56a3bb', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:56', '2023-11-25 13:15:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (209, 4, '4教学楼105.5', '6a477b896d0a4bd894cef1e700465495', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:32', '2023-11-25 13:15:34', 'sa', 1);
INSERT INTO `sys_device` VALUES (210, 4, '4教学楼104.3', 'cdfe51ea9dc84f09b5e48fdababa53d9', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:49', '2023-11-25 13:15:42', 'sa', 1);
INSERT INTO `sys_device` VALUES (211, 4, '3教学楼105.6', 'b5b579df17f84e558e7835a3c0372c17', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:31', '2023-11-25 13:15:34', 'sa', 1);
INSERT INTO `sys_device` VALUES (212, 4, '3教学楼105.7', '23f9c8d616ad4fa5b8882b32674d86f0', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:30', '2023-11-25 13:15:34', 'sa', 1);
INSERT INTO `sys_device` VALUES (213, 4, '4教学楼104.4', '81f69f36ea894bc5867421cbe1ca20e9', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:49', '2023-11-25 13:15:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (214, 4, '3教学楼105.8', '8cb2892012c6409180f58f1c4bbfa240', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:30', '2023-11-25 13:15:34', 'sa', 1);
INSERT INTO `sys_device` VALUES (215, 4, '4教学楼104.5', '647ff018ce3a4e8581a909945f501599', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:49', '2023-11-25 13:15:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (216, 4, '3教学楼105.9', 'bb308f88d94c4b5699b6f141068aa32f', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:30', '2023-11-25 13:15:35', 'sa', 1);
INSERT INTO `sys_device` VALUES (217, 4, '3教学楼104.6', '719c81ce927643ef9f7e5a0eebac7e65', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:48', '2023-11-25 13:15:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (218, 4, '3教学楼105.10', 'b9e601acf11d415a92a7ba49502c020f', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:30', '2023-11-25 13:15:35', 'sa', 1);
INSERT INTO `sys_device` VALUES (219, 4, '3教学楼104.7', '27b307571c61433d94d0e5e44663df36', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:48', '2023-11-25 13:15:44', 'sa', 1);
INSERT INTO `sys_device` VALUES (220, 4, '3教学楼105.11', '64b5a07c251247808000809c401b5e51', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:29', '2023-11-25 13:15:36', 'sa', 1);
INSERT INTO `sys_device` VALUES (221, 4, '3教学楼104.8', '107026a9e146450bbda1198786216204', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:47', '2023-11-25 13:15:44', 'sa', 1);
INSERT INTO `sys_device` VALUES (222, 4, '3教学楼104.9', '2b31873506dc4694bbe3ec2776a0289b', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 07:52:18', '2023-11-01 13:09:59', 'sa', 1);
INSERT INTO `sys_device` VALUES (223, 4, '3教学楼105.12', 'a39087583356435eb1bca96326eea054', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:29', '2023-11-25 13:15:36', 'sa', 1);
INSERT INTO `sys_device` VALUES (224, 4, '3教学楼104.10', '2fcc4fd5424c4b1f88fcb94ec1f3f656', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:47', '2023-11-25 13:15:45', 'sa', 1);
INSERT INTO `sys_device` VALUES (225, 4, '教学楼103.13', '7a3405b3fc9f49bdbb8e71af8edba57b', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:56', '2023-11-25 13:15:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (226, 4, '教学楼103.15', '2e19b9d391734e858cf2b3ea1a36cf5c', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 17:20:56', '2023-11-01 18:31:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (227, 4, '3教学楼104.11', '880be59dfdc3412596645cd7c1f2e19d', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:46', '2023-11-25 13:15:45', 'sa', 1);
INSERT INTO `sys_device` VALUES (228, 4, '教学楼103.16', '6ae6892aa6aa48d39a3909e1ae720586', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:55', '2023-11-25 13:15:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (229, 4, '教学楼103.17', 'f22bb5c914954cdd977bbddda442b3a8', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:54', '2023-11-25 13:15:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (230, 4, '教学楼103.18', '23f95d172c8f4baeb7d8a29a28a6190e', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:54', '2023-11-25 13:15:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (231, 4, '教学楼103.19', 'a7074e8b38e14a7fb041205776e198c8', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:54', '2023-11-25 13:15:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (232, 4, '教学楼103.21', 'de1876cd71724a199929d02dd97e5f56', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:53', '2023-11-25 13:15:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (233, 4, '教学楼103.22', 'b7bf1d0202a74e8b9e9dd7f292cdf816', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:53', '2023-11-25 13:15:55', 'sa', 1);
INSERT INTO `sys_device` VALUES (234, 4, '教学楼103.24', 'aa28701c68524b3ca6813162ab0efcdb', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:52', '2023-11-25 13:15:56', 'sa', 1);
INSERT INTO `sys_device` VALUES (235, 4, '教学楼103.25', 'd9bfc1458db64ada92a73f7395a77e1c', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 17:30:04', '2023-11-01 18:31:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (236, 4, '4教学楼106.1', 'ddb9ab0bc57d435fa732dce350755106', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:05', '2023-11-25 13:15:23', 'sa', 1);
INSERT INTO `sys_device` VALUES (237, 4, '2教学楼105.13', 'b66fae3282c941a8953b37d65c8297ee', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:28', '2023-11-25 13:15:36', 'sa', 1);
INSERT INTO `sys_device` VALUES (238, 4, '教学楼103.27', '1c695ee984034348b58ec5c1f7c1492c', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 17:16:02', '2023-11-01 18:31:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (239, 4, '4教学楼106.2', 'c4cd9f211b7d4e93bd994e717ea7648d', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:05', '2023-11-25 13:15:23', 'sa', 1);
INSERT INTO `sys_device` VALUES (240, 4, '2教学楼105.14', '144fe7d100fa47afae51ee144b6647ba', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:29', '2023-11-25 13:15:37', 'sa', 1);
INSERT INTO `sys_device` VALUES (241, 4, '4教学楼106.3', 'ce0d941e1b5c45a7b41484afd6b7cf0e', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:05', '2023-11-25 13:15:24', 'sa', 1);
INSERT INTO `sys_device` VALUES (242, 4, '2教学楼105.15', 'b4a349ded5414ff188022abdce51332d', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:28', '2023-11-25 13:15:37', 'sa', 1);
INSERT INTO `sys_device` VALUES (243, 4, '教学楼103.26', '8748588b76514bc7886e137209924ade', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:51', '2023-11-25 13:15:56', 'sa', 1);
INSERT INTO `sys_device` VALUES (244, 4, '4教学楼106.4', 'b523c8391e9f48bf9093ff76ecbce343', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:04', '2023-11-25 13:15:24', 'sa', 1);
INSERT INTO `sys_device` VALUES (245, 4, '2教学楼105.16', '9982ddf17d184a54915ef38feaa08697', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:27', '2023-11-25 13:15:37', 'sa', 1);
INSERT INTO `sys_device` VALUES (246, 4, '教学楼103.28', 'a027007d99124eec9bd6a6a2a14dc001', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:51', '2023-11-25 13:15:56', 'sa', 1);
INSERT INTO `sys_device` VALUES (247, 4, '4教学楼106.5', 'b154baf53741449d84d6d33848a53f90', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:04', '2023-11-25 13:15:24', 'sa', 1);
INSERT INTO `sys_device` VALUES (248, 4, '2教学楼105.17', '865c9a18e6b34ffa8ec7e52c6dabf3f8', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:27', '2023-11-25 13:15:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (249, 4, '教学楼103.29', 'f6eb847eb943458191c2b48edda3dbf9', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:50', '2023-11-25 13:15:57', 'sa', 1);
INSERT INTO `sys_device` VALUES (250, 4, '4教学楼106.6', 'b352a53d0fa1468eb8e3e6dc91d90db1', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:04', '2023-11-25 13:15:25', 'sa', 1);
INSERT INTO `sys_device` VALUES (251, 4, '2教学楼105.18', '97ca4f75a59c40cd98b2787ede81ecf6', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:27', '2023-11-25 13:15:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (252, 4, '4教学楼106.7', '67d3a63e1e014e008fc8f397d49c2622', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:03', '2023-11-25 13:15:25', 'sa', 1);
INSERT INTO `sys_device` VALUES (253, 4, '2教学楼105.19', '623c0355d4f142f386710bce63c211c7', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:26', '2023-11-25 13:15:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (254, 4, '4教学楼106.8', 'e3ca98cf38e64507824510a034a38f8b', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:03', '2023-11-25 13:15:26', 'sa', 1);
INSERT INTO `sys_device` VALUES (255, 4, '1教学楼105.20', '6712f56408d94429adf7a98dc357f8fb', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:26', '2023-11-25 13:15:39', 'sa', 1);
INSERT INTO `sys_device` VALUES (256, 4, '3教学楼106.9', 'f5fcd2c786f940d0a0fea07821e34955', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 15:36:57', '2023-10-31 08:20:37', 'sa', 1);
INSERT INTO `sys_device` VALUES (257, 4, '1教学楼105.21', '0445a286e2a74d99942f87a6a5a16666', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:26', '2023-11-25 13:15:39', 'sa', 1);
INSERT INTO `sys_device` VALUES (258, 4, '3教学楼106.10', 'cb4cca9c1eac440e9031e627d864726c', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:02', '2023-11-25 13:15:26', 'sa', 1);
INSERT INTO `sys_device` VALUES (259, 4, '1教学楼105.22', '12d2a9a4f4424a3ead20a9144e9060f7', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:39', 'sa', 1);
INSERT INTO `sys_device` VALUES (260, 4, '教学楼103.30', 'a5b5a8b702a74213b8ae3662aec99d6a', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:50', '2023-11-25 13:15:57', 'sa', 1);
INSERT INTO `sys_device` VALUES (261, 4, '1教学楼105.23', 'abe786712a82482ab22fa4413b1673f0', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (262, 4, '1教学楼105.24', '958ccee872ec46cfa09f4d0def7d1e5f', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (263, 4, '1教学楼105.25', '11e3eb65155548d5ba099fcee11f94b9', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (264, 4, '教学楼103.31', '69a0aedcfcf84afa90346c9c6703ac19', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:49', '2023-11-25 13:15:57', 'sa', 1);
INSERT INTO `sys_device` VALUES (265, 4, '1教学楼105.26', '442b897c670d4f9a9c75be13313309f0', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (266, 4, '教学楼103.32', '3b2cbc2ef45142618b58730ab6fbf15a', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:49', '2023-11-25 13:15:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (267, 4, '1教学楼105.27', '331f6bd50b1b4306b8af4f6c5a098f9c', '77c95c4ec1784fcda1d0a41f68e94f97', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:25', '2023-11-25 13:15:41', 'sa', 1);
INSERT INTO `sys_device` VALUES (268, 4, '教学楼103.33', 'ea799dff4d6a4e85990ac9b6893cf447', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:55:49', '2023-11-25 13:15:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (269, 4, '教学楼102.22', 'd3ddd6ea495c442f95184a3ddaee88b5', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:39', '2023-11-25 13:16:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (270, 4, '教学楼103.34', 'bc3408666619497787398808e9d43aa5', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:26:07', '2023-11-25 13:15:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (271, 4, '教学楼103.36', '50234c24e64946dcbbb99d809f2d5369', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:26:07', '2023-11-25 13:15:59', 'sa', 1);
INSERT INTO `sys_device` VALUES (272, 4, '3教学楼106.11', '6c3d427af22d4a4ea541539d5d4acbbf', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:02', '2023-11-25 13:15:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (273, 4, '教学楼103.35', '6cb4e12b52d34cfaa56335247f70b488', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 17:34:39', '2023-11-01 18:31:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (274, 4, '3教学楼106.12', 'ddd0363c903c4b8c9cce6caef5f9e7e4', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:02', '2023-11-25 13:15:27', 'sa', 1);
INSERT INTO `sys_device` VALUES (275, 4, '教学楼103.37', '51b6553d35b74424ad7c0b309664b79f', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:26:07', '2023-11-25 13:15:59', 'sa', 1);
INSERT INTO `sys_device` VALUES (276, 4, '3教学楼106.13', '234f8e43fb964adbb9614ecddcc54a14', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:02', '2023-11-25 13:15:28', 'sa', 1);
INSERT INTO `sys_device` VALUES (277, 4, '3教学楼104.12', 'cdf7048c0314447783edbdef0c800e55', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:46', '2023-11-25 13:15:45', 'sa', 1);
INSERT INTO `sys_device` VALUES (278, 4, '2教学楼104.13', 'ce5ad452ca274352b4f586cf54bd2f66', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 07:59:01', '2023-11-01 13:10:21', 'sa', 1);
INSERT INTO `sys_device` VALUES (279, 4, '3教学楼106.31', '88c3c7326cdd413a9d07a22e51b139ac', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-29 17:19:23', '2023-11-25 13:15:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (280, 4, '教学楼103.38', 'a3cfddcaeb774713a4c04003f1dd4bcd', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:26:08', '2023-11-25 13:15:59', 'sa', 1);
INSERT INTO `sys_device` VALUES (281, 4, '教学楼103.39', '61ae7aaefe354e51815eb01be712e9ff', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 09:56:27', '2023-11-01 18:31:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (282, 4, '3教学楼106.14', '06a59a1f20a747a2b0b1534e2da9819e', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:01', '2023-11-25 13:15:28', 'sa', 1);
INSERT INTO `sys_device` VALUES (283, 4, '3教学楼106.15', 'af97c8b668e946f3b921e6f4171b9afc', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:01', '2023-11-25 13:15:29', 'sa', 1);
INSERT INTO `sys_device` VALUES (284, 4, '2教学楼106.16', 'adbbbc31dac14433b2bec341417c703a', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-30 15:36:57', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (285, 4, '教学楼103.40', 'c99bafc7a2fe4a12ad7458940f2e247c', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:07', '2023-11-25 13:16:00', 'sa', 1);
INSERT INTO `sys_device` VALUES (286, 4, '教学楼103.41', '5039cb18649644a9beef1bafa679d904', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:26:07', '2023-11-25 13:16:00', 'sa', 1);
INSERT INTO `sys_device` VALUES (287, 4, '教学楼103.42', '90c7a933eac44bbe8cea5b96f29d9a92', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:07', '2023-11-25 13:16:00', 'sa', 1);
INSERT INTO `sys_device` VALUES (288, 4, '教学楼102.23', 'd8ca2ff5fe654ba4a4e7c72337ddec63', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:39', '2023-11-25 13:16:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (289, 4, '教学楼102.24', '0a84ef7acb9f4f8ab14b90336284c626', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:38', '2023-11-25 13:16:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (290, 4, '教学楼102.25', '299c8431e1b24b89ad25f74256e97c44', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:50:47', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (291, 4, '2教学楼104.15', '5a570321012141a1830a5501ffe7f4d6', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:45', '2023-11-25 13:15:46', 'sa', 1);
INSERT INTO `sys_device` VALUES (292, 4, '2教学楼104.16', 'cd913a7798d042ebac4cffd17eadad89', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 08:41:50', '2023-11-01 13:10:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (293, 4, '教学楼102.26', 'f87ab359273f44cabaad462b60ab95cf', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 14:48:01', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (294, 4, '2教学楼104.17', 'f2cbeaf675f84c1e9d40518deccff18a', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:44', '2023-11-25 13:15:46', 'sa', 1);
INSERT INTO `sys_device` VALUES (295, 4, '教学楼103.44', '5c85523135964a34839dd0568dda5145', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:07', '2023-11-25 13:16:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (296, 4, '教学楼103.45', '6f81d90541744c98a180a1fbff7eaa09', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:07', '2023-11-25 13:16:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (297, 4, '教学楼103.46', '94d4b76beee44d1e807e085ad08cc807', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 10:03:14', '2023-11-01 18:31:04', 'sa', 1);
INSERT INTO `sys_device` VALUES (298, 4, '2F食堂入口103.100', '9ba228eb95aa4daab7d45b58eb52212e', '8510c034c21c463aaf79aca4f65dc9a8', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 08:52:01', '2023-11-25 13:15:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (299, 4, '教学楼102.28', '56b182860afd456497a6e17b9bc8e44b', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-12 17:11:09', '2023-11-25 13:14:56', 'sa', 1);
INSERT INTO `sys_device` VALUES (300, 4, '教学楼102.29', '54d97fcbc1ab4325a40842106cf0c29e', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 12:38:19', '2023-11-25 13:15:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (301, 4, '教学楼102.3', 'ef8d4aab4f864c2483be110480c30a37', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:45', '2023-11-25 13:16:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (302, 4, '教学楼102.30', '748275eb3ee64f3faa162d63d8bdc495', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:36', '2023-11-25 13:16:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (303, 4, '教学楼102.31', '88885cce79aa41a7a522c8f257c2f742', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:36', '2023-11-25 13:16:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (304, 4, '教学楼102.32', 'bba7b67df8cc456cb5aa2006223485c5', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:36', '2023-11-25 13:16:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (305, 4, '教学楼102.33', '166f42ee158d41c1b79061050acb959b', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:35', '2023-11-25 13:16:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (306, 4, '教学楼102.34', 'c5a9e626515a42b1b8b52c92521dfde5', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:35', '2023-11-25 13:16:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (307, 4, '教学楼102.35', 'e6b3483d47ba4e4a82cd762b2b13eecd', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 09:54:06', '2023-11-25 13:15:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (308, 4, '2教学楼104.18', '9376e2b0eadf493393fadd05e6424d71', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:44', '2023-11-25 13:15:47', 'sa', 1);
INSERT INTO `sys_device` VALUES (309, 4, '2教学楼104.19', '58a861d7fa15442b93e8a7c0c03ea9a1', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:44', '2023-11-25 13:15:47', 'sa', 1);
INSERT INTO `sys_device` VALUES (310, 4, '教学楼102.36', '66c912a0b2ff410283648a4f462555f1', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:34', '2023-11-25 13:16:07', 'sa', 1);
INSERT INTO `sys_device` VALUES (311, 4, '1教学楼104.20', '9256e80af72f455da652295013285253', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:43', '2023-11-25 13:15:47', 'sa', 1);
INSERT INTO `sys_device` VALUES (312, 4, '教学楼102.37', '417e004071d546359c38b5eda7046316', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:34', '2023-11-25 13:16:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (313, 4, '1教学楼104.21', '7c62e7c1a1134640b297e17d05916a5d', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:43', '2023-11-25 13:15:48', 'sa', 1);
INSERT INTO `sys_device` VALUES (314, 4, '1教学楼104.22', 'cec72cca0db847fb94d95d8aaab92e2a', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:43', '2023-11-25 13:15:48', 'sa', 1);
INSERT INTO `sys_device` VALUES (315, 4, '1教学楼104.23', '6021a63795254ccca1be46cad9fc5364', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:42', '2023-11-25 13:15:48', 'sa', 1);
INSERT INTO `sys_device` VALUES (316, 4, '1教学楼104.24', '2ec5f527b55f444bac8534983ed49fd0', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:42', '2023-11-25 13:15:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (317, 4, '1教学楼104.25', 'aa0a0c0d43fa426abb9603a949290b49', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-01 09:52:09', '2023-11-01 13:11:36', 'sa', 1);
INSERT INTO `sys_device` VALUES (318, 4, '教学楼102.38', 'c42a8cbb80204af3815570a4f74860e2', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:34', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (319, 4, '1教学楼104.26', 'f4e7b2ef6c574371afce266c02ac269b', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:42', '2023-11-25 13:15:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (320, 4, '1教学楼104.27', 'c3e45c27f04146c9855de56c8e90a0a8', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:42', '2023-11-25 13:15:49', 'sa', 1);
INSERT INTO `sys_device` VALUES (321, 4, '1教学楼104.28', '29a33fd8c5fc41e5b06b0b5e9b7d99cd', '2cb644fddd3345fe8e73057da0c49c53', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:56:42', '2023-11-25 13:15:50', 'sa', 1);
INSERT INTO `sys_device` VALUES (322, 4, '教学楼102.39', 'e095da28b0e14955b1297d029fe1e707', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:33', '2023-11-25 13:16:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (323, 4, '教学楼102.4', 'a498f4d3e00b49e4bfb15224460ca22a', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:45', '2023-11-25 13:16:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (324, 4, '教学楼打菜区102.40', 'fc256141261e453690b005e99787d220', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-22 14:48:07', '2024-02-27 11:06:25', 'sa', 1);
INSERT INTO `sys_device` VALUES (325, 4, '教学楼102.41', '649d9e2e30db41399cd8d7f05f6904f6', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:32', '2023-11-25 13:16:08', 'sa', 1);
INSERT INTO `sys_device` VALUES (326, 4, '食堂打菜区102.42', 'f9c628bfe9c74bc4866ad03b123f0e4b', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:32', '2024-02-27 11:06:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (327, 4, '教学楼102.43', '7a98999456ba4e819478700d6bdb6965', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:32', '2023-11-25 13:16:09', 'sa', 1);
INSERT INTO `sys_device` VALUES (328, 4, '教学楼102.44', 'bc22a7bc436f4e679b44c7ebd703aa0f', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:32', '2023-11-25 13:16:09', 'sa', 1);
INSERT INTO `sys_device` VALUES (329, 4, '教学楼102.45', '7db88ce524fc4951b73482ca22e61755', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-02 15:33:51', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (330, 4, '教学楼102.46', '4debf27e9cb94d9abfd696c942045df7', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:34', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (331, 4, '教学楼102.47', 'fff561ec15374911927c9d58f0a33540', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:31', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (332, 4, '教学楼102.48', '2752c8b7c1e04877a9038bf6eaba503a', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:31', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (333, 4, '教学楼102.49', '7c3b712206d941a8870401c622cb96c3', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-02 11:03:27', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (334, 4, '教学楼102.5', '67927235c8ee4e468fd5d03dbeaba234', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-03 14:12:10', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (335, 4, '行政楼107.28', 'af70a56dbf8b481aa30f494d11b90a29', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:08', '2023-11-25 13:15:14', 'sa', 1);
INSERT INTO `sys_device` VALUES (336, 4, '行政楼107.29', 'e556bd93db9c441baf9ccd113ab5c72a', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 07:14:58', '2023-10-06 07:46:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (337, 4, '行政楼107.30', '7cff3108b9f742c4901ae176996f08fa', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:07', '2023-11-25 13:15:14', 'sa', 1);
INSERT INTO `sys_device` VALUES (338, 4, '行政楼107.31', '0a4cba48728845a4a094ccf19bb4c778', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:06', '2023-11-25 13:15:15', 'sa', 1);
INSERT INTO `sys_device` VALUES (339, 4, '行政楼107.32', 'e08034d47c3e42919b36e9cc3bcf64c8', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-01 10:55:23', '2023-10-06 07:46:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (340, 4, '行政楼107.33', '3b7b6054b12146c0987bf1f006d284ec', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:06', '2023-11-25 13:15:15', 'sa', 1);
INSERT INTO `sys_device` VALUES (341, 4, '行政楼107.34', '9851d2ce502b43268986a7b4b20b7615', '34fa131b393949ca99e2245f69cc3114', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:59:06', '2023-11-25 13:15:16', 'sa', 1);
INSERT INTO `sys_device` VALUES (342, 4, '2教学楼106.17', 'a79ea8afdf224de78933c109e3190da1', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:54', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (343, 4, '2教学楼106.18', '6fbab09d62b640f8b8fc7d1d45fff64e', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:54', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (344, 4, '2教学楼106.19', '37109a47f504475bbe47ba1a11286e2e', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:58:00', '2023-11-25 13:15:29', 'sa', 1);
INSERT INTO `sys_device` VALUES (345, 4, '2教学楼106.20', 'e358b11c3788408bbfc4ad647f307c86', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:54', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (346, 4, '2教学楼106.21', '55567e2e28c64aee85cc97da4448a77c', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:51', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (347, 4, '2教学楼106.32', '163632feda484cf8a1c9f30dbefbfe30', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-31 08:15:06', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (348, 4, '2教学楼106.22', 'a93f5e4c744e4cebb8616a9989a3b5e4', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:51', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (349, 4, '1教学楼106.23', 'bdc15d9fed374f9285e3ea3c34e4062b', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-18 07:51:51', '2023-10-31 08:20:38', 'sa', 1);
INSERT INTO `sys_device` VALUES (350, 4, '1教学楼106.24', 'ef507790836e4e9c9fa81553ce432838', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:58', '2023-11-25 13:15:30', 'sa', 1);
INSERT INTO `sys_device` VALUES (351, 4, '1教学楼106.25', '6ebc8a7459f14a169e415b5b636dacc2', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:58', '2023-11-25 13:15:30', 'sa', 1);
INSERT INTO `sys_device` VALUES (352, 4, '1教学楼106.26', 'd3094b22f04043af88dd4baba1bbc317', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:58', '2023-11-25 13:15:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (353, 4, '1教学楼106.27', '77d0d67498894413a3c8e0dae38c8f55', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:57', '2023-11-25 13:15:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (354, 4, '1教学楼106.28', '1ff1994b29c64b0eb3bb6668e5f63493', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:57', '2023-11-25 13:15:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (355, 4, '1教学楼106.29', 'c8f7f6d827394b1db9c013e5f03fd1b7', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:57', '2023-11-25 13:15:31', 'sa', 1);
INSERT INTO `sys_device` VALUES (356, 4, '1教学楼106.30', '35171d69a2db48e885104beaa7201f4f', '05ba6ff7073d4881b9c4929b6f5602fd', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:57:57', '2023-11-25 13:15:32', 'sa', 1);
INSERT INTO `sys_device` VALUES (357, 4, '教学楼102.6', '79d4028d267141899acb86c576162be7', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-10-06 13:25:33', '2023-11-25 13:15:02', 'sa', 1);
INSERT INTO `sys_device` VALUES (358, 4, '教学楼102.7', '98ea92b708b044c9998f6eda6c6cfc4d', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 16:54:44', '2023-11-25 13:16:03', 'sa', 1);
INSERT INTO `sys_device` VALUES (359, 4, '教学楼102.9', 'de7a841717df45e691495da2e9c0cf4f', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-02 10:09:58', '2023-11-25 13:15:00', 'sa', 1);
INSERT INTO `sys_device` VALUES (360, 4, '食堂102.50', 'ce697fc9fd374dd48fbf6df4d2fe9abf', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:20', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (361, 4, '食堂102.51', '5acbcb858c044ae3aaebb891da9e548c', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:13', '2023-11-25 13:16:10', 'sa', 1);
INSERT INTO `sys_device` VALUES (362, 4, '食堂102.52', '1d7eee89b9004258b459d03a0e88c308', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:13', '2023-11-25 13:16:10', 'sa', 1);
INSERT INTO `sys_device` VALUES (363, 4, '食堂102.53', '41e2d9c2b39148df8d0a0f8ccb4c4f20', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:20', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (364, 4, '食堂102.54', '79c9df295ea84bb88addb465e854c878', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:13', '2023-11-25 13:16:10', 'sa', 1);
INSERT INTO `sys_device` VALUES (365, 4, '食堂备菜间102.55', '0fdfc405d99149549cc30bc38bead8f2', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2024-02-27 11:05:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (366, 4, '食堂打菜区102.56', '18586803a6324030a15a38bfd8775636', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2024-02-27 11:06:01', 'sa', 1);
INSERT INTO `sys_device` VALUES (367, 4, '食堂102.57', 'da7d4b20b2614374898c250a990ff5e1', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (368, 4, '食堂102.58', '799b962577fb46cc912c4bf3c222e291', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:11', 'sa', 1);
INSERT INTO `sys_device` VALUES (369, 4, '食堂102.60', '8ab73d67d93e408184c2b69c2738ad71', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:20', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (370, 4, '食堂打菜区102.61', '235e47f83cb94cebb640a7ccaa3c7fdc', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2024-02-27 11:05:40', 'sa', 1);
INSERT INTO `sys_device` VALUES (371, 4, '食堂102.62', '65b1747c30e542b08b58186d66e3536a', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (372, 4, '食堂102.63', 'baf712433c644d139e6d8d2248ea1ef7', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:20', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (373, 4, '食堂102.64', '40c82c22c77a4326a818f83e3719e365', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (374, 4, '食堂102.65', '4e1b61a5214b4c5b89ee953ab32a07e1', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (375, 4, '食堂102.66', 'a2cc6db61e264cb48c8e2e4e8e7e384f', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-09-04 07:11:20', '2023-11-15 15:32:54', 'sa', 1);
INSERT INTO `sys_device` VALUES (376, 4, '食堂102.67', '6ee71e7419de4e8ca1ad5ef187548eb2', 'ecfe75a1e24346148c5d5a3cfaaa256f', NULL, 'analog', NULL, NULL, 0, '', NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-08-23 17:32:14', '2023-11-25 13:16:12', 'sa', 1);
INSERT INTO `sys_device` VALUES (377, 4, '食堂二次更衣室', 'c37c88b996a8453ca15c7b891da77f5b', 'ecfe75a1e24346148c5d5a3cfaaa256f', '1楼', 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-14 09:29:48', '2023-11-25 13:14:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (378, 4, '食堂洗碗间', 'd51835c49bef4d05b23f961860b7d776', 'ecfe75a1e24346148c5d5a3cfaaa256f', '1楼', 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-14 09:30:07', '2023-11-25 13:14:51', 'sa', 1);
INSERT INTO `sys_device` VALUES (379, 4, '食堂消毒间', 'be15d70c649f49e190ad701c004dff09', 'ecfe75a1e24346148c5d5a3cfaaa256f', '1楼', 'analog', NULL, NULL, 0, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2023-11-14 09:30:07', '2023-11-25 13:14:51', 'sa', 1);
INSERT INTO `sys_device` VALUES (380, 5, '实验小学摄像机(20231013)', 'AF7891684', '652202783020784', NULL, NULL, NULL, 4, 1, NULL, 'V4.75.000 build 230620', 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-7604N-E1-V3', NULL, NULL, '183.246.161.60', NULL, 'PTNn1vdK', NULL, NULL, NULL, 'sa', 1, '2023-10-13 11:28:27', '2023-10-13 15:14:05', 'sa', 1);
INSERT INTO `sys_device` VALUES (381, 5, '白云中学1号摄像头', 'K00958645', '652202783020784', NULL, NULL, NULL, 1, 1, NULL, 'V5.3.0 build 220419', 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'CS-H6c-V101-1G2WF', NULL, NULL, '122.226.158.135', NULL, 'GJXOSS', NULL, NULL, NULL, 'sa', 1, '2023-03-22 15:03:49', '2023-03-22 15:03:50', 'sa', 1);
INSERT INTO `sys_device` VALUES (382, 5, '白云中学2号摄像头', 'K00958958', '652202783020784', NULL, NULL, NULL, 1, 1, NULL, 'V5.3.0 build 220419', 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'CS-H6c-V101-1G2WF', NULL, NULL, '122.226.158.135', NULL, 'LCOFUK', NULL, NULL, NULL, 'sa', 1, '2023-03-22 15:01:29', '2023-03-22 15:01:29', 'sa', 1);
INSERT INTO `sys_device` VALUES (383, 5, '实验小学1号摄像头', 'F60068965', '652202783020784', NULL, NULL, NULL, 1, 1, NULL, 'V5.3.5 build 211028', 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 'CS-C3HC-3G2WFL1', NULL, NULL, '183.246.161.60', NULL, 'LAQKFU', NULL, NULL, NULL, 'sa', 1, '2022-10-25 14:01:24', '2022-10-25 14:01:24', 'sa', 1);
INSERT INTO `sys_device` VALUES (384, 10, '新谊村老年活动室1-枪', 'cd64cf32c0b04453bacaa0b581abb50f', 'root00000000', NULL, 'digital', '53', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-28 18:05:59', '2024-11-01 10:59:18', NULL, NULL);
INSERT INTO `sys_device` VALUES (385, 10, '新谊村老年活动室2-枪', '6fc35e4ff6344b468f45f57dc86aa5df', 'root00000000', NULL, 'digital', '54', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-28 18:05:59', '2024-11-01 10:59:18', NULL, NULL);
INSERT INTO `sys_device` VALUES (386, 10, '新谊村老年活动室3-枪', '4cdb122713c544d3899ea82da6b7139d', 'root00000000', NULL, 'digital', '55', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-28 18:05:59', '2024-11-01 10:59:18', NULL, NULL);
INSERT INTO `sys_device` VALUES (387, 10, '新谊村老年活动室4-枪', 'c5b2cb6759e1467eb1898d779ad9ee35', 'root00000000', NULL, 'digital', '56', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-28 18:05:59', '2024-11-01 10:59:18', NULL, NULL);
INSERT INTO `sys_device` VALUES (388, 10, '老年活动室路口—球', 'ab095bdb30184f708e57845c462f370d', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-29 17:55:22', '2024-10-29 17:55:44', NULL, NULL);
INSERT INTO `sys_device` VALUES (389, 10, '金园路与建浦路交叉口-球', '861d4d44623c4f10b5805016bc15e9f8', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 15:59:05', '2024-10-30 16:03:06', NULL, NULL);
INSERT INTO `sys_device` VALUES (390, 10, '建浦路和东三路岔口1-枪', '76d222b1e1d64f1a90c92431aeab1e25', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 16:11:09', '2024-10-30 16:24:16', NULL, NULL);
INSERT INTO `sys_device` VALUES (391, 10, '颖雨棋牌转弯处2-枪', 'fa7604020a8a4a8fa34a8fe10fd7aa58', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 16:24:27', '2024-10-30 17:10:02', NULL, NULL);
INSERT INTO `sys_device` VALUES (392, 10, '厂区朱家厕所旁-枪', 'b9d82aedb4ea47ac876792128eb1a567', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:09:34', '2024-10-30 17:10:01', NULL, NULL);
INSERT INTO `sys_device` VALUES (393, 10, '水库监控-球', 'cd94e92e737746da8ccd2636a1934276', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.23, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:09:35', '2024-10-30 17:10:01', NULL, NULL);
INSERT INTO `sys_device` VALUES (394, 10, '土地庙往西岔路口-球', '3b66bf237a614f88936e68e4f8b73c99', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.23, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:17:54', '2024-10-31 11:14:59', NULL, NULL);
INSERT INTO `sys_device` VALUES (395, 10, '茗渎坞东思线岔口2-枪', '695103fd7f1d4a5d8ecaf6320f2fdc83', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:24:31', '2024-10-31 11:14:58', NULL, NULL);
INSERT INTO `sys_device` VALUES (396, 10, '茗渎坞东思线岔口-枪', '4155f7cea5e14ccb994fad43daab4504', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:27:01', '2024-10-31 11:14:57', NULL, NULL);
INSERT INTO `sys_device` VALUES (397, 10, '包洪线岔口2-球', 'd92322970f08476288e6c0bbf6e7516f', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:36:46', '2024-11-01 09:31:46', NULL, NULL);
INSERT INTO `sys_device` VALUES (398, 10, '包洪线岔口1-球', '32021bb8e6bb4e8098fb589f67b95cfa', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:39:04', '2024-10-31 11:14:55', NULL, NULL);
INSERT INTO `sys_device` VALUES (399, 10, '金园路包洪线交叉口-球', 'cab773b32c764b3ba58f26eda4b8e69b', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:52:08', '2024-10-31 11:14:54', NULL, NULL);
INSERT INTO `sys_device` VALUES (400, 10, '萧山区三岔口-球', 'b84f4d8b9ed5443581bcde2b8916145b', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 17:58:13', '2024-10-31 11:14:53', NULL, NULL);
INSERT INTO `sys_device` VALUES (401, 10, '公园湖边3-球', '19863f95e5cc42fd85d8c79e118b7749', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 18:03:29', '2024-10-31 11:14:52', NULL, NULL);
INSERT INTO `sys_device` VALUES (402, 10, '公园湖边2-枪', 'aae3cfd31a1b444b9dff7d422313357b', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 18:07:40', '2024-10-31 11:14:51', NULL, NULL);
INSERT INTO `sys_device` VALUES (403, 10, '村委门口-枪', '6b54a6ec89fc4b24b449ec497d9ce174', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 18:22:47', '2024-10-31 11:14:50', NULL, NULL);
INSERT INTO `sys_device` VALUES (404, 10, '村委门口-球', '9cc3643c4a4b443caf26f37e79e5478e', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29.98, 120.24, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-30 18:25:05', '2024-10-31 11:14:49', NULL, NULL);
INSERT INTO `sys_device` VALUES (405, 10, '颖雨棋牌转弯处1-枪', '9ca6054f0cf8466b9f4432e34412b234', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 09:23:26', '2024-11-01 09:28:32', NULL, NULL);
INSERT INTO `sys_device` VALUES (406, 10, '停车场2-枪', '233fc1c5e5614046a69162f615a9c9af', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 09:23:27', '2024-11-01 09:28:31', NULL, NULL);
INSERT INTO `sys_device` VALUES (407, 10, '茗坞村岔口-枪', '4c2b93e68ff24a4288efda39a9cd8c50', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 09:24:03', '2024-11-01 09:28:30', NULL, NULL);
INSERT INTO `sys_device` VALUES (408, 10, '停车场1-枪', '394325fb38954dafa55e3ed784613bc2', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 09:24:35', '2024-11-01 09:28:29', NULL, NULL);
INSERT INTO `sys_device` VALUES (409, 10, '东思线-枪', '59efa08bc032424194d2b9c032871fab', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 09:24:35', '2024-11-01 09:28:28', NULL, NULL);
INSERT INTO `sys_device` VALUES (410, 10, '建浦路和东三路岔口2-枪', '47f64dc6cb8a4070883e3d10d862fbfc', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 10:07:49', '2024-11-01 10:09:41', NULL, NULL);
INSERT INTO `sys_device` VALUES (411, 10, '金园路转弯路口-枪', '3809ba8a52fd4074b54c2f8ce8a4882a', 'root00000000', NULL, 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 10:18:47', '2024-11-01 10:19:24', NULL, NULL);
INSERT INTO `sys_device` VALUES (412, 10, '公园湖边1-枪', '4ff002f19e2c468d9652103856ad1c56', 'root00000000', '根节点', 'analog', '1', 1, 1, NULL, NULL, 'CAMERA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-11-01 10:31:38', '2024-11-01 10:34:25', 'sa', 1);
INSERT INTO `sys_device` VALUES (413, 5, '设备1', '11', NULL, NULL, NULL, '1', 1, 0, NULL, NULL, 'CAMERA', 'admin', 'Wlxc258369', 'rtsp://${sysDeviceAccount}:${sysDevicePassword}@${sysDeviceNetAddress}/cam/realmonitor?channel=${channelNo}&subtype=${subtype}', 'http://127.0.0.1:8000', '7', NULL, NULL, 1, NULL, NULL, NULL, '115.231.119.62:554', '浙江省 杭州市', NULL, '0', '1', NULL, 'sa', 1, '2025-02-11 16:11:30', '2025-03-26 19:16:58', 'sa', 1);
INSERT INTO `sys_device` VALUES (414, 20, 'DS-K(FN2476983)', 'FN2476983', '122a4c58ca0b46c99bd11c1292530a7b', '331022-S000050', NULL, NULL, 2, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-21 17:17:43', 'sa', 1);
INSERT INTO `sys_device` VALUES (415, 20, 'DS-K(FN2494125)', 'FN2494125', '122a4c58ca0b46c99bd11c1292530a7b', NULL, NULL, NULL, 2, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 13:08:52', 'sa', 1);
INSERT INTO `sys_device` VALUES (416, 20, '通道七', 'FR0202058', '122a4c58ca0b46c99bd11c1292530a7b', '331022-S000050', NULL, NULL, 4, 1, NULL, 'V3.0.0 build 240723', 'MEN_JIN', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TMSW', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (417, 20, 'DS-K(FN2494123)', 'FN2494123', '122a4c58ca0b46c99bd11c1292530a7b', NULL, NULL, NULL, 2, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 14:10:06', 'sa', 1);
INSERT INTO `sys_device` VALUES (418, 20, 'DS-K(FN2494126)', 'FN2494126', '122a4c58ca0b46c99bd11c1292530a7b', NULL, NULL, NULL, 2, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (419, 20, 'DS-K(FN2494124)', 'FN2494124', '122a4c58ca0b46c99bd11c1292530a7b', NULL, NULL, NULL, 2, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device` VALUES (420, 20, 'DS-K(FN3104779)', 'FN3104779', '122a4c58ca0b46c99bd11c1292530a7b', NULL, NULL, NULL, 4, 1, NULL, 'V3.0.0 build 240723', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 'DS-K3M220-B221TMSW', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 10:31:09', '2025-03-18 13:08:54', 'sa', 1);

-- ----------------------------
-- Table structure for sys_device_card_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_card_event`;
CREATE TABLE `sys_device_card_event`  (
  `sys_device_card_event_id` int(11) NOT NULL,
  `sys_device_card_event_time` datetime NULL DEFAULT NULL COMMENT '时间',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `sys_device_card_event_person_involved` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人ID',
  `sys_device_card_event_person_involved_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人姓名',
  `sys_device_card_event_major_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主事件类型',
  `sys_device_card_event_minor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '次事件类型',
  `sys_device_card_event_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_card_event_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '门禁事件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device_card_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_channel
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_channel`;
CREATE TABLE `sys_device_channel`  (
  `sys_device_channel_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道名称',
  `sys_device_channel_no` int(11) NULL DEFAULT NULL COMMENT '通道号',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `sys_device_channel_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道类型，10300:视频通道，10302：报警输入',
  `sys_device_channel_status` int(11) NULL DEFAULT NULL COMMENT '通道状态，0：离线，1：在线，-1：设备未上报',
  `sys_device_channel_use` int(11) NULL DEFAULT NULL COMMENT '通道启用状态：0-禁用；1-启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_channel_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备管道' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device_channel
-- ----------------------------
INSERT INTO `sys_device_channel` VALUES (1, 'DS-K(FN2476983)', 0, 'FN2476983', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:48', '2025-03-18 10:21:30', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (2, 'DS-K(FN2494125)', 0, 'FN2494125', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:50', '2025-03-18 10:21:30', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (6, 'DS-K(FN2494123)', 0, 'FN2494123', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:50', '2025-03-18 10:21:31', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (7, 'DS-K(FN2494126)', 0, 'FN2494126', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:51', '2025-03-18 10:21:31', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (8, 'DS-K(FN2494124)', 0, 'FN2494124', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:51', '2025-03-18 10:21:32', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (9, '视频1@DS-K(FN3104779)', 1, 'FN3104779', '10300', 1, 0, 'sa', 1, '2025-03-18 09:24:51', '2025-03-18 10:21:32', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (10, '视频2@DS-K(FN3104779)', 2, 'FN3104779', '10300', 1, 0, 'sa', 1, '2025-03-18 09:24:51', '2025-03-18 10:21:32', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (11, 'DS-K(FN3104779)', 0, 'FN3104779', '30201', 1, 1, 'sa', 1, '2025-03-18 09:24:51', '2025-03-18 10:21:32', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (16, '视频1@DS-K(FR0202058)', 1, 'FR0202058', '10300', 1, 0, 'sa', 1, '2025-03-18 13:08:22', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (17, '视频2@DS-K(FR0202058)', 2, 'FR0202058', '10300', 1, 0, 'sa', 1, '2025-03-18 13:08:22', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (18, 'DS-K(FR0202058)', 0, 'FR0202058', '30201', 1, 1, 'sa', 1, '2025-03-18 13:08:22', '2025-03-18 13:08:53', 'sa', 1);
INSERT INTO `sys_device_channel` VALUES (20, '通道号1', 1, '11', '', 1, 1, 'sa', 1, '2025-03-27 09:47:45', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_device_message_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_message_event`;
CREATE TABLE `sys_device_message_event`  (
  `sys_device_message_event_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_device_message_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `sys_device_message_event_event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件ID',
  `sys_device_message_event_data_time` datetime NULL DEFAULT NULL COMMENT '事件时间',
  `sys_device_message_event_employee_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员ID',
  `sys_device_message_event_employee_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `sys_device_message_event_card_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卡号',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `sys_device_message_event_channel_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发报警的设备通道号',
  `sys_device_message_event_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发报警的设备通道名称',
  `sys_device_message_event_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发设备通道位置',
  `sys_device_message_event_card_reader_kind` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器	',
  `sys_device_message_event_card_reader_no` int(11) NULL DEFAULT NULL COMMENT '读卡器编号',
  `sys_device_message_event_door_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门编号',
  `sys_device_message_event_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件消息类型',
  `sys_device_message_event_event_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件消息类型名称',
  `sys_device_message_event_event_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件码',
  `sys_device_message_event_event_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件描述',
  `sys_device_message_event_event_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件来源(目前暂未使用)',
  `sys_device_message_event_event_picture_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_message_event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '事件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device_message_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_org`;
CREATE TABLE `sys_device_org`  (
  `sys_device_org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织ID',
  `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
  `sys_device_org_pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上级组织',
  `sys_device_org_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备组织' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device_org
-- ----------------------------
INSERT INTO `sys_device_org` VALUES ('05ba6ff7073d4881b9c4929b6f5602fd', '5楼', '8c015c920d034d22a7f057fa105b580f', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('122a4c58ca0b46c99bd11c1292530a7b', '331022-S000050', NULL, '331022S000050', 20, 'sa', 1, '2025-03-18 13:08:54', '2025-03-18 13:08:54', 'sa', 1);
INSERT INTO `sys_device_org` VALUES ('25b773e5f16848549b77f4b8cba6d85a', '3楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('2b511f35e33e4d788dcdcb06260599c7', '1年级教室', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('2cb644fddd3345fe8e73057da0c49c53', '3楼', '8c015c920d034d22a7f057fa105b580f', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('320a84af4f0d4cf88eb2306968ccf980', '9楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('34fa131b393949ca99e2245f69cc3114', '2楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('4002633ca7cc466988a91560bbb6a9cc', '室外', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('42e80bef18e8476a875dbd03fba35e71', '6楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('4d7c6257a1c7475b85a38eccfd6feea4', '8楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('67c815b1878043edaf2920cab7662e8e', '1楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('77c95c4ec1784fcda1d0a41f68e94f97', '4楼', '8c015c920d034d22a7f057fa105b580f', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('8510c034c21c463aaf79aca4f65dc9a8', '2楼', '8c015c920d034d22a7f057fa105b580f', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('8c015c920d034d22a7f057fa105b580f', '教学楼', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('8ea259ac17ac4d31b4bfe35608d88427', '5楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('956dcd62dfa94a2085f1c5f62c40ad7a', '新谊村', 'root00000000', NULL, 10, 'sa', 1, '2025-02-11 15:52:46', '2025-02-11 15:52:46', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('a20a05f4b4d544f9aee4ae1d0a745c24', '4楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('bdd001adfdf4418689c30f49c41fc03b', '地下室', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('d110d790a96443148bdca48f48cd753d', '行政楼', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('d374331f1bca426587ae9c51ae922af1', '屋顶球机', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('e431804a28e949329e367e37a77be93a', '行政楼专业教室10间', 'root000000', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('ecfe75a1e24346148c5d5a3cfaaa256f', '1楼', '8c015c920d034d22a7f057fa105b580f', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('ef8bc1ff16ed4d77b58b4511fa00b2db', '7楼', 'd110d790a96443148bdca48f48cd753d', NULL, 4, 'sa', 1, '2025-02-05 15:48:31', '2025-02-05 15:48:31', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('root000000', '心海小学', '-1', NULL, 4, 'sa', 1, '2025-02-05 15:48:30', '2025-02-05 15:48:30', NULL, NULL);
INSERT INTO `sys_device_org` VALUES ('root00000000', '根节点', '-1', NULL, 10, 'sa', 1, '2025-02-11 15:52:46', '2025-02-11 15:52:46', NULL, NULL);

-- ----------------------------
-- Table structure for sys_device_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_record`;
CREATE TABLE `sys_device_record`  (
  `sys_camera_record_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_camera_record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摄像头记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_device_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `sys_dict_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
  `sys_dict_status` int(11) NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
  `sys_dict_in_system` int(11) NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
  `sys_dict_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
  `sys_dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `sys_dict_pid` int(11) NULL DEFAULT NULL COMMENT 'PID',
  `sys_dict_sort` int(11) NULL DEFAULT NULL COMMENT '排序字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, '厂家', 1, 0, 'CHANG_JIA', '三方厂家', '', 0, NULL, 'sa', NULL, '2024-08-31 20:29:00', '2024-08-31 20:35:57', 'sa', 1);
INSERT INTO `sys_dict` VALUES (2, '模板类型', 1, 0, 'MU_BAN_LEI_XING', '', '', 0, NULL, 'sa', NULL, '2024-09-01 18:16:46', '2024-09-09 09:33:56', 'sa', 1);
INSERT INTO `sys_dict` VALUES (3, '短信', 1, 0, 'DUAN_XIN', '', '', 2, NULL, 'sa', 1, '2024-09-07 09:07:09', '2024-09-09 09:23:22', 'sa', 1);
INSERT INTO `sys_dict` VALUES (4, '微信', 1, 0, 'WEI_XIN', NULL, NULL, 2, NULL, 'sa', 1, '2024-09-09 10:07:50', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (5, '组件功能', 1, 0, 'ZU_JIAN_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2024-10-24 17:17:50', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (6, '项目功能', 1, 0, 'XIANG_MU_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2025-01-24 15:50:58', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (7, '设备类型', 1, 0, 'SHE_BEI_LEI_XING', '', '', 0, NULL, 'sa', 1, '2025-02-11 16:20:16', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `sys_dict_item_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dict_id` int(11) NULL DEFAULT NULL COMMENT '字典ID',
  `sys_dict_item_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sys_dict_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典子项名称',
  `sys_dict_item_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_item_status` int(11) NULL DEFAULT NULL COMMENT '状态;0: 隐藏',
  `sys_dict_item_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_dict_item_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `sys_dict_item_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `sys_dict_item_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_dict_item_property` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可配置项',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (1, 1, NULL, '海康云曜', '海康云曜', 1, 1, '', 'HAI_KANG_YUN_YAO', 0, NULL, 'sa', NULL, '2024-09-01 10:02:02', '2024-09-01 10:08:45', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (2, 1, NULL, '阿里云', '阿里云', 1, 1, NULL, 'ALIYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:03', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (3, 1, NULL, '百度云', '百度云', 1, 1, NULL, 'BDYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:13', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (4, 2, NULL, '短信', '', 1, 1, '', 'SMS', 0, NULL, 'sa', 1, '2024-09-03 13:35:36', '2024-09-09 10:04:58', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (5, 3, NULL, '短信通知', '', 1, 2, '', 'DUAN_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-03 13:48:51', '2024-09-05 16:13:21', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (6, 2, NULL, '站内', '', 1, 2, '', 'WEB', 0, NULL, 'sa', 1, '2024-09-03 13:54:31', '2024-09-03 13:55:18', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (7, 2, NULL, '微信', NULL, 1, 2, NULL, 'WECHAT', 0, NULL, 'sa', 1, '2024-09-03 13:55:13', '2024-09-05 15:54:08', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (8, 3, NULL, '验证码', '', 1, 1, '', 'YAN_ZHENG_MA', 0, NULL, 'sa', 1, '2024-09-09 09:24:59', '2024-09-09 09:31:22', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (9, 4, NULL, '微信通知', NULL, 1, NULL, NULL, 'WEI_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-09 10:08:38', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (10, 1, NULL, '讯飞', '', 1, 1, '', 'XUN_FEI', 0, 'sysProjectAppId,sysProjectAppSecret', 'sa', 1, '2024-10-24 15:07:24', '2025-02-17 10:38:56', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (11, 5, NULL, '天气', '', 1, 1, '', 'TIAN_QI', 0, NULL, 'sa', 1, '2024-10-24 17:18:02', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (12, 5, NULL, '时间', '', 1, 1, '', 'SHI_JIAN', 0, NULL, 'sa', 1, '2024-10-28 09:31:55', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (13, 6, 'fa-solid:sms', '短信', NULL, 1, 1, NULL, 'DUAN_XIN', 0, NULL, 'sa', 1, '2025-01-24 15:51:14', '2025-01-24 15:51:23', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (14, 6, 'ep:video-camera', '摄像头', '', 1, 1, '', 'SHE_XIANG_TOU', 1, NULL, 'sa', 1, '2025-01-24 15:51:33', '2025-01-24 15:54:21', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (15, 6, 'humbleicons:eye', '人像识别', '', 1, 1, '', 'REN_XIANG_SHI_BIE', 0, NULL, 'sa', 1, '2025-01-25 18:55:49', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (16, 6, 'humbleicons:mail', '邮件', '', 1, 1, '', 'YOU_JIAN', 0, NULL, 'sa', 1, '2025-01-27 13:47:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (17, 6, 'pixelarticons:device-tv-smart', '设备', '', 1, 1, '', 'SHE_BEI', 0, NULL, 'sa', 1, '2025-01-27 13:48:31', '2025-02-06 13:36:28', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (18, 1, 'bi:pc-display-horizontal', '内部系统', '', 1, 1, '', 'NEI_BU_XI_TONG', 0, NULL, 'sa', 1, '2025-01-27 13:49:49', '2025-02-17 10:32:02', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (19, 1, NULL, '海康安防', '海康安防', 1, 1, '', 'HAI_KANG_AN_FANG', 0, 'sysProjectAppId,sysProjectAppSecret,sysProjectEndpoint', 'sa', 1, '2025-02-05 14:43:32', '2025-02-05 14:43:40', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (20, 6, 'ri:robot-2-line', '大语言', '', 1, 1, '', 'DA_YU_YAN', 0, 'sysProjectAppKey,sysProjectEndpoint', 'sa', 1, '2025-02-07 14:47:07', '2025-02-17 10:34:02', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (21, 1, NULL, 'deepseek', '', 1, 1, '', 'deepseek', 0, NULL, 'sa', 1, '2025-02-08 14:28:56', '2025-02-17 10:31:59', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (22, 1, NULL, 'kimi', '', 1, 1, '', 'kimi', 0, NULL, 'sa', 1, '2025-02-08 16:33:03', '2025-02-17 10:31:57', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (23, 1, NULL, 'ollama', '', 1, 1, '', 'ollama', 0, NULL, 'sa', 1, '2025-02-10 10:16:25', '2025-02-17 10:04:54', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (24, 7, 'mingcute:computer-camera-fill', '摄像头', '', 1, 1, '', 'CAMERA', 0, NULL, 'sa', 1, '2025-02-11 16:37:27', '2025-02-11 16:46:19', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (25, 1, NULL, 'OpenAi', '', 1, 1, '', 'openai', 0, NULL, 'sa', 1, '2025-02-17 09:34:25', '2025-02-17 10:06:38', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (26, 1, NULL, '豆包', '', 1, 1, '', 'DOUBAO', 0, NULL, 'sa', 1, '2025-02-20 14:39:27', '2025-02-20 14:39:56', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (27, 6, 'ri:inbox-unarchive-line', '文生图', '', 1, 1, '', 'WEN_SHENG_TU', 0, NULL, 'sa', 1, '2025-02-26 10:36:37', '2025-02-28 11:24:54', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (28, 1, NULL, '硅基流动', '', 1, 1, '', 'SILICONFLOW', 0, NULL, 'sa', 1, '2025-02-27 16:20:06', '2025-02-27 16:20:44', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (29, 1, NULL, 'OpenRouter', '', 1, 1, '', 'OPEN_ROUTER', 0, NULL, 'sa', 1, '2025-02-28 13:57:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (30, 1, NULL, '智谱清言', '', 1, 1, '', 'ZHIPU', 0, NULL, 'sa', 1, '2025-02-28 15:28:05', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (31, 1, NULL, '无问芯穹', '', 1, 1, '', 'WU_WEN_XIN_QIONG', 0, NULL, 'sa', 1, '2025-03-01 21:28:23', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (32, 1, NULL, 'Cloudflare', '', 1, 1, '', 'Cloudflare', 0, NULL, 'sa', 1, '2025-03-01 22:34:45', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (33, 6, 'ep:video-camera', '文生视频', '', 1, 1, '', 'WEN_SHENG_SHI_PIN', 0, NULL, 'sa', 1, '2025-03-04 14:32:30', '2025-03-04 14:34:29', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (34, 1, NULL, '腾讯', '', 1, 1, '', 'TENCENT', 0, NULL, 'sa', 1, '2025-03-07 15:38:26', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (35, 1, 'ep:camera-filled', '海康云眸', '', 1, 1, '', 'HAI_KANG_YUN_MOU', 0, NULL, 'sa', 1, '2025-03-17 08:45:39', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (36, 7, 'ep:credit-card', '门禁', '', 1, 1, '', 'MEN_JIN', 0, NULL, 'sa', 1, '2025-03-17 10:51:36', '2025-03-17 10:51:48', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (37, 5, NULL, '日历', '', 1, 1, '', 'RI_LI', 0, NULL, 'sa', 1, '2025-03-18 15:03:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (38, 1, NULL, '佐糖', '', 1, 1, '', 'ZUO_TANG', 0, NULL, 'sa', 1, '2025-03-20 13:41:08', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (39, 6, 'simple-icons:davinciresolve', '超分辨率', '', 1, 1, '', 'RESOLUTION', 0, NULL, 'sa', 1, '2025-03-20 13:44:02', '2025-03-20 13:45:14', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (40, 6, 'mingcute:color-picker-fill', '图像上色', '', 1, 1, '', 'COLORIZATION', 0, NULL, 'sa', 1, '2025-03-21 10:29:53', '2025-03-21 10:30:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_dict_item_property
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item_property`;
CREATE TABLE `sys_dict_item_property`  (
  `sys_dict_item_property_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dict_item_id` int(11) NULL DEFAULT NULL COMMENT '字典子项ID',
  `sys_dict_item_property_group` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项分组',
  `sys_dict_item_property_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项',
  `sys_dict_item_property_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `sys_dict_item_property_selected_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选中类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_property_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典配置项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item_property
-- ----------------------------
INSERT INTO `sys_dict_item_property` VALUES (14, 20, 'DA_YU_YAN', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (19, 20, 'DA_YU_YAN', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (21, 10, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (22, 10, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (23, 23, 'DA_YU_YAN', 'sysProjectAppKey', 'false', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (25, 19, 'DEVICE', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (26, 19, 'DEVICE', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (27, 19, 'DEVICE', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (29, 27, 'WEN_SHENG_TU', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (30, 27, 'WEN_SHENG_TU', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (31, 34, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (32, 34, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_email_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_log`;
CREATE TABLE `sys_email_log`  (
  `sys_email_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_email_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人',
  `sys_email_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
  `sys_email_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数',
  `sys_email_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送的模板内容',
  `sys_email_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时时间',
  `sys_email_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
  `sys_email_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `sys_email_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件厂家',
  `sys_email_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件密钥',
  `sys_email_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_email_read` int(11) NULL DEFAULT NULL COMMENT '是否已读;0:未读',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_email_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件下发日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_email_log
-- ----------------------------
INSERT INTO `sys_email_log` VALUES (6, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 2392.00, '00000', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_email_log` VALUES (7, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 1348.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (8, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 893.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (9, 'request.getParams()', '1', NULL, '<p><strong>测试</strong></p>', 40.00, 'FAILURE', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (10, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 917.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, 2);

-- ----------------------------
-- Table structure for sys_email_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template`  (
  `sys_email_template_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `sys_project_type` int(11) NULL DEFAULT NULL COMMENT '类别字典表(dictId=6)',
  `sys_email_template_sort` int(11) NULL DEFAULT NULL COMMENT '模板优先级',
  `sys_email_template_category` int(11) NULL DEFAULT NULL COMMENT '子类型; (dictId = 2)',
  `sys_email_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_email_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_email_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容',
  `sys_email_template_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_email_template_status` int(11) NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
  `sys_email_template_type` int(11) NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
  `sys_email_template_default` int(11) NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
  `sys_email_template_approve` int(11) NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
  `sys_email_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `sys_email_template_approve_create_by` int(11) NULL DEFAULT NULL COMMENT '审批人',
  `sys_email_template_disabled` int(11) NULL DEFAULT NULL COMMENT '模板编辑状态;0:可编辑; 1: 不可编辑;',
  `sys_email_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `sys_email_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源uuid',
  `sys_email_template_callback_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回调地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_email_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_email_template
-- ----------------------------
INSERT INTO `sys_email_template` VALUES (1, 2, NULL, 1, 4, '测试', '111', '<p><strong>测试</strong></p>', NULL, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 'http://127.0.0.1:18170/system/api/v2/project/template/email/callback', 'sa', 1, '2025-01-27 20:00:31', '2025-01-27 20:04:46', 'sa', 1);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `sys_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_menu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `sys_menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `sys_menu_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化',
  `sys_menu_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `sys_menu_perm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限',
  `sys_menu_pid` int(11) NULL DEFAULT NULL COMMENT '父菜单',
  `sys_menu_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `sys_menu_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单组件',
  `sys_menu_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sys_menu_extra_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '右侧菜单图标',
  `sys_menu_type` int(11) NULL DEFAULT NULL COMMENT '菜单:0 iframe:1 外链:2 按钮:3',
  `sys_menu_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_menu_hidden` int(11) NULL DEFAULT NULL COMMENT '是否隐藏；0:显示',
  `sys_menu_show_parent` int(11) NULL DEFAULT NULL COMMENT '当一个子级菜单是否隐藏父级菜单；0:显示',
  `sys_menu_hidden_tag` int(11) NULL DEFAULT NULL COMMENT '是否在标签栏隐藏；0:否',
  `sys_menu_fixed_tag` int(11) NULL DEFAULT NULL COMMENT '是否固定标签；0:否',
  `sys_menu_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由重定向',
  `sys_menu_enter_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进场动画',
  `sys_menu_leave_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离场动画',
  `sys_menu_active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单激活',
  `sys_menu_frame_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接地址',
  `sys_menu_keep_alive` int(11) NULL DEFAULT NULL COMMENT '是否缓存；0:否',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_menu_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属权限',
  `sys_menu_home` int(11) NULL DEFAULT NULL COMMENT '是否只显示在主页； 0: 否',
  `sys_menu_engine` int(11) NULL DEFAULT NULL COMMENT '是否engine布局主页；0: 否',
  PRIMARY KEY (`sys_menu_id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (41, 'aiColorization', '/ai/colorization', NULL, 'AI 图像上色', NULL, 37, '0,37', '/ai/colorization/index', 'mingcute:color-picker-fill', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-21 10:29:02', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (40, 'aiResolution', '/ai/resolution', NULL, 'AI 分辨率', NULL, 37, '0,37,40', '/ai/resolution/index', 'simple-icons:davinciresolve', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-18 16:59:22', '2025-03-20 13:08:36', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (39, 'aiVideo', '/ai/video', NULL, 'AI 文生视频', NULL, 37, '0,37,39', '/ai/video/index', 'ri:folder-video-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 16:24:52', '2025-03-21 10:42:06', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (38, 'aiVincent', '/ai/vincent', NULL, 'AI 文生图', NULL, 37, '0,37,38', '/ai/vincent/index', 'ri:picture-in-picture-exit-line', NULL, 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:46:55', '2025-02-26 10:47:06', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (37, 'ai', '/ai', NULL, 'AI 管理', NULL, 0, '0,37', '/ai', 'ri:robot-2-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:23:48', '2025-02-26 10:24:22', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (36, 'aiLlm', '/ai/llm', NULL, 'AI 对话', NULL, 37, '0,37,36', '/ai/llm/index', 'ri:robot-2-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-25 17:19:27', '2025-02-25 17:20:27', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (35, 'serviceTenant', '/service/tenant', NULL, '租户管理', NULL, 32, '0,32', '/service/tenant/index', 'bi:people', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-19 15:44:30', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (34, 'serviceService', '/service/service', NULL, '服务管理', NULL, 32, '0,32', '/service/service/index', 'ri:customer-service-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:44:13', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (33, 'serviceModule', '/service/module', NULL, '模块管理', NULL, 32, '0,32,33', '/service/module/index', 'ri:sound-module-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:43:26', '2025-02-18 10:44:30', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (32, 'service', '/service', NULL, '服务管理', NULL, 0, '0,32', '/service', 'mingcute:service-fill', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:41:32', '2025-02-18 10:41:54', 'sa', 1, 'ADMIN,SUPER_ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (31, NULL, NULL, NULL, '查询', 'sys:login:page', 8, '0,1,8', NULL, NULL, NULL, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-13 15:48:07', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (30, 'device', '/manage/device', '', '设备管理', '', 1, '0,1,30', '/manage/device/index', 'pixelarticons:device-tv-smart', NULL, 0, 995, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-10 15:39:45', '2025-02-13 10:36:09', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (29, 'llm-template', '/ai/llm', '', '模板管理-大语言', '', 37, '0,37,29', '/ai/llm/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-07 15:14:04', '2025-02-13 10:33:47', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (28, 'device-template', '/manage/device/camera', '', '模板管理-设备', '', 1, '0,1,28', '/manage/template/device/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-05 10:52:22', '2025-02-05 10:53:31', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (27, 'email-template', '/manage/email/template', '', '模板管理-邮件', '', 1, '0,1,27', '/manage/template/email/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-01-27 19:17:19', '2025-01-27 19:17:44', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (26, 'project', '/manage/project', '', '项目管理', NULL, 1, '0,1,26', '/manage/project/index', 'simple-icons:saltproject', NULL, 0, 997, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-01-24 14:33:09', '2025-01-24 14:33:44', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (24, 'sfc', '/manage/sfc', '', '组件管理', NULL, 1, '0,1,24', '/manage/sfc/index', 'ri:compasses-2-fill', NULL, 0, 996, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-24 16:34:53', '2024-10-24 16:35:51', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (20, 'sms-template', '/manage/sms/template', '', '模板管理-短信', '', 1, '0,1,20', '/manage/template/sms/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 15:59:48', '2024-09-02 16:00:05', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (17, 'demo', '/demo/index', '', '自定义弹框组件', '', 18, '0,18,17', '/demo/index', 'ep:alarm-clock', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 09:54:43', '2024-09-02 15:09:18', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (16, 'secret', '/manage/secret', '', '密钥管理', '', 1, '0,1,16', '/manage/secret/index', 'ep:lock', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-30 11:26:23', '2024-08-30 11:26:57', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', '/manage/dict/index', 'humbleicons:dashboard', NULL, 0, 6, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-27 08:41:06', '2024-09-02 16:00:14', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:30:07', '2024-08-23 13:30:39', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:56', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:45', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', '/manage/dept/index', 'ep:star', NULL, 0, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 16:25:12', '2024-08-21 16:25:29', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', '/manage/log/login/index', 'ep:alarm-clock', NULL, 0, 998, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 15:46:26', '2024-08-21 16:12:12', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (8, 'login', '/manage/login', '', '用户管理', '', 1, '0,1,8', '/manage/login/index', 'ep:login-filled', NULL, 0, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-20 15:51:07', '2024-08-20 16:29:51', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:41', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (6, NULL, '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:03', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 15:58:58', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', '/manage/role/index', 'ep:login', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:36:18', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/manage/setting', '', '系统配置', '', 1, '0,1,3', '@repo/pages/setting/index.vue', 'ep:setting', NULL, 0, 999, 0, 1, 0, 0, '', '', '', '', '', 0, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', '/manage/menu/index', 'ep:menu', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_project
-- ----------------------------
DROP TABLE IF EXISTS `sys_project`;
CREATE TABLE `sys_project`  (
  `sys_project_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目名称',
  `sys_project_icon` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图标',
  `sys_project_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
  `sys_project_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
  `sys_project_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
  `sys_project_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
  `sys_project_app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appkey',
  `sys_project_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_project_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_project_status` int(11) NULL DEFAULT NULL COMMENT '是否启用0:未启用',
  `sys_project_sign_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目标识编码',
  `sys_project_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_project_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
  `sys_project_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
  `sys_project_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
  `sys_project_vender` int(11) NULL DEFAULT NULL COMMENT '厂家(字典1)',
  `sys_project_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_project_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能; 字典表6',
  `sys_project_smtp_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp主机',
  `sys_project_smtp_port` int(255) NULL DEFAULT NULL COMMENT 'smtp端口',
  `sys_project_smtp_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp密码',
  `sys_project_smtp_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp主题账号',
  `sys_project_location_temp_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件路径',
  `sys_project_location_oss_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'oss地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_project
-- ----------------------------
INSERT INTO `sys_project` VALUES (1, '阿里', 'https://img1.baidu.com/it/u=2383102008,2259360939&fm=253&fmt=auto?w=800&h=800', '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '', '犀鸟校园', 0, 1, NULL, 2, NULL, NULL, NULL, 2, NULL, '13,15', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);
INSERT INTO `sys_project` VALUES (2, '内部系统', NULL, NULL, '01jjk500nsgh4tgmrm9e0t7bmh', NULL, NULL, NULL, NULL, 0, 1, NULL, 3, NULL, NULL, NULL, 18, NULL, '16', 'smtp.163.com', 25, 'CVIRRMZQPYLQIQMW', 'yemengzhidiandi@163.com', NULL, NULL, 'sa', 1, '2025-01-27 13:50:49', '2025-01-27 13:56:48', 'sa', 1);
INSERT INTO `sys_project` VALUES (4, '海康安防', 'https://open.hikvision.com/fileserver/opsimgs/综合安防.png', NULL, '01jk9zrhcnzsxrm71ybxh85abj', '23031179', 'yKeMbw5czqVuwRnxYHlJ', NULL, '', 0, 1, NULL, 3, NULL, '112.124.44.21:443', NULL, 19, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-05 10:41:53', '2025-02-05 10:44:07', 'sa', 1);
INSERT INTO `sys_project` VALUES (5, '海康云曜', 'https://static.hikyun.com/portal/vportalimages/brand-logo.png', NULL, '01jkczk8ns06derb1m0t6aptb5', '16643603898674415', 'VQYaHTWVmatXVdCbrKZJ', NULL, '651515588253424', 0, 1, '1664360485020781', 3, NULL, NULL, NULL, 1, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-06 14:36:44', '2025-02-06 14:37:46', 'sa', 1);
INSERT INTO `sys_project` VALUES (6, '讯飞', 'https://www.xfyun.cn/static/img/layout/header/logo.svg', NULL, '01jkfjbnn8tvqj03938qkvb2ga', '7d9dec86', 'NTZiODE3MjI3ZGZhMmRjM2FjYTk5ZWJk', '4f810aad8b96224fc2aae62d8d440123', '', 0, 1, NULL, 3, NULL, 'http://spark-api.xf-yun.com/v1.1/chat', NULL, 10, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-07 14:43:07', '2025-02-07 14:47:17', 'sa', 1);
INSERT INTO `sys_project` VALUES (7, 'deepseek', 'https://gimg3.baidu.com/search/src=http%3A%2F%2Fgips0.baidu.com%2Fit%2Fu%3D501085362%2C3268784142%26fm%3D3030%26app%3D3030%26f%3DJPEG%3Fw%3D338%26h%3D225%26s%3D46F088725076FF8208BF9FDD020080AC&refer=http%3A%2F%2Fwww.baidu.com&app=2021&size=f242,150&n=0&g=0n&q=100&fmt=auto?sec=1739120400&t=07d69eb6c8aa160d9c89849bb067310c', NULL, '01jkj3yxzmzppaj8c3afphentc', NULL, NULL, 'EWSNmypiPVPCzRVlcIdKsyNLGJNuw8CTSR+8IcKrheTl1rqvC2QFB8YUaJ8MaP7v', NULL, 0, 1, NULL, 3, NULL, 'https://api.deepseek.com/chat/completions', NULL, 21, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-08 14:29:12', '2025-02-08 15:02:56', 'sa', 1);
INSERT INTO `sys_project` VALUES (8, 'kimi', 'https://platform.moonshot.cn/logo.png', NULL, '01jkjb2jc4grwqtv0vn9tjjayw', NULL, NULL, 'sk-2KiwJ1D0DLOpdb6cozbY5R0H8ydum7YYcDIONGetw4bu6jsy', NULL, 0, 1, NULL, 3, NULL, 'https://api.moonshot.cn/v1/chat/completions', NULL, 22, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-08 16:33:32', '2025-02-08 16:33:43', 'sa', 1);
INSERT INTO `sys_project` VALUES (9, 'ollama', 'https://ollama.com/public/ollama.png', NULL, '01jkpta36f0yds272z1g81qs5r', NULL, NULL, NULL, NULL, 0, 1, NULL, 3, NULL, 'http://127.0.0.1:11434/v1/chat/completions', NULL, 23, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-10 10:16:45', '2025-02-10 10:40:30', 'sa', 1);
INSERT INTO `sys_project` VALUES (10, '测试', NULL, NULL, '01jkszw2aj1ht2pyr971fv2bz0', '28282971', 'q4S7VR7AXTcSpLuKsMlF', '', NULL, 0, 1, NULL, 3, NULL, '39.170.78.27:1443', NULL, 19, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-11 15:51:40', '2025-02-11 15:52:37', 'sa', 1);
INSERT INTO `sys_project` VALUES (11, 'Cloudflare', 'https://developers.cloudflare.com/_astro/logo.p_ySeMR1.svg', NULL, '01jm8rp81pxz21ys3a7jtjw17w', NULL, NULL, 'ac4ctLVeplrjya5eT5ax024mgnT8hz1U-G-JF5ZR', NULL, 0, 1, NULL, 3, NULL, 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/v1', NULL, 32, NULL, '20,27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-17 09:34:45', '2025-02-17 09:36:25', 'sa', 1);
INSERT INTO `sys_project` VALUES (12, '豆包', 'https://lf-flow-web-cdn.doubao.com/obj/flow-doubao/samantha/logo-icon-white-bg.png', NULL, '01jmh1ctzzqxk5pgxc5mv50h81', NULL, NULL, 'd3eec490-7aaf-468c-9e08-71021807460e1', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, 26, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-20 14:40:50', '2025-02-20 14:41:05', 'sa', 1);
INSERT INTO `sys_project` VALUES (13, '阿里百炼', 'https://gw.alicdn.com/imgextra/i1/O1CN01gAwVtW296IdlNcfkW_!!6000000008018-2-tps-264-104.png', NULL, '01jn01sjfvgm9a9v2qjbm5ywtn', NULL, NULL, 'sk-8e1cd4c8176b494b97c361b5a1d78ea3', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, 2, NULL, '27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:36:23', '2025-02-26 10:39:46', 'sa', 1);
INSERT INTO `sys_project` VALUES (14, '硅基流动', 'https://framerusercontent.com/images/4li2PjWxZJmoGkzXRMJWU1rJmI.svg', NULL, '01jn3803z7e41x7fjzvj539j20', NULL, NULL, 'sk-fqfxjiyhpadgvdlkousbnztmuttiegwmgaymubnaxofwoxhx', NULL, 0, 1, NULL, 3, NULL, 'https://api.siliconflow.cn/v1/images/generations', NULL, 28, NULL, '27,20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-27 16:22:33', '2025-02-27 16:23:53', 'sa', 1);
INSERT INTO `sys_project` VALUES (16, 'openrouter', ' https://openrouter.ai/favicon.ico', NULL, '01jn5j39e1h4yqnw4qs801jzqn', NULL, NULL, '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', NULL, 0, 1, NULL, 3, NULL, 'https://openrouter.ai/api/v1/chat/completions', NULL, 29, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 13:57:31', '2025-03-25 14:11:37', 'sa', 1);
INSERT INTO `sys_project` VALUES (17, '智谱清言', 'https://bigmodel.cn/img/icons/android-chrome-192x192.png', NULL, '01jn5qbe49btdqzc2fe17rjysf', NULL, NULL, '29ed238f4660416cb2efe39aaaf71ed5.S2I2AfOu75HQwZ30', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, 30, NULL, '27,20,33', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 15:29:21', '2025-02-28 15:29:36', 'sa', 1);
INSERT INTO `sys_project` VALUES (18, '无问芯穹', 'https://content.cloud.infini-ai.com/platform-web-prod/assets/svg/logo_new.ceae5ff6.svg', NULL, '01jn8yb4ak2amap9qsc3amj4js', NULL, NULL, 'sk-daulbrd77wpadaw6', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, 31, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-01 21:29:14', NULL, NULL, NULL);
INSERT INTO `sys_project` VALUES (19, '腾讯', 'https://pic.rmb.bdstatic.com/bjh/news/87715fa6e1758590fc4b076538d704ad.png', NULL, '01jnqrnw0z28t5bhkf6a2qkc2d', 'AKIDOgokSxEI8pIYaP1RedPxKTSEn8GAtPp1', '1rUt9E7mETWMyQhlbEetwpV0KXXKtEMJ', 'sk-RBopn00LV7WB5KMQHdDAaV256zyjjQGuVA9DftZrUWTFsM0w', NULL, 0, 1, NULL, 1, NULL, NULL, NULL, 34, NULL, '20,27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-07 15:38:51', '2025-03-07 15:42:17', 'sa', 1);
INSERT INTO `sys_project` VALUES (20, '海康云眸', ' https://pic.hik-cloud.com/favicon.ico', NULL, '01jpgs1jm6tbvdp6tq9zfw59ss', 'e431e02c369b4adbad8f72c1eafbe8f7', '492c2864b59d459ebdb64795119e062b', NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, 35, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 08:46:16', '2025-03-17 08:51:09', 'sa', 1);
INSERT INTO `sys_project` VALUES (21, '佐糖', 'https://qncdn.aoscdn.com/astro/picwish/_astro/logo-zh.Bv2rokLa.svg', NULL, '01jps1ab36ykbgd6mkygprmcmd', NULL, NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, 38, NULL, '39,40', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-20 13:44:47', '2025-03-21 10:30:34', 'sa', 1);

-- ----------------------------
-- Table structure for sys_project_default
-- ----------------------------
DROP TABLE IF EXISTS `sys_project_default`;
CREATE TABLE `sys_project_default`  (
  `sys_project_default_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_project_default_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型； 字典表6;',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_project_default_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_project_default
-- ----------------------------
INSERT INTO `sys_project_default` VALUES (30, '6', 3);
INSERT INTO `sys_project_default` VALUES (33, '16', 2);
INSERT INTO `sys_project_default` VALUES (37, '14', 4);
INSERT INTO `sys_project_default` VALUES (46, '13', 1);
INSERT INTO `sys_project_default` VALUES (47, '20', 16);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `sys_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `sys_role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码; 唯一',
  `sys_role_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统角色; 0：非系统角色',
  `sys_role_status` int(11) NULL DEFAULT NULL COMMENT '是否启用; 0：不启用',
  `sys_role_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_role_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_role_hidden` int(11) NULL DEFAULT 0 COMMENT '是否显示; 0:显示',
  `sys_role_readable` int(11) NULL DEFAULT NULL COMMENT '角色可读',
  `sys_role_writeable` int(11) NULL DEFAULT NULL COMMENT '角色可写',
  `sys_role_executable` int(11) NULL DEFAULT NULL COMMENT '角色可执行',
  `sys_role_board_card` int(11) NULL DEFAULT NULL COMMENT '角色可看板类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '管理员', 'ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2024-08-21 13:49:40', 'sa', 1);
INSERT INTO `sys_role` VALUES (3, '运维', 'OPS', 1, 1, 1, '运维', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2025-02-13 11:31:33', 'sa', 1);
INSERT INTO `sys_role` VALUES (4, '机构', 'DEPT', 1, 1, 1, '机构账号', 0, 1, 16, 256, NULL, 'sa', 1, '2025-02-13 15:19:04', '2025-02-13 15:19:23', 'sa', 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `sys_menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`sys_role_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 189 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (82, 2, 4);
INSERT INTO `sys_role_menu` VALUES (83, 2, 5);
INSERT INTO `sys_role_menu` VALUES (84, 2, 6);
INSERT INTO `sys_role_menu` VALUES (85, 2, 7);
INSERT INTO `sys_role_menu` VALUES (86, 2, 8);
INSERT INTO `sys_role_menu` VALUES (87, 2, 12);
INSERT INTO `sys_role_menu` VALUES (88, 2, 13);
INSERT INTO `sys_role_menu` VALUES (89, 2, 14);
INSERT INTO `sys_role_menu` VALUES (90, 2, 1);
INSERT INTO `sys_role_menu` VALUES (91, 2, 2);
INSERT INTO `sys_role_menu` VALUES (92, 2, 11);
INSERT INTO `sys_role_menu` VALUES (93, 2, 15);
INSERT INTO `sys_role_menu` VALUES (94, 2, 16);
INSERT INTO `sys_role_menu` VALUES (95, 2, 20);
INSERT INTO `sys_role_menu` VALUES (96, 2, 9);
INSERT INTO `sys_role_menu` VALUES (97, 2, 3);
INSERT INTO `sys_role_menu` VALUES (131, 3, 2);
INSERT INTO `sys_role_menu` VALUES (132, 3, 4);
INSERT INTO `sys_role_menu` VALUES (133, 3, 5);
INSERT INTO `sys_role_menu` VALUES (134, 3, 6);
INSERT INTO `sys_role_menu` VALUES (135, 3, 7);
INSERT INTO `sys_role_menu` VALUES (136, 3, 8);
INSERT INTO `sys_role_menu` VALUES (137, 3, 12);
INSERT INTO `sys_role_menu` VALUES (138, 3, 13);
INSERT INTO `sys_role_menu` VALUES (139, 3, 14);
INSERT INTO `sys_role_menu` VALUES (140, 3, 9);
INSERT INTO `sys_role_menu` VALUES (141, 3, 11);
INSERT INTO `sys_role_menu` VALUES (142, 3, 15);
INSERT INTO `sys_role_menu` VALUES (143, 3, 26);
INSERT INTO `sys_role_menu` VALUES (166, 4, 20);
INSERT INTO `sys_role_menu` VALUES (167, 4, 24);
INSERT INTO `sys_role_menu` VALUES (168, 4, 26);
INSERT INTO `sys_role_menu` VALUES (169, 4, 27);
INSERT INTO `sys_role_menu` VALUES (170, 4, 28);
INSERT INTO `sys_role_menu` VALUES (171, 4, 29);
INSERT INTO `sys_role_menu` VALUES (172, 4, 30);
INSERT INTO `sys_role_menu` VALUES (173, 4, 1);
INSERT INTO `sys_role_menu` VALUES (174, 4, 2);
INSERT INTO `sys_role_menu` VALUES (175, 4, 4);
INSERT INTO `sys_role_menu` VALUES (176, 4, 5);
INSERT INTO `sys_role_menu` VALUES (177, 4, 6);
INSERT INTO `sys_role_menu` VALUES (178, 4, 7);
INSERT INTO `sys_role_menu` VALUES (179, 4, 8);
INSERT INTO `sys_role_menu` VALUES (180, 4, 12);
INSERT INTO `sys_role_menu` VALUES (181, 4, 13);
INSERT INTO `sys_role_menu` VALUES (182, 4, 14);
INSERT INTO `sys_role_menu` VALUES (183, 4, 31);
INSERT INTO `sys_role_menu` VALUES (184, 4, 11);
INSERT INTO `sys_role_menu` VALUES (185, 4, 15);
INSERT INTO `sys_role_menu` VALUES (186, 4, 16);
INSERT INTO `sys_role_menu` VALUES (187, 4, 9);
INSERT INTO `sys_role_menu` VALUES (188, 4, 3);

-- ----------------------------
-- Table structure for sys_secret
-- ----------------------------
DROP TABLE IF EXISTS `sys_secret`;
CREATE TABLE `sys_secret`  (
  `sys_secret_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_secret_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
  `sys_secret_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
  `sys_secret_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
  `sys_secret_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
  `sys_secret_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_secret_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_secret_status` int(11) NULL DEFAULT NULL COMMENT '是否启用0:未启用',
  `sys_secret_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
  `sys_secret_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
  `sys_secret_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
  `sys_secret_dict_item_id` int(11) NULL DEFAULT NULL COMMENT '字典项ID(字典1)',
  `sys_secret_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_secret_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_secret_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_secret
-- ----------------------------
INSERT INTO `sys_secret` VALUES (1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '犀鸟校园', 0, 1, NULL, NULL, NULL, 2, NULL, 'SMS', 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);

-- ----------------------------
-- Table structure for sys_service
-- ----------------------------
DROP TABLE IF EXISTS `sys_service`;
CREATE TABLE `sys_service`  (
  `sys_service_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_service_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务图片地址',
  `sys_service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务名称',
  `sys_service_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务唯一编码',
  `sys_service_delete` int(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '是否删除; 0:正常',
  `sys_service_status` int(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '是否禁用; 0:正常',
  `sys_service_sort` int(10) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '优先级',
  `sys_service_valid_time` datetime NULL DEFAULT NULL COMMENT '到期时间',
  `sys_service_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本号',
  `sys_service_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用环境',
  `sys_service_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务角色权限',
  `sys_service_category` int(11) NULL DEFAULT NULL COMMENT '服务类型; 字段表8',
  `sys_service_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务描述',
  `sys_service_image_upload_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片上传类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_service_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统服务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_service
-- ----------------------------
INSERT INTO `sys_service` VALUES (1, 'https://picsum.photos/300/200', '基础服务', 'base', 0, 0, 0000000001, '2025-02-19 16:00:00', NULL, NULL, NULL, 1, NULL, 'ONLINE', 'sa', 1, '2025-02-18 15:29:35', '2025-02-20 08:52:01', 'sa', 1);
INSERT INTO `sys_service` VALUES (2, 'https://picsum.photos/id/1036/300/200', '设备管理', 'DEVICE', 0, 0, 0000000001, '2025-02-12 16:00:00', NULL, NULL, NULL, 1, NULL, 'ONLINE', 'sa', 1, '2025-02-20 08:56:41', '2025-02-20 09:48:50', 'sa', 1);

-- ----------------------------
-- Table structure for sys_service_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_service_module`;
CREATE TABLE `sys_service_module`  (
  `sys_service_module_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_service_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块名称',
  `sys_service_module_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块编码',
  `sys_service_module_delete` int(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '是否删除; 0:正常',
  `sys_service_module_status` int(1) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '是否禁用; 0:正常',
  `sys_service_module_sort` int(11) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '优先级',
  `sys_service_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型; API, SERVICE',
  `sys_service_module_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `sys_service_module_menu_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块菜单标签',
  `sys_service_module_menu_tags2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块菜单标签(包含父标签)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_service_module_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_service_module
-- ----------------------------
INSERT INTO `sys_service_module` VALUES (1, '基础模块', 'BASE', 0, 0, 00000000001, 'SERVICE', 'v1.0.0.0', '2,5,6,7,12,13,14,31,11,15,16,20,26,27,28,29,9,3', '2,4,5,6,7,8,12,13,14,31,11,15,16,20,26,27,28,29,9,3,1', 'sa', 1, '2025-02-18 13:19:16', '2025-02-18 13:27:10', 'sa', 1);
INSERT INTO `sys_service_module` VALUES (2, '设备管理', 'DEVICE', 0, 0, 00000000001, 'SERVICE', 'v1.0.0.0', '30', NULL, 'sa', 1, '2025-02-20 08:51:05', '2025-02-20 08:51:12', 'sa', 1);

-- ----------------------------
-- Table structure for sys_service_module_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_service_module_item`;
CREATE TABLE `sys_service_module_item`  (
  `sys_service_item_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_service_id` int(255) NULL DEFAULT NULL COMMENT '服务子ID',
  `sys_service_module_id` int(255) NULL DEFAULT NULL COMMENT '模块ID',
  PRIMARY KEY (`sys_service_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务子项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_service_module_item
-- ----------------------------
INSERT INTO `sys_service_module_item` VALUES (26, 2, 2);
INSERT INTO `sys_service_module_item` VALUES (31, 1, 1);

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `sys_setting_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_setting_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sys_setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `sys_setting_status` int(11) NULL DEFAULT NULL COMMENT '是否启用; 0:不启用',
  `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型; 用于前端组件判断; e.g.BOOL：布尔',
  `sys_setting_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属分组',
  `sys_setting_collect` int(255) NULL DEFAULT NULL COMMENT '是否是多个配置集合;0:单个',
  `sys_setting_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统配置; 0:非系统配置',
  `sys_setting_app_inner` int(11) NULL DEFAULT NULL COMMENT '配置是否作用域系统启动',
  `sys_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
  `sys_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
  `sys_setting_admin` int(11) NULL DEFAULT NULL COMMENT '是否只有管理员生效',
  `sys_setting_version` int(11) NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 4, 'CheckCodeOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '验证码开关', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (2, 0, 'SystemName', '后台管理系统', 1, 'String', 'default', 0, 1, 0, '系统标题', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (3, 5, 'SlidingBlockOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '滑动验证码', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (4, 9, 'CrashPageOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '调试模式崩溃当前页', NULL, NULL, 26, 'sa', NULL, '2024-08-18 11:22:26', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (5, 10, 'CodecResponseOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '返回加密', NULL, NULL, 26, 'sa', NULL, '2024-08-18 11:31:57', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (6, 5, 'WatermarkOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '水印', NULL, NULL, 26, 'sa', NULL, '2024-08-19 19:53:55', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (7, 11, 'LoopDebuggerOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '循环debugger', NULL, NULL, 26, 'sa', NULL, '2024-08-18 10:52:15', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (8, 4, 'Version', '1', 1, 'Number', 'config', 0, 1, 0, '系统版本', 'fe7db08a-1001-4e7b-95ab-776a569a85c0', NULL, 123, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (9, 2, 'SocketUrl', 'http://172.16.2.226:29181', 1, 'Array', 'config', 0, 1, 0, 'socket.io服务器地址', NULL, 1, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (10, 12, 'SocketOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, 'socket.io是否开启', NULL, 1, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (11, 13, 'SocketPath', '/socket.io', 1, 'String', 'config', 0, 1, 0, 'socket.io路径', NULL, NULL, NULL, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (12, 6, 'WatermarkColor', '#409EFF', 1, 'Color', 'config', 0, 1, 0, '水印颜色', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (13, 8, 'WatermarkAlpha', '0.35', 1, 'Number', 'config', 0, 1, 0, '水印透明度', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (14, 7, 'WatermarkRotate', '-30', 1, 'Number', 'config', 0, 1, 0, '水印角度', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (15, 14, 'CodecRequestOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '请求加密', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (16, 3, 'CodecRequestKey', '0123456789abcdef', 1, 'String', 'config', 0, 1, 1, '请求加密密钥', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (17, 2, 'Host', 'smtp.163.com', 1, 'String', 'smtp', 1, 1, 0, '服务器配置', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (18, 3, 'Port', '25', 1, 'Number', 'smtp', 1, 1, 0, '服务器端口', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (19, 4, 'Password', 'CVIRRMZQPYLQIQMW_C', 1, 'Password', 'smtp', 1, 1, 0, '授权码', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (20, 1, 'From', 'yemengzhidiandi@163.com', 1, 'Mail', 'smtp', 1, 1, 0, '发送账号', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (21, 1, 'AppId', 'wx8cd1bb691119e3f7', 1, 'String', 'weixin', 1, 1, 0, '微信AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting` VALUES (22, 2, 'AppSecret', 'abff9e5a7541a5db21a819b087ecbf10', 1, 'String', 'weixin', 1, 1, 0, '微信AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting` VALUES (23, 1, 'AppId', 'f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943', 1, 'String', 'gitee', 1, 1, 0, 'GiteeAppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting` VALUES (24, 2, 'AppSecret', '158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7', 1, 'String', 'gitee', 1, 1, 0, 'GiteeAppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting` VALUES (25, 3, 'Gitee', 'true', 1, 'Boolean', 'sso', 1, 1, 0, 'Gitee登录', NULL, NULL, NULL, NULL, NULL, NULL, '2025-02-18 16:11:53', 'sa', 1);
INSERT INTO `sys_setting` VALUES (26, 1, 'AppId', 'LTAI5tKQQnvCS3yEjGxjHmCd', 1, 'String', 'sms', 1, 1, 0, '短信AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (27, 2, 'AppSecret', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAhh', 1, 'String', 'sms', 1, 1, 0, '短信AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (28, 3, 'AppSign', '犀鸟校园', 1, 'String', 'sms', 1, 1, 0, '短信签名', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (29, 4, 'AppType', 'ALIYUN', 1, 'Dict', 'sms', 1, 1, 0, '短信厂商', '1', NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (30, 1, 'AppKey', 'YfuzeuBcnvSluLjHkviR:WFjoVbqyzcqfttbxnsZu', 1, 'AppKey', 'llm', 1, 1, 0, '密钥', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (31, 2, 'AppType', 'XUN_FEI', 1, 'Dict', 'llm', 1, 1, 0, '语言模型厂商', '1', NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (32, 3, 'AppId', '7d9dec86', 1, 'String', 'llm', 1, 1, 0, '模型AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (33, 4, 'AppSecret', 'NTZiODE3MjI3ZGZhMmRjM2FjYTk5ZWJk', 1, 'AppSecret', 'llm', 1, 1, 0, '模型AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (34, 6, 'CheckTotpCodeOpen', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '开启totp', NULL, NULL, NULL, 'sa', 1, '2024-12-08 11:56:39', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (35, 1, 'WebrtcOpen', 'true', 1, 'Boolean', 'webrtc', NULL, NULL, NULL, '是否开启', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (36, 2, 'WebrtcUrl', 'http://172.16.2.226:29182', 1, 'String', 'webrtc', NULL, NULL, NULL, 'webrtc服务器地址', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (37, 3, 'WebrtcContext', '/webrtc', 1, 'String', 'webrtc', NULL, NULL, NULL, '路径', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (38, 1, 'OssAddress', 'http://127.0.0.01:19180', 1, 'String', 'config', NULL, 0, NULL, '资源地址', NULL, NULL, NULL, 'sa', 1, '2025-02-10 16:10:37', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (39, 0, 'ApiAddress', NULL, 1, 'String', 'config', NULL, 0, NULL, '接口地址', NULL, NULL, NULL, 'sa', 1, '2025-02-17 08:48:56', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (40, 2, 'OpenTenantLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '租户登录', NULL, NULL, NULL, 'sa', 1, '2025-02-19 11:14:15', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (41, 3, 'OpenTenantLoginRequire', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '填写租户', NULL, NULL, NULL, 'sa', 1, '2025-02-19 14:18:26', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (42, 1, 'OpenBaseLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '基础登录', NULL, NULL, NULL, 'sa', 1, '2025-02-19 14:18:52', '2025-02-19 14:19:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_sfc
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc`;
CREATE TABLE `sys_sfc`  (
  `sys_sfc_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_sfc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `sys_sfc_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `sys_sfc_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件功能, 多个逗号分隔',
  `sys_sfc_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件图标',
  `sys_sfc_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类；HOME',
  `sys_sfc_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件存放路径',
  `sys_sfc_status` int(11) NULL DEFAULT NULL COMMENT '组件状态; 0:未启用',
  `sys_sfc_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述',
  `sys_sfc_proxy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理地址',
  `sys_sfc_type` int(11) NULL DEFAULT NULL COMMENT '组件类型; 0:文件; 1:文本',
  `sys_sfc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '组件文本',
  `sys_sfc_model_cache` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模块缓存',
  `sys_sfc_delay` int(11) NULL DEFAULT NULL COMMENT '延迟时间; 2000',
  `sys_sfc_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_sfc_timeout` int(11) NULL DEFAULT NULL COMMENT '超时时间; 3000',
  PRIMARY KEY (`sys_sfc_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组件库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc
-- ----------------------------
INSERT INTO `sys_sfc` VALUES (2, 'time', '时间组件', '时间', 'ep:timer', 'HOME', NULL, 1, NULL, NULL, 1, '<template>\n  <el-card shadow=\"hover\" header=\"时钟\" class=\"item-background\">\n    <div class=\"time\">\n      <h2>{{ time }}</h2>\n      <p>{{ day }}</p>\n    </div>\n  </el-card>\n</template>\n\n<script>\nimport { dateFormat } from \"@/utils/date\";\nexport default {\n  title: \"时钟\",\n  icon: \"ep:clock\",\n  description: \"演示部件效果\",\n  data() {\n    return {\n      time: \"\",\n      day: \"\"\n    };\n  },\n  mounted() {\n    this.showTime();\n    setInterval(() => {\n      this.showTime();\n    }, 1000);\n	this.$emit(\"loaded\", true);\n  },\n  methods: {\n    showTime() {\n      this.time = dateFormat(new Date(), \"hh:mm:ss\");\n      this.day = dateFormat(new Date(), \"yyyy年MM月dd日\");\n    }\n  }\n};\n</script>\n\n<style scoped>\n.item-background {\n  background: linear-gradient(to right, #8e54e9, #4776e6);\n  color: #fff;\n}\n.time h2 {\n  font-size: 40px;\n}\n.time p {\n  font-size: 14px;\n  margin-top: 13px;\n  opacity: 0.7;\n}\n</style>\n', '[{\"name\":\"Date\",\"value\":\"import * as Date from \\\"@/utils/date\\\";\"}]', NULL, '1.0.0', 'sa', 1, '2024-10-25 09:26:42', '2024-10-25 09:27:12', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (3, 'weather', '天气', '天气', 'meteocons:clear-day', 'HOME', '.\\sfc\\3\\weather.vue', 1, NULL, NULL, 0, 'null', 'null', NULL, '1.0.0', 'sa', 1, '2024-10-28 09:32:27', '2024-10-28 13:36:56', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (4, 'local-weather', '本地天气组件', '天气', 'ep:basketball', 'HOME', '@repo/module/weather/index.vue', 1, NULL, NULL, 3, NULL, '[]', NULL, '1.0.0', 'sa', 1, '2024-11-24 10:09:56', '2024-11-24 10:10:49', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (5, 'frame', 'test', '天气', 'ep:trophy-base', 'HOME', 'https://blog.csdn.net/m0_64494670/article/details/135143478', 1, NULL, NULL, 2, NULL, '[]', NULL, '1.0.0', 'sa', 1, '2024-11-24 12:55:03', '2024-11-24 13:21:45', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (6, 'calendar', '日历', '日历', 'ep:calendar', 'HOME', '@repo/module/calendar/index.vue', 1, NULL, NULL, 3, '', '[]', NULL, '1.0.0', 'sa', 1, '2025-03-18 15:04:30', '2025-03-18 15:04:47', '', 0, NULL);
INSERT INTO `sys_sfc` VALUES (7, 'day', '当日天气', '天气', 'meteocons:sun-hot-fill', 'HOME', '@repo/module/day/index.vue', 1, NULL, NULL, 3, '', '[]', NULL, '1.0.0', '', 0, '2025-03-24 13:22:54', '2025-03-24 13:23:06', 'sa', 1, NULL);

-- ----------------------------
-- Table structure for sys_sfc_install
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc_install`;
CREATE TABLE `sys_sfc_install`  (
  `sys_sfc_install_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '安裝的用户ID',
  `sys_sfc_id` int(11) NULL DEFAULT NULL COMMENT '组件ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_sfc_install_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户安装的组件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc_install
-- ----------------------------
INSERT INTO `sys_sfc_install` VALUES (1, 1, 2, 'sa', 1, '2024-10-25 09:33:10', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (2, 1, 3, 'sa', 1, '2024-10-29 10:46:53', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (3, 1, 4, 'sa', 1, '2024-11-24 10:16:03', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (5, 1, 6, 'sa', 1, '2025-03-18 15:04:53', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (6, 1, 5, 'sa', 1, '2025-03-18 15:04:57', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_sms_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_sms_log`;
CREATE TABLE `sys_sms_log`  (
  `sys_sms_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_sms_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人',
  `sys_sms_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
  `sys_sms_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数',
  `sys_sms_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送的模板内容',
  `sys_sms_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时时间',
  `sys_sms_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
  `sys_sms_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `sys_sms_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信厂家',
  `sys_sms_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信密钥',
  `sys_sms_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_sms_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '短信下发日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sms_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_sms_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_sms_template`;
CREATE TABLE `sys_sms_template`  (
  `sys_sms_template_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_project_id` int(11) NULL DEFAULT NULL COMMENT '项目ID',
  `sys_project_type` int(11) NULL DEFAULT NULL COMMENT '类别字典表(dictId=6)',
  `sys_sms_template_sort` int(11) NULL DEFAULT NULL COMMENT '模板优先级',
  `sys_sms_template_category` int(11) NULL DEFAULT NULL COMMENT '子类型; (dictId = 2)',
  `sys_sms_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_sms_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_sms_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容',
  `sys_sms_template_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_sms_template_status` int(11) NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
  `sys_sms_template_type` int(11) NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
  `sys_sms_template_default` int(11) NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
  `sys_sms_template_approve` int(11) NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
  `sys_sms_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `sys_sms_template_approve_create_by` int(11) NULL DEFAULT NULL COMMENT '审批人',
  `sys_sms_template_disabled` int(11) NULL DEFAULT NULL COMMENT '模板编辑状态;0:可编辑; 1: 不可编辑;',
  `sys_sms_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `sys_sms_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源uuid',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_sms_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_sms_template
-- ----------------------------
INSERT INTO `sys_sms_template` VALUES (25, 1, 13, 3, 5, '调代课-上课前10分钟提醒（代课）', 'SMS_463614480', '代课提醒：${name1}老师：下一节课(${time1}第${number1}节)，您将要上${class}的${subject1}（原课是${name2}老师的${subject2} ），请不要忘记，谢谢。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:15', '2025-01-26 14:46:21', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (26, 1, 13, 4, NULL, '调代课-上课前10分钟提醒（调课）', 'SMS_463639554', '调课提醒：${name1}老师：下一节课(${time1}第${number1}节)，您将要上${class}的${subject1}（同(${time2}第${number2}节对调）（原课是${name2}老师的${subject2} ），请不要忘记，谢谢。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:21', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (27, 1, 13, 2, NULL, '调代课通知-被申请方', 'SMS_463632994', '${name}老师:  由于[${teacher}老师](${reason})， 在${class}(时间：${time}) 第${number}节的${subject}  将由您代授 ，请做好交接工作，谢谢您对教导处工作的支持。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:21', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (28, 1, 13, 2, NULL, '调代课通知-申请方', 'SMS_463602234', '${name}老师:  由于您(${reason})，您在 ${class}(时间：${time}) 第${number}节的${subject}  将由 [${teacher}老师]代授 ，请做好交接工作，谢谢您对教导处工作的支持。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:21', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (29, 1, 13, 2, NULL, '乡贤调解一件事云评价提示', 'SMS_251040478', '您的案件已经办结，您可以通过事项链接中的“乡贤评价‘’按钮，对参与办案的乡贤进行评价。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:21', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (30, 1, 13, 2, NULL, '乡贤调解一件事事项提醒', 'SMS_249080566', '您有新的调解事项动态，请登陆查看！', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (31, 1, 13, 2, NULL, '智校邦学生审核失败', 'SMS_249130041', '尊敬的${parentName}家长您好：\n您的${studentName}学生账号审核未通过，请修改信息重新申请或联系校方。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (32, 1, 13, 2, NULL, '智校邦学生审核通过', 'SMS_249170028', '尊敬的${parentName}家长您好：\n您的${studentName}学生账号审核已通过，请打开智校邦小程序查看。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (33, 1, 13, 2, NULL, '居家养老告警信息-副本', 'SMS_229637958', '位于${village}的${addName}设备，发出异常警告，警告名称${deviceName}，请留意处理。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (34, 1, 13, 2, NULL, '居家养老告警信息', 'SMS_229610452', '您的${addName}设备，发出异常警告，警告名称${deviceName}，请留意处理。', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (35, 1, 13, 2, NULL, '数字门牌短信通知', 'SMS_229470909', '您有新的求助，求助人：${consignee}，联系方式：${number}，求助类型${helptext}请您及时查收！', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);
INSERT INTO `sys_sms_template` VALUES (36, 1, 13, 2, NULL, '验证码短信', 'SMS_229240120', '您的验证码为：${code}，请勿泄露于他人！', 0, 1, 0, 0, 2, NULL, NULL, 1, 'sms同步', NULL, 'sa', 1, '2025-01-26 14:46:16', '2025-01-26 14:46:22', 'sa', 1);

-- ----------------------------
-- Table structure for sys_task_sso
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_sso`;
CREATE TABLE `sys_task_sso`  (
  `sys_task_sso_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_task_sso_seq` int(11) NULL DEFAULT NULL COMMENT '任务序号',
  `sys_task_sso_tid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID',
  `sys_task_sso_status` int(11) NULL DEFAULT NULL COMMENT '任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常',
  `sys_task_sso_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
  `sys_task_sso_number` int(11) NULL DEFAULT NULL COMMENT '分片数量',
  `sys_task_sso_chuck` bigint(20) NULL DEFAULT NULL COMMENT '当前文件大小',
  `sys_task_sso_summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
  `sys_task_sso_total` bigint(20) NULL DEFAULT NULL COMMENT '任务总大小',
  `sys_task_sso_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件md5',
  `sys_task_sso_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子任务本地路径',
  `sys_task_sso_version` int(11) NULL DEFAULT NULL COMMENT '任务锁',
  `sys_task_sso_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sso类型',
  `sys_task_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_task_sso_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'sso任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_sso
-- ----------------------------

-- ----------------------------
-- Table structure for sys_task_sso_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_sso_item`;
CREATE TABLE `sys_task_sso_item`  (
  `sys_task_sso_item_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_task_sso_item_seq` int(11) NULL DEFAULT NULL COMMENT '任务序号',
  `sys_task_sso_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID',
  `sys_task_sso_item_status` int(11) NULL DEFAULT NULL COMMENT '任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常',
  `sys_task_sso_item_version` int(11) NULL DEFAULT NULL COMMENT '任务锁',
  `sys_task_sso_item_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件md5',
  `sys_task_sso_item_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子任务本地路径',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_task_sso_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'sso任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_sso_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `sys_tenant_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_tenant_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_tenant_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_tenant_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `sys_tenant_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_tenant_corporation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司',
  `sys_tenant_delete` int(1) NULL DEFAULT NULL COMMENT '是否删除; 0:正常',
  `sys_tenant_status` int(1) NULL DEFAULT NULL COMMENT '是否禁用; 0:正常',
  `sys_tenant_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `sys_tenant_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sys_tenant_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `sys_tenant_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件',
  `sys_tenant_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_tenant_gid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户对于配置唯一ID',
  `sys_tenant_home_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户访问地址',
  `sys_tenant_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
INSERT INTO `sys_tenant` VALUES (1, '测试', 'test', '123456', NULL, '测试', 0, 0, NULL, '1', '测试地点1', NULL, 'test', NULL, NULL, NULL, 'sa', 1, '2025-02-19 16:19:41', '2025-02-19 16:20:48', 'sa', 1);

-- ----------------------------
-- Table structure for sys_tenant_service
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_service`;
CREATE TABLE `sys_tenant_service`  (
  `sys_tenant_service_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_tenant_id` int(11) NULL DEFAULT NULL COMMENT '租户ID',
  `sys_service_id` int(11) NULL DEFAULT NULL COMMENT '服务ID',
  `sys_tenant_service_valid_time` datetime NULL DEFAULT NULL COMMENT '到期时间',
  PRIMARY KEY (`sys_tenant_service_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户 - 服务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant_service
-- ----------------------------
INSERT INTO `sys_tenant_service` VALUES (47, 1, 1, '2025-02-28 00:00:00');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `sys_user_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_user_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `sys_user_salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
  `sys_user_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除; 0: 未删除',
  `sys_user_status` int(11) NULL DEFAULT NULL COMMENT '是否启用；0：禁用',
  `sys_user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sys_user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `sys_user_sex` int(11) NULL DEFAULT NULL COMMENT '性别; 0:女;1:男; 2:其他',
  `sys_user_full_address` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '居住地址',
  `sys_user_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统账号; 0: 非系统账号',
  `sys_user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像',
  `sys_user_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '部门ID',
  `sys_user_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_user_last_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录地点',
  `sys_user_register_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地点',
  `sys_user_last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录IP',
  `sys_user_register_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
  `sys_user_login_count` int(11) NULL DEFAULT NULL COMMENT '登录次数',
  `sys_client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `sys_user_totp_code` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'topt',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '53006d111477a9b5b16c4b38ac8fad69', '123456', 0, 1, '12345678901', 'test@qq.com', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '192.168.110.79', NULL, 9, NULL, 'b2fe2a1217dd70078efbaaf4f969ed3d37a5ea2eb1577006922e541f9e00ee6ab8d2d590ea5b4dcddccfa04ad65985e1a9f565dd9f017170d6911e5991dba0f4', NULL, NULL, NULL, '2024-08-28 10:09:26', 'sa', 1);
INSERT INTO `sys_user` VALUES (2, 'test', '测试', '0a44d441a857bc12ce1e764af68bb068', 'i6iuq2o73anxs5', 0, 1, '1', NULL, 1, NULL, 1, NULL, NULL, 1, '', NULL, NULL, '172.16.2.226', '172.16.2.226', 16, NULL, NULL, 'sa', NULL, '2024-08-20 17:06:18', '2025-03-21 15:27:46', 'sa', 1);

-- ----------------------------
-- Table structure for sys_user_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config`  (
  `sys_user_config_id` int(11) NOT NULL,
  `sys_user_config_wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `sys_user_config_wx_unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信unionid',
  `sys_user_config_gitee_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'giteeID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息 - 用户设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_layout
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_layout`;
CREATE TABLE `sys_user_layout`  (
  `sys_layout_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_layout_layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局',
  `sys_layout_grid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局网格',
  `sys_layout_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `sys_layout_user_id` int(11) NULL DEFAULT NULL COMMENT '所属用户',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_layout_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_layout
-- ----------------------------
INSERT INTO `sys_user_layout` VALUES (1, '[{\"x\":9,\"y\":0,\"w\":3,\"h\":1,\"i\":\"local-day\",\"id\":\"local-day\",\"static\":false,\"type\":1,\"moved\":false}]', '[]', '[[],[],[],{\"id\":\"local-day\"}]', 1, 'sa', NULL, '2024-08-29 17:20:12', '2025-03-26 15:23:56', 'sa', 1);

-- ----------------------------
-- Table structure for sys_user_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_log`;
CREATE TABLE `sys_user_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_isp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运营商',
  `sys_log_system` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统',
  `sys_log_ua` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'ua',
  `sys_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `sys_log_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器指纹',
  `sys_log_login_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '机构ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE,
  INDEX `index_sys_log_from`(`sys_log_from` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 373 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------
INSERT INTO `sys_user_log` VALUES (193, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 9991, 'LOGIN', 1, NULL, '', 1, NULL, 2, '2025-02-14 11:05:33', '2025-02-14 11:05:33', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (194, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"test\",\n    \"password\": \"57ba172a6be125cca2f449826f9980ca\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 47148, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 2, '2025-02-14 11:06:11', '2025-02-14 11:06:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (195, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"test\",\n    \"password\": \"57ba172a6be125cca2f449826f9980ca\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 3234, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 2, '2025-02-14 11:06:31', '2025-02-14 11:06:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (196, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 30, 'LOGIN', 1, NULL, '', 1, NULL, 2, '2025-02-14 11:06:42', '2025-02-14 11:06:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (197, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 83, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-14 11:08:01', '2025-02-14 11:08:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (198, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 11, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-02-17 15:56:36', '2025-02-17 15:56:36', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (199, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 167, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-17 15:56:44', '2025-02-17 15:56:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (200, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 8, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-02-18 10:35:22', '2025-02-18 10:35:22', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (201, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 151, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-18 10:35:31', '2025-02-18 10:35:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (202, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 488, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-18 16:13:58', '2025-02-18 16:13:58', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (203, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 417, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-18 16:15:09', '2025-02-18 16:15:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (204, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 602, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:15:05', '2025-02-19 11:15:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (205, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 1311, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 11:21:29', '2025-02-19 11:21:29', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (206, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 48, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:21:32', '2025-02-19 11:21:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (207, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 87, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 11:24:42', '2025-02-19 11:24:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (208, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:24:44', '2025-02-19 11:24:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (209, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 543, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:19:35', '2025-02-19 13:19:35', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (210, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 68, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:19:38', '2025-02-19 13:19:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (211, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 121, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:22:54', '2025-02-19 13:22:54', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (212, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 198, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:31:48', '2025-02-19 13:31:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (213, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 132, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:33:57', '2025-02-19 13:33:57', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (214, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 31, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:02', '2025-02-19 13:34:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (215, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:15', '2025-02-19 13:34:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (216, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:18', '2025-02-19 13:34:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (217, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 4205, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:34:31', '2025-02-19 13:34:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (218, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 123, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:35:19', '2025-02-19 13:35:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (219, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 13008, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:36:31', '2025-02-19 13:36:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (220, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 30, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:37:04', '2025-02-19 13:37:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (221, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\",\n    \"accountType\": \"tenant\"\n  }\n]', 6937, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:37:14', '2025-02-19 13:37:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (222, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:37:48', '2025-02-19 13:37:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (223, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\"\n  }\n]', 2945, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:37:55', '2025-02-19 13:37:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (224, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\"\n  }\n]', 7846, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:38:06', '2025-02-19 13:38:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (225, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 30, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:38:25', '2025-02-19 13:38:25', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (226, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 1122, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:39:50', '2025-02-19 13:39:50', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (227, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 6900, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:40:12', '2025-02-19 13:40:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (228, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 5839, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:43:30', '2025-02-19 13:43:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (229, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 2769, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:44:32', '2025-02-19 13:44:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (230, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 3315, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:44:40', '2025-02-19 13:44:40', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (231, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"1\"\n  }\n]', 3280, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:50:08', '2025-02-19 13:50:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (232, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 103, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:50:14', '2025-02-19 13:50:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (233, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 257, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:50:15', '2025-02-19 13:50:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (234, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4266, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:04:49', '2025-02-19 14:04:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (235, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 92, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:04:52', '2025-02-19 14:04:52', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (236, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 538, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:05:53', '2025-02-19 14:05:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (237, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 15444, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:06:49', '2025-02-19 14:06:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (238, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 18131, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:07:20', '2025-02-19 14:07:20', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (239, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 18688, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:07:44', '2025-02-19 14:07:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (240, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 167, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:19:07', '2025-02-19 14:19:07', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (241, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"\"\n  }\n]', 8130, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:20:08', '2025-02-19 14:20:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (242, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 2748, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:25:03', '2025-02-19 14:25:03', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (243, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2355, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:25:05', '2025-02-19 14:25:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (244, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 93, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:26:28', '2025-02-19 14:26:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (245, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 308, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:26:30', '2025-02-19 14:26:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (246, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 294, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:30:14', '2025-02-19 14:30:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (247, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 699, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:44:59', '2025-02-19 14:44:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (248, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2920, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:00:08', '2025-02-19 15:00:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (249, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2252, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:17:27', '2025-02-19 15:17:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (250, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 315, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 15:34:16', '2025-02-19 15:34:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (251, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 331, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:34:17', '2025-02-19 15:34:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (252, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 3203, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-20 15:46:18', '2025-02-20 15:46:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (253, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 655, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 15:48:08', '2025-02-24 15:48:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (254, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 698, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:45:59', '2025-02-24 16:45:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (255, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 206, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:00', '2025-02-24 16:46:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (256, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 275, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:06', '2025-02-24 16:46:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (257, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 134, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:06', '2025-02-24 16:46:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (258, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 5357, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:28', '2025-02-24 16:46:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (259, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 48, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:29', '2025-02-24 16:46:29', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (260, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 276, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:38', '2025-02-24 16:46:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (261, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 73, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:38', '2025-02-24 16:46:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (262, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 274, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:47:00', '2025-02-24 16:47:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (263, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 56, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:47:09', '2025-02-24 16:47:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (264, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 60017, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:48:16', '2025-02-24 16:48:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (265, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 60016, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:00:10', '2025-02-24 17:00:10', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (266, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 10383, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:00:27', '2025-02-24 17:00:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (267, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 50, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:00:27', '2025-02-24 17:00:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (268, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2351, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:09', '2025-02-24 17:01:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (269, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:09', '2025-02-24 17:01:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (270, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 12081, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:28', '2025-02-24 17:01:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (271, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:28', '2025-02-24 17:01:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (272, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4257, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:44', '2025-02-24 17:01:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (273, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 47, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:44', '2025-02-24 17:01:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (274, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 7829, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:10:32', '2025-02-24 17:10:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (275, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 516, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 10:43:15', '2025-02-25 10:43:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (276, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 698, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 10:45:41', '2025-02-25 10:45:41', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (277, NULL, '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"\"\n  }\n]', 2489, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-25 11:22:16', '2025-02-25 11:22:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (278, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1170, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 11:23:16', '2025-02-25 11:23:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (279, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 391, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 11:25:56', '2025-02-25 11:25:56', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (280, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 928, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 13:08:28', '2025-02-25 13:08:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (281, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1181, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 13:20:50', '2025-02-25 13:20:50', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (282, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 255, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 13:31:01', '2025-02-25 13:31:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (283, 'test', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 172, 'LOGIN', 1, NULL, '', 1, NULL, 20, '2025-02-25 14:17:59', '2025-02-25 14:17:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (284, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1562, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 14:18:01', '2025-02-25 14:18:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (285, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1142, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 14:48:21', '2025-02-25 14:48:21', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (286, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 2893, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 15:08:39', '2025-02-25 15:08:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (287, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 550, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 15:08:39', '2025-02-25 15:08:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (288, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 257, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 15:14:18', '2025-02-25 15:14:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (289, 'test', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 659, 'LOGIN', 1, NULL, '', 1, NULL, 20, '2025-02-25 16:32:23', '2025-02-25 16:32:23', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (290, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1037, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 16:32:25', '2025-02-25 16:32:25', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (291, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 216, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 16:51:27', '2025-02-25 16:51:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (292, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 47, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 16:59:39', '2025-02-25 16:59:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (293, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 86, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 17:01:52', '2025-02-25 17:01:52', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (294, 'sa', NULL, '192.168.137.1', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 291, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-26 17:27:06', '2025-02-26 17:27:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (295, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 523, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-27 09:15:56', '2025-02-27 09:15:56', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (296, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1279, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-04 14:00:16', '2025-03-04 14:00:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (297, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 40, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-04 14:00:17', '2025-03-04 14:00:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (298, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-04 14:00:26', '2025-03-04 14:00:26', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (299, 'sa', NULL, '192.168.137.1', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1967, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-05 13:18:16', '2025-03-05 13:18:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (300, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 202, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-07 15:36:04', '2025-03-07 15:36:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (301, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 90, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-07 15:36:06', '2025-03-07 15:36:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (302, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 59, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-07 15:37:46', '2025-03-07 15:37:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (303, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 18, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-18 08:42:11', '2025-03-18 08:42:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (304, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 264, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-18 08:42:24', '2025-03-18 08:42:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (305, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1008, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:06:10', '2025-03-21 13:06:10', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (306, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:06:11', '2025-03-21 13:06:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (307, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 61, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:06:27', '2025-03-21 13:06:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (308, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:06:27', '2025-03-21 13:06:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (309, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 13:08:08', '2025-03-21 13:08:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (310, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 49, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:09', '2025-03-21 13:08:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (311, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 13:08:11', '2025-03-21 13:08:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (312, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:13', '2025-03-21 13:08:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (313, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:08:13', '2025-03-21 13:08:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (314, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 38, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:24', '2025-03-21 13:08:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (315, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:08:24', '2025-03-21 13:08:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (316, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:34', '2025-03-21 13:08:34', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (317, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 57, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:11:08', '2025-03-21 13:11:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (318, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:11:08', '2025-03-21 13:11:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (319, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 57, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:21:55', '2025-03-21 13:21:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (320, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:21:55', '2025-03-21 13:21:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (321, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:22:17', '2025-03-21 13:22:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (322, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:22:47', '2025-03-21 13:22:47', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (323, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 38, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:18', '2025-03-21 13:23:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (324, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:18', '2025-03-21 13:23:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (325, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 90, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:46', '2025-03-21 13:23:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (326, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:46', '2025-03-21 13:23:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (327, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 43, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:53', '2025-03-21 13:23:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (328, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:53', '2025-03-21 13:23:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (329, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:24:14', '2025-03-21 13:24:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (330, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:24:27', '2025-03-21 13:24:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (331, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2978, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:26:30', '2025-03-21 13:26:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (332, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:27:00', '2025-03-21 13:27:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (333, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 265, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:28:37', '2025-03-21 13:28:37', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (334, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:29:07', '2025-03-21 13:29:07', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (335, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 9173, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:06', '2025-03-21 14:12:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (336, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 456, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:27', '2025-03-21 14:12:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (337, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 276, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:42', '2025-03-21 14:12:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (338, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:13:12', '2025-03-21 14:13:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (339, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1026, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:14:48', '2025-03-21 14:14:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (340, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:14:55', '2025-03-21 14:14:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (341, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 69, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:15:02', '2025-03-21 14:15:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (342, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:15:02', '2025-03-21 14:15:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (343, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 14:15:12', '2025-03-21 14:15:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (344, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 45, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:15:13', '2025-03-21 14:15:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (345, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 636, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 15:41:53', '2025-03-21 15:41:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (346, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 47, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 15:42:18', '2025-03-21 15:42:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (347, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 395, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 16:13:04', '2025-03-21 16:13:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (348, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 109, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 16:13:05', '2025-03-21 16:13:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (349, NULL, NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1133, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-03-24 13:12:19', '2025-03-24 13:12:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (350, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4906, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:12:19', '2025-03-24 13:12:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (351, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 61, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:12:22', '2025-03-24 13:12:22', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (352, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 13:15:42', '2025-03-24 13:15:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (353, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 54, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:15:44', '2025-03-24 13:15:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (354, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 328, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 14:17:39', '2025-03-24 14:17:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (355, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 271, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 14:57:31', '2025-03-24 14:57:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (356, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1709, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 14:57:36', '2025-03-24 14:57:36', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (357, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 110, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:10:12', '2025-03-24 16:10:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (358, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1025, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:38:33', '2025-03-24 16:38:33', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (359, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 50, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:00', '2025-03-24 16:39:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (360, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:39:14', '2025-03-24 16:39:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (361, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 17, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:16', '2025-03-24 16:39:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (362, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 56, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:39:41', '2025-03-24 16:39:41', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (363, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 39, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:43', '2025-03-24 16:39:43', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (364, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 46, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:14', '2025-03-24 16:40:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (365, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 20, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:40:16', '2025-03-24 16:40:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (366, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 58, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:46', '2025-03-24 16:40:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (367, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 34, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:40:49', '2025-03-24 16:40:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (368, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:51', '2025-03-24 16:40:51', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (369, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 61, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:44:31', '2025-03-24 16:44:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (370, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 68, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:45:21', '2025-03-24 16:45:21', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (371, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1270, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-25 21:29:05', '2025-03-25 21:29:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (372, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1029, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-26 15:23:44', '2025-03-26 15:23:44', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `sys_user_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`sys_user_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 - 角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (30, 2, 4);

-- ----------------------------
-- Table structure for sys_user_third_party
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_third_party`;
CREATE TABLE `sys_user_third_party`  (
  `sys_user_third_party_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_third_party_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方类型. gitee',
  `sys_user_third_party_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息名称',
  `sys_user_third_party_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息',
  `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '当前系统用户ID',
  `sys_user_third_party_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方用户ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_third_party_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户三方绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_third_party
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
