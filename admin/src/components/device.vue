<template>
    <div>
        <el-button type="primary" :disabled="!tableData.selectDateList.length" class="mb-[10px]"
            @click="isShow = true">批量操作({{ tableData.selectDateList.length }}个)</el-button>
        <el-table :data="props.list" border style="width: 100%" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="controlId" label="主板编号" align="center" />
            <el-table-column prop="deviceId" label="设备编号" align="center" />
            <el-table-column label="设备类型" align="center">
                <template #default="{ row }">
                    {{ row.deviceTypeId == 1 ? "空调" : "其他" }}
                </template>
            </el-table-column>
            <el-table-column prop="stateA" label="温度" align="center">
                <template #default="{ row }">
                    {{ isStatus(row.stateA) }}
                </template>
            </el-table-column>
            <el-table-column prop="stateB" label="定时状态" align="center">
                <template #default="{ row }">
                    {{ isStatus(row.stateB) }}
                </template>
            </el-table-column>
            <el-table-column prop="stateC" label="电源状态" align="center">
                <template #default="{ row }">
                    {{ isStatus(row.stateC) }}
                </template>
            </el-table-column>
            <el-table-column prop="stateD" label="运行状态" align="center">
                <template #default="{ row }">
                    {{ isStatus(row.stateD) }}
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
    <conditioning-model v-model="show" :item="active" @upList="upList"></conditioning-model>
    <el-dialog v-model="isShow" title="控制" width="50%">
        <el-button type="primary" @click="onSubmit(1)" class="mx-[10px] my-[12px]">批量查询</el-button>
        <el-button type="primary" @click="onSubmit(2)" class="mx-[10px] my-[12px]">批量开机（自动）</el-button>
        <el-button type="primary" @click="onSubmit(3)" class="mx-[10px] my-[12px]">批量关机</el-button>
        <el-button type="primary" @click="onSubmit(4)" class="mx-[10px] my-[12px]">批量制冷</el-button>
        <el-button type="primary" @click="onSubmit(5)" class="mx-[10px] my-[12px]">批量制热</el-button>
        <el-button type="primary" @click="onSubmit(6)" class="mx-[10px] my-[12px]">批量除湿</el-button>
        <template #footer>
            <span class="dialog-footer">
                <!-- <el-button @click="isShow = false">Cancel</el-button> -->
                <el-button @click="isShow = false">
                    关闭
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup lang='ts'>
const emits = defineEmits(['upList'])
import type { Item } from '@/types/index.ts'
import ConditioningModel from './conditioningModel.vue';
import { ref, reactive } from 'vue';
import { setAirBatch } from '@/api/index.ts'
interface ItemType {
    controlId: number,
    deviceId: number
}
const isShow = ref<boolean>(false)
const onSubmit = async (type: number) => {
    await setAirBatch({
        "controlId": tableData.selectDateList[0].controlId,
        "deviceIds": tableData.selectDateList.map(item => item.deviceId),                                    //批量选择的空调id集，必传
        "deviceTypeId": 1,  //设备类型必传
        "operationType": 1,  //操作方式必传  批量操作填1
        "operation": type  // 操作编码必传
    })
    emits('upList')
}
const isStatus = (type: number) => {
    if (type === 0) {
        return '关闭'
    } else if (type == 1) {
        return '开启'
    } else {
        return '暂无'
    }
}
const props = defineProps({
    list: Array
})
const tableData = reactive({
    selectDateList: [] as ItemType[],
})
const handleSelectionChange = (val: any) => {
    tableData.selectDateList = val
    console.log(val);
}
const upList = () => {
    emits('upList')
}
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