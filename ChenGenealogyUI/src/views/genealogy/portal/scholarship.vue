<template>
  <div>
    <div class="flex justify-between items-start mb-16px">
      <div>
        <div class="text-22px font-bold">学海无涯</div>
        <div class="text-13px text-gray-500">家族助学 · 透明可追溯</div>
      </div>
      <el-space>
        <el-button @click="$router.push('/portal/my-applications')">我的申请</el-button>
        <el-button type="primary" :disabled="!config?.open" @click="goApply">立即申请</el-button>
      </el-space>
    </div>
    <el-alert v-if="config && !config.open" type="warning" :closable="false" class="mb-16px"
      :title="'当前不在申请期内，申请开放时间为 ' + format(config.windowStart) + ' 至 ' + format(config.windowEnd)" />
    <el-alert v-else-if="config?.open" type="success" :closable="false" class="mb-16px"
      :title="'申请窗口开放中：' + format(config.windowStart) + ' 至 ' + format(config.windowEnd)" />
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card header="资助政策">
          <div class="whitespace-pre-wrap leading-7">{{ config?.policy || '政策说明由管理员维护。' }}</div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card header="申请进度">
          <div v-if="latest">
            <div>编号 {{ latest.applyNo }}</div>
            <dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS" :value="latest.status" class="mt-8px" />
            <el-button class="mt-12px" text type="primary" @click="$router.push('/portal/my-applications')">查看详情</el-button>
          </div>
          <el-empty v-else description="暂无申请记录" />
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px" header="资助公示">
      <div v-for="f in notices" :key="f.id" class="py-12px border-b">
        <div class="font-bold">{{ f.title }}</div>
        <div class="text-13px text-gray-500 mt-4px">{{ f.content }}</div>
      </div>
      <el-empty v-if="!notices.length" description="学年发放完成后将在此公示" />
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyFeedApi, GenealogyMemberApi, GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'PortalScholarship' })
const message = useMessage()
const router = useRouter()
const config = ref<any>()
const latest = ref<any>()
const notices = ref<any[]>([])
const format = (t: number) => (t ? new Date(t).toLocaleDateString() : '')
const goApply = async () => {
  try {
    const me = await GenealogyMemberApi.me()
    if (!me?.id) {
      message.warning('您的成员身份未认证，请联系管理员')
      return
    }
  } catch {
    message.warning('您的成员身份未认证，请联系管理员')
    return
  }
  router.push('/portal/scholarship-apply')
}
onMounted(async () => {
  config.value = await GenealogyScholarshipApi.getConfig()
  const mine = await GenealogyScholarshipApi.myPage({ pageNo: 1, pageSize: 1 })
  latest.value = mine.list?.[0]
  notices.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 10, type: 3, status: 1 })).list
})
</script>
