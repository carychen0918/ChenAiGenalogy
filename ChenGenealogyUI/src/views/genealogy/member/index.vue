<template>
  <ContentWrap>
    <el-form :inline="true" :model="queryParams" class="-mb-15px" ref="queryFormRef">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="世代" prop="generationNo">
        <el-input-number v-model="queryParams.generationNo" class="!w-140px" :controls="false" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['genealogy:member:create']">新增</el-button>
        <el-button type="success" @click="handleExport" v-hasPermi="['genealogy:member:export']">导出</el-button>
        <el-button @click="handleImport" v-hasPermi="['genealogy:member:import']">导入</el-button>
        <el-button @click="openRecycle">回收站</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="姓名" prop="name" />
      <el-table-column label="性别" prop="gender">
        <template #default="scope">{{ scope.row.gender === 1 ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column label="世代" prop="generationNo" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.alive === false ? 'info' : 'success'" size="small">
            {{ scope.row.alive === false ? '已离世' : '在世' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="字辈" min-width="140">
        <template #default="scope">
          {{ formatMemberGeneration(scope.row) || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="地区" min-width="160">
        <template #default="scope">{{ scope.row.regionName || '未填写' }}</template>
      </el-table-column>
      <el-table-column label="父亲" prop="fatherName" />
      <el-table-column label="配偶" prop="spouseNames">
        <template #default="scope">{{ (scope.row.spouseNames || []).join('、') }}</template>
      </el-table-column>
      <el-table-column label="登录账号" min-width="120">
        <template #default="scope">
          <span v-if="scope.row.loginUsername">{{ scope.row.loginUsername }}</span>
          <el-tag v-else type="info" size="small">未开通</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">编辑</el-button>
          <el-button link type="primary" @click="openArchive(scope.row.id)" v-hasPermi="['genealogy:member:update']">维护档案</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <MemberForm ref="formRef" @success="getList" />
  <MemberArchiveForm ref="archiveRef" @success="getList" />
  <el-dialog v-model="recycleVisible" title="回收站（30天内可恢复）" width="640px">
    <el-table :data="recycleList">
      <el-table-column label="姓名" prop="name" />
      <el-table-column label="删除时间" prop="deletedTime" :formatter="dateFormatter" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="restore(scope.row.id)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
  <el-dialog v-model="importVisible" title="批量导入" width="480px">
    <el-upload :auto-upload="false" :limit="1" accept=".xls,.xlsx" :on-change="onFile">
      <el-button>选择 Excel</el-button>
    </el-upload>
    <div class="mt-12px">
      <el-button @click="downloadTemplate">下载模板</el-button>
      <el-button type="primary" @click="doImport" :disabled="!importFile">开始导入</el-button>
    </div>
  </el-dialog>
</template>
<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { GenealogyMemberApi } from '@/api/genealogy'
import { formatMemberGeneration } from '@/views/genealogy/utils/generation'
import MemberForm from './MemberForm.vue'
import MemberArchiveForm from './MemberArchiveForm.vue'

defineOptions({ name: 'GenealogyMember' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: undefined, generationNo: undefined })
const queryFormRef = ref()
const formRef = ref()
const archiveRef = ref()
const recycleVisible = ref(false)
const recycleList = ref<any[]>([])
const importVisible = ref(false)
const importFile = ref<File>()

const getList = async () => {
  loading.value = true
  try {
    const data = await GenealogyMemberApi.getPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
const openForm = (type: string, id?: number) => formRef.value.open(type, id)
const openArchive = (id: number) => archiveRef.value.open(id)
const handleDelete = async (row: any) => {
  const n = row.descendantCount || 0
  if (n > 0) {
    await message.confirm(`该成员存在${n}位后代，删除后后代将变为未挂载状态`)
  } else {
    await message.delConfirm()
  }
  await GenealogyMemberApi.delete(row.id, true)
  message.success('已删除')
  await getList()
}
const openRecycle = async () => {
  recycleList.value = await GenealogyMemberApi.recycleList()
  recycleVisible.value = true
}
const restore = async (id: number) => {
  await GenealogyMemberApi.restore(id)
  message.success('已恢复')
  await openRecycle()
  await getList()
}
const handleExport = async () => {
  const data = await GenealogyMemberApi.export(queryParams)
  download.excel(data, '家族成员.xls')
}
const handleImport = () => { importVisible.value = true; importFile.value = undefined }
const onFile = (file: any) => { importFile.value = file.raw }
const downloadTemplate = async () => {
  download.excel(await GenealogyMemberApi.importTemplate(), '成员导入模板.xls')
}
const doImport = async () => {
  if (!importFile.value) return
  const res = await GenealogyMemberApi.import(importFile.value, false)
  message.success(`新增 ${res.createCount}，失败 ${res.failureCount}`)
  if (res.failureMessages?.length) {
    message.alert(res.failureMessages.join('\n'))
  }
  importVisible.value = false
  await getList()
}
onMounted(getList)
</script>
