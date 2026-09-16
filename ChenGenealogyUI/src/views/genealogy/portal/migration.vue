<template>
  <div>
    <div class="text-22px font-bold mb-16px">姓氏源流与迁徙路线</div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="迁徙时间轴">
          <el-timeline>
            <el-timeline-item
              v-for="n in nodes"
              :key="n.id"
              :timestamp="n.nodeTime"
              placement="top"
              :class="{ active: activeId === n.id }"
            >
              <div class="font-bold cursor-pointer" @click="focusNode(n)">{{ n.eventTitle }}</div>
              <div>{{ n.place }} {{ n.person ? '· ' + n.person : '' }}</div>
              <div class="text-gray-500">{{ n.description }}</div>
              <el-tag v-if="!hasCoord(n)" type="info" size="small">信息待补充</el-tag>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="迁徙地图">
          <AmapView ref="mapRef" :points="mapPoints" show-line height="420px" @select="onMapSelect" />
          <div class="text-12px text-gray-500 mt-8px">圆点为迁徙节点，连线按时间先后画出路线，点击标记可查看详情。</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px" header="迁徙故事">
      <el-row :gutter="16">
        <el-col :span="8" v-for="n in nodes" :key="n.id" class="mb-16px">
          <el-card
            shadow="hover"
            class="story"
            :id="'story-' + n.id"
            :class="{ active: activeId === n.id }"
            @click="focusNode(n)"
          >
            <div class="text-12px text-gray-500">{{ n.nodeTime }}</div>
            <div class="text-16px font-bold mt-6px">{{ n.eventTitle }}</div>
            <div class="mt-8px">{{ n.place }} {{ n.person ? '· ' + n.person : '' }}</div>
            <div class="text-13px text-gray-500 mt-8px story-desc">{{ n.description }}</div>
            <el-tag v-if="hasCoord(n)" size="small" class="mt-8px">高德可导航</el-tag>
            <el-tag v-else type="info" size="small" class="mt-8px">信息待补充</el-tag>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    <el-card class="mt-16px" header="姓氏源流"><div v-html="family.originContent" /></el-card>
  </div>
</template>
<script setup lang="ts">
import AmapView from '@/views/genealogy/components/AmapView.vue'
import { GenealogyContentApi } from '@/api/genealogy'
import type { AmapPoint } from '@/utils/amap'

defineOptions({ name: 'PortalMigration' })

const nodes = ref<any[]>([])
const family = ref<any>({})
const mapRef = ref<any>()
const activeId = ref<number | string>()

const hasCoord = (n: any) => n?.longitude != null && n?.latitude != null && n.longitude !== '' && n.latitude !== ''

const mapPoints = computed<AmapPoint[]>(() =>
  nodes.value.filter(hasCoord).map((n) => ({
    id: n.id,
    lng: Number(n.longitude),
    lat: Number(n.latitude),
    title: n.eventTitle || n.place,
    content: [n.nodeTime, n.place, n.person, n.description].filter(Boolean).join('\n')
  }))
)
const focusNode = (n: any) => {
  activeId.value = n.id
  if (hasCoord(n)) mapRef.value?.focus?.(n.id)
}
const onMapSelect = (p: AmapPoint) => {
  activeId.value = p.id
  document.getElementById('story-' + p.id)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
}

onMounted(async () => {
  family.value = await GenealogyContentApi.getFamily()
  nodes.value = await GenealogyContentApi.migrationList()
})
</script>
<style scoped>
.story { min-height: 180px; cursor: pointer; }
.story.active { border-color: #a63d2f; box-shadow: 0 0 0 1px #a63d2f; }
.story-desc { display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; overflow: hidden; }
</style>
