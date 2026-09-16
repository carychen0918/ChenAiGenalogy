<template>
  <div>
    <div class="text-22px font-bold mb-16px">{{ tomb?.name || '温蒂坟地' }}导航</div>
    <el-alert v-if="!canNav" type="warning" :closable="false" class="mb-16px" title="地点信息待管理员配置" />
    <el-card>
      <p>详细地址：{{ tomb?.address || '—' }}</p>
      <p class="mt-8px">停车场：{{ tomb?.parking || '待补充' }}</p>
      <p class="mt-8px">入口：{{ tomb?.entrance || '待补充' }}</p>
      <p class="mt-8px">公共厕所：{{ tomb?.toilet || '待补充' }}</p>
      <el-space class="mt-16px" wrap>
        <el-button type="primary" :disabled="!canNav" @click="openMap">高德导航前往</el-button>
        <el-button :disabled="!tomb?.address" @click="copy">复制地址</el-button>
      </el-space>
      <AmapView v-if="canNav" class="mt-16px" :points="points" height="420px" />
      <el-divider />
      <el-form inline>
        <el-form-item label="起点地址（未授权定位时）">
          <el-input v-model="from" placeholder="例如：温州市鹿城区" class="!w-280px" />
        </el-form-item>
        <el-button :disabled="!canNav || !from" @click="openMapFrom">按起点规划</el-button>
      </el-form>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import AmapView from '@/views/genealogy/components/AmapView.vue'
import { GenealogyContentApi } from '@/api/genealogy'
import { amapNavUrl, openAmapNav, type AmapPoint } from '@/utils/amap'

defineOptions({ name: 'PortalNav' })
const message = useMessage()
const tomb = ref<any>()
const from = ref('')
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const points = computed<AmapPoint[]>(() => {
  if (!canNav.value) return []
  return [{
    lng: Number(tomb.value.longitude),
    lat: Number(tomb.value.latitude),
    title: tomb.value.name || '温蒂坟地',
    content: [tomb.value.address, tomb.value.parking && ('停车场：' + tomb.value.parking)].filter(Boolean).join('\n')
  }]
})
const openMap = () => {
  openAmapNav(tomb.value.longitude, tomb.value.latitude, tomb.value.name || '温蒂坟地')
}
const openMapFrom = () => {
  const url = `${amapNavUrl(Number(tomb.value.longitude), Number(tomb.value.latitude), tomb.value.name || '温蒂坟地')}&from=${encodeURIComponent(from.value)}`
  window.open(url)
}
const copy = async () => {
  await navigator.clipboard.writeText(tomb.value.address)
  message.success('地址已复制，可粘贴到高德地图搜索')
}
onMounted(async () => { tomb.value = await GenealogyContentApi.getTomb() })
</script>
