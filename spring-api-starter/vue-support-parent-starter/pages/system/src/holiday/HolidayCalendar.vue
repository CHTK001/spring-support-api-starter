
<template>
  <div class="holiday-calendar">
    <!-- Element Plus 日历组件 -->
    <el-calendar v-model="selectedDate" @input="handleDateChange" :first-day-of-week="1">
    </el-calendar>

  </div>
</template>
