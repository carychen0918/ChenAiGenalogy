<template>
  <div>
    <div class="flex items-center justify-between mb-16px">
      <div>
        <div class="text-22px font-bold">族谱工作台</div>
        <div class="text-13px text-gray-500">数据总览与待办处理</div>
      </div>
      <el-button type="primary" @click="router.push('/portal/home')">进入族人端门户</el-button>
    </div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="cursor-pointer" @click="item.to && router.push(item.to)">
          <div class="text-28px font-bold">{{ summary[item.key] ?? '-' }}</div>
          <div class="text-13px text-gray-500 mt-4px">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button @click="router.push('/genealogy/family/member')">成员管理</el-button>
        <el-button @click="router.push('/genealogy/scholarship/audit')">申请审核</el-button>
        <el-button @click="router.push('/genealogy/scholarship/disburse')">发放登记</el-button>
        <el-button @click="router.push('/genealogy/worship/activity')">祭祖活动</el-button>
        <el-button @click="router.push('/genealogy/ops/feed')">动态公告</el-button>
      </el-space>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { GenealogyDashboardApi } from '@/api/genealogy'

defineOptions({ name: 'GenealogyHome' })
const router = useRouter()
const summary = ref<Record<string, any>>({})
const cards = [
  { key: 'memberCount', label: '成员总数', to: '/genealogy/family/member' },
  { key: 'pendingFirst', label: '待初审申请', to: '/genealogy/scholarship/audit' },
  { key: 'pendingFinal', label: '待终审申请', to: '/genealogy/scholarship/audit' },
  { key: 'pendingDisburse', label: '待发放', to: '/genealogy/scholarship/disburse' },
  { key: 'pendingFeed', label: '待审动态', to: '/genealogy/ops/feed' },
  { key: 'pendingArchive', label: '待审档案', to: '/genealogy/family/archive' }
]
onMounted(async () => {
  summary.value = await GenealogyDashboardApi.summary()
})
</script>
