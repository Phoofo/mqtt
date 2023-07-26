<template>
  <div class="w-[80%] mx-auto">
    <mother-board></mother-board>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MotherBoard from './components/motherboard.vue'
import { DeviceInfoList } from '@/api/index.ts'
const list = ref([])

onMounted(async () => {
  const { data } = await DeviceInfoList()
  list.value = data
})

</script>
<style scoped></style>
