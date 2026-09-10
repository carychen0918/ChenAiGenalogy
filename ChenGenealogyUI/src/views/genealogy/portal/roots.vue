<template>
  <div>
    <div class="text-22px font-bold mb-16px">寻根问祖</div>
    <el-row :gutter="16">
      <el-col :span="12"><el-card class="cursor-pointer" @click="$router.push('/portal/migration')"><h3>姓氏源流与迁徙</h3><p class="text-gray-500">始祖溯源 · 迁徙地图 · 时间轴</p></el-card></el-col>
      <el-col :span="12"><el-card class="cursor-pointer" @click="$router.push('/portal/generation')"><h3>字辈 · 派语</h3><p class="text-gray-500">世代排行 · 新生儿取名推荐</p></el-card></el-col>
      <el-col :span="12" class="mt-16px"><el-card class="cursor-pointer" @click="$router.push('/portal/ai-match')"><h3>AI 寻宗问祖</h3><p class="text-gray-500">对话分析是否本支同族 · 匹配地区管理员</p></el-card></el-col>
      <el-col :span="12" class="mt-16px"><el-card class="cursor-pointer" @click="$router.push('/portal/achievements')"><h3>祖先事迹</h3><p class="text-gray-500">先贤生平 · 家族记忆</p></el-card></el-col>
    </el-row>
    <el-card class="mt-16px" header="姓氏源流简介"><p>{{ family.intro }}</p></el-card>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'PortalRoots' })
const family = ref<any>({})
onMounted(async () => { family.value = await GenealogyContentApi.getFamily() })
</script>
