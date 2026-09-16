<template>
  <div>
    <div class="text-22px font-bold mb-8px">族影长廊</div>
    <div class="text-13px text-gray-500 mb-16px">汇聚成员头像与照片集，点击进入档案。不改动原图数据。</div>
    <div class="grid">
      <div v-for="(g, i) in list" :key="g.url + i" class="item" @click="go(g)">
        <el-image :src="g.url" fit="cover" class="img" :preview-src-list="urls" :initial-index="i" preview-teleported @click.stop />
        <div class="name">{{ g.memberName }}</div>
      </div>
    </div>
    <el-empty v-if="!list.length && !loading" description="暂无照片" />
  </div>
</template>
<script setup lang="ts">
import { GenealogyShowcaseApi } from '@/api/genealogy'
defineOptions({ name: 'PortalGallery' })
const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const urls = computed(() => list.value.map((x) => x.url))
const go = (g: any) => g.memberId && router.push('/portal/member?id=' + g.memberId)
onMounted(async () => {
  loading.value = true
  try { list.value = (await GenealogyShowcaseApi.gallery(80)) || [] } finally { loading.value = false }
})
</script>
<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 14px; }
.item { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px; overflow: hidden; cursor: pointer; }
.img { width: 100%; height: 140px; }
.name { padding: 8px 10px; font-size: 13px; }
</style>
