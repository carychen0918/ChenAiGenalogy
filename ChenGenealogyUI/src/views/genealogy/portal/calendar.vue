<template>
  <div>
    <div class="flex justify-between items-center mb-16px">
      <div>
        <div class="text-22px font-bold">家族日历</div>
        <div class="text-13px text-gray-500 mt-4px">仅展示管理员勾选成员的寿辰、忌日，以及清明、祭祖活动与资助窗口</div>
      </div>
      <el-date-picker v-model="month" type="month" value-format="YYYY-MM" @change="load" />
    </div>
    <el-timeline>
      <el-timeline-item v-for="c in list" :key="c.type + c.title + c.date" :timestamp="fmt(c.date)" placement="top">
        <el-card class="cursor-pointer" shadow="hover" @click="go(c)">
          <el-tag size="small">{{ label(c.type) }}</el-tag>
          <span class="ml-8px font-bold">{{ c.title }}</span>
          <div class="text-13px text-gray-500 mt-6px">{{ c.remark }}</div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!list.length && !loading" description="本月暂无事项" />
  </div>
</template>
<script setup lang="ts">
import { GenealogyShowcaseApi } from '@/api/genealogy'
defineOptions({ name: 'PortalCalendar' })
const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const now = new Date()
const month = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const label = (t: string) => ({ birthday: '寿辰', memorial: '忌日', festival: '节气', activity: '活动', scholarship: '资助' }[t] || t)
const fmt = (t: any) => (t ? String(t).slice(0, 10) : '')
const go = (c: any) => c.link && router.push(c.link)
const load = async () => {
  loading.value = true
  try {
    const [y, m] = (month.value || '').split('-').map(Number)
    list.value = (await GenealogyShowcaseApi.calendar(y, m)) || []
  } finally { loading.value = false }
}
onMounted(load)
</script>
