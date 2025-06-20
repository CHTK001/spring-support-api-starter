<template>
  <div class="app-layout">
    <!-- 头部 -->
    <el-header class="app-header">
      <div class="header-left">
        <el-button
          type="text"
          @click="toggleSidebar"
          class="sidebar-toggle"
        >
          <el-icon><Expand v-if="sidebarCollapsed" /><Fold v-else /></el-icon>
        </el-button>
        <h1 class="app-title">服务器监控系统</h1>
      </div>
      
      <div class="header-right">
        <!-- WebSocket连接状态 -->
        <el-tooltip :content="wsConnected ? 'WebSocket已连接' : 'WebSocket未连接'">
          <el-badge :is-dot="!wsConnected" type="danger">
            <el-icon :color="wsConnected ? '#67c23a' : '#f56c6c'">
              <Connection />
            </el-icon>
          </el-badge>
        </el-tooltip>
        
        <!-- 主题切换 -->
        <el-button
          type="text"
          @click="toggleTheme"
          class="theme-toggle"
        >
          <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
        </el-button>
        
        <!-- 全屏 -->
        <el-button
          type="text"
          @click="toggleFullscreen"
          class="fullscreen-toggle"
        >
          <el-icon><FullScreen /></el-icon>
        </el-button>
      </div>
    </el-header>
    
    <el-container class="app-container">
      <!-- 侧边栏 -->
      <el-aside
        :width="sidebarCollapsed ? '64px' : '240px'"
        class="app-sidebar"
      >
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/server/list">
            <el-icon><List /></el-icon>
            <template #title>服务器列表</template>
          </el-menu-item>
          
          <el-menu-item index="/server/connection-status">
            <el-icon><Connection /></el-icon>
            <template #title>连接状态监控</template>
          </el-menu-item>
          
          <el-menu-item index="/server/log-management">
            <el-icon><Document /></el-icon>
            <template #title>日志管理</template>
          </el-menu-item>
          
          <el-menu-item index="/server/file-upload">
            <el-icon><Upload /></el-icon>
            <template #title>文件上传</template>
          </el-menu-item>
          
          <el-menu-item index="/server/script-management">
            <el-icon><DocumentCopy /></el-icon>
            <template #title>脚本管理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useWebSocketStore } from '@/stores/websocket'
import {
  Expand,
  Fold,
  Connection,
  Sunny,
  Moon,
  FullScreen,
  List,
  Document,
  Upload,
  DocumentCopy
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
const wsStore = useWebSocketStore()

// 计算属性
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const isDark = computed(() => appStore.isDark)
const wsConnected = computed(() => wsStore.connected)
const activeMenu = computed(() => route.path)

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 切换主题
const toggleTheme = () => {
  appStore.toggleTheme()
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 生命周期
onMounted(() => {
  // 连接WebSocket
  wsStore.connect()
})

onUnmounted(() => {
  // 断开WebSocket连接
  wsStore.disconnect()
})
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .sidebar-toggle {
      margin-right: 16px;
      font-size: 18px;
    }
    
    .app-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .theme-toggle,
    .fullscreen-toggle {
      font-size: 18px;
    }
  }
}

.app-container {
  flex: 1;
  height: calc(100vh - 60px);
}

.app-sidebar {
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
  transition: width 0.3s ease;
  
  .sidebar-menu {
    border: none;
    height: 100%;
    
    .el-menu-item {
      height: 56px;
      line-height: 56px;
      
      &.is-active {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background-color: var(--el-color-primary);
        }
      }
    }
  }
}

.app-main {
  background: var(--app-bg-color);
  padding: 0;
  overflow: auto;
}

// 响应式
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
    
    .header-left {
      .app-title {
        font-size: 18px;
      }
    }
    
    .header-right {
      gap: 12px;
    }
  }
}
</style>
