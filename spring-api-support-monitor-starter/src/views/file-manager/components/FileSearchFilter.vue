<template>
  <div class="file-search-filter">
    <!-- 基础搜索栏 -->
    <div class="basic-search">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文件和文件夹..."
        :prefix-icon="Search"
        clearable
        class="search-input"
        @input="handleSearch"
        @keyup.enter="handleAdvancedSearch"
      >
        <template #append>
          <el-button
            :icon="Filter"
            @click="toggleAdvancedSearch"
            :type="showAdvanced ? 'primary' : 'default'"
            title="高级搜索"
          />
        </template>
      </el-input>
    </div>

    <!-- 高级搜索面板 -->
    <el-collapse-transition>
      <div v-show="showAdvanced" class="advanced-search">
        <el-card shadow="never" class="search-panel">
          <template #header>
            <div class="panel-header">
              <span>高级搜索</span>
              <el-button
                text
                type="primary"
                size="small"
                @click="resetFilters"
              >
                重置
              </el-button>
            </div>
          </template>

          <el-form :model="filters" label-width="80px" size="small">
            <el-row :gutter="16">
              <!-- 文件名搜索 -->
              <el-col :span="12">
                <el-form-item label="文件名">
                  <el-input
                    v-model="filters.name"
                    placeholder="支持通配符 * 和 ?"
                    clearable
                  />
                </el-form-item>
              </el-col>

              <!-- 文件类型 -->
              <el-col :span="12">
                <el-form-item label="文件类型">
                  <el-select
                    v-model="filters.type"
                    placeholder="选择文件类型"
                    clearable
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                  >
                    <el-option-group label="常用类型">
                      <el-option label="文件夹" value="directory" />
                      <el-option label="图片" value="image" />
                      <el-option label="文档" value="document" />
                      <el-option label="视频" value="video" />
                      <el-option label="音频" value="audio" />
                      <el-option label="代码" value="code" />
                    </el-option-group>
                    <el-option-group label="具体格式">
                      <el-option label="PDF" value="pdf" />
                      <el-option label="Word" value="doc,docx" />
                      <el-option label="Excel" value="xls,xlsx" />
                      <el-option label="PowerPoint" value="ppt,pptx" />
                      <el-option label="文本" value="txt" />
                      <el-option label="JSON" value="json" />
                      <el-option label="XML" value="xml" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <!-- 文件大小 -->
              <el-col :span="12">
                <el-form-item label="文件大小">
                  <div class="size-filter">
                    <el-select
                      v-model="filters.sizeOperator"
                      style="width: 80px"
                    >
                      <el-option label=">" value="gt" />
                      <el-option label="<" value="lt" />
                      <el-option label="=" value="eq" />
                      <el-option label="≥" value="gte" />
                      <el-option label="≤" value="lte" />
                    </el-select>
                    <el-input-number
                      v-model="filters.sizeValue"
                      :min="0"
                      :precision="2"
                      style="width: 120px; margin: 0 8px"
                    />
                    <el-select
                      v-model="filters.sizeUnit"
                      style="width: 80px"
                    >
                      <el-option label="B" value="B" />
                      <el-option label="KB" value="KB" />
                      <el-option label="MB" value="MB" />
                      <el-option label="GB" value="GB" />
                    </el-select>
                  </div>
                </el-form-item>
              </el-col>

              <!-- 修改时间 -->
              <el-col :span="12">
                <el-form-item label="修改时间">
                  <el-select
                    v-model="filters.timeRange"
                    placeholder="选择时间范围"
                    clearable
                    @change="handleTimeRangeChange"
                  >
                    <el-option label="今天" value="today" />
                    <el-option label="昨天" value="yesterday" />
                    <el-option label="本周" value="thisWeek" />
                    <el-option label="上周" value="lastWeek" />
                    <el-option label="本月" value="thisMonth" />
                    <el-option label="上月" value="lastMonth" />
                    <el-option label="自定义" value="custom" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 自定义时间范围 -->
            <el-row v-if="filters.timeRange === 'custom'" :gutter="16">
              <el-col :span="24">
                <el-form-item label="时间范围">
                  <el-date-picker
                    v-model="filters.customTimeRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <!-- 文件内容搜索 -->
              <el-col :span="12">
                <el-form-item label="文件内容">
                  <el-input
                    v-model="filters.content"
                    placeholder="搜索文件内容（仅文本文件）"
                    clearable
                  />
                </el-form-item>
              </el-col>

              <!-- 文件所有者 -->
              <el-col :span="12">
                <el-form-item label="所有者">
                  <el-input
                    v-model="filters.owner"
                    placeholder="文件所有者"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 搜索选项 -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="搜索选项">
                  <el-checkbox-group v-model="filters.options">
                    <el-checkbox label="caseSensitive">区分大小写</el-checkbox>
                    <el-checkbox label="includeHidden">包含隐藏文件</el-checkbox>
                    <el-checkbox label="searchSubfolders">搜索子文件夹</el-checkbox>
                    <el-checkbox label="useRegex">使用正则表达式</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 操作按钮 -->
            <el-row>
              <el-col :span="24">
                <div class="search-actions">
                  <el-button
                    type="primary"
                    :icon="Search"
                    @click="handleAdvancedSearch"
                    :loading="searching"
                  >
                    搜索
                  </el-button>
                  <el-button
                    :icon="RefreshLeft"
                    @click="resetFilters"
                  >
                    重置
                  </el-button>
                  <el-button
                    :icon="Download"
                    @click="exportResults"
                    :disabled="!hasResults"
                  >
                    导出结果
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </div>
    </el-collapse-transition>

    <!-- 快速筛选标签 -->
    <div class="quick-filters" v-if="activeFilters.length > 0">
      <div class="filter-tags">
        <span class="filter-label">当前筛选:</span>
        <el-tag
          v-for="filter in activeFilters"
          :key="filter.key"
          closable
          @close="removeFilter(filter.key)"
          class="filter-tag"
        >
          {{ filter.label }}: {{ filter.value }}
        </el-tag>
        <el-button
          text
          type="primary"
          size="small"
          @click="clearAllFilters"
          v-if="activeFilters.length > 1"
        >
          清除全部
        </el-button>
      </div>
    </div>

    <!-- 搜索结果统计 -->
    <div class="search-stats" v-if="showStats">
      <el-alert
        :title="searchStatsText"
        type="info"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Search,
  Filter,
  RefreshLeft,
  Download
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  totalFiles: {
    type: Number,
    default: 0
  },
  filteredFiles: {
    type: Number,
    default: 0
  }
})

// Emits
const emit = defineEmits([
  'update:modelValue',
  'search',
  'filter-change',
  'advanced-search'
])

// 响应式数据
const searchQuery = ref(props.modelValue)
const showAdvanced = ref(false)
const searching = ref(false)

const filters = ref({
  name: '',
  type: [],
  sizeOperator: 'gt',
  sizeValue: null,
  sizeUnit: 'MB',
  timeRange: '',
  customTimeRange: null,
  content: '',
  owner: '',
  options: ['searchSubfolders']
})

// 计算属性
const activeFilters = computed(() => {
  const active = []
  
  if (filters.value.name) {
    active.push({ key: 'name', label: '文件名', value: filters.value.name })
  }
  
  if (filters.value.type.length > 0) {
    active.push({ key: 'type', label: '类型', value: filters.value.type.join(', ') })
  }
  
  if (filters.value.sizeValue) {
    const operator = {
      gt: '大于',
      lt: '小于',
      eq: '等于',
      gte: '大于等于',
      lte: '小于等于'
    }[filters.value.sizeOperator]
    active.push({
      key: 'size',
      label: '大小',
      value: `${operator} ${filters.value.sizeValue} ${filters.value.sizeUnit}`
    })
  }
  
  if (filters.value.timeRange) {
    const timeLabels = {
      today: '今天',
      yesterday: '昨天',
      thisWeek: '本周',
      lastWeek: '上周',
      thisMonth: '本月',
      lastMonth: '上月',
      custom: '自定义时间'
    }
    active.push({
      key: 'timeRange',
      label: '修改时间',
      value: timeLabels[filters.value.timeRange]
    })
  }
  
  if (filters.value.content) {
    active.push({ key: 'content', label: '内容', value: filters.value.content })
  }
  
  if (filters.value.owner) {
    active.push({ key: 'owner', label: '所有者', value: filters.value.owner })
  }
  
  return active
})

const hasResults = computed(() => {
  return props.filteredFiles > 0
})

const showStats = computed(() => {
  return searchQuery.value || activeFilters.value.length > 0
})

const searchStatsText = computed(() => {
  if (props.filteredFiles === props.totalFiles) {
    return `共找到 ${props.totalFiles} 个文件`
  }
  return `在 ${props.totalFiles} 个文件中找到 ${props.filteredFiles} 个匹配项`
})

// 监听搜索查询变化
watch(searchQuery, (newValue) => {
  emit('update:modelValue', newValue)
})

// 方法
const handleSearch = () => {
  emit('search', searchQuery.value)
}

const toggleAdvancedSearch = () => {
  showAdvanced.value = !showAdvanced.value
}

const handleAdvancedSearch = async () => {
  searching.value = true
  try {
    const searchParams = {
      query: searchQuery.value,
      filters: { ...filters.value }
    }
    
    emit('advanced-search', searchParams)
    
    // 模拟搜索延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('搜索完成')
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    name: '',
    type: [],
    sizeOperator: 'gt',
    sizeValue: null,
    sizeUnit: 'MB',
    timeRange: '',
    customTimeRange: null,
    content: '',
    owner: '',
    options: ['searchSubfolders']
  }
  
  emit('filter-change', filters.value)
}

const removeFilter = (key) => {
  switch (key) {
    case 'name':
      filters.value.name = ''
      break
    case 'type':
      filters.value.type = []
      break
    case 'size':
      filters.value.sizeValue = null
      break
    case 'timeRange':
      filters.value.timeRange = ''
      filters.value.customTimeRange = null
      break
    case 'content':
      filters.value.content = ''
      break
    case 'owner':
      filters.value.owner = ''
      break
  }
  
  emit('filter-change', filters.value)
}

const clearAllFilters = () => {
  resetFilters()
  searchQuery.value = ''
  emit('update:modelValue', '')
}

const handleTimeRangeChange = (value) => {
  if (value !== 'custom') {
    filters.value.customTimeRange = null
  }
}

const exportResults = () => {
  ElMessage.info('导出功能开发中...')
}

// 监听筛选条件变化
watch(filters, (newFilters) => {
  emit('filter-change', newFilters)
}, { deep: true })
</script>

<style lang="scss" scoped>
.file-search-filter {
  .basic-search {
    margin-bottom: 12px;

    .search-input {
      width: 100%;
      max-width: 500px;
    }
  }

  .advanced-search {
    margin-bottom: 16px;

    .search-panel {
      :deep(.el-card__body) {
        padding: 16px;
      }

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 500;
      }

      .size-filter {
        display: flex;
        align-items: center;
      }

      .search-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
        padding-top: 16px;
        border-top: 1px solid #e4e7ed;
      }
    }
  }

  .quick-filters {
    margin-bottom: 12px;

    .filter-tags {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .filter-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }

      .filter-tag {
        margin: 0;
      }
    }
  }

  .search-stats {
    margin-bottom: 12px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-search-filter {
    .advanced-search {
      .search-panel {
        :deep(.el-card__body) {
          padding: 12px;
        }

        .size-filter {
          flex-direction: column;
          gap: 8px;
          align-items: stretch;
        }

        .search-actions {
          flex-direction: column;
        }
      }
    }

    .quick-filters {
      .filter-tags {
        flex-direction: column;
        align-items: stretch;
        gap: 4px;
      }
    }
  }
}
</style>