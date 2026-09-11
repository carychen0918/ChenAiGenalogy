<template>
  <ContentWrap>
    <el-form :inline="true" :model="queryParams">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable class="!w-160px">
          <el-option v-for="d in getIntDictOptions(DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS)" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button @click="getList">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" @selection-change="(rows) => (selected = rows)">
      <el-table-column type="selection" width="40" />
      <el-table-column label="编号" prop="applyNo" width="140" />
      <el-table-column label="申请人" prop="memberName" width="100" />
      <el-table-column label="地区" min-width="160" prop="regionName" />
      <el-table-column label="学校" prop="school" min-width="140" />
      <el-table-column label="类型" prop="type" width="120">
        <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_TYPE" :value="scope.row.type" /></template>
      </el-table-column>
      <el-table-column label="当前环节" width="110">
        <template #default="scope">{{ levelName(scope.row.currentAuditLevel) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110">
        <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <template v-if="scope.row.canAudit">
            <el-button link type="success" @click="doAudit(scope.row, 1)">通过</el-button>
            <el-button link type="warning" @click="needMaterial(scope.row)">补材料</el-button>
            <el-button link type="danger" @click="reject(scope.row)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-button class="mt-12px" @click="batchPass" v-hasPermi="['genealogy:scholarship:first-audit']">批量通过本级</el-button>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <el-drawer v-model="drawerVisible" title="资助申请详情" size="640px">
    <div v-if="detail" class="pr-8px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ detail.year }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.memberName }} · {{ detail.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="字辈">{{ formatMemberGeneration(detail) }}</el-descriptions-item>
        <el-descriptions-item label="地区" :span="2">{{ detail.regionName || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="学校" :span="2">{{ detail.school }} / {{ detail.major }} / {{ detail.grade }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_TYPE" :value="detail.type" />
        </el-descriptions-item>
        <el-descriptions-item label="建议金额">{{ detail.suggestAmount || '—' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.memberMobile || '仅管理员可见' }}</el-descriptions-item>
        <el-descriptions-item label="家庭情况" :span="2">{{ detail.familySituation }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.supplementRemark" label="补材料说明" :span="2">{{ detail.supplementRemark }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">{{ detail.rejectReason }}</el-descriptions-item>
      </el-descriptions>

      <div class="mt-20px font-bold mb-8px">审核进度</div>
      <el-timeline>
        <el-timeline-item
          v-for="step in detail.auditSteps || []"
          :key="step.level"
          :type="stepType(step.state)"
          :timestamp="step.auditTime ? new Date(step.auditTime).toLocaleString() : step.remark"
        >
          <div class="font-bold">{{ step.name }}
            <el-tag size="small" class="ml-8px" :type="stepType(step.state)">{{ stepStateName(step.state) }}</el-tag>
          </div>
          <div v-if="step.auditorName" class="text-13px text-gray-500 mt-4px">审核人：{{ step.auditorName }}</div>
          <div v-if="step.opinion" class="text-13px mt-4px">意见：{{ step.opinion }}</div>
        </el-timeline-item>
      </el-timeline>

      <div class="mt-12px font-bold mb-8px">证明材料</div>
      <el-empty v-if="!detail.materials?.length" description="暂无材料" />
      <div v-for="(m, i) in detail.materials || []" :key="i" class="mb-8px">
        <el-link :href="m.url" target="_blank" type="primary">{{ m.name || m.type }}</el-link>
        <el-image v-if="isImage(m.url)" :src="m.url" class="mt-6px w-120px h-80px" fit="cover" preview-teleported :preview-src-list="[m.url]" />
      </div>

      <div v-if="detail.canAudit" class="mt-24px flex justify-end gap-8px">
        <el-button type="success" @click="doAudit(detail, 1)">本级通过</el-button>
        <el-button type="warning" @click="needMaterial(detail)">要求补材料</el-button>
        <el-button type="danger" @click="reject(detail)">驳回</el-button>
      </div>
    </div>
  </el-drawer>
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { GenealogyScholarshipApi } from '@/api/genealogy'
import { formatMemberGeneration } from '@/views/genealogy/utils/generation'
defineOptions({ name: 'GenealogyScholarshipAudit' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const selected = ref<any[]>([])
const detail = ref<any>()
const drawerVisible = computed({
  get: () => !!detail.value,
  set: (open: boolean) => {
    if (!open) detail.value = undefined
  }
})
const queryParams = reactive({ pageNo: 1, pageSize: 10, status: undefined })
const levelName = (level?: string) =>
  ({ county: '县审', city: '市审', province: '省审', family: '家族审' } as any)[level || ''] || '—'
const stepStateName = (state: string) =>
  ({ pending: '待审', passed: '已通过', rejected: '已驳回', skipped: '已跳过', waiting: '未开始', supplement: '补材料' } as any)[state] || state
const stepType = (state: string) =>
  ({ pending: 'warning', passed: 'success', rejected: 'danger', skipped: 'info', waiting: '', supplement: 'warning' } as any)[state]
const isImage = (url?: string) => !!url && /\.(png|jpe?g|gif|webp)$/i.test(url)

const getList = async () => {
  loading.value = true
  try {
    const data = await GenealogyScholarshipApi.page(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const openDetail = async (row: any) => {
  detail.value = await GenealogyScholarshipApi.get(row.id)
}
const doAudit = async (row: any, result: number, opinion?: string) => {
  await GenealogyScholarshipApi.audit(row.id, result, opinion)
  message.success('已处理')
  detail.value = undefined
  await getList()
}
const needMaterial = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请填写需补充的材料说明', '要求补充材料', { inputPattern: /.+/, inputErrorMessage: '说明必填' })
  await doAudit(row, 3, value)
}
const reject = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回', { inputPattern: /.+/, inputErrorMessage: '原因必填' })
  await doAudit(row, 2, value)
}
const batchPass = async () => {
  const ids = selected.value.filter((i) => i.canAudit).map((i) => i.id)
  if (!ids.length) return message.warning('请选择当前可由您审核的申请')
  await GenealogyScholarshipApi.batchFirst(ids, 1, '批量通过')
  message.success('已处理')
  await getList()
}
onMounted(getList)
</script>
