<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看族谱需登录" desc="登录后可浏览谱系与成员档案。也可以先逛逛首页、寻根问祖和 AI 寻宗。" />
    <template v-else>
      <view class="search card">
        <input v-model="kw" placeholder="搜索成员姓名" confirm-type="search" />
      </view>
      <view v-if="kw">
        <view class="card member" v-for="m in filtered" :key="m.id" @click="open(m.id)">
          {{ m.name }} · {{ m.generationNo }}世{{ m.generationWord ? ' · ' + m.generationWord + '字辈' : '' }}
        </view>
        <view v-if="!filtered.length" class="muted empty">未找到成员</view>
      </view>
      <view v-else>
        <view v-for="gen in grouped" :key="gen.no" class="gen">
          <view class="muted gen-title">{{ gen.no }}世{{ gen.words ? ' · ' + gen.words : '' }}</view>
          <view class="wrap">
            <view class="node card" v-for="m in gen.nodes" :key="m.id" @click="open(m.id)">
              <view class="name">{{ m.name }}</view>
              <view class="muted">{{ life(m) }}</view>
            </view>
          </view>
        </view>
        <view v-if="!list.length" class="muted empty">{{ loading ? '加载中…' : '族谱录入中' }}</view>
      </view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'

const logged = ref(isLoggedIn())
const loading = ref(false)
const list = ref<any[]>([])
const all = ref<any[]>([])
const kw = ref('')

const life = (m: any) => {
  const birth = m.birthDate ? String(m.birthDate).slice(0, 4) : '?'
  const death = m.alive === false || m.alive === 0 ? (m.deathDate ? String(m.deathDate).slice(0, 4) : '?') : '今'
  return `${birth} — ${death}`
}
const grouped = computed(() => {
  const map = new Map<number, any>()
  list.value.forEach((m) => {
    const no = m.generationNo || 0
    if (!map.has(no)) map.set(no, { no, words: new Set<string>(), nodes: [] as any[] })
    const row = map.get(no)
    row.nodes.push(m)
    if (m.generationWord) row.words.add(m.generationWord)
  })
  return [...map.values()]
    .sort((a, b) => a.no - b.no)
    .map((row) => ({ no: row.no, words: [...row.words].join('、') + (row.words.size ? '字辈' : ''), nodes: row.nodes }))
})
const filtered = computed(() => all.value.filter((m) => m.name?.includes(kw.value.trim())))
const open = (id: number) => uni.navigateTo({ url: '/pages/member/member?id=' + id })

onShow(async () => {
  logged.value = isLoggedIn()
  if (!logged.value) {
    list.value = []
    all.value = []
    return
  }
  loading.value = true
  try {
    list.value = (await GenealogyMemberApi.tree()) || []
    all.value = (await GenealogyMemberApi.simpleList()) || list.value
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
})
</script>
<style scoped>
.search input { background: #f4ece0; border-radius: 12rpx; padding: 18rpx 20rpx; }
.member { margin-bottom: 16rpx; }
.gen { margin-bottom: 16rpx; }
.gen-title { margin: 8rpx 8rpx 12rpx; }
.wrap { display: flex; flex-wrap: wrap; gap: 16rpx; }
.node { width: calc(50% - 8rpx); margin-bottom: 0; box-sizing: border-box; }
.name { font-weight: 700; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
