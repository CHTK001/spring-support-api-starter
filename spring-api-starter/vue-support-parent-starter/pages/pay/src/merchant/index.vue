<template>
  <div class="merchant-page">
    <div class="merchant-header">
      <div class="left">
<iconifyIconOnline icon="ri:store-2-fill" width="18" />
        <span class="title">商户管理</span>
      </div>
      <div class="right">
        <el-form :inline="true" :model="form">
          <el-form-item label="商户名称">
            <el-input v-model="form.payMerchantName" placeholder="请输入商户名称" clearable @keyup.enter="handleRefresh" />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleRefresh">
              <iconifyIconOnline icon="ep:refresh" />
            </el-button>
            <el-button type="primary" @click="handleOpen('add', {})">
              <iconifyIconOnline icon="ep:plus" />
              新增商户
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <ScTable ref="tableRef" :url="fetchPageMerchant" :params="form" :border="false" :stripe="true" class="merchant-table" @data-loaded="handleDataLoaded">
      <el-table-column label="商户名称" prop="payMerchantName" min-width="160">
        <template #default="{ row }">
          <el-tag effect="plain">{{ row.payMerchantName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商户号" prop="payMerchantCode" min-width="160" show-overflow-tooltip />

      <el-table-column label="启用" prop="payMerchantStatus" width="120" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.payMerchantStatus" :active-value="1" :inactive-value="0" @change="() => onToggle(row)" />
        </template>
      </el-table-column>

      <el-table-column label="钱包" prop="payMerchantOpenWallet" width="120" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.payMerchantOpenWallet" :active-value="1" :inactive-value="0" @change="() => onToggle(row)" />
        </template>
      </el-table-column>

      <el-table-column label="创建时间" prop="createTime" min-width="180" show-overflow-tooltip />

      <el-table-column label="操作" fixed="right" min-width="160">
        <template #default="{ row }">
          <el-button text @click="handleOpen('edit', row)"><iconifyIconOnline icon="ep:edit" />编辑</el-button>
          <el-popconfirm title="确认删除该商户？" @confirm="onDelete(row)">
            <template #reference>
              <el-button text type="danger"><iconifyIconOnline icon="ep:delete" />删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </ScTable>

    <SaveDialog ref="saveDialogRef" @success="handleRefresh" />
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent, nextTick, reactive, ref, defineExpose, watch } from 'vue'
import { fetchPageMerchant, fetchDeleteMerchant, fetchUpdateMerchant } from '../api/merchant'

const SaveDialog = defineAsyncComponent(() => import('./save.vue'))

const tableRef = ref()
const saveDialogRef = ref()
const total = ref(0)

const form = reactive<any>({})

const handleDataLoaded = (data: any, _total: number) => {
  total.value = _total
}

const handleOpen = (type: 'add' | 'edit', row: any) => {
  nextTick(() => {
    saveDialogRef.value?.handleOpen(type, row)
  })
}

const handleRefresh = () => {
  tableRef.value?.reload(form)
}

const onToggle = (row: any) => {
  fetchUpdateMerchant({
    payMerchantId: row.payMerchantId,
    payMerchantStatus: row.payMerchantStatus,
    payMerchantOpenWallet: row.payMerchantOpenWallet,
  }).then(() => handleRefresh())
}

const onDelete = (row: any) => {
  fetchDeleteMerchant(row).then(() => handleRefresh())
}

// defineExpose 和 watch 必须放在最后（遵循项目规则）

defineExpose({ handleRefresh })

watch(form, () => {}, { deep: true })
</script>

<style scoped>
.merchant-page { padding: 8px; }
.merchant-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.title { font-weight: 600; margin-left: 6px; }
.merchant-table { --el-table-row-hover-bg-color: var(--el-fill-color-lighter); }
</style>