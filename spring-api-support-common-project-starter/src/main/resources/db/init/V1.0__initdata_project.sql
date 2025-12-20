-- =====================================================
-- Project 模块初始化数据脚本
-- 版本: V1.0
-- 说明: 包含AI模型、文生图设置、风格、模板、邮件日志/模板、项目配置等基础数据
-- 执行方式: 异步执行（虚拟线程）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 初始数据 - AI模型分类表 (sys_ai_module)
-- =====================================================
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

-- =====================================================
-- 初始数据 - 文生图模型参数 (sys_ai_vincent_setting)
-- =====================================================
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

-- =====================================================
-- 初始数据 - 文生图风格 (sys_ai_vincent_style)
-- =====================================================
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

-- =====================================================
-- 初始数据 - 文生图模板 (sys_ai_vincent_template)
-- =====================================================
INSERT INTO `sys_ai_vincent_template` VALUES (1, 7, '四格漫画', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (2, 7, '情侣头像', 'qinglvtouxiang', 'https://wanx.alicdn.com/model/qinglvtouxiang.png', '擅长清新可爱的情侣头像', 2, '303-2.1-t2i-plus', 'qinglvtouxiang', 'combining_images', 'wanx2.1-t2i-plus', '针织娃娃相对而望，甜蜜的情侣头像, 一对白色毛毡小鸟站在树枝上，轻声细语, 一对情侣戴动物帽子相互微笑，黄色卡通背景, 两只毛毡小狗在绿草地上欢快地追逐嬉戏, 一对白兔子耳朵缠绕在一起，构成情侣头像', '1440*720', '描述你想要的头像内容与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (3, 7, '图标印花', 'tubiaoyinhua', 'https://wanx.alicdn.com/model/tubiaoyinhua.png', '擅长设计和展示印花效果', 3, '302-2.1-t2i-plus', 'tubiaoyinhua', 'combining_images', 'wanx2.1-t2i-plus', '面包店的菱形logo应用在招牌上, 健身房的logo设计应用于健身T恤, 花店的logo展示在漂亮的橱窗中, 茶馆的logo应用在精美的茶杯上, 服装店的logo别致地印在衣物标签上', '1248*832', '描述你希望设计的Logo印花样式与贴图效果', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (4, 7, '电影分镜', 'dianyingfenjing', 'https://wanx.alicdn.com/model/dianyingfenjing.png', '擅长电影质感画面', 4, '300-2.1-t2i-plus', 'dianyingfenjing', 'combining_images', 'wanx2.1-t2i-plus', '两个人在雪地里一起堆雪人，欢乐无比。, 蒙古草原上的骏马飞驰，场景壮丽。, 圣诞老人点燃烟花，场面充满欢乐。, 火箭从发射升空，壮观地进入太空。, 两人在篝火旁载歌载舞，气氛热烈。', '1072*1424', '描述你希望展现的电影故事主题与画面内容', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (5, 7, '新年请神', 'qingshen', 'https://wanx.alicdn.com/model/qingshen.png', '擅长人像', 5, '304-2.1-t2i-plus', 'qingshen', 'style', 'wanx2.1-t2i-plus', '一个女人，双手托着笔记本电脑, 一个女孩捧着鲜花, 一个女孩拿着一杯珍珠奶茶, 一个女孩拿着红包, 一个女孩捧着榴莲，背景里飘着榴莲', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (9, 7, '四格漫画2', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', 'sa', 1, '2025-03-04 11:08:14', NULL, NULL, NULL);

-- =====================================================
-- 初始数据 - 邮件日志表 (sys_email_log)
-- =====================================================
INSERT INTO `sys_email_log` VALUES (6, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 2392.00, '00000', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_email_log` VALUES (7, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 1348.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (8, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 893.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (9, 'request.getParams()', '1', NULL, '<p><strong>测试</strong></p>', 40.00, 'FAILURE', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (10, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 917.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, 2);

-- =====================================================
-- 初始数据 - 邮件模板 (sys_email_template)
-- =====================================================
INSERT INTO `sys_email_template` VALUES (1, 2, NULL, 1, 4, '测试', '111', '<p><strong>测试</strong></p>', NULL, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 'http://127.0.0.1:18170/system/api/v2/project/template/email/callback', 'sa', 1, '2025-01-27 20:00:31', '2025-01-27 20:04:46', 'sa', 1);

-- =====================================================
-- 初始数据 - 默认功能映射 (sys_project_default)
-- =====================================================
INSERT INTO `sys_project_default` VALUES (30, '6', 3);
INSERT INTO `sys_project_default` VALUES (33, '16', 2);
INSERT INTO `sys_project_default` VALUES (37, '14', 4);
INSERT INTO `sys_project_default` VALUES (46, '13', 1);
INSERT INTO `sys_project_default` VALUES (47, '20', 16);

-- =====================================================
-- 初始数据 - 密钥 (sys_secret)
-- =====================================================
INSERT INTO `sys_secret` VALUES (1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '犀鸟校园', 0, 1, NULL, NULL, NULL, 2, NULL, 'SMS', 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);

SET FOREIGN_KEY_CHECKS = 1;
