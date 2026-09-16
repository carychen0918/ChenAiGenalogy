<template>
  <div class="page">
    <div class="title">清明祭祖</div>
    <section class="band">
      <article class="panel">
        <div class="panel-h">祭祖地点</div>
        <div class="tomb-name">{{ tomb?.name || '温蒂坟地' }}</div>
        <div class="sub">{{ tomb?.address || '地点信息待管理员配置' }}</div>
        <div class="cta">
          <el-button type="primary" :disabled="!canNav" @click="$router.push('/portal/nav')">
            {{ canNav ? '导航前往' : '地点信息待管理员配置' }}
          </el-button>
        </div>
      </article>
      <article class="panel">
        <div class="panel-h">祭祖文化指南</div>
        <div v-if="cultures.length" class="list">
          <button v-for="c in cultures" :key="c.id" class="row" type="button" @click="openGuide(c)">
            <b>{{ c.title }}</b>
            <i>{{ (c.content || '').replace(/<[^>]+>/g, '').slice(0, 48) }}</i>
          </button>
        </div>
        <div v-else class="quiet">指南待补充</div>
      </article>
    </section>

    <section class="panel">
      <div class="panel-h">祭祖活动</div>
      <div v-if="activities.length" class="list">
        <div v-for="a in activities" :key="a.id" class="act">
          <div>
            <div class="act-title">{{ a.title }}</div>
            <div class="sub">{{ a.place }} · {{ format(a.startTime) }}</div>
          </div>
          <el-button type="primary" @click="$router.push('/portal/activity?id=' + a.id)">查看详情</el-button>
        </div>
      </div>
      <div v-else class="quiet sm">暂无祭祖活动</div>
    </section>

    <section class="panel">
      <div class="panel-h">祭扫记录</div>
      <div v-if="worships.length" class="list">
        <div v-for="w in worships" :key="w.id" class="worship">
          <div class="worship-head">
            <el-tag v-if="w.pinned" type="danger" size="small">精选</el-tag>
            <el-tag v-if="w.online" type="info" size="small">线上</el-tag>
            <span class="name">{{ w.userName }}</span>
            <span v-if="w.ancestorName" class="sub">致 {{ w.ancestorName }}</span>
          </div>
          <div class="worship-body">{{ w.content }}</div>
          <div v-if="(w.images || []).length" class="thumbs">
            <el-image v-for="(img, i) in w.images" :key="i" :src="img" fit="cover" :preview-src-list="w.images" :initial-index="i" />
          </div>
        </div>
      </div>
      <div v-else class="quiet sm">暂无祭扫记录</div>
    </section>
  </div>
</template>
<script setup lang="ts">
import { GenealogyActivityApi, GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'PortalAncestor' })
const tomb = ref<any>()
const cultures = ref<any[]>([])
const activities = ref<any[]>([])
const worships = ref<any[]>([])
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const format = (t: number) => (t ? new Date(t).toLocaleString() : '')
const openGuide = (c: any) => ElMessageBox.alert(c.content || '暂无内容', c.title, { dangerouslyUseHTMLString: true })
onMounted(async () => {
  tomb.value = await GenealogyContentApi.getTomb()
  cultures.value = await GenealogyContentApi.cultureList()
  activities.value = (await GenealogyActivityApi.page({ pageNo: 1, pageSize: 20 })).list
  worships.value = await GenealogyActivityApi.worshipList()
})
</script>
<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.title { font-size: 22px; font-weight: 700; }
.band { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: stretch; }
.panel {
  background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px;
  padding: 16px 18px; display: flex; flex-direction: column; min-height: 160px;
}
.panel-h { font-weight: 700; margin-bottom: 12px; }
.tomb-name { font-size: 16px; font-weight: 700; }
.sub { color: #8b8273; font-size: 13px; margin-top: 6px; }
.cta { margin-top: auto; padding-top: 16px; }
.list { display: flex; flex-direction: column; }
.row {
  display: block; width: 100%; text-align: left; background: transparent; border: 0;
  border-bottom: 1px solid #f0e8d8; padding: 10px 0; cursor: pointer; color: inherit;
}
.row:last-child { border-bottom: 0; }
.row b { display: block; font-size: 14px; }
.row i { display: block; margin-top: 4px; font-style: normal; color: #8b8273; font-size: 12px; line-height: 1.6; }
.act {
  display: flex; justify-content: space-between; align-items: center; gap: 16px;
  padding: 12px 0; border-bottom: 1px solid #f0e8d8;
}
.act:last-child { border-bottom: 0; }
.act-title { font-weight: 700; }
.worship { padding: 12px 0; border-bottom: 1px solid #f0e8d8; }
.worship:last-child { border-bottom: 0; }
.worship-head { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.name { font-weight: 700; }
.worship-body { margin-top: 6px; color: #5c5348; line-height: 1.7; }
.thumbs { display: flex; gap: 8px; margin-top: 8px; }
.thumbs :deep(.el-image) { width: 72px; height: 72px; border-radius: 8px; overflow: hidden; background: #efe7d6; }
.quiet {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: #8b8273; font-size: 13px; background: #f8f3e8; border-radius: 8px; min-height: 88px;
}
.quiet.sm { min-height: 56px; }
@media (max-width: 860px) {
  .band { grid-template-columns: 1fr; }
}
</style>
