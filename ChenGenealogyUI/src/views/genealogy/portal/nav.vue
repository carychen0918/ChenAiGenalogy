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
        <el-button type="primary" :disabled="!canNav" @click="openMap">导航前往</el-button>
        <el-button :disabled="!tomb?.address" @click="copy">复制地址到地图 APP</el-button>
      </el-space>
      <el-divider />
      <el-form inline>
        <el-form-item label="起点地址（未授权定位时）">
          <el-input v-model="from" placeholder="例如：温州市鹿城区" class="!w-280px" />
        </el-form-item>
        <el-button :disabled="!canNav || !from" @click="openMapFrom">按起点规划</el-button>
      </el-form>
      <div class="text-12px text-gray-500 mt-8px">地图 API 不可用时，请复制地址到高德/百度地图 APP 搜索。</div>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'PortalNav' })
const message = useMessage()
const tomb = ref<any>()
const from = ref('')
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const openMap = () => {
  const url = `https://uri.amap.com/navigation?to=${tomb.value.longitude},${tomb.value.latitude},${encodeURIComponent(tomb.value.name || '温蒂坟地')}&mode=car`
  window.open(url)
}
const openMapFrom = () => {
  const url = `https://uri.amap.com/navigation?from=${encodeURIComponent(from.value)}&to=${tomb.value.longitude},${tomb.value.latitude},${encodeURIComponent(tomb.value.name || '温蒂坟地')}&mode=car`
  window.open(url)
}
const copy = async () => {
  await navigator.clipboard.writeText(tomb.value.address)
  message.success('地址已复制，请打开地图 APP 粘贴搜索')
}
onMounted(async () => { tomb.value = await GenealogyContentApi.getTomb() })
</script>
