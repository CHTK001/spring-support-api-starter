<template>
  <div class="directory-tree">
    <!-- 头部 -->
    <div class="tree-header">
      <div class="header-title">
        <el-icon><FolderOpened /></el-icon>
        <span>目录结构</span>
      </div>
      <div class="header-actions">
        <el-button
          text
          :icon="Refresh"
          @click="handleRefresh"
          title="刷新"
          size="small"
        />
        <el-button
          text
          :icon="Expand"
          @click="expandAll"
          title="展开所有"
          size="small"
        />
        <el-button
          text
          :icon="Fold"
          @click="collapseAll"
          title="折叠所有"
          size="small"
        />
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="tree-search">
      <el-input
        v-model="searchQuery"
        placeholder="搜索目录..."
        :prefix-icon="Search"
        clearable
        size="small"
        @input="handleSearch"
      />
    </div>

    <!-- 快速访问 -->
    <div class="quick-access">
      <div class="access-title">快速访问</div>
      <div class="access-items">
        <div
          v-for="item in quickAccessItems"
          :key="item.path"
          class="access-item"
          :class="{ active: currentPath === item.path }"
          @click="navigateToPath(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.name }}</span>
        </div>
      </div>
    </div>

    <!-- 目录树 -->
    <div class="tree-container">
      <el-tree
        ref="treeRef"
        :data="filteredTreeData"
        :props="treeProps"
        :expand-on-click-node="false"
        :default-expanded-keys="defaultExpandedKeys"
        :current-node-key="currentNodeKey"
        :highlight-current="true"
        :lazy="true"
        :load="loadNode"
        node-key="path"
        @node-click="handleNodeClick"
        @node-contextmenu="handleContextMenu"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <el-icon class="node-icon">
                <component :is="getNodeIcon(data, node.expanded)" />
              </el-icon>
              <span class="node-label" :title="data.name">{{ data.name }}</span>
            </div>
            <div class="node-actions" v-if="!node.isLeaf">
              <el-button
                text
                :icon="FolderAdd"
                @click.stop="createFolder(data)"
                title="新建文件夹"
                size="small"
              />
              <el-button
                text
                :icon="Refresh"
                @click.stop="refreshNode(node, data)"
                title="刷新"
                size="small"
              />
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenuRef"
      trigger="contextmenu"
      :teleported="false"
      @command="handleContextCommand"
    >
      <div></div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            command="open"
            :icon="FolderOpened"
          >
            打开
          </el-dropdown-item>
          <el-dropdown-item
            command="newTab"
            :icon="Plus"
          >
            在新标签页中打开
          </el-dropdown-item>
          <el-dropdown-item divided />
          <el-dropdown-item
            command="createFolder"
            :icon="FolderAdd"
          >
            新建文件夹
          </el-dropdown-item>
          <el-dropdown-item
            command="refresh"
            :icon="Refresh"
          >
            刷新
          </el-dropdown-item>
          <el-dropdown-item divided />
          <el-dropdown-item
            command="copyPath"
            :icon="DocumentCopy"
          >
            复制路径
          </el-dropdown-item>
          <el-dropdown-item
            command="properties"
            :icon="InfoFilled"
          >
            属性
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import {
  FolderOpened,
  Folder,
  Refresh,
  Expand,
  Fold,
  Search,
  FolderAdd,
  Plus,
  DocumentCopy,
  InfoFilled,
  HomeFilled,
  Document,
  Picture,
  VideoPlay,
  Headphone
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  currentPath: {
    type: String,
    default: '/'
  },
  expandedPaths: {
    type: Set,
    default: () => new Set()
  }
})

// Emits
const emit = defineEmits([
  'navigate',
  'create-folder',
  'refresh'
])

// 响应式数据
const searchQuery = ref('')
const treeRef = ref()
const contextMenuRef = ref()
const contextMenuTarget = ref(null)
const treeData = ref([])
const loading = ref(false)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'isLeaf'
}

// 快速访问项目
const quickAccessItems = ref([
  { name: '根目录', path: '/', icon: HomeFilled },
  { name: '文档', path: '/documents', icon: Document },
  { name: '图片', path: '/images', icon: Picture },
  { name: '视频', path: '/videos', icon: VideoPlay },
  { name: '音频', path: '/audio', icon: Headphone }
])

// 计算属性
const currentNodeKey = computed(() => props.currentPath)

const defaultExpandedKeys = computed(() => {
  return Array.from(props.expandedPaths)
})

const filteredTreeData = computed(() => {
  if (!searchQuery.value) return treeData.value
  
  const filterTree = (nodes) => {
    return nodes.filter(node => {
      const matchesSearch = node.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      if (node.children) {
        const filteredChildren = filterTree(node.children)
        if (filteredChildren.length > 0) {
          return { ...node, children: filteredChildren }
        }
      }
      return matchesSearch
    })
  }
  
  return filterTree(treeData.value)
})

// 方法
const loadNode = async (node, resolve) => {
  if (node.level === 0) {
    // 根节点
    const rootNodes = await loadDirectories('/')
    resolve(rootNodes)
    return
  }
  
  // 子节点
  const children = await loadDirectories(node.data.path)
  resolve(children)
}

const loadDirectories = async (path) => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 模拟目录数据
    const mockDirectories = [
      {
        name: 'documents',
        path: path === '/' ? '/documents' : `${path}/documents`,
        type: 'directory',
        isLeaf: false,
        children: []
      },
      {
        name: 'images',
        path: path === '/' ? '/images' : `${path}/images`,
        type: 'directory',
        isLeaf: false,
        children: []
      },
      {
        name: 'videos',
        path: path === '/' ? '/videos' : `${path}/videos`,
        type: 'directory',
        isLeaf: false,
        children: []
      }
    ]
    
    return mockDirectories
  } catch (error) {
    ElMessage.error('加载目录失败')
    console.error('Load directories error:', error)
    return []
  }
}

const getNodeIcon = (data, expanded) => {
  if (data.type === 'directory') {
    return expanded ? FolderOpened : Folder
  }
  return Document
}

const handleNodeClick = (data, node) => {
  if (data.type === 'directory') {
    navigateToPath(data.path)
  }
}

const navigateToPath = (path) => {
  emit('navigate', path)
}

const handleRefresh = () => {
  emit('refresh')
  refreshTree()
}

const refreshTree = async () => {
  loading.value = true
  try {
    const rootNodes = await loadDirectories('/')
    treeData.value = rootNodes
    
    // 刷新树组件
    await nextTick()
    if (treeRef.value) {
      treeRef.value.setData(rootNodes)
    }
  } finally {
    loading.value = false
  }
}

const refreshNode = async (node, data) => {
  loading.value = true
  try {
    const children = await loadDirectories(data.path)
    node.childNodes = []
    node.loaded = false
    node.loading = false
    
    // 重新加载节点
    await nextTick()
    if (treeRef.value) {
      treeRef.value.store.loadNode(node, () => {})
    }
  } finally {
    loading.value = false
  }
}

const expandAll = () => {
  if (treeRef.value) {
    const allNodes = treeRef.value.store._getAllNodes()
    allNodes.forEach(node => {
      if (!node.isLeaf) {
        node.expand()
      }
    })
  }
}

const collapseAll = () => {
  if (treeRef.value) {
    const allNodes = treeRef.value.store._getAllNodes()
    allNodes.forEach(node => {
      if (!node.isLeaf && node.level > 0) {
        node.collapse()
      }
    })
  }
}

const handleSearch = (query) => {
  // 搜索逻辑已在计算属性中处理
}

const createFolder = (data) => {
  emit('create-folder', data.path)
}

const handleContextMenu = (event, data, node) => {
  event.preventDefault()
  contextMenuTarget.value = { data, node }
  
  // 显示右键菜单
  nextTick(() => {
    if (contextMenuRef.value) {
      contextMenuRef.value.handleOpen()
    }
  })
}

const handleContextCommand = (command) => {
  if (!contextMenuTarget.value) return
  
  const { data, node } = contextMenuTarget.value
  
  switch (command) {
    case 'open':
      navigateToPath(data.path)
      break
    case 'newTab':
      window.open(`#/file-manager?path=${encodeURIComponent(data.path)}`, '_blank')
      break
    case 'createFolder':
      createFolder(data)
      break
    case 'refresh':
      refreshNode(node, data)
      break
    case 'copyPath':
      navigator.clipboard.writeText(data.path)
      ElMessage.success('路径已复制到剪贴板')
      break
    case 'properties':
      ElMessage.info(`查看 ${data.name} 的属性`)
      break
  }
  
  contextMenuTarget.value = null
}

// 生命周期
onMounted(() => {
  refreshTree()
})
</script>

<style lang="scss" scoped>
.directory-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .tree-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background: #f8f9fa;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 4px;
    }
  }

  .tree-search {
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
  }

  .quick-access {
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;

    .access-title {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }

    .access-items {
      .access-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 8px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
        color: #606266;
        transition: all 0.2s;

        &:hover {
          background: #f0f9ff;
          color: #409eff;
        }

        &.active {
          background: #e1f3d8;
          color: #67c23a;
        }
      }
    }
  }

  .tree-container {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;

    :deep(.el-tree) {
      background: transparent;

      .el-tree-node {
        .el-tree-node__content {
          height: 32px;
          padding-left: 8px;
          padding-right: 8px;

          &:hover {
            background: #f0f9ff;

            .node-actions {
              opacity: 1;
            }
          }

          .el-tree-node__expand-icon {
            color: #909399;
            
            &.is-leaf {
              color: transparent;
            }
          }
        }

        &.is-current > .el-tree-node__content {
          background: #e1f3d8;
          color: #67c23a;
        }
      }
    }

    .tree-node {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding-right: 8px;

      .node-content {
        display: flex;
        align-items: center;
        gap: 6px;
        flex: 1;
        min-width: 0;

        .node-icon {
          color: #409eff;
          flex-shrink: 0;
        }

        .node-label {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .node-actions {
        display: flex;
        gap: 2px;
        opacity: 0;
        transition: opacity 0.2s;
        flex-shrink: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .directory-tree {
    .tree-header {
      padding: 8px 12px;
    }

    .tree-search {
      padding: 8px 12px;
    }

    .quick-access {
      padding: 8px 12px;
    }
  }
}
</style>