/**
 * 类型检查和转换工具函数
 */

/**
 * Base64 编码
 * @param str 要编码的字符串
 * @returns Base64 编码后的字符串
 */
export function base64Encode(str: string): string {
  try {
    return btoa(unescape(encodeURIComponent(str)))
  } catch (error) {
    console.error('Base64 编码失败:', error)
    return str
  }
}

/**
 * Base64 解码
 * @param str Base64 编码的字符串
 * @returns 解码后的字符串
 */
export function base64Decode(str: string): string {
  try {
    return decodeURIComponent(escape(atob(str)))
  } catch (error) {
    console.error('Base64 解码失败:', error)
    return str
  }
}

/**
 * 检查是否为字符串
 * @param value 要检查的值
 * @returns 是否为字符串
 */
export function isString(value: any): value is string {
  return typeof value === 'string'
}

/**
 * 检查是否为数字
 * @param value 要检查的值
 * @returns 是否为数字
 */
export function isNumber(value: any): value is number {
  return typeof value === 'number' && !isNaN(value)
}

/**
 * 检查是否为布尔值
 * @param value 要检查的值
 * @returns 是否为布尔值
 */
export function isBoolean(value: any): value is boolean {
  return typeof value === 'boolean'
}

/**
 * 检查是否为对象
 * @param value 要检查的值
 * @returns 是否为对象
 */
export function isObject(value: any): value is object {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

/**
 * 检查是否为数组
 * @param value 要检查的值
 * @returns 是否为数组
 */
export function isArray(value: any): value is any[] {
  return Array.isArray(value)
}

/**
 * 检查是否为函数
 * @param value 要检查的值
 * @returns 是否为函数
 */
export function isFunction(value: any): value is Function {
  return typeof value === 'function'
}

/**
 * 检查是否为 null 或 undefined
 * @param value 要检查的值
 * @returns 是否为 null 或 undefined
 */
export function isNullOrUndefined(value: any): value is null | undefined {
  return value === null || value === undefined
}

/**
 * 检查是否为空值（null、undefined、空字符串、空数组、空对象）
 * @param value 要检查的值
 * @returns 是否为空值
 */
export function isEmpty(value: any): boolean {
  if (isNullOrUndefined(value)) return true
  if (isString(value)) return value.trim() === ''
  if (isArray(value)) return value.length === 0
  if (isObject(value)) return Object.keys(value).length === 0
  return false
}

/**
 * 安全的 JSON 解析
 * @param str JSON 字符串
 * @param defaultValue 解析失败时的默认值
 * @returns 解析结果或默认值
 */
export function safeJsonParse<T = any>(str: string, defaultValue: T = null as T): T {
  try {
    return JSON.parse(str)
  } catch (error) {
    console.warn('JSON 解析失败:', error)
    return defaultValue
  }
}

/**
 * 安全的 JSON 字符串化
 * @param obj 要字符串化的对象
 * @param defaultValue 字符串化失败时的默认值
 * @returns JSON 字符串或默认值
 */
export function safeJsonStringify(obj: any, defaultValue: string = '{}'): string {
  try {
    return JSON.stringify(obj)
  } catch (error) {
    console.warn('JSON 字符串化失败:', error)
    return defaultValue
  }
}

/**
 * 深度克隆对象
 * @param obj 要克隆的对象
 * @returns 克隆后的对象
 */
export function deepClone<T>(obj: T): T {
  if (isNullOrUndefined(obj)) return obj
  if (typeof obj !== 'object') return obj
  
  try {
    return JSON.parse(JSON.stringify(obj))
  } catch (error) {
    console.warn('深度克隆失败:', error)
    return obj
  }
}

/**
 * 类型转换为字符串
 * @param value 要转换的值
 * @param defaultValue 转换失败时的默认值
 * @returns 字符串
 */
export function toString(value: any, defaultValue: string = ''): string {
  if (isNullOrUndefined(value)) return defaultValue
  if (isString(value)) return value
  try {
    return String(value)
  } catch (error) {
    return defaultValue
  }
}

/**
 * 类型转换为数字
 * @param value 要转换的值
 * @param defaultValue 转换失败时的默认值
 * @returns 数字
 */
export function toNumber(value: any, defaultValue: number = 0): number {
  if (isNumber(value)) return value
  if (isString(value)) {
    const num = parseFloat(value)
    return isNaN(num) ? defaultValue : num
  }
  return defaultValue
}

/**
 * 类型转换为整数
 * @param value 要转换的值
 * @param defaultValue 转换失败时的默认值
 * @returns 整数
 */
export function toInteger(value: any, defaultValue: number = 0): number {
  const num = toNumber(value, defaultValue)
  return Math.floor(num)
}

/**
 * 类型转换为布尔值
 * @param value 要转换的值
 * @returns 布尔值
 */
export function toBoolean(value: any): boolean {
  if (isBoolean(value)) return value
  if (isString(value)) {
    const lower = value.toLowerCase().trim()
    return lower === 'true' || lower === '1' || lower === 'yes'
  }
  if (isNumber(value)) return value !== 0
  return Boolean(value)
}
