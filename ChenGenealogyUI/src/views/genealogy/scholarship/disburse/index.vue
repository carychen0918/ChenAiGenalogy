<template>
  <ContentWrap>
    <el-tabs v-model="tab">
      <el-tab-pane label="待发放" name="pending" />
      <el-tab-pane label="已发放" name="done" />
    </el-tabs>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="编号" prop="applyNo" />
      <el-table-column label="申请人" prop="memberName" />
      <el-table-column label="学校" prop="school" />
      <el-table-column label="建议金额" prop="suggestAmount" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" v-if="tab === 'pending'">
        <template #default="scope">
          <el-button link type="primary" @click="open(scope.row)">发放登记</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button class="mt-12px" type="success" @click="exportExcel" v-if="tab === 'done'">导出Excel</el-button>
  </ContentWrap>
  <Dialog title="发放登记" v-model="visible">
    <el-form :model="form" label-width="100px">
      <el-form-item label="金额" required><el-input-number v-model="form.amount" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="方式"><el-select v-model="form.method"><el-option v-for="d in getIntDictOptions(DICT_TYPE.GENEALOGY_DISBURSE_METHOD)" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="日期"><el-date-picker v-model="form.disburseDate" value-format="x" /></el-form-item>
      <el-form-item label="经办人"><el-input v-model="form.handlerName" /></el-form-item>
      <el-form-item label="凭证"><UploadImg v-model="form.voucherUrls" :limit="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit">确认发放</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import download from '@/utils/download'
import { GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyDisburse' })
const message = useMessage()
const tab = ref('pending')
const loading = ref(false)
const list = ref<any[]>([])
const visible = ref(false)
const form = ref<any>({ method: 1 })
const getList = async () => {
  loading.value = true
  try {
    const status = tab.value === 'pending' ? 4 : 5
    const data = await GenealogyScholarshipApi.page({ pageNo: 1, pageSize: 50, status })
    list.value = data.list
  } finally { loading.value = false }
}
watch(tab, getList)
const open = (row: any) => {
  form.value = { applicationId: row.id, amount: row.suggestAmount, method: 1, disburseDate: Date.now(), handlerName: '', voucherUrls: [] }
  visible.value = true
}
const submit = async () => {
  if (!form.value.amount) return message.error('发放金额不能为空')
  await message.confirm('确认登记发放？')
  await GenealogyScholarshipApi.disburse(form.value)
  message.success('已发放'); visible.value = false; await getList()
}
const exportExcel = async () => {
  download.excel(await GenealogyScholarshipApi.disburseExport({}), '资助发放记录.xls')
}
onMounted(getList)
</script>
