<template>
    <div class="mx-auto w-[80%] mt-[20px]">
        <h2 class="text-[30px] text-center mb-[10px]">智能园区管理系统</h2>
        <el-table :data="tableData.tableList" border style="width: 100%">
            <el-table-column label="地址">
                <template #default="{ row }">
                    {{ row.address || '暂无' }}
                </template>
            </el-table-column>
            <el-table-column label="连接状态">
                <template #default="{ row }">
                    <el-tag v-if="row.connectionStatus" class="ml-2" type="info">已连接</el-tag>
                    <el-tag v-else class="ml-2" type="danger">未连接</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createdDate" label="创建时间" width="200"></el-table-column>
            <el-table-column label="最后修改时间" width="200">
                <template #default="{ row }">
                    {{ row.lastModifiedDate || '暂无' }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
                <template #default="{ row }">
                    <el-button link type="primary" :disabled="!row.connectionStatus" @click="onUpdateDia(row)">{{
                        row.connectionStatus ? '设备查看' : '查看关联设备'
                    }}</el-button>
                    <el-button link type="primary" :disabled="!row.connectionStatus"
                        @click="onUpdateAddress(row)">修改地址</el-button>
                    <el-button link type="primary" :disabled="!row.connectionStatus"
                        @click="onUpdateTouch(row)">一键操作</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-dialog v-model="isShow" title="查看设备" width="80%">
            <device :list="tableData.deviceList" @upList="upList"></device>
        </el-dialog>
        <el-dialog v-model="isShowTouch" title="控制" width="50%">
            <el-button type="primary" @click="onSubmit(1)" class="mx-[10px] my-[12px]">查询</el-button>
            <el-button type="primary" @click="onSubmit(2)" class="mx-[10px] my-[12px]">开机（自动）</el-button>
            <el-button type="primary" @click="onSubmit(3)" class="mx-[10px] my-[12px]">关机</el-button>
            <el-button type="primary" @click="onSubmit(4)" class="mx-[10px] my-[12px]">制冷</el-button>
            <el-button type="primary" @click="onSubmit(5)" class="mx-[10px] my-[12px]">制热</el-button>
            <el-button type="primary" @click="onSubmit(6)" class="mx-[10px] my-[12px]">除湿</el-button>
            <template #footer>
                <span class="dialog-footer">
                    <!-- <el-button @click="isShow = false">Cancel</el-button> -->
                    <el-button @click="isShowTouch = false">
                        关闭
                    </el-button>
                </span>
            </template>
        </el-dialog>
        <el-dialog v-model="isShowAddress" title="修改地址" width="50%">
            <el-input v-model="addressValue" placeholder="请输入要修改的地址">
                <template #prepend>地址</template>
            </el-input>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isShowAddress = false">关闭</el-button>
                    <el-button type="primary" @click="onUpdataSubmit">
                        修改
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang='ts'>
import { onMounted, reactive, ref } from 'vue'
const isShow = ref<boolean>(false)
const isShowAddress = ref<boolean>(false)
import Device from './device.vue'
import { listControl, configurationLocation, listByControl } from '@/api/index.ts';
import { setAirBatch } from '@/api/index.ts'
interface ItemObj {
    id: number
}
const addressValue = ref<string>('')
const isShowTouch = ref<boolean>(false)
const activeId = ref(0)
let tableData = reactive({
    tableList: [],
    deviceList: [],
    selectDateList: []
})
const activeTouch = reactive({
    item: {} as ItemObj
})
const onUpdateTouch = (item: any) => {
    activeTouch.item = item
    isShowTouch.value = true
}
const upList = async () => {
    const { data } = await listByControl({ controlId: activeId.value })
    tableData.deviceList = data
}
const onSubmit = async (type: number) => {
    await setAirBatch({
        "controlId": activeTouch.item.id,
        "deviceTypeId": 1,  //设备类型必传
        "operationType": 2,  //操作方式必传  批量操作填1
        "operation": type  // 操作编码必传
    })
    isShowTouch.value = false
    const { data } = await listControl()
    tableData.tableList = data
}
let activeItem = reactive<{ id: number, address: string }>({
    id: 0,
    address: ''
})
const onUpdateDia = async (item: any) => {
    const { data } = await listByControl({ controlId: item.id })
    tableData.deviceList = data
    activeId.value = item.id
    activeItem = item
    isShow.value = true
}
onMounted(async () => {
    const { data } = await listControl()
    tableData.tableList = data
})
const onUpdateAddress = async (item: any) => {
    activeItem = item
    isShowAddress.value = true
}
const onUpdataSubmit = async () => {
    await configurationLocation({
        id: activeItem.id,
        address: addressValue.value
    })
    const { data: res } = await listControl()
    tableData.tableList = res
    isShowAddress.value = false
    addressValue.value = ''
}
</script>

<style scoped lang='less'></style>