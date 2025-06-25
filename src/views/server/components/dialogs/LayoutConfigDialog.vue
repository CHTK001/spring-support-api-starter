<template>
  <el-dialog
    v-model="visible"
    title="布局配置管理"
    width="90%"
    :close-on-click-modal="false"
    destroy-on-close
    class="layout-config-dialog"
    top="5vh"
  >
    <div class="layout-config-container">
      <!-- 左侧模板列表 -->
      <div class="template-panel">
        <div class="panel-header">
          <h3>布局模板</h3>
          <div class="header-actions">
            <el-button size="small" @click="handleCreateTemplate">
              <IconifyIconOnline icon="ri:add-line" />
              新建模板
            </el-button>
            <el-button size="small" @click="handleImportTemplate">
              <IconifyIconOnline icon="ri:upload-line" />
              导入
            </el-button>
          </div>
        </div>
        
        <div class="template-list">
          <div class="template-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索模板..."
              clearable
              size="small"
            >
              <template #prefix>
                <IconifyIconOnline icon="ri:search-line" />
              </template>
            </el-input>
          </div>
          
          <div class="template-categories">
            <el-tabs v-model="activeCategory" type="card" size="small">
              <el-tab-pane label="全部" name="all" />
              <el-tab-pane label="默认" name="default" />
              <el-tab-pane label="自定义" name="custom" />
              <el-tab-pane label="共享" name="shared" />
            </el-tabs>
          </div>
          
          <div class="template-items">
            <div
              v-for="template in filteredTemplates"
              :key="template.id"
              class="template-item"
              :class="{ active: selectedTemplate?.id === template.id }"
              @click="selectTemplate(template)"
            >
              <div class="template-preview">
                <img
                  v-if="template.preview"
                  :src="template.preview"
                  :alt="template.name"
                  class="preview-image"
                />
                <div v-else class="preview-placeholder">
                  <IconifyIconOnline icon="ri:layout-grid-line" />
                </div>
              </div>
              
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-desc">{{ template.description }}</div>
                <div class="template-meta">
                  <span class="template-version">v{{ template.version }}</span>
                  <span class="template-author">{{ template.author }}</span>
                </div>
                <div class="template-tags">
                  <el-tag
                    v-for="tag in template.tags"
                    :key="tag"
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              
              <div class="template-actions">
                <el-dropdown @command="(cmd) => handleTemplateAction(cmd, template)">
                  <el-button size="small" text>
                    <IconifyIconOnline icon="ri:more-line" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="apply">应用模板</el-dropdown-item>
                      <el-dropdown-item command="edit">编辑模板</el-dropdown-item>
                      <el-dropdown-item command="duplicate">复制模板</el-dropdown-item>
                      <el-dropdown-item command="export">导出模板</el-dropdown-item>
                      <el-dropdown-item command="share" v-if="!template.isPublic">分享模板</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除模板</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧详情面板 -->
      <div class="detail-panel">
        <div v-if="selectedTemplate" class="template-detail">
          <div class="detail-header">
            <h3>{{ selectedTemplate.name }}</h3>
            <div class="header-actions">
              <el-button type="primary" @click="applyTemplate">
                <IconifyIconOnline icon="ri:check-line" />
                应用模板
              </el-button>
              <el-button @click="exportTemplate">
                <IconifyIconOnline icon="ri:download-line" />
                导出
              </el-button>
            </div>
          </div>
          
          <div class="detail-content">
            <!-- 模板预览 -->
            <el-card class="detail-section" shadow="never">
              <template #header>
                <span>布局预览</span>
              </template>
              <div class="layout-preview">
                <div class="preview-container">
                  <!-- 这里显示布局预览 -->
                  <div class="grid-preview">
                    <div
                      v-for="item in selectedTemplate.config.layout"
                      :key="item.i"
                      class="grid-item-preview"
                      :style="getItemStyle(item)"
                    >
                      <div class="item-content">
                        <span class="item-type">{{ getComponentTypeName(item.componentType) }}</span>
                        <span class="item-id">{{ item.i }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
            
            <!-- 模板信息 -->
            <el-card class="detail-section" shadow="never">
              <template #header>
                <span>模板信息</span>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="模板名称">
                  {{ selectedTemplate.name }}
                </el-descriptions-item>
                <el-descriptions-item label="版本">
                  {{ selectedTemplate.version }}
                </el-descriptions-item>
                <el-descriptions-item label="作者">
                  {{ selectedTemplate.author || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatTime(selectedTemplate.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ formatTime(selectedTemplate.updateTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="组件数量">
                  {{ selectedTemplate.components.length }}
                </el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                  {{ selectedTemplate.description || '暂无描述' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
            
            <!-- 组件列表 -->
            <el-card class="detail-section" shadow="never">
              <template #header>
                <span>包含组件</span>
              </template>
              <el-table :data="selectedTemplate.components" size="small">
                <el-table-column prop="name" label="组件名称" />
                <el-table-column prop="title" label="组件标题" />
                <el-table-column prop="type" label="组件类型">
                  <template #default="{ row }">
                    <el-tag size="small">{{ getComponentTypeName(row.type) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="位置">
                  <template #default="{ row }">
                    {{ row.position.x }},{{ row.position.y }} ({{ row.position.w }}×{{ row.position.h }})
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>
        
        <div v-else class="no-selection">
          <el-empty description="请选择一个布局模板">
            <template #image>
              <IconifyIconOnline icon="ri:layout-grid-line" />
            </template>
          </el-empty>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
      </div>
    </template>

    <!-- 模板编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑模板"
      width="600px"
      append-to-body
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="editForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" placeholder="请选择分类">
            <el-option label="默认" value="default" />
            <el-option label="监控" value="monitoring" />
            <el-option label="分析" value="analytics" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="editForm.tags"
            multiple
            filterable
            allow-create
            placeholder="请输入标签"
          >
            <el-option label="监控" value="monitoring" />
            <el-option label="性能" value="performance" />
            <el-option label="系统" value="system" />
            <el-option label="网络" value="network" />
          </el-select>
        </el-form-item>
        <el-form-item label="公开模板">
          <el-switch v-model="editForm.isPublic" />
          <span class="form-tip">公开后其他用户可以使用此模板</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文件上传 -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".json,.yaml,.yml"
      style="display: none"
      @change="handleFileImport"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from '@repo/utils';
import { ElMessageBox } from 'element-plus';
import type { LayoutTemplate, LayoutItem } from '../../types/layout';
import { useLayoutManager } from '../../composables/useLayoutManager';

// Emits
const emit = defineEmits<{
  apply: [template: LayoutTemplate];
}>();

// 组合式函数
const { templates, setLayout } = useLayoutManager();

// 响应式状态
const visible = ref(false);
const loading = ref(false);
const searchKeyword = ref('');
const activeCategory = ref('all');
const selectedTemplate = ref<LayoutTemplate | null>(null);
const editDialogVisible = ref(false);

// 编辑表单
const editForm = reactive({
  name: '',
  description: '',
  category: '',
  tags: [] as string[],
  isPublic: false
});

// 文件输入引用
const fileInputRef = ref<HTMLInputElement>();

// 计算属性
const filteredTemplates = computed(() => {
  let result = templates.value;
  
  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(template => {
      switch (activeCategory.value) {
        case 'default':
          return template.isDefault;
        case 'custom':
          return !template.isDefault && !template.isPublic;
        case 'shared':
          return template.isPublic;
        default:
          return true;
      }
    });
  }
  
  // 按关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(template =>
      template.name.toLowerCase().includes(keyword) ||
      template.description?.toLowerCase().includes(keyword) ||
      template.tags?.some(tag => tag.toLowerCase().includes(keyword))
    );
  }
  
  return result;
});

/**
 * 选择模板
 */
const selectTemplate = (template: LayoutTemplate) => {
  selectedTemplate.value = template;
};

/**
 * 应用模板
 */
const applyTemplate = () => {
  if (selectedTemplate.value) {
    emit('apply', selectedTemplate.value);
    visible.value = false;
    message.success('模板应用成功');
  }
};

/**
 * 导出模板
 */
const exportTemplate = () => {
  if (!selectedTemplate.value) return;
  
  const dataStr = JSON.stringify(selectedTemplate.value, null, 2);
  const dataBlob = new Blob([dataStr], { type: 'application/json' });
  const url = URL.createObjectURL(dataBlob);
  
  const link = document.createElement('a');
  link.href = url;
  link.download = `${selectedTemplate.value.name}_layout_template.json`;
  link.click();
  
  URL.revokeObjectURL(url);
  message.success('模板导出成功');
};

/**
 * 处理模板操作
 */
const handleTemplateAction = async (command: string, template: LayoutTemplate) => {
  switch (command) {
    case 'apply':
      selectedTemplate.value = template;
      applyTemplate();
      break;
    case 'edit':
      editTemplate(template);
      break;
    case 'duplicate':
      duplicateTemplate(template);
      break;
    case 'export':
      selectedTemplate.value = template;
      exportTemplate();
      break;
    case 'share':
      shareTemplate(template);
      break;
    case 'delete':
      deleteTemplate(template);
      break;
  }
};

/**
 * 编辑模板
 */
const editTemplate = (template: LayoutTemplate) => {
  Object.assign(editForm, {
    name: template.name,
    description: template.description || '',
    category: template.category || '',
    tags: template.tags || [],
    isPublic: template.isPublic || false
  });
  editDialogVisible.value = true;
};

/**
 * 复制模板
 */
const duplicateTemplate = (template: LayoutTemplate) => {
  const newTemplate: LayoutTemplate = {
    ...template,
    id: `template_${Date.now()}`,
    name: `${template.name} (副本)`,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString(),
    isDefault: false,
    isPublic: false
  };
  
  templates.value.push(newTemplate);
  message.success('模板复制成功');
};

/**
 * 分享模板
 */
const shareTemplate = async (template: LayoutTemplate) => {
  try {
    await ElMessageBox.confirm('确定要公开分享此模板吗？', '分享确认');
    template.isPublic = true;
    message.success('模板已公开分享');
  } catch {
    // 用户取消
  }
};

/**
 * 删除模板
 */
const deleteTemplate = async (template: LayoutTemplate) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板"${template.name}"吗？`, '删除确认', {
      type: 'warning'
    });
    
    const index = templates.value.findIndex(t => t.id === template.id);
    if (index > -1) {
      templates.value.splice(index, 1);
      if (selectedTemplate.value?.id === template.id) {
        selectedTemplate.value = null;
      }
      message.success('模板删除成功');
    }
  } catch {
    // 用户取消
  }
};

/**
 * 创建新模板
 */
const handleCreateTemplate = () => {
  Object.assign(editForm, {
    name: '',
    description: '',
    category: 'custom',
    tags: [],
    isPublic: false
  });
  editDialogVisible.value = true;
};

/**
 * 保存模板
 */
const saveTemplate = () => {
  if (!editForm.name.trim()) {
    message.error('请输入模板名称');
    return;
  }
  
  // 这里应该调用API保存模板
  message.success('模板保存成功');
  editDialogVisible.value = false;
};

/**
 * 导入模板
 */
const handleImportTemplate = () => {
  fileInputRef.value?.click();
};

/**
 * 处理文件导入
 */
const handleFileImport = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const content = e.target?.result as string;
      const template = JSON.parse(content) as LayoutTemplate;
      
      // 验证模板格式
      if (!template.id || !template.name || !template.config) {
        throw new Error('模板格式不正确');
      }
      
      // 生成新ID避免冲突
      template.id = `imported_${Date.now()}`;
      template.createTime = new Date().toISOString();
      template.updateTime = new Date().toISOString();
      
      templates.value.push(template);
      message.success('模板导入成功');
    } catch (error) {
      console.error('导入失败:', error);
      message.error('模板导入失败，请检查文件格式');
    }
  };
  
  reader.readAsText(file);
  
  // 清空文件输入
  if (fileInputRef.value) {
    fileInputRef.value.value = '';
  }
};

/**
 * 获取组件样式
 */
const getItemStyle = (item: LayoutItem) => {
  const colWidth = 100 / 24; // 24列网格
  return {
    left: `${item.x * colWidth}%`,
    top: `${item.y * 30}px`,
    width: `${item.w * colWidth}%`,
    height: `${item.h * 30}px`
  };
};

/**
 * 获取组件类型名称
 */
const getComponentTypeName = (type?: string) => {
  const typeMap: Record<string, string> = {
    card: '卡片',
    gauge: '仪表盘',
    line: '折线图',
    bar: '柱状图',
    pie: '饼图',
    table: '表格'
  };
  return typeMap[type || ''] || type || '未知';
};

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return new Date(time).toLocaleString();
};

/**
 * 打开对话框
 */
const open = () => {
  visible.value = true;
  loadTemplates();
};

/**
 * 加载模板列表
 */
const loadTemplates = () => {
  // 这里应该从API加载模板，暂时使用模拟数据
  const mockTemplates: LayoutTemplate[] = [
    {
      id: 'default_1',
      name: '默认监控布局',
      description: '包含CPU、内存、磁盘、网络等基础监控组件',
      category: 'monitoring',
      tags: ['监控', '系统', '默认'],
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      version: '1.0.0',
      author: '系统',
      isDefault: true,
      isPublic: true,
      config: {
        layout: [
          { i: 'cpu', x: 0, y: 0, w: 6, h: 4, componentType: 'card' },
          { i: 'memory', x: 6, y: 0, w: 6, h: 4, componentType: 'card' },
          { i: 'disk', x: 12, y: 0, w: 6, h: 4, componentType: 'card' },
          { i: 'network', x: 18, y: 0, w: 6, h: 4, componentType: 'card' }
        ],
        colNum: 24,
        rowHeight: 30,
        margin: [10, 10],
        containerPadding: [10, 10],
        isDraggable: true,
        isResizable: true,
        isMirrored: false,
        verticalCompact: true,
        useCSSTransforms: true,
        responsive: false
      },
      components: [
        { id: 'cpu', name: 'cpu_usage', title: 'CPU使用率', type: 'card', position: { x: 0, y: 0, w: 6, h: 4 }, config: {} },
        { id: 'memory', name: 'memory_usage', title: '内存使用率', type: 'card', position: { x: 6, y: 0, w: 6, h: 4 }, config: {} },
        { id: 'disk', name: 'disk_usage', title: '磁盘使用率', type: 'card', position: { x: 12, y: 0, w: 6, h: 4 }, config: {} },
        { id: 'network', name: 'network_io', title: '网络IO', type: 'card', position: { x: 18, y: 0, w: 6, h: 4 }, config: {} }
      ]
    }
  ];
  
  templates.value = mockTemplates;
};

// 暴露方法
defineExpose({
  open
});

onMounted(() => {
  loadTemplates();
});
</script>

<style scoped lang="scss">
.layout-config-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.layout-config-container {
  display: flex;
  height: 70vh;
}

.template-panel {
  width: 350px;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  
  .panel-header {
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .template-list {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .template-search {
      padding: 12px 16px;
    }
    
    .template-categories {
      padding: 0 16px;
      
      :deep(.el-tabs__header) {
        margin: 0;
      }
    }
    
    .template-items {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
      
      .template-item {
        display: flex;
        padding: 12px;
        margin-bottom: 8px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
          border-color: var(--el-color-primary);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        &.active {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }
        
        .template-preview {
          width: 60px;
          height: 60px;
          margin-right: 12px;
          
          .preview-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 4px;
          }
          
          .preview-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--el-fill-color-light);
            border-radius: 4px;
            color: var(--el-text-color-placeholder);
            font-size: 24px;
          }
        }
        
        .template-info {
          flex: 1;
          
          .template-name {
            font-weight: 600;
            margin-bottom: 4px;
          }
          
          .template-desc {
            font-size: 12px;
            color: var(--el-text-color-regular);
            margin-bottom: 4px;
            line-height: 1.4;
          }
          
          .template-meta {
            font-size: 11px;
            color: var(--el-text-color-placeholder);
            margin-bottom: 4px;
            
            .template-version {
              margin-right: 8px;
            }
          }
          
          .template-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
          }
        }
        
        .template-actions {
          align-self: flex-start;
        }
      }
    }
  }
}

.detail-panel {
  flex: 1;
  overflow: hidden;
  
  .template-detail {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .detail-header {
      padding: 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
      
      .header-actions {
        display: flex;
        gap: 8px;
      }
    }
    
    .detail-content {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      
      .detail-section {
        margin-bottom: 16px;
        
        :deep(.el-card__header) {
          padding: 12px 16px;
          background: var(--el-fill-color-light);
        }
      }
    }
  }
  
  .no-selection {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-placeholder);
  }
}

.layout-preview {
  .preview-container {
    height: 200px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    position: relative;
    overflow: hidden;
    background: var(--el-fill-color-lighter);
    
    .grid-preview {
      position: relative;
      width: 100%;
      height: 100%;
      
      .grid-item-preview {
        position: absolute;
        border: 1px solid var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        border-radius: 2px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 10px;
        
        .item-content {
          text-align: center;
          
          .item-type {
            display: block;
            font-weight: 600;
            color: var(--el-color-primary);
          }
          
          .item-id {
            display: block;
            color: var(--el-text-color-regular);
            margin-top: 2px;
          }
        }
      }
    }
  }
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.dialog-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  text-align: right;
}
</style>
