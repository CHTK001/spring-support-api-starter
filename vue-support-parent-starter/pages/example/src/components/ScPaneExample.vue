<template>
  <div class="p-4">
    <ScPane title="Pane 标题">这是 Pane 内容</ScPane>

    <el-divider />
    <CodeDisplay :code="code" language="html" title="使用示例" />
  </div>
</template>
<script setup>
import { computed } from "vue";
import ScPane from "@repo/components/ScPane/index.vue";
import CodeDisplay from "./CodeDisplay.vue";

const code = computed(() => `<ScPane title=\"Pane 标题\">这是 Pane 内容</ScPane>`);
</script>