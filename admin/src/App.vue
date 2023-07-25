<template>
  <div>
    <h2 class="text-[#4e4e4e] text-[30px]">空调</h2>
    <el-row>
      <el-col :span="7" v-for="item in list" class="item">
        <el-card class="box-card">
          <div class="flex justify-between mt-[10px]">
            <p>地址</p>
            <!-- <p>{{ item.address }}</p> -->
            <el-tag type="success">{{ item.address }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>状态</p>
            <!-- <p>{{ item.state }}</p> -->
            <el-tag type="warning">{{ item.state }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>ip</p>
            <!-- <p>{{ item.ip }}</p> -->
            <el-tag type="success">{{ item.ip }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>端口</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.port }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>设备</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.sate1 }}</el-tag>
          </div>

          <div style="margin-bottom: 10px;">

          </div>

          <el-button type="primary" @click="onSubmit(item, '03')">关机</el-button>
          <el-button type="primary" @click="onSubmit(item, '04')">制冷</el-button>
          <el-button type="primary" @click="onSubmit(item, '05')">制热</el-button>
          <el-button type="primary" @click="onSubmit(item, '06')">除湿</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { hardWareGet, airLinkSet } from '@/api/index.ts'
import type { Item } from '@/types/index.ts'
const list = ref<Item[]>([])
const onSubmit = async (item: Item, type: string) => {
  await airLinkSet({
    ...item,
    set: type,
    number: item.number1,
    sate: item.sate1
  })
}
onMounted(async () => {
  const { msg } = await hardWareGet()
  list.value = msg
})
</script>
<style scoped></style>
