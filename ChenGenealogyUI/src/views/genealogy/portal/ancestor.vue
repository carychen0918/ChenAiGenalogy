<template>
  <div>
    <div class="text-22px font-bold mb-16px">清明祭祖</div>
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="12">
        <el-card>
          <div class="font-bold text-16px">{{ tomb?.name || '温蒂坟地' }}</div>
          <div class="text-13px text-gray-500 mt-8px">{{ tomb?.address || '地点信息待管理员配置' }}</div>
          <el-button class="mt-12px" type="primary" :disabled="!canNav" @click="$router.push('/portal/nav')">
            {{ canNav ? '导航前往' : '地点信息待管理员配置' }}
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="祭祖文化指南">
          <div v-for="c in cultures" :key="c.id" class="py-8px border-b cursor-pointer" @click="openGuide(c)">
            <div class="font-bold">{{ c.title }}</div>
            <div class="text-12px text-gray-500">{{ (c.content || '').replace(/<[^>]+>/g, '').slice(0, 40) }}</div>
          </div>
          <el-empty v-if="!cultures.length" description="指南待补充" />
        </el-card>
      </el-col>
    </el-row>
    <el-card header="祭祖活动">
      <div v-for="a in activities" :key="a.id" class="py-14px border-b flex justify-between items-center">
        <div>
          <div class="font-bold">{{ a.title }}</div>
          <div class="text-13px text-gray-500 mt-4px">{{ a.place }} · {{ format(a.startTime) }}</div>
        </div>
        <el-button type="primary" @click="$router.push('/portal/activity?id=' + a.id)">查看详情</el-button>
      </div>
      <el-empty v-if="!activities.length" description="暂无祭祖活动" />
    </el-card>
    <el-card class="mt-16px" header="祭扫记录">
      <div v-for="w in worships" :key="w.id" class="py-12px border-b">
        <el-tag v-if="w.pinned" type="danger" size="small">精选</el-tag>
        <el-tag v-if="w.online" type="info" size="small" class="ml-4px">线上</el-tag>
        <span class="ml-8px font-bold">{{ w.userName }}</span>
        <span v-if="w.ancestorName" class="text-gray-500"> · 致 {{ w.ancestorName }}</span>
        <div class="mt-6px">{{ w.content }}</div>
        <el-image v-for="(img, i) in w.images || []" :key="i" :src="img" class="w-72px h-72px mr-8px mt-8px" :preview-src-list="w.images" />
      </div>
      <el-empty v-if="!worships.length" description="暂无祭扫记录" />
    </el-card>
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
