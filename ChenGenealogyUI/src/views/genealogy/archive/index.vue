<template>
  <ContentWrap>
    <el-form :inline="true" :model="queryParams">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable class="!w-160px">
          <el-option v-for="d in getIntDictOptions(DICT_TYPE.GENEALOGY_ARCHIVE_STATUS)" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button @click="getList">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="成员" prop="memberName" />
      <el-table-column label="补充内容" prop="content" show-overflow-tooltip />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_ARCHIVE_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="时间" prop="createTime" :formatter="dateFormatter" />
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" link type="success" @click="audit(scope.row, 1)">通过</el-button>
          <el-button v-if="scope.row.status === 0" link type="danger" @click="reject(scope.row)">驳回</el-button>
          <el-button link type="primary" @click="openArchive(scope.row.memberId)">维护档案</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <MemberArchiveForm ref="archiveRef" />
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { GenealogyMemberApi } from '@/api/genealogy'
import MemberArchiveForm from '../member/MemberArchiveForm.vue'
defineOptions({ name: 'GenealogyArchive' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const archiveRef = ref()
const queryParams = reactive({ pageNo: 1, pageSize: 10, status: undefined })
const getList = async () => {
  loading.value = true
  try {
    const data = await GenealogyMemberApi.archivePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const audit = async (row: any, status: number, reason?: string) => {
  await GenealogyMemberApi.auditArchive(row.id, status, reason)
  message.success('已处理')
  await getList()
}
const reject = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回', { inputPattern: /.+/, inputErrorMessage: '原因必填' })
  await audit(row, 2, value)
}
const openArchive = (memberId: number) => archiveRef.value.open(memberId)
onMounted(getList)
</script>
