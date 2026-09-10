<template>
  <ContentWrap>
    <el-alert
      class="mb-12px"
      type="info"
      :closable="false"
      title="先在系统用户中给账号分配省 / 市 / 县管理员角色，再在此绑定管辖地区。未绑定的层级在助学审核时会自动跳过。"
    />
    <el-table :data="list" v-loading="loading">
      <el-table-column label="账号" prop="username" width="140" />
      <el-table-column label="姓名" prop="nickname" width="140" />
      <el-table-column label="角色" prop="roleName" width="120" />
      <el-table-column label="管辖地区">
        <template #default="scope">
          <span v-if="scope.row.bound">{{ scope.row.regionName }}</span>
          <el-tag v-else type="warning" size="small">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <AreaSelect
            :model-value="scope.row.areaId"
            :check-strictly="true"
            :selectable-levels="levelsOf(scope.row.auditLevel)"
            class="!w-180px mr-8px"
            @update:model-value="(val) => bind(scope.row, val)"
          />
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>
<script setup lang="ts">
import AreaSelect from '@/views/system/area/components/AreaSelect.vue'
import { GenealogyAdminRegionApi } from '@/api/genealogy'

defineOptions({ name: 'GenealogyRegionAdmin' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])

const levelsOf = (level: string) => {
  if (level === 'province') return [1]
  if (level === 'city') return [2]
  return [3]
}

const getList = async () => {
  loading.value = true
  try {
    list.value = await GenealogyAdminRegionApi.list()
  } finally {
    loading.value = false
  }
}

const bind = async (row: any, areaId?: number) => {
  if (!areaId) return
  await GenealogyAdminRegionApi.save({ userId: row.userId, areaId })
  message.success('已绑定管辖地区')
  await getList()
}

onMounted(getList)
</script>
