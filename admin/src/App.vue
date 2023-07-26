<template>
  <div class="w-[80%] mx-auto">
    <!-- <el-tabs v-model="activeName" class="demo-tabs">
      <el-tab-pane label="主板信息" name="first"></el-tab-pane>
      <el-tab-pane label="设备信息" name="second"></el-tab-pane>
    </el-tabs> -->
    <!-- <template v-if="activeName === 'second'">
      <device :list="list"></device>
    </template>
    <template v-else> -->
      <mother-board></mother-board>
    <!-- </template> -->
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
// import Device from './components/device.vue'
import MotherBoard from './components/motherboard.vue'
import { DeviceInfoList } from '@/api/index.ts'
// let activeName = ref<string>('first')
const list = ref([])

onMounted(async () => {
  const { data } = await DeviceInfoList()
  list.value = data
})

</script>
<style scoped></style>
