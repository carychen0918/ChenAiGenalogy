<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看族谱需登录" desc="登录后可浏览谱系与成员档案。也可以先逛逛首页、寻根问祖和 AI 寻宗。" />
    <template v-else>
      <view class="search card">
        <input v-model="kw" placeholder="搜索姓名 / 几世 / 字辈" confirm-type="search" @confirm="globalSearch" />
        <view class="acts" v-if="meId">
          <button class="mini" @click="locateMe">定位我</button>
        </view>
      </view>
      <view class="card seek">
        <view class="seek-head">
          <text class="seek-title">寻根</text>
          <text class="muted">同名会显示几世、父亲、生卒</text>
        </view>
        <input v-model="seekKw" placeholder="输入姓名选择成员" confirm-type="search" />
        <view v-if="seekHits.length" class="seek-hits">
          <view
            v-for="m in seekHits"
            :key="m.id"
            class="seek-hit"
            :class="{ on: Number(seekId) === Number(m.id) }"
            @click="pickSeek(m)"
          >
            {{ lineagePickLabel(m, memberById) }}
          </view>
        </view>
        <view v-else-if="seekKw.trim()" class="muted empty-hit">未找到匹配成员</view>
        <view v-if="pickedSeek" class="picked">已选：{{ lineagePickLabel(pickedSeek, memberById) }}</view>
        <view class="acts">
          <button class="mini" :disabled="!seekId" @click="seekRoot()">寻根</button>
          <button v-if="lineageIds.length" class="mini ghost" @click="clearLineage">清除高亮</button>
        </view>
      </view>
      <view v-if="lineagePath.length" class="card path lineage">
        <view class="muted">直系寻根</view>
        <view class="chain">
          <view
            v-for="(m, i) in lineagePath"
            :key="m.id"
            class="chain-item"
            :class="{ now: Number(m.id) === Number(lineageNowId) }"
            @click="scrollToMember(m.id)"
          >
            <view v-if="i" class="chain-line" />
            <text>{{ m.generationNo ? m.generationNo + '世 · ' : '' }}{{ m.name }}</text>
          </view>
        </view>
      </view>
      <view v-if="path.length" class="card path">
        <view class="muted">关系路径</view>
        <view class="hops">
          <text v-for="(h, i) in path" :key="h.memberId" class="hop" @click="goMember(h.memberId)">{{ i ? h.edge + ' · ' : '' }}{{ h.name }}</text>
        </view>
      </view>
      <view v-if="kw">
        <view class="card member" v-for="m in filtered" :key="m.id" @click="openCard(m)">
          {{ lineagePickLabel(m, memberById) }}
        </view>
        <view v-if="!filtered.length" class="muted empty">未找到成员</view>
      </view>
      <view v-else>
        <view v-for="gen in grouped" :key="gen.no" class="gen">
          <view class="muted gen-title">{{ gen.no }}世{{ gen.words ? ' · ' + gen.words : '' }}</view>
          <view class="wrap">
            <view
              class="node card"
              :id="'node-' + m.id"
              :class="{
                me: meId && Number(m.id) === Number(meId),
                lineage: isLineage(m.id),
                'lineage-self': Number(m.id) === Number(lineageSelfId),
                'lineage-now': Number(m.id) === Number(lineageNowId)
              }"
              v-for="m in gen.nodes"
              :key="m.id"
              @click="openCard(m)"
            >
              <image v-if="m.avatar" class="node-avatar" :src="m.avatar" mode="aspectFill" />
              <view v-else class="node-fallback">{{ m.name?.[0] }}</view>
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
        <view class="thick-head">
          <image v-if="card?.avatar || card?.photoUrls?.[0]" class="thick-avatar" :src="card.avatar || card.photoUrls[0]" mode="aspectFill" />
          <view v-else class="thick-fallback">{{ card?.name?.[0] }}</view>
          <view>
            <view class="card-title">{{ card?.name }}</view>
            <view class="muted">{{ formatMemberGeneration(card) || '—' }} · {{ card?.gender === 2 ? '女' : '男' }}</view>
            <view class="muted mt">生卒：{{ formatLifeSpan(card) }}</view>
            <view class="muted mt" v-if="card?.spouseNames?.length">配偶：{{ card.spouseNames.join('、') }}</view>
          </view>
        </view>
        <view class="intro mt" v-if="card?.intro">{{ card.intro }}</view>
        <view class="muted mt" v-else>暂无简介</view>
        <view v-if="(card?.photoUrls || []).length" class="photos">
          <image v-for="(p, i) in card.photoUrls.slice(0, 6)" :key="p + i" :src="p" class="photo" mode="aspectFill" @click="previewCard(i)" />
        </view>
        <button class="btn" @click="openDetail">查看详情</button>
        <button class="btn ghost" v-if="card?.id" @click="seekFromCard">从此人寻根</button>
        <button class="btn ghost" v-if="meId && card?.id && Number(card.id) !== Number(meId)" @click="showRelation">查看与我的关系</button>
      </view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import {
  formatGenerationWord,
  formatLifeSpan,
  formatMemberGeneration,
  isSpouseOnlyMember,
  lineagePickLabel,
  paternalLineage
} from '@/utils'

const logged = ref(isLoggedIn())
const loading = ref(false)
const list = ref<any[]>([])
const all = ref<any[]>([])
const kw = ref('')
const hits = ref<any[]>([])
const meId = ref<number>()
const path = ref<any[]>([])
const cardVisible = ref(false)
const card = ref<any>()
const seekKw = ref('')
const seekId = ref<number>()
const lineageIds = ref<number[]>([])
const lineageNowId = ref<number | null>(null)
const lineageSelfId = ref<number | null>(null)
const lineagePath = ref<any[]>([])
let seekSeq = 0
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const memberById = computed(() => {
  const map = new Map<number, any>()
  ;[...list.value, ...all.value].forEach((m) => map.set(Number(m.id), m))
  return map
})
const seekPool = computed(() => (list.value.length ? list.value : all.value).filter((m) => m?.id && m.name && !isSpouseOnlyMember(m)))
const seekHits = computed(() => {
  const q = seekKw.value.trim()
  if (!q) return []
  return seekPool.value.filter((m) => lineagePickLabel(m, memberById.value).includes(q) || m.name?.includes(q)).slice(0, 20)
})
const pickedSeek = computed(() => (seekId.value ? memberById.value.get(Number(seekId.value)) : null))
const isLineage = (id: number) => lineageIds.value.includes(Number(id))
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
const filtered = computed(() => {
  const q = kw.value.trim()
  if (!q) return []
  if (hits.value.length) return hits.value
  return all.value.filter((m) => m.name?.includes(q))
})
const goMember = (id?: number) => id && uni.navigateTo({ url: '/pages/member/member?id=' + id })
const globalSearch = async () => {
  const q = kw.value.trim()
  if (!q) {
    hits.value = []
    return
  }
  try { hits.value = (await GenealogyShowcaseApi.search(q)) || [] } catch { hits.value = [] }
}
const locateMe = async () => {
  if (!meId.value) return
  const mine = list.value.find((m) => Number(m.id) === Number(meId.value)) || all.value.find((m) => Number(m.id) === Number(meId.value))
  if (mine) openCard(mine)
}
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
const previewCard = (i: number) => {
  const urls = card.value?.photoUrls || []
  uni.previewImage({ urls, current: urls[i] })
}
const showRelation = async () => {
  if (!meId.value || !card.value?.id) return
  try {
    path.value = (await GenealogyShowcaseApi.relation(meId.value, card.value.id)) || []
    cardVisible.value = false
    if (!path.value.length) uni.showToast({ title: '谱上暂未找到通路', icon: 'none' })
  } catch {
    uni.showToast({ title: '请登录后查看关系', icon: 'none' })
  }
}
const pickSeek = (m: any) => {
  seekId.value = Number(m.id)
  seekKw.value = m.name || ''
}
const clearLineage = () => {
  seekSeq += 1
  lineageIds.value = []
  lineageNowId.value = null
  lineageSelfId.value = null
  lineagePath.value = []
}
const scrollToMember = (id: number | string) => {
  nextTick(() => {
    const q = uni.createSelectorQuery()
    q.select('#node-' + id).boundingClientRect()
    q.selectViewport().scrollOffset()
    q.exec((res: any[]) => {
      const rect = res?.[0]
      const scroll = res?.[1]
      if (!rect || !scroll) return
      uni.pageScrollTo({ scrollTop: Math.max(0, scroll.scrollTop + rect.top - 140), duration: 280 })
    })
  })
}
const seekRoot = async (id?: number) => {
  const target = typeof id === 'number' ? id : seekId.value
  if (!target) {
    uni.showToast({ title: '请先选择成员', icon: 'none' })
    return
  }
  kw.value = ''
  hits.value = []
  const pool = list.value.length ? list.value : all.value
  const chain = paternalLineage(target, pool)
  if (!chain.length) {
    uni.showToast({ title: '谱上未找到该成员', icon: 'none' })
    return
  }
  const seq = ++seekSeq
  seekId.value = Number(target)
  seekKw.value = ''
  lineageSelfId.value = Number(target)
  lineagePath.value = chain
  lineageIds.value = []
  lineageNowId.value = null
  await nextTick()
  for (const m of chain) {
    if (seq !== seekSeq) return
    lineageIds.value = [...lineageIds.value, Number(m.id)]
    lineageNowId.value = Number(m.id)
    scrollToMember(m.id)
    await wait(520)
  }
  if (seq !== seekSeq) return
  lineageNowId.value = null
}
const seekFromCard = async () => {
  if (!card.value?.id) return
  cardVisible.value = false
  await seekRoot(Number(card.value.id))
}

onShow(async () => {
  logged.value = isLoggedIn()
  if (!logged.value) {
    list.value = []
    all.value = []
    meId.value = undefined
    return
  }
  loading.value = true
  try {
    const me = await GenealogyMemberApi.me().catch(() => null)
    meId.value = me?.id
    list.value = (await GenealogyMemberApi.tree({ up: 30, down: 30 })) || []
    all.value = (await GenealogyMemberApi.simpleList()) || list.value
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
})
</script>
<style scoped>
.search input, .seek input { background: #f4ece0; border-radius: 12rpx; padding: 18rpx 20rpx; }
.acts { margin-top: 16rpx; display: flex; gap: 12rpx; }
.mini { background: #a63d2f; color: #fff; font-size: 24rpx; }
.mini[disabled] { opacity: 0.45; }
.mini.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; }
.seek-head { display: flex; align-items: baseline; gap: 12rpx; margin-bottom: 12rpx; }
.seek-title { font-weight: 700; color: #a63d2f; }
.seek-hits { margin-top: 12rpx; max-height: 360rpx; overflow: auto; }
.seek-hit { padding: 16rpx 8rpx; border-bottom: 1px solid #eadfcb; font-size: 26rpx; color: #3d3428; }
.seek-hit.on { color: #a63d2f; font-weight: 700; background: #fff5f2; }
.empty-hit { padding: 16rpx 0; }
.picked { margin-top: 12rpx; color: #a63d2f; font-size: 24rpx; }
.path .hops { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 8rpx; }
.hop { background: #f4e3de; color: #a63d2f; padding: 4rpx 12rpx; border-radius: 999rpx; font-size: 24rpx; }
.chain { margin-top: 8rpx; }
.chain-item { color: #a63d2f; font-weight: 700; font-size: 26rpx; }
.chain-item.now { font-size: 28rpx; }
.chain-line { width: 4rpx; height: 22rpx; background: #a63d2f; margin: 4rpx 0 4rpx 18rpx; border-radius: 4rpx; }
.node.me, .node.lineage { border-color: #a63d2f; background: #fff5f2; }
.node.lineage-self { box-shadow: 0 0 0 4rpx rgba(166, 61, 47, 0.35); }
.node.lineage-now { transform: scale(1.03); }
.btn.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; }
.member { margin-bottom: 16rpx; }
.gen { margin-bottom: 16rpx; }
.gen-title { margin: 8rpx 8rpx 12rpx; }
.wrap { display: flex; flex-wrap: wrap; gap: 16rpx; }
.node { width: calc(50% - 8rpx); margin-bottom: 0; box-sizing: border-box; display: flex; flex-direction: column; align-items: center; }
.node-avatar, .node-fallback { width: 72rpx; height: 88rpx; border-radius: 8rpx; background: #f4ece0; margin-bottom: 8rpx; }
.node-fallback { display: flex; align-items: center; justify-content: center; color: #a63d2f; font-weight: 700; }
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
.thick-head { display: flex; gap: 20rpx; align-items: flex-start; }
.thick-avatar, .thick-fallback { width: 112rpx; height: 136rpx; border-radius: 12rpx; background: #f4ece0; flex-shrink: 0; }
.thick-fallback { display: flex; align-items: center; justify-content: center; color: #a63d2f; font-size: 40rpx; font-weight: 700; }
.photos { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.photo { width: 120rpx; height: 120rpx; border-radius: 8rpx; background: #f4ece0; }
</style>
