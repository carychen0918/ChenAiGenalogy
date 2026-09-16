<template>
  <view v-if="!validPoints.length" class="amap-empty">暂无带经纬度的地点</view>
  <view v-else>
    <!-- #ifdef H5 -->
    <view ref="hostRef" class="amap" :style="{ height: height }" />
    <!-- #endif -->
    <!-- #ifndef H5 -->
    <button class="btn-primary" @click="openFirst">打开高德地图</button>
    <!-- #endif -->
  </view>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { loadAmap, escapeHtml, amapNavUrl, chinaMapOptions, limitToChina, type AmapPoint } from '@/utils/amap'

const props = withDefaults(
  defineProps<{
    points?: AmapPoint[]
    showLine?: boolean
    height?: string
  }>(),
  {
    points: () => [],
    showLine: false,
    height: '360px'
  }
)

const emit = defineEmits<{ select: [point: AmapPoint, index: number] }>()

const hostRef = ref<any>()
const validPoints = computed(() =>
  (props.points || []).filter((p) => Number.isFinite(p.lng) && Number.isFinite(p.lat))
)

let map: any
let seq = 0
let markers: any[] = []

const openFirst = () => {
  const p = validPoints.value[0]
  if (!p) return
  const url = amapNavUrl(p.lng, p.lat, p.title)
  const g = globalThis as any
  if (g.plus?.runtime?.openURL) {
    g.plus.runtime.openURL(url)
    return
  }
  if (typeof window !== 'undefined' && window.open) {
    window.open(url)
    return
  }
  uni.openLocation({ longitude: p.lng, latitude: p.lat, name: p.title || '目的地' })
}

const hostEl = (): HTMLElement | null => {
  const raw = hostRef.value
  if (!raw) return null
  if (raw instanceof HTMLElement) return raw
  if (raw.$el instanceof HTMLElement) return raw.$el
  return null
}

const waitDiv = async (): Promise<HTMLDivElement | null> => {
  for (let i = 0; i < 25; i++) {
    await nextTick()
    const host = hostEl()
    if (host) {
      let div = host.querySelector(':scope > div.amap-el') as HTMLDivElement | null
      if (!div || div.nodeName !== 'DIV') {
        host.innerHTML = ''
        div = document.createElement('div')
        div.className = 'amap-el'
        div.style.cssText = 'width:100%;height:100%;min-height:200px;'
        host.appendChild(div)
      }
      if (div.clientWidth > 0 || host.clientWidth > 0) return div
    }
    await new Promise((r) => setTimeout(r, 50))
  }
  return null
}

const render = async () => {
  const my = ++seq
  const pts = validPoints.value
  if (!pts.length) {
    map?.destroy?.()
    map = undefined
    return
  }
  if (typeof document === 'undefined') return
  try {
    const AMap = await loadAmap()
    if (my !== seq) return
    const el = await waitDiv()
    if (!el || my !== seq) {
      if (!el) uni.showToast({ title: '地图容器未就绪', icon: 'none' })
      return
    }
    map?.destroy?.()
    map = new AMap.Map(el, {
      ...chinaMapOptions(),
      zoom: pts.length === 1 ? 14 : 4,
      center: pts.length === 1 ? [pts[0].lng, pts[0].lat] : chinaMapOptions().center
    })
    limitToChina(AMap, map)
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
              content: `<div style="min-width:140px;max-width:220px;line-height:1.6">
            <div style="font-weight:700">${escapeHtml(p.title)}</div>
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
          showDir: true
        })
      )
    }
    map.add(overlays)
    if (pts.length > 1) map.setFitView(overlays, false, [36, 36, 36, 36], 12)
    limitToChina(AMap, map)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '地图加载失败', icon: 'none' })
  }
}

watch(validPoints, () => render(), { deep: true })
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
.amap { width: 100%; position: relative; overflow: hidden; border-radius: 16rpx; border: 1px solid #e8dfcc; }
.amap-empty { color: #8b8273; font-size: 24rpx; text-align: center; padding: 48rpx 0; }
</style>
