/**
 * 通用接口定义
 */

/**
 * 全局窗口对象接口
 */
export interface GlobalWindow extends Window {
  /** API 超时时间 */
  apiTimeout?: string | number
  /** 路由基础路径 */
  routerBase?: string
  /** 应用配置 */
  appConfig?: Record<string, any>
  /** 用户信息 */
  userInfo?: UserInfo
  /** 权限信息 */
  permissions?: string[]
  /** 主题配置 */
  themeConfig?: ThemeConfig
}

/**
 * 用户信息接口
 */
export interface UserInfo {
  /** 用户ID */
  id: string | number
  /** 用户名 */
  username: string
  /** 昵称 */
  nickname?: string
  /** 邮箱 */
  email?: string
  /** 头像 */
  avatar?: string
  /** 角色列表 */
  roles?: string[]
  /** 权限列表 */
  permissions?: string[]
  /** 部门信息 */
  department?: DepartmentInfo
  /** 创建时间 */
  createTime?: string
  /** 最后登录时间 */
  lastLoginTime?: string
}

/**
 * 部门信息接口
 */
export interface DepartmentInfo {
  /** 部门ID */
  id: string | number
  /** 部门名称 */
  name: string
  /** 部门编码 */
  code?: string
  /** 父部门ID */
  parentId?: string | number
  /** 部门层级 */
  level?: number
  /** 部门描述 */
  description?: string
}

/**
 * 主题配置接口
 */
export interface ThemeConfig {
  /** 主题模式 */
  mode: 'light' | 'dark' | 'auto'
  /** 主色调 */
  primaryColor: string
  /** 布局模式 */
  layout: 'vertical' | 'horizontal' | 'mix'
  /** 是否显示标签页 */
  showTabs: boolean
  /** 是否显示面包屑 */
  showBreadcrumb: boolean
  /** 是否显示页脚 */
  showFooter: boolean
  /** 侧边栏宽度 */
  sidebarWidth: number
  /** 是否折叠侧边栏 */
  sidebarCollapsed: boolean
}

/**
 * 分页参数接口
 */
export interface PaginationParams {
  /** 当前页码 */
  current: number
  /** 每页条数 */
  size: number
  /** 总条数 */
  total?: number
}

/**
 * 分页响应接口
 */
export interface PaginationResponse<T = any> {
  /** 数据列表 */
  records: T[]
  /** 当前页码 */
  current: number
  /** 每页条数 */
  size: number
  /** 总条数 */
  total: number
  /** 总页数 */
  pages: number
}

/**
 * 排序参数接口
 */
export interface SortParams {
  /** 排序字段 */
  field: string
  /** 排序方向 */
  order: 'asc' | 'desc'
}

/**
 * 查询参数接口
 */
export interface QueryParams extends PaginationParams {
  /** 排序参数 */
  sort?: SortParams[]
  /** 搜索关键词 */
  keyword?: string
  /** 其他查询条件 */
  [key: string]: any
}

/**
 * 操作按钮接口
 */
export interface ActionButton {
  /** 按钮标识 */
  key: string
  /** 按钮文本 */
  label: string
  /** 按钮类型 */
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  /** 按钮图标 */
  icon?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 是否显示 */
  visible?: boolean
  /** 权限标识 */
  permission?: string
  /** 点击事件 */
  onClick?: (record?: any) => void
}

/**
 * 表格列配置接口
 */
export interface TableColumn {
  /** 列标识 */
  key: string
  /** 列标题 */
  title: string
  /** 数据字段 */
  dataIndex?: string
  /** 列宽度 */
  width?: number | string
  /** 是否固定 */
  fixed?: 'left' | 'right'
  /** 是否可排序 */
  sortable?: boolean
  /** 是否可筛选 */
  filterable?: boolean
  /** 对齐方式 */
  align?: 'left' | 'center' | 'right'
  /** 自定义渲染 */
  render?: (value: any, record: any, index: number) => any
}

/**
 * 表单字段配置接口
 */
export interface FormField {
  /** 字段标识 */
  key: string
  /** 字段标签 */
  label: string
  /** 字段类型 */
  type: 'input' | 'textarea' | 'select' | 'radio' | 'checkbox' | 'date' | 'datetime' | 'number' | 'switch'
  /** 是否必填 */
  required?: boolean
  /** 字段规则 */
  rules?: any[]
  /** 字段选项（用于 select、radio、checkbox） */
  options?: Array<{ label: string; value: any }>
  /** 字段属性 */
  props?: Record<string, any>
  /** 是否禁用 */
  disabled?: boolean
  /** 是否显示 */
  visible?: boolean
  /** 默认值 */
  defaultValue?: any
  /** 占位符 */
  placeholder?: string
  /** 帮助文本 */
  help?: string
}

/**
 * 菜单项接口
 */
export interface MenuItem {
  /** 菜单ID */
  id: string | number
  /** 菜单标题 */
  title: string
  /** 菜单路径 */
  path?: string
  /** 菜单图标 */
  icon?: string
  /** 父菜单ID */
  parentId?: string | number
  /** 菜单排序 */
  sort?: number
  /** 是否隐藏 */
  hidden?: boolean
  /** 权限标识 */
  permission?: string
  /** 子菜单 */
  children?: MenuItem[]
  /** 菜单元数据 */
  meta?: Record<string, any>
}

/**
 * 文件信息接口
 */
export interface FileInfo {
  /** 文件ID */
  id: string
  /** 文件名 */
  name: string
  /** 文件大小 */
  size: number
  /** 文件类型 */
  type: string
  /** 文件URL */
  url: string
  /** 上传时间 */
  uploadTime: string
  /** 上传者 */
  uploader?: string
}

/**
 * 通知消息接口
 */
export interface NotificationMessage {
  /** 消息ID */
  id: string
  /** 消息标题 */
  title: string
  /** 消息内容 */
  content: string
  /** 消息类型 */
  type: 'info' | 'success' | 'warning' | 'error'
  /** 是否已读 */
  read: boolean
  /** 创建时间 */
  createTime: string
  /** 发送者 */
  sender?: string
}

/**
 * 系统配置接口
 */
export interface SystemConfig {
  /** 配置键 */
  key: string
  /** 配置值 */
  value: any
  /** 配置描述 */
  description?: string
  /** 配置分组 */
  group?: string
  /** 是否可编辑 */
  editable?: boolean
}

/**
 * 日志信息接口
 */
export interface LogInfo {
  /** 日志ID */
  id: string
  /** 操作模块 */
  module: string
  /** 操作类型 */
  action: string
  /** 操作描述 */
  description: string
  /** 操作者 */
  operator: string
  /** 操作时间 */
  operateTime: string
  /** 操作结果 */
  result: 'success' | 'failure'
  /** 错误信息 */
  errorMessage?: string
  /** 请求参数 */
  requestParams?: any
  /** 响应结果 */
  responseData?: any
  /** IP地址 */
  ipAddress?: string
  /** 用户代理 */
  userAgent?: string
}
