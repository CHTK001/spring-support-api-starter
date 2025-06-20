import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 主题模式
  const isDark = ref(false)
  
  // 加载状态
  const loading = ref(false)
  
  // 设备类型
  const device = ref<'desktop' | 'mobile'>('desktop')
  
  // 初始化应用
  const initApp = () => {
    // 检测设备类型
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
    device.value = isMobile ? 'mobile' : 'desktop'
    
    // 移动端默认折叠侧边栏
    if (device.value === 'mobile') {
      sidebarCollapsed.value = true
    }
    
    // 从localStorage恢复设置
    const savedCollapsed = localStorage.getItem('sidebarCollapsed')
    if (savedCollapsed !== null) {
      sidebarCollapsed.value = JSON.parse(savedCollapsed)
    }
    
    const savedTheme = localStorage.getItem('isDark')
    if (savedTheme !== null) {
      isDark.value = JSON.parse(savedTheme)
      toggleTheme(isDark.value)
    }
  }
  
  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('sidebarCollapsed', JSON.stringify(sidebarCollapsed.value))
  }
  
  // 切换主题
  const toggleTheme = (dark?: boolean) => {
    if (dark !== undefined) {
      isDark.value = dark
    } else {
      isDark.value = !isDark.value
    }
    
    const html = document.documentElement
    if (isDark.value) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
    
    localStorage.setItem('isDark', JSON.stringify(isDark.value))
  }
  
  // 设置加载状态
  const setLoading = (state: boolean) => {
    loading.value = state
  }
  
  return {
    sidebarCollapsed,
    isDark,
    loading,
    device,
    initApp,
    toggleSidebar,
    toggleTheme,
    setLoading
  }
})
