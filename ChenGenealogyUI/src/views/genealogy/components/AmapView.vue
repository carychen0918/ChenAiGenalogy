<template>
  <div v-if="!points.length" class="amap-empty">暂无带经纬度的地点</div>
  <div v-else ref="boxRef" class="amap" :style="{ height }" />
</template>
<script setup lang="ts">
import { loadAmap, escapeHtml, chinaMapOptions, limitToChina, type AmapPoint } from '@/utils/amap'

defineOptions({ name: 'AmapView' })

const props = withDefaults(
  defineProps<{
    points?: AmapPoint[]
    showLine?: boolean
    height?: string
  }>(),
  {
    points: () => [],
    showLine: false,
    height: '420px'
  }
)

const emit = defineEmits<{ select: [point: AmapPoint, index: number] }>()

const boxRef = ref<HTMLElement>()
let map: any
let seq = 0
let markers: any[] = []

const render = async () => {
  const my = ++seq
  const pts = props.points.filter(
    (p) => Number.isFinite(p.lng) && Number.isFinite(p.lat)
  )
  if (!pts.length) {
    map?.destroy?.()
    map = undefined
    return
  }
  const AMap = await loadAmap()
  if (my !== seq) return
  await nextTick()
  if (!boxRef.value || my !== seq) return
  map?.destroy?.()
  map = new AMap.Map(boxRef.value, {
    ...chinaMapOptions(),
    zoom: pts.length === 1 ? 14 : 4,
    center: pts.length === 1 ? [pts[0].lng, pts[0].lat] : chinaMapOptions().center
  })
  limitToChina(AMap, map)
  AMap.plugin(['AMap.ToolBar', 'AMap.Scale'], () => {
    if (map) {
      map.addControl(new AMap.ToolBar({ position: 'RT' }))
      map.addControl(new AMap.Scale())
    }
  })
  const overlays: any[] = []
  markers = []
  pts.forEach((p, idx) => {
    const marker = new AMap.Marker({
      position: [p.lng, p.lat],
      title: p.title || '',
      anchor: 'bottom-center'
    })
    const info =
      p.title || p.content
        ? new AMap.InfoWindow({
            offset: new AMap.Pixel(0, -36),
            content: `<div style="min-width:160px;max-width:240px;line-height:1.6">
          <div style="font-weight:700;color:#2a251f">${escapeHtml(p.title)}</div>
          <div style="color:#5c5348;margin-top:4px;white-space:pre-wrap">${escapeHtml(p.content)}</div>
        </div>`
          })
        : null
    marker.on('click', () => {
      emit('select', p, idx)
      if (info) info.open(map, marker.getPosition())
    })
    markers.push({ marker, info, point: p })
    overlays.push(marker)
  })
  if (props.showLine && pts.length > 1) {
    overlays.push(
      new AMap.Polyline({
        path: pts.map((p) => [p.lng, p.lat]),
        strokeColor: '#a63d2f',
        strokeWeight: 4,
        strokeOpacity: 0.85,
        showDir: true,
        lineJoin: 'round'
      })
    )
  }
  map.add(overlays)
  if (pts.length > 1) map.setFitView(overlays, false, [40, 40, 40, 40], 12)
  limitToChina(AMap, map)
}

watch(
  () => [props.points, props.showLine, props.height],
  () => render(),
  { deep: true }
)
const focus = (id?: number | string) => {
  const hit = markers.find((m) => String(m.point?.id) === String(id))
  if (!hit || !map) return
  map.setZoomAndCenter(12, hit.marker.getPosition())
  hit.info?.open(map, hit.marker.getPosition())
}

defineExpose({ focus })

onMounted(render)
onUnmounted(() => {
  seq += 1
  map?.destroy?.()
  map = undefined
})
</script>
<style scoped>
.amap { width: 100%; border-radius: 10px; overflow: hidden; border: 1px solid #e8dfcc; }
.amap-empty { color: #8b8273; font-size: 13px; padding: 48px 0; text-align: center; }
</style>
