<template>
  <div>
    <h2 class="text-[#4e4e4e] text-[30px]">空调</h2>
    <el-row>
      <el-col :span="9" v-for="item in list" class="mr-[10px]">

        <el-card class="box-card">
          <div class="flex justify-between mt-[10px]">
            <p>主板编号</p>
            <el-tag type="success">{{ item.controlId }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>设备编号</p>
            <!-- <p>{{ item.state }}</p> -->
            <el-tag type="warning">{{ item.deviceId }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>设备类型</p>
            <!-- <p>{{ item.ip }}</p> -->
            <el-tag type="success">{{ item.deviceTypeId == 1 ? "空调" : "其他" }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>状态A</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.stateA || "暂无信息" }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>状态B</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.stateB || "暂无信息" }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>状态C</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.stateC || "暂无信息" }}</el-tag>
          </div>
          <div class="flex justify-between mt-[10px]">
            <p>状态D</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.stateD || "暂无信息" }}</el-tag>
          </div>
          <div style="margin-bottom: 10px;">

          </div>
          <el-button type="primary" @click="onSubmit(item, '01')">查询</el-button>
          <el-button type="primary" @click="onSubmit(item, '02')">开机（自动）</el-button>
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
import {ref, onMounted} from 'vue';
import {airLinkSet, DeviceInfoList} from '@/api/index.ts'
import type {Item} from '@/types/index.ts'

const list = ref<Item[]>([])
const onSubmit = async (item: Item, type: string) => {
  await airLinkSet({
    ...item,
    controlId: item.controlId,
    deviceId: item.deviceId,
    deviceTypeId: item.deviceTypeId,
    operation: type
  })
}
onMounted(async () => {
  // const { msg } = await hardWareGet()
  // list.value = msg
  const {data} = await DeviceInfoList()
  console.log(data);
  list.value = data

})
</script>
<style scoped></style>
