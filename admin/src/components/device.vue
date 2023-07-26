<template>
    <div>
        <el-table :data="props.list" border style="width: 100%">
            <el-table-column prop="controlId" label="主板编号" align="center" />
            <el-table-column prop="deviceId" label="设备编号" align="center" />
            <el-table-column label="设备类型" align="center">
                <template #default="{ row }">
                    {{ row.deviceTypeId == 1 ? "空调" : "其他" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateA" label="状态A" align="center">
                <template #default="{ row }">
                    {{ row.stateA || "暂无信息" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateB" label="状态B" align="center">
                <template #default="{ row }">
                    {{ row.stateB || "暂无信息" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateC" label="状态C" align="center">
                <template #default="{ row }">
                    {{ row.stateC || "暂无信息" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateD" label="状态D" align="center">
                <template #default="{ row }">
                    {{ row.stateD || "暂无信息" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateD" label="操作" align="center">
                <template #default="{ row }">
                    <!-- {{ row.stateD || "暂无信息" }} -->
                    <el-button @click="setTools(row)">操作</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
    <conditioning-model v-model="show" :item="active"></conditioning-model>
</template>

<script setup lang='ts'>
import type { Item } from '@/types/index.ts'
import ConditioningModel from './conditioningModel.vue';
import { ref, reactive } from 'vue';
const props = defineProps({
    list: Array
})
let show = ref<boolean>(false)
let active = reactive<Item>({
    sate1: '',
    number: '',
    address: '',
    port: '',
    type_id: 0,
    latitude: 0,
    ip: '',
    id1: 0,
    number1: '',
    id: 0,
    state: '',
    longitude: 0,
    controlId: '',
    deviceId: '',
    operation: '',
    deviceTypeId: 0,
})
const setTools = (item: Item) => {
    show.value = true
    active = item
}
</script>

<style scoped lang='less'></style>