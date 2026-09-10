<template>
  <ContentWrap>
    <el-button type="primary" class="mb-12px" @click="open('create')" v-hasPermi="['genealogy:activity:create']">创建活动</el-button>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="名称" prop="title" />
      <el-table-column label="地点" prop="place" />
      <el-table-column label="开始" prop="startTime" :formatter="dateFormatter" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_ACTIVITY_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="scope">
          <el-button link type="primary" @click="open('update', scope.row.id)">编辑</el-button>
          <el-button link @click="showReg(scope.row)">报名名单</el-button>
          <el-button link type="warning" @click="cancel(scope.row)">取消</el-button>
          <el-button link type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <Dialog :title="form.id ? '编辑活动' : '创建活动'" v-model="visible" width="640px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="活动名称"><el-input v-model="form.title" maxlength="30" /></el-form-item>
      <el-form-item label="开始/结束">
        <el-date-picker v-model="form.startTime" value-format="x" placeholder="开始" />
        <el-date-picker v-model="form.endTime" value-format="x" placeholder="结束" class="ml-8px" />
      </el-form-item>
      <el-form-item label="地点"><el-input v-model="form.place" /></el-form-item>
      <el-form-item label="集合地点"><el-input v-model="form.gatherPlace" /></el-form-item>
      <el-form-item label="报名截止"><el-date-picker v-model="form.deadline" value-format="x" type="datetime" /></el-form-item>
      <el-form-item label="人数上限"><el-input-number v-model="form.maxCount" :min="1" :max="500" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
      <el-form-item label="流程"><el-input type="textarea" v-model="form.processDesc" /></el-form-item>
      <el-form-item label="注意事项"><el-input type="textarea" v-model="form.notice" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="save">保存</el-button></template>
  </Dialog>
  <el-drawer v-model="regVisible" title="报名名单" size="520px">
    <el-button class="mb-8px" @click="exportReg">导出</el-button>
    <el-table :data="regs">
      <el-table-column label="姓名" prop="userName" />
      <el-table-column label="人数" prop="peopleCount" />
      <el-table-column label="乘车" prop="needBus"><template #default="s">{{ s.row.needBus ? '是' : '否' }}</template></el-table-column>
      <el-table-column label="状态" prop="status"><template #default="s">{{ s.row.status === 2 ? '候补' : '已报名' }}</template></el-table-column>
    </el-table>
  </el-drawer>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { GenealogyActivityApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyActivity' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })
const visible = ref(false)
const form = ref<any>({})
const regVisible = ref(false)
const regs = ref<any[]>([])
const currentId = ref<number>()
const getList = async () => {
  loading.value = true
  try {
    const data = await GenealogyActivityApi.page(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const open = async (type: string, id?: number) => {
  form.value = { status: 1 }
  if (id) form.value = await GenealogyActivityApi.get(id)
  visible.value = true
}
const save = async () => {
  if (form.value.id) await GenealogyActivityApi.update(form.value)
  else await GenealogyActivityApi.create(form.value)
  message.success('已保存'); visible.value = false; await getList()
}
const cancel = async (row: any) => {
  await message.confirm('取消活动将通知已报名族人，确认？')
  await GenealogyActivityApi.cancel(row.id)
  message.success('已取消'); await getList()
}
const remove = async (id: number) => {
  await message.delConfirm()
  await GenealogyActivityApi.delete(id)
  message.success('已删除'); await getList()
}
const showReg = async (row: any) => {
  currentId.value = row.id
  regs.value = await GenealogyActivityApi.registrations(row.id)
  regVisible.value = true
}
const exportReg = async () => {
  if (!currentId.value) return
  download.excel(await GenealogyActivityApi.exportReg(currentId.value), '报名名单.xls')
}
onMounted(getList)
</script>
