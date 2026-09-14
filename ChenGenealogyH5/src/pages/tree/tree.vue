<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看族谱需登录" desc="登录后可浏览谱系与成员档案。也可以先逛逛首页、寻根问祖和 AI 寻宗。" />
    <template v-else>
      <view class="search card">
        <input v-model="kw" placeholder="搜索成员姓名" confirm-type="search" />
      </view>
      <view v-if="kw">
        <view class="card member" v-for="m in filtered" :key="m.id" @click="openCard(m)">
          {{ m.name }} · {{ formatMemberGeneration(m) }}
        </view>
        <view v-if="!filtered.length" class="muted empty">未找到成员</view>
      </view>
      <view v-else>
        <view v-for="gen in grouped" :key="gen.no" class="gen">
          <view class="muted gen-title">{{ gen.no }}世{{ gen.words ? ' · ' + gen.words : '' }}</view>
          <view class="wrap">
            <view class="node card" v-for="m in gen.nodes" :key="m.id" @click="openCard(m)">
              <view class="name">{{ m.name }}</view>
              <view class="muted">{{ formatGenerationWord(m) ? formatGenerationWord(m) + ' · ' : '' }}{{ formatLifeSpan(m) }}</view>
            </view>
          </view>
        </view>
        <view v-if="!list.length" class="muted empty">{{ loading ? '加载中…' : '族谱录入中' }}</view>
      </view>
    </template>
    <view v-if="cardVisible" class="mask" @click="cardVisible = false">
      <view class="card-popup" @click.stop>
        <view class="card-title">{{ card?.name }}</view>
        <view class="muted">{{ formatMemberGeneration(card) || '—' }} · {{ card?.gender === 2 ? '女' : '男' }}</view>
        <view class="muted mt">生卒：{{ formatLifeSpan(card) }}</view>
        <view class="muted mt" v-if="card?.spouseNames?.length">配偶：{{ card.spouseNames.join('、') }}</view>
        <view class="intro mt" v-if="card?.intro">{{ card.intro }}</view>
        <view class="muted mt" v-else>暂无简介</view>
        <button class="btn" @click="openDetail">查看详情</button>
      </view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { formatGenerationWord, formatLifeSpan, formatMemberGeneration, isSpouseOnlyMember } from '@/utils'

const logged = ref(isLoggedIn())
const loading = ref(false)
const list = ref<any[]>([])
const all = ref<any[]>([])
const kw = ref('')
const cardVisible = ref(false)
const card = ref<any>()
const grouped = computed(() => {
  const map = new Map<number, any>()
  list.value
    .filter((m) => !isSpouseOnlyMember(m) && Number(m.generationNo) > 0)
    .forEach((m) => {
    const no = Number(m.generationNo)
    if (!map.has(no)) map.set(no, { no, words: new Set<string>(), nodes: [] as any[] })
    const row = map.get(no)
    row.nodes.push(m)
    const label = formatGenerationWord(m)
    if (label) row.words.add(label)
  })
  return [...map.values()]
    .sort((a, b) => a.no - b.no)
    .map((row) => ({ no: row.no, words: [...row.words].join('、') + (row.words.size ? '字辈' : ''), nodes: row.nodes }))
})
const filtered = computed(() => all.value.filter((m) => m.name?.includes(kw.value.trim())))
const openCard = async (m: any) => {
  cardVisible.value = true
  card.value = m
  try {
    card.value = await GenealogyMemberApi.get(m.id)
  } catch {
    card.value = m
  }
}
const openDetail = () => {
  if (!card.value?.id) return
  cardVisible.value = false
  uni.navigateTo({ url: '/pages/member/member?id=' + card.value.id })
}

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
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 20;
  display: flex;
  align-items: flex-end;
}
.card-popup {
  width: 100%;
  background: #fffdf7;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx 28rpx 48rpx;
  box-sizing: border-box;
}
.mt { margin-top: 12rpx; }
.intro {
  margin-top: 16rpx;
  line-height: 1.7;
  color: #5c5348;
  max-height: 280rpx;
  overflow: auto;
}
.btn {
  margin-top: 24rpx;
  background: #a63d2f;
  color: #fff;
  border-radius: 12rpx;
}
</style>
