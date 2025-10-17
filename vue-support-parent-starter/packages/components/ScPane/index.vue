<template>
  <el-container class="sc-pane" direction="vertical" ref="rootRef">
    <!-- Header -->
    <template v-if="hasHeader">
      <el-header :height="headerPx + 'px'" class="sc-pane__header">
        <slot name="header" :resizable="headerResizable" :resizing="resizing === 'header'" :size="headerSizeInner" />
      </el-header>
      <div
        v-if="headerResizable && hasHeader"
        class="sc-pane__divider sc-pane__divider--horizontal"
        @mousedown="onDragStart('header')"
      />
    </template>

    <!-- Middle (Left / Center / Right) -->
    <el-container class="sc-pane__middle">
      <template v-if="hasLeft">
        <el-aside :width="leftPx + 'px'" class="sc-pane__left">
          <slot name="left" :resizable="leftResizable" :resizing="resizing === 'left'" :size="leftSizeInner" />
        </el-aside>
        <div
          v-if="leftResizable && hasLeft"
          class="sc-pane__divider sc-pane__divider--vertical"
          @mousedown="onDragStart('left')"
        />
      </template>

      <el-main class="sc-pane__center">
        <slot name="center" :resizable="false" :resizing="false" />
      </el-main>

      <template v-if="hasRight">
        <div
          v-if="rightResizable && hasRight"
          class="sc-pane__divider sc-pane__divider--vertical"
          @mousedown="onDragStart('right')"
        />
        <el-aside :width="rightPx + 'px'" class="sc-pane__right">
          <slot name="right" :resizable="rightResizable" :resizing="resizing === 'right'" :size="rightSizeInner" />
        </el-aside>
      </template>
    </el-container>

    <!-- Footer -->
    <template v-if="hasFooter">
      <div
        v-if="footerResizable && hasFooter"
        class="sc-pane__divider sc-pane__divider--horizontal"
        @mousedown="onDragStart('footer')"
      />
      <el-footer :height="footerPx + 'px'" class="sc-pane__footer">
        <slot name="footer" :resizable="footerResizable" :resizing="resizing === 'footer'" :size="footerSizeInner" />
      </el-footer>
    </template>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, useSlots, watch } from 'vue'

/**
 * ScPane 基础布局容器
 * - 上中下（header/center/footer）
 * - 左中右（left/center/right）
 * - 支持尺寸占比/像素定义
 * - 支持分隔条拖拽调整大小
 */

const props = withDefaults(defineProps<{
  /** 头部默认尺寸：支持像素(如 80)、百分比字符串("20%")，或小数占比(如 0.2) */
  headerSize?: number | string
  /** 底部默认尺寸 */
  footerSize?: number | string
  /** 左侧默认尺寸 */
  leftSize?: number | string
  /** 右侧默认尺寸 */
  rightSize?: number | string

  /** 是否允许拖拽：对应四个分隔条 */
  headerResizable?: boolean
  footerResizable?: boolean
  leftResizable?: boolean
  rightResizable?: boolean

  /** 各方向的最小像素尺寸（拖拽时生效） */
  minHeader?: number
  minFooter?: number
  minLeft?: number
  minRight?: number
}>(), {
  headerSize: 0,
  footerSize: 0,
  leftSize: 0,
  rightSize: 0,

  headerResizable: false,
  footerResizable: false,
  leftResizable: false,
  rightResizable: false,

  minHeader: 40,
  minFooter: 40,
  minLeft: 120,
  minRight: 120
})

const emit = defineEmits<{
  (e: 'resize', section: 'header' | 'footer' | 'left' | 'right', sizePx: number): void
  (e: 'update:headerSize', val: number | string): void
  (e: 'update:footerSize', val: number | string): void
  (e: 'update:leftSize', val: number | string): void
  (e: 'update:rightSize', val: number | string): void
}>()

const slots = useSlots()
const rootRef = ref<HTMLElement | null>(null)

// 内部状态（像素）
const state = reactive({
  header: 0,
  footer: 0,
  left: 0,
  right: 0
})

const resizing = ref<null | 'header' | 'footer' | 'left' | 'right'>(null)
let startPos = 0
let startVal = 0

// slot 存在与否
const hasHeader = computed(() => !!slots.header)
const hasFooter = computed(() => !!slots.footer)
const hasLeft = computed(() => !!slots.left)
const hasRight = computed(() => !!slots.right)

// 将用户尺寸（px/%/ratio）转像素
function toPx(val: number | string | undefined, total: number): number {
  if (val == null) return 0
  if (typeof val === 'number') {
    // 小于1认为是占比
    if (val > 0 && val < 1) return Math.round(total * val)
    return Math.round(val)
  }
  const s = String(val).trim()
  if (s.endsWith('%')) {
    const p = parseFloat(s) / 100
    return Math.round(total * p)
  }
  if (s.endsWith('px')) {
    return Math.round(parseFloat(s))
  }
  const n = parseFloat(s)
  if (!Number.isNaN(n)) return Math.round(n)
  return 0
}

// 反向：像素转占比或px字符串，尽量保持与输入相同的语义
function fromPx(px: number, raw: number | string | undefined, total: number): number | string {
  if (raw == null) return px
  if (typeof raw === 'number') {
    if (raw > 0 && raw < 1) return +(px / total).toFixed(4)
    return Math.round(px)
  }
  const s = String(raw).trim()
  if (s.endsWith('%')) return +((px / total) * 100).toFixed(2) + '%'
  if (s.endsWith('px')) return Math.round(px) + 'px'
  const n = parseFloat(s)
  if (!Number.isNaN(n)) return Math.round(px)
  return Math.round(px)
}

// 根容器尺寸
const rootHeight = computed(() => rootRef.value?.clientHeight || 0)
const rootWidth = computed(() => rootRef.value?.clientWidth || 0)

// 初始化与响应 props
function initSizes() {
  state.header = hasHeader.value ? toPx(props.headerSize, rootHeight.value) : 0
  state.footer = hasFooter.value ? toPx(props.footerSize, rootHeight.value) : 0
  state.left = hasLeft.value ? toPx(props.leftSize, rootWidth.value) : 0
  state.right = hasRight.value ? toPx(props.rightSize, rootWidth.value) : 0
}

watch([rootHeight, rootWidth], () => initSizes())
watch(() => [props.headerSize, props.footerSize, props.leftSize, props.rightSize], () => initSizes(), { deep: true })

onMounted(() => {
  initSizes()
  window.addEventListener('mousemove', onDragMove)
  window.addEventListener('mouseup', onDragEnd)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onDragMove)
  window.removeEventListener('mouseup', onDragEnd)
})

// 拖拽
function onDragStart(section: 'header' | 'footer' | 'left' | 'right') {
  resizing.value = section
  if (section === 'header') {
    startPos = window.event instanceof MouseEvent ? (window.event as MouseEvent).clientY : 0
    startVal = state.header
  } else if (section === 'footer') {
    startPos = window.event instanceof MouseEvent ? (window.event as MouseEvent).clientY : 0
    startVal = state.footer
  } else if (section === 'left') {
    startPos = window.event instanceof MouseEvent ? (window.event as MouseEvent).clientX : 0
    startVal = state.left
  } else if (section === 'right') {
    startPos = window.event instanceof MouseEvent ? (window.event as MouseEvent).clientX : 0
    startVal = state.right
  }
  document.body.classList.add('sc-pane--resizing')
}

function onDragMove(e: MouseEvent) {
  if (!resizing.value) return
  if (resizing.value === 'header') {
    const dy = e.clientY - startPos
    state.header = Math.max(props.minHeader, startVal + dy)
  } else if (resizing.value === 'footer') {
    const dy = startPos - e.clientY
    state.footer = Math.max(props.minFooter, startVal + dy)
  } else if (resizing.value === 'left') {
    const dx = e.clientX - startPos
    state.left = Math.max(props.minLeft, startVal + dx)
  } else if (resizing.value === 'right') {
    const dx = startPos - e.clientX
    state.right = Math.max(props.minRight, startVal + dx)
  }
}

function onDragEnd() {
  if (!resizing.value) return
  const section = resizing.value
  resizing.value = null
  document.body.classList.remove('sc-pane--resizing')

  // 同步 v-model 与触发事件
  if (section === 'header') {
    emit('update:headerSize', fromPx(state.header, props.headerSize, rootHeight.value))
    emit('resize', 'header', state.header)
  } else if (section === 'footer') {
    emit('update:footerSize', fromPx(state.footer, props.footerSize, rootHeight.value))
    emit('resize', 'footer', state.footer)
  } else if (section === 'left') {
    emit('update:leftSize', fromPx(state.left, props.leftSize, rootWidth.value))
    emit('resize', 'left', state.left)
  } else if (section === 'right') {
    emit('update:rightSize', fromPx(state.right, props.rightSize, rootWidth.value))
    emit('resize', 'right', state.right)
  }
}

// 对外暴露只读像素值
const headerPx = computed(() => state.header)
const footerPx = computed(() => state.footer)
const leftPx = computed(() => state.left)
const rightPx = computed(() => state.right)

// 提供给插槽的尺寸（保持与传入的单位语义）
const headerSizeInner = computed(() => fromPx(state.header, props.headerSize, rootHeight.value))
const footerSizeInner = computed(() => fromPx(state.footer, props.footerSize, rootHeight.value))
const leftSizeInner = computed(() => fromPx(state.left, props.leftSize, rootWidth.value))
const rightSizeInner = computed(() => fromPx(state.right, props.rightSize, rootWidth.value))
</script>

<style lang="scss" scoped>
.sc-pane {
  width: 100%;
  height: 100%;
  min-height: 0;
}

.sc-pane__header,
.sc-pane__footer {
  padding: 0;
}

.sc-pane__middle {
  min-height: 0;
}

.sc-pane__left,
.sc-pane__right {
  padding: 0;
}

.sc-pane__center {
  padding: 0;
  min-height: 0;
}

.sc-pane__divider {
  background-color: var(--el-border-color);
  opacity: 0.6;
  transition: background-color .2s, opacity .2s;
  &--horizontal {
    height: 6px;
    cursor: ns-resize;
  }
  &--vertical {
    width: 6px;
    cursor: ew-resize;
  }
  &:hover {
    opacity: 1;
    background-color: var(--el-color-primary);
  }
}

:global(body.sc-pane--resizing) {
  cursor: col-resize;
}
</style>