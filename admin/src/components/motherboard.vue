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
            <el-table-column label="操作" width="200">
                <template #default="{ row }">
                    <el-button link type="primary" :disabled="!row.connectionStatus" @click="onUpdateDia(row)">{{
                        row.connectionStatus ? '设备查看' : '查看关联设备'
                    }}</el-button>
                    <el-button link type="primary" :disabled="!row.connectionStatus"
                        @click="onUpdateAddress(row)">修改地址</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-dialog v-model="isShow" title="查看设备" width="80%">
            <device :list="tableData.deviceList"></device>
            <!-- <el-input v-model="addressValue" placeholder="请输入要修改的地址">
                <template #prepend>地址</template>
            </el-input> -->
            <!-- <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isShow = false">关闭</el-button>
                    <el-button type="primary" @click="onUpdataSubmit">
                        修改
                    </el-button>
                </span>
            </template> -->
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
const addressValue = ref<string>('')
let tableData = reactive({
    tableList: [],
    deviceList: []
})
let activeItem = reactive<{ id: number, address: string }>({
    id: 0,
    address: ''
})
const onUpdateDia = async (item: any) => {
    const { data } = await listByControl({ controlId: item.id })
    tableData.deviceList = data
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