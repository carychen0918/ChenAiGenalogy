<template>
  <div>
    <div class="text-22px font-bold mb-16px">姓氏源流与迁徙路线</div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="迁徙时间轴">
          <el-timeline>
            <el-timeline-item v-for="n in nodes" :key="n.id" :timestamp="n.nodeTime" placement="top">
              <div class="font-bold">{{ n.eventTitle }}</div>
              <div>{{ n.place }} {{ n.person ? '· ' + n.person : '' }}</div>
              <div class="text-gray-500">{{ n.description }}</div>
              <el-tag v-if="!hasCoord(n)" type="info" size="small">信息待补充</el-tag>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="迁徙地图">
          <Echart v-if="mapPoints.length" :height="420" :options="mapOption" />
          <el-empty v-else description="暂无带经纬度的迁徙节点，请在管理端补充坐标" />
          <div class="text-12px text-gray-500 mt-8px">圆点为迁徙节点，连线按时间先后画出路线，点击可查看详情。</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px" header="姓氏源流"><div v-html="family.originContent" /></el-card>
  </div>
</template>
<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import china from '@/assets/map/json/china.json'
import echarts from '@/plugins/echarts'
import { GenealogyContentApi } from '@/api/genealogy'

defineOptions({ name: 'PortalMigration' })

echarts.registerMap('china', china as any)

const nodes = ref<any[]>([])
const family = ref<any>({})

const hasCoord = (n: any) => n?.longitude != null && n?.latitude != null && n.longitude !== '' && n.latitude !== ''

const mapPoints = computed(() =>
  nodes.value
    .filter(hasCoord)
    .map((n) => ({
      name: n.place,
      value: [Number(n.longitude), Number(n.latitude)],
      nodeTime: n.nodeTime,
      eventTitle: n.eventTitle,
      person: n.person,
      description: n.description
    }))
)

const mapLines = computed(() => {
  const pts = mapPoints.value
  const lines: any[] = []
  for (let i = 0; i < pts.length - 1; i++) {
    lines.push({
      fromName: pts[i].name,
      toName: pts[i + 1].name,
      coords: [pts[i].value, pts[i + 1].value]
    })
  }
  return lines
})

const mapOption = computed<EChartsOption>(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'item',
    formatter: (p: any) => {
      const d = p.data
      if (p.seriesType === 'lines') {
        return `${d.fromName} → ${d.toName}`
      }
      if (!d) return p.name
      return [
        `<div style="font-weight:600">${d.eventTitle || d.name}</div>`,
        d.nodeTime ? `<div>${d.nodeTime}</div>` : '',
        d.name ? `<div>${d.name}${d.person ? ' · ' + d.person : ''}</div>` : '',
        d.description ? `<div style="color:#8b8273;max-width:240px;white-space:normal">${d.description}</div>` : ''
      ]
        .filter(Boolean)
        .join('')
    }
  },
  geo: {
    map: 'china',
    roam: true,
    zoom: 1.15,
    itemStyle: {
      areaColor: '#f4ece0',
      borderColor: '#d9cbb3'
    },
    emphasis: {
      itemStyle: {
        areaColor: '#f4e3de'
      },
      label: { color: '#5c554a' }
    }
  },
  series: [
    {
      type: 'lines',
      coordinateSystem: 'geo',
      zlevel: 2,
      effect: {
        show: true,
        period: 5,
        trailLength: 0.35,
        color: '#a63d2f',
        symbolSize: 4
      },
      lineStyle: {
        color: '#a63d2f',
        width: 1.6,
        opacity: 0.75,
        curveness: 0.2
      },
      data: mapLines.value
    },
    {
      type: 'effectScatter',
      coordinateSystem: 'geo',
      zlevel: 3,
      rippleEffect: { brushType: 'stroke', scale: 3 },
      symbolSize: 12,
      itemStyle: { color: '#a63d2f' },
      label: {
        show: true,
        formatter: '{b}',
        position: 'right',
        color: '#2a251f',
        fontSize: 11
      },
      data: mapPoints.value
    }
  ]
}))

onMounted(async () => {
  family.value = await GenealogyContentApi.getFamily()
  nodes.value = await GenealogyContentApi.migrationList()
})
</script>
