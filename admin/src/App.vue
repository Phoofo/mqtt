<template>
  <div>
    <h2>空调</h2>
    <el-row>
      <el-col :span="7" v-for="item in list" class="item">
        <el-card class="box-card">
          <div>
            <p>地址</p>
            <!-- <p>{{ item.address }}</p> -->
            <el-tag type="success">{{ item.address }}</el-tag>
          </div>
          <div>
            <p>状态</p>
            <!-- <p>{{ item.state }}</p> -->
            <el-tag type="warning">{{ item.state }}</el-tag>
          </div>
          <div>
            <p>ip</p>
            <!-- <p>{{ item.ip }}</p> -->
            <el-tag type="success">{{ item.ip }}</el-tag>
          </div>
          <div>
            <p>端口</p>
            <!-- <p>{{ item.port }}</p> -->
            <el-tag type="warning">{{ item.port }}</el-tag>
          </div>
          <div>
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

          <!-- <div slot="header" class="clearfix">
                        <span>一号空调</span>
                        <el-button style="float: right; padding: 3px 0" type="text">操作按钮</el-button>
                        <el-switch v-model="item.hot" active-text="制冷" inactive-text="制热">
                        </el-switch>
                    </div>
                    <div class="snow">
                        <div>
                            <span>温度</span>
                            <el-input-number v-model="item.temperature" controls-position="right" :min="1" :max="30">
                            </el-input-number>
                        </div>
                        <div>
                            <span>风向</span>
                            <el-radio v-model="item.direction" label="1">风向1</el-radio>
                            <el-radio v-model="item.direction" label="2">风向2</el-radio>
                        </div>
                    </div> -->
        </el-card>
      </el-col>
    </el-row>
  </div>
  <HelloWorld msg="Vite + Vue" />
</template>
<script setup lang="ts">
import { ref, onMounted } from "vue";
import { hardWareGet, airLinkSet } from '@/api/index.ts'
const list = ref([])
const onSubmit = async (item: any, type: any) => {
  await airLinkSet({
    ...item,
    set: type
  })
}
onMounted(async () => {
  const { data } = await hardWareGet()
  list.value = data.msg
}),
</script>
<style scoped></style>
