<template>
  <ContentWrap>
    <el-tabs>
      <el-tab-pane label="资助窗口期">
        <el-form :model="config" label-width="120px" class="!w-520px">
          <el-form-item label="学年"><el-input-number v-model="config.year" :controls="false" /></el-form-item>
          <el-form-item label="开始时间"><el-date-picker v-model="config.windowStart" value-format="x" type="datetime" /></el-form-item>
          <el-form-item label="结束时间"><el-date-picker v-model="config.windowEnd" value-format="x" type="datetime" /></el-form-item>
          <el-form-item label="政策说明"><el-input type="textarea" v-model="config.policy" rows="5" /></el-form-item>
          <el-form-item><el-button type="primary" @click="saveConfig">保存</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="温蒂坟地">
        <el-form :model="tomb" label-width="120px" class="!w-520px">
          <el-form-item label="名称"><el-input v-model="tomb.name" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="tomb.address" /></el-form-item>
          <el-form-item label="经度"><el-input v-model="tomb.longitude" /></el-form-item>
          <el-form-item label="纬度"><el-input v-model="tomb.latitude" /></el-form-item>
          <el-form-item label="停车场"><el-input v-model="tomb.parking" /></el-form-item>
          <el-form-item label="入口"><el-input v-model="tomb.entrance" /></el-form-item>
          <el-form-item label="公厕"><el-input v-model="tomb.toilet" /></el-form-item>
          <el-form-item><el-button type="primary" @click="saveTomb">保存</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="家族信息">
        <el-form :model="family" label-width="120px" class="!w-520px">
          <el-form-item label="家族名"><el-input v-model="family.name" /></el-form-item>
          <el-form-item label="姓氏"><el-input v-model="family.surname" /></el-form-item>
          <el-form-item label="始祖"><el-input v-model="family.ancestorName" /></el-form-item>
          <el-form-item label="地区"><el-input v-model="family.region" /></el-form-item>
          <el-form-item label="简介"><el-input type="textarea" v-model="family.intro" rows="4" /></el-form-item>
          <el-form-item><el-button type="primary" @click="GenealogyContentApi.updateFamily(family).then(() => message.success('已保存'))">保存</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>
<script setup lang="ts">
import { GenealogyContentApi, GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyConfig' })
const message = useMessage()
const config = ref<any>({ year: new Date().getFullYear() })
const tomb = ref<any>({})
const family = ref<any>({})
onMounted(async () => {
  config.value = (await GenealogyScholarshipApi.getConfig()) || config.value
  tomb.value = (await GenealogyContentApi.getTomb()) || {}
  family.value = await GenealogyContentApi.getFamily()
})
const saveConfig = async () => { await GenealogyScholarshipApi.saveConfig(config.value); message.success('已保存') }
const saveTomb = async () => { await GenealogyContentApi.saveTomb(tomb.value); message.success('已保存') }
</script>
