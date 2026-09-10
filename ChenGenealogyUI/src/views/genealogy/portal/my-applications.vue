<template>
  <div>
    <div class="flex justify-between mb-16px">
      <div class="text-22px font-bold">我的申请</div>
      <el-button type="primary" @click="$router.push('/portal/scholarship-apply')">新建申请</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="编号" prop="applyNo" width="150" />
      <el-table-column label="学年" prop="year" width="80" />
      <el-table-column label="学校" prop="school" />
      <el-table-column label="类型" prop="type">
        <template #default="s"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_TYPE" :value="s.row.type" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="s"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS" :value="s.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="s">
          <el-button link type="primary" @click="open(s.row)">详情</el-button>
          <el-button v-if="[1, 2, 10, 11, 12, 13].includes(s.row.status)" link type="warning" @click="withdraw(s.row)">撤回</el-button>
          <el-button v-if="[0, 2, 6, 7].includes(s.row.status)" link @click="$router.push('/portal/scholarship-apply')">重新编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="query.pageNo" v-model:limit="query.pageSize" @pagination="load" />
    <el-drawer v-model="visible" title="申请详情" size="560px">
      <div v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="编号">{{ detail.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ detail.regionName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ detail.school }} / {{ detail.major }} / {{ detail.grade }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="家庭情况">{{ detail.familySituation }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectReason" label="驳回原因">{{ detail.rejectReason }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.supplementRemark" label="补充说明">{{ detail.supplementRemark }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-16px font-bold mb-8px">审核进度</div>
        <el-timeline>
          <el-timeline-item v-for="step in detail.auditSteps || []" :key="step.level" :timestamp="step.remark">
            {{ step.name }} · {{ step.opinion || step.state }}
          </el-timeline-item>
        </el-timeline>
        <div class="mt-12px">材料：</div>
        <div v-for="(m, i) in detail.materials || []" :key="i">
          <el-link :href="m.url" target="_blank">{{ m.name || m.type }}</el-link>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'PortalMyApplications' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10 })
const visible = ref(false)
const detail = ref<any>()
const load = async () => {
  loading.value = true
  try {
    const data = await GenealogyScholarshipApi.myPage(query)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const open = async (row: any) => {
  detail.value = await GenealogyScholarshipApi.get(row.id)
  visible.value = true
}
const withdraw = async (row: any) => {
  await message.confirm('确认撤回该申请？撤回后可修改再提交。')
  await GenealogyScholarshipApi.withdraw(row.id)
  message.success('已撤回')
  await load()
}
onMounted(load)
</script>
