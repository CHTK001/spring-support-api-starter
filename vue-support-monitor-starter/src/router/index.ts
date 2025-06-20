import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/server/list'
  },
  {
    path: '/server',
    component: () => import('@/layout/index.vue'),
    redirect: '/server/list',
    meta: {
      title: '服务器管理',
      icon: 'Server'
    },
    children: [
      {
        path: 'list',
        name: 'ServerList',
        component: () => import('@/views/server/ServerList.vue'),
        meta: {
          title: '服务器列表',
          icon: 'List'
        }
      },
      {
        path: 'connection-status',
        name: 'ServerConnectionStatus',
        component: () => import('@/views/server/ServerConnectionStatus.vue'),
        meta: {
          title: '连接状态监控',
          icon: 'Connection'
        }
      },
      {
        path: 'log-management',
        name: 'ServerLogManagement',
        component: () => import('@/views/server/ServerLogManagement.vue'),
        meta: {
          title: '日志管理',
          icon: 'Document'
        }
      },
      {
        path: 'file-upload',
        name: 'ServerFileUpload',
        component: () => import('@/views/server/ServerFileUpload.vue'),
        meta: {
          title: '文件上传',
          icon: 'Upload'
        }
      },
      {
        path: 'script-management',
        name: 'ServerScriptManagement',
        component: () => import('@/views/server/ServerScriptManagement.vue'),
        meta: {
          title: '脚本管理',
          icon: 'DocumentCopy'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 服务器监控系统`
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
