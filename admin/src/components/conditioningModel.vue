<template>
    <el-dialog v-model="isShow" title="控制" width="50%">
        <el-button type="primary" @click="onSubmit(1)" class="mx-[10px] my-[12px]">查询</el-button>
        <el-button type="primary" @click="onSubmit(2)" class="mx-[10px] my-[12px]">开机（自动）</el-button>
        <el-button type="primary" @click="onSubmit(3)" class="mx-[10px] my-[12px]">关机</el-button>
        <el-button type="primary" @click="onSubmit(4)" class="mx-[10px] my-[12px]">制冷</el-button>
        <el-button type="primary" @click="onSubmit(5)" class="mx-[10px] my-[12px]">制热</el-button>
        <el-button type="primary" @click="onSubmit(6)" class="mx-[10px] my-[12px]">除湿</el-button>
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
import { ElMessage } from 'element-plus'
import { computed } from 'vue'
import type { Item } from '@/types/index.ts'
import { airLinkSet } from '@/api/index.ts'
const emits = defineEmits(['update:modelValue'])
interface Prop {
    modelValue: boolean,
    item: Item
}
const props = defineProps<Prop>()
const onSubmit = async (type: number) => {
    const data = await airLinkSet({
        ...props.item,
        controlId: props.item.controlId,
        deviceId: props.item.deviceId,
        deviceTypeId: props.item.deviceTypeId,
        operation: type
    })
    if (data === 'Success!') {
        ElMessage({
            message: '操作成功',
            type: 'success',
        })
        emits('update:modelValue', false)
    } else {
        ElMessage({
            message: '操作失败',
            type: 'error',
        })
    }
}

const isShow = computed({
    get() {
        return props.modelValue
    },
    set(value) {
        emits('update:modelValue', value)
    }
})

</script>

<style scoped>
::v-deep.el-button+.el-button {
    margin-left: 10px;
}
</style>