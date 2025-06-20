# 服务器管理组件说明

本目录包含服务器管理相关的子组件，这些组件被主页面引用以实现特定功能。

## 组件列表

### 服务器管理组件
- `ServerFormDialog.vue` - 服务器添加/编辑对话框
- `ConnectionHistoryDialog.vue` - 连接历史记录对话框

### 日志管理组件
- `LogDetailDialog.vue` - 日志详情对话框
- `LogConfigDialog.vue` - 日志配置对话框
- `LogCleanupDialog.vue` - 日志清理对话框

### 文件上传组件
- `UploadTaskDialog.vue` - 上传任务创建对话框
- `UploadTaskDetailDialog.vue` - 上传任务详情对话框
- `UploadProgressDialog.vue` - 上传进度监控对话框

### 脚本管理组件
- `ScriptEditorDialog.vue` - 脚本编辑器对话框
- `ScriptExecuteDialog.vue` - 脚本执行对话框
- `ScriptDetailDialog.vue` - 脚本详情对话框
- `ScriptHistoryDialog.vue` - 脚本执行历史对话框

## 组件特性

### 通用特性
- 所有对话框组件都支持 `v-model:visible` 双向绑定
- 统一的成功回调事件 `@success`
- 响应式设计，支持移动端适配
- 统一的错误处理和加载状态

### 数据交互
- 使用 TypeScript 进行类型约束
- 统一的 API 调用封装
- WebSocket 实时数据更新支持

### UI/UX
- 遵循 Element Plus 设计规范
- 统一的样式和交互模式
- 友好的用户反馈和提示

## 开发指南

### 创建新组件
1. 在对应功能目录下创建 `.vue` 文件
2. 使用 TypeScript 和 Composition API
3. 遵循现有的命名和结构规范
4. 添加必要的类型定义和文档注释

### 组件通信
```typescript
// Props 定义
interface Props {
  visible: boolean
  data?: SomeType
}

// Emits 定义
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()
```

### 样式规范
- 使用 SCSS 预处理器
- 遵循 BEM 命名规范
- 使用 CSS 变量进行主题适配

## 注意事项

1. **性能优化**
   - 大数据量时使用虚拟滚动
   - 合理使用 `v-show` 和 `v-if`
   - 避免不必要的响应式数据

2. **错误处理**
   - 统一的错误提示机制
   - 网络请求失败的降级处理
   - 表单验证和用户输入校验

3. **可访问性**
   - 支持键盘导航
   - 合理的 ARIA 标签
   - 色彩对比度符合标准

4. **国际化**
   - 预留国际化支持
   - 文本内容可配置化
   - 日期时间格式本地化
