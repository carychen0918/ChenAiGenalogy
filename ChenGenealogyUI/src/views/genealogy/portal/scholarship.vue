<template>
  <div class="page">
    <div class="head">
      <div>
        <div class="title">学海无涯</div>
        <div class="sub">家族助学 · 透明可追溯</div>
      </div>
      <el-space>
        <el-button @click="$router.push('/portal/my-applications')">我的申请</el-button>
        <el-button type="primary" :disabled="!config?.open" @click="goApply">立即申请</el-button>
      </el-space>
    </div>
    <el-alert v-if="config && !config.open" type="warning" :closable="false"
      :title="'当前不在申请期内，申请开放时间为 ' + format(config.windowStart) + ' 至 ' + format(config.windowEnd)" />
    <el-alert v-else-if="config?.open" type="success" :closable="false"
      :title="'申请窗口开放中：' + format(config.windowStart) + ' 至 ' + format(config.windowEnd)" />

    <section class="band">
      <article class="panel">
        <div class="panel-h">资助政策</div>
        <div class="policy">{{ config?.policy || '政策说明由管理员维护。' }}</div>
      </article>
      <article class="panel">
        <div class="panel-h">申请进度</div>
        <div v-if="latest" class="progress">
          <div class="progress-no">编号 {{ latest.applyNo }}</div>
          <dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_STATUS" :value="latest.status" />
          <div v-if="(latest.auditSteps || []).length" class="steps">
            <div v-for="step in latest.auditSteps" :key="step.level" class="step" :class="step.state">
              <span class="step-name">{{ step.name }}</span>
              <span class="step-state">{{ stepLabel(step) }}</span>
            </div>
          </div>
          <el-button class="mt-8px" text type="primary" @click="$router.push('/portal/my-applications')">查看详情</el-button>
        </div>
        <div v-else class="quiet">暂无申请记录</div>
      </article>
    </section>

    <section class="band">
      <article class="panel">
        <div class="panel-h">助学榜样</div>
        <div v-if="wall.length" class="wall">
          <div v-for="w in wall" :key="w.applicationId" class="wall-item">
            <b>{{ w.name }} <el-tag size="small">{{ w.year }}</el-tag></b>
            <i>{{ [w.school, w.major, w.grade].filter(Boolean).join(' · ') }}</i>
          </div>
        </div>
        <div v-else class="quiet">榜样墙将在发放完成后展示</div>
      </article>
      <article class="panel">
        <div class="panel-h">资助公示</div>
        <div v-if="notices.length" class="list">
          <div v-for="f in notices" :key="f.id" class="row">
            <b>{{ f.title }}</b>
            <i>{{ f.content }}</i>
          </div>
        </div>
        <div v-else class="quiet">学年发放完成后将在此公示</div>
      </article>
    </section>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyFeedApi, GenealogyMemberApi, GenealogyScholarshipApi, GenealogyShowcaseApi } from '@/api/genealogy'
defineOptions({ name: 'PortalScholarship' })
const message = useMessage()
const router = useRouter()
const config = ref<any>()
const latest = ref<any>()
const notices = ref<any[]>([])
const wall = ref<any[]>([])
const format = (t: number) => (t ? new Date(t).toLocaleDateString() : '')
const stepLabel = (step: any) => {
  const map: Record<string, string> = {
    pending: '待审',
    passed: '已通过',
    rejected: '驳回',
    skipped: '已跳过',
    waiting: '等待',
    supplement: '待补充'
  }
  return map[step.state] || step.opinion || step.state || ''
}
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
  if (latest.value?.id) {
    try { latest.value = await GenealogyScholarshipApi.get(latest.value.id) } catch {}
  }
  notices.value = (await GenealogyFeedApi.frontPage({ pageNo: 1, pageSize: 10, type: 3, status: 1 })).list
  try { wall.value = (await GenealogyShowcaseApi.scholarshipWall()) || [] } catch {}
})
</script>
<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.title { font-size: 22px; font-weight: 700; }
.sub { font-size: 13px; color: #8b8273; margin-top: 4px; }
.band { display: grid; grid-template-columns: 1.15fr 0.85fr; gap: 16px; align-items: stretch; }
.panel {
  background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px;
  padding: 16px 18px; display: flex; flex-direction: column; min-height: 200px;
}
.panel-h { font-weight: 700; margin-bottom: 12px; }
.policy { white-space: pre-wrap; line-height: 1.75; color: #5c5348; font-size: 14px; }
.progress-no { font-weight: 700; margin-bottom: 8px; }
.steps { margin-top: 12px; display: flex; flex-direction: column; gap: 0; }
.step {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  padding: 8px 0; border-bottom: 1px solid #f0e8d8; font-size: 13px;
}
.step:last-child { border-bottom: 0; }
.step-name { color: #2a251f; }
.step-state { color: #8b8273; flex-shrink: 0; }
.step.passed .step-state, .step.disbursed .step-state { color: #2f7d4a; }
.step.skipped .step-state { color: #b3aa9c; }
.step.rejected .step-state { color: #a63d2f; }
.quiet {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: #8b8273; font-size: 13px; background: #f8f3e8; border-radius: 8px; min-height: 88px;
}
.wall { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.wall-item { background: #f8f3e8; border-radius: 8px; padding: 10px 12px; display: grid; gap: 4px; }
.wall-item b { font-size: 14px; display: flex; align-items: center; gap: 6px; }
.wall-item i { font-style: normal; color: #8b8273; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.list { display: flex; flex-direction: column; }
.row { padding: 10px 0; border-bottom: 1px solid #f0e8d8; }
.row:last-child { border-bottom: 0; }
.row b { display: block; font-size: 14px; }
.row i {
  margin-top: 4px;
  font-style: normal;
  color: #8b8273;
  font-size: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
@media (max-width: 960px) {
  .band, .wall { grid-template-columns: 1fr; }
}
</style>
