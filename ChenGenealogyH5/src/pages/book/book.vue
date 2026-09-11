<template>
  <view class="page book-wrap">
    <view class="bar">
      <input class="search" v-model="kw" placeholder="定位人名、字辈、N世" confirm-type="search" @confirm="doSearch" />
      <view class="bar-btn" @click="showToc = true">目录</view>
    </view>
    <picker v-if="meta.generations?.length" :range="genLabels" @change="onGen">
      <view class="picker">定位世代 / 字辈：{{ genLabels[genIdx] || '请选择' }}</view>
    </picker>

    <view v-if="hits.length" class="hits">
      <view
        class="hit"
        v-for="(h, i) in hits"
        :key="i"
        :class="{ on: h.memberId === highlightId }"
        @click="jumpTo(h.pageNo, h.memberId)"
      >
        {{ h.memberId ? (h.generationNo || '?') + '世 ' + h.name : h.title }}
      </view>
    </view>

    <view class="book-frame">
      <view class="spine" />
      <view class="book-viewport" @touchstart="onTouchStart" @touchend="onTouchEnd">
        <view
          v-for="sheet in sheets"
          :key="sheet.role"
          class="sheet"
          :class="[sheet.role, flipDir && sheet.role === 'leaving' ? 'turn-' + flipDir : '']"
        >
          <view v-if="sheet.data.loginRequired">
            <guest-login title="翻阅世系需登录" desc="封面、前言和字辈可先看。登录后按辈展示全部分支，并可定位到人。" />
          </view>
          <view v-else-if="sheet.data.type === 'COVER'" class="cover">
            <view class="cover-en">CHEN GENEALOGY</view>
            <view class="cover-title">{{ sheet.data.bookTitle }}</view>
            <view class="cover-line" />
            <view class="muted">{{ sheet.data.surname }}氏 · 始祖{{ sheet.data.ancestorName || '待考' }}</view>
            <view class="muted">{{ sheet.data.region }}</view>
            <view class="cover-rev">{{ sheet.data.bookRevision }}</view>
          </view>
          <view v-else-if="sheet.data.type === 'PREFACE'">
            <view class="chapter">前言</view>
            <rich-text :nodes="sheet.data.html || ''" />
          </view>
          <view v-else-if="sheet.data.type === 'GENERATION'">
            <view class="chapter">字辈派语</view>
            <scroll-view scroll-x class="poem-scroll">
              <view class="poem-table">
                <view class="tr head">
                  <text class="th">赤土官庄世序</text>
                  <text class="th">全国统一字派</text>
                  <text class="th">高安椒坊字派</text>
                  <text class="th">长房</text>
                  <text class="th">二房</text>
                  <text class="th">三房</text>
                  <text class="th">三房织金</text>
                  <text class="th">四五房</text>
                </view>
                <view class="tr" v-for="r in sheet.data.generationRows" :key="r.generationNo">
                  <text class="td">{{ r.chituOrder || (r.generationNo ? r.generationNo + '世' : '—') }}</text>
                  <text class="td">{{ r.nationalSource || '—' }}</text>
                  <text class="td">{{ r.jiaofangSource || '—' }}</text>
                  <text class="td">{{ r.house1 || '—' }}</text>
                  <text class="td">{{ r.house2 || '—' }}</text>
                  <text class="td">{{ r.house3 || '—' }}</text>
                  <text class="td">{{ r.house3Zhijin || '—' }}</text>
                  <text class="td">{{ r.house45 || '—' }}</text>
                </view>
              </view>
            </scroll-view>
          </view>
          <view v-else>
            <view class="chapter">
              {{ sheet.data.title }}
              <text v-if="sheet.data.generationWords" class="muted"> {{ sheet.data.generationWords }}字辈</text>
            </view>
            <view
              v-for="(b, bi) in sheet.data.branches || []"
              :key="(b.generationWord || '') + '-' + (b.fatherId || 'r') + '-' + bi"
              class="branch"
            >
              <view class="branch-title">{{ b.title }}</view>
              <view
                class="person"
                v-for="m in b.members"
                :key="m.id"
                :class="{ on: m.id === highlightId }"
                @click="openMember(m.id)"
              >
                <view class="name">
                  {{ m.name }}
                  <text class="muted">{{ m.gender === 2 ? '女' : '男' }}</text>
                  <text v-if="m.generationWord" class="muted"> {{ m.generationWord }}字辈</text>
                  <text v-if="m.lifeSpan" class="muted"> {{ m.lifeSpan }}</text>
                </view>
                <view v-if="m.spouseNames" class="muted">配 {{ m.spouseNames }}</view>
                <view v-if="m.intro" class="intro">{{ m.intro }}</view>
                <swiper
                  v-if="m.photoUrls && m.photoUrls.length"
                  class="album"
                  :indicator-dots="m.photoUrls.length > 1"
                  autoplay
                  circular
                  :interval="4000"
                  @click.stop
                  @touchstart.stop
                  @touchend.stop
                >
                  <swiper-item v-for="(p, pi) in m.photoUrls" :key="pi">
                    <image class="album-img" :src="p" mode="aspectFit" />
                  </swiper-item>
                </swiper>
                <view v-if="m.deeds && m.deeds.length" class="deeds">
                  <view class="deed-h">事迹荣誉</view>
                  <view v-for="d in m.deeds" :key="d.id" class="deed">
                    <view class="deed-title">
                      {{ d.title }}
                      <text v-if="d.occurYear" class="muted"> {{ d.occurYear }}</text>
                    </view>
                    <view v-if="d.source" class="muted">来源：{{ d.source }}</view>
                    <view v-if="d.content" class="intro">{{ d.content }}</view>
                  </view>
                </view>
              </view>
            </view>
            <view v-if="!sheet.data.branches?.length" class="muted empty">本世暂无世系记录</view>
          </view>
          <view class="folio">第 {{ sheet.data.pageNo || pageNo }} 页</view>
        </view>
      </view>
    </view>

    <view class="flip">
      <button class="ghost" :disabled="pageNo <= 1 || flipping" @click="flip(-1)">上一页</button>
      <view class="pager-text">{{ pageNo }} / {{ meta.totalPages || 1 }}</view>
      <button class="btn-primary" :disabled="pageNo >= (meta.totalPages || 1) || flipping" @click="flip(1)">下一页</button>
    </view>

    <view v-if="showToc" class="mask" @click="showToc = false">
      <view class="drawer" @click.stop>
        <view class="card-title">目录</view>
        <view
          class="toc-item"
          v-for="t in meta.toc || []"
          :key="t.pageNo + '-' + t.title"
          :class="{ on: t.pageNo === pageNo }"
          @click="jumpTo(t.pageNo); showToc = false"
        >
          <text>{{ t.title }}</text>
          <text class="muted">{{ t.pageNo }}</text>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyBookApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'

const meta = ref<any>({})
const page = ref<any>({})
const leaving = ref<any>(null)
const pageNo = ref(1)
const kw = ref('')
const hits = ref<any[]>([])
const highlightId = ref<number>()
const showToc = ref(false)
const genIdx = ref(0)
const touchX = ref(0)
const flipping = ref(false)
const flipDir = ref<'next' | 'prev' | ''>('')

const genLabels = computed(() =>
  (meta.value.generations || []).map((g: any) => `${g.generationNo}世${g.words ? ' · ' + g.words : ''}`)
)
const sheets = computed(() => {
  const list: { role: string; data: any }[] = [{ role: 'current', data: page.value || {} }]
  if (flipping.value && leaving.value) {
    list.unshift({ role: 'leaving', data: leaving.value })
  }
  return list
})

const loadPage = async (n: number, memberId?: number) => {
  const total = meta.value.totalPages || 1
  pageNo.value = Math.min(Math.max(1, n), total)
  try {
    page.value = (await GenealogyBookApi.page(pageNo.value)) || {}
  } catch {
    page.value = {}
  }
  highlightId.value = memberId
}

const startFlip = async (nextNo: number, memberId?: number, dir?: 'next' | 'prev') => {
  const total = meta.value.totalPages || 1
  if (flipping.value || nextNo < 1 || nextNo > total) return
  if (nextNo === pageNo.value) {
    highlightId.value = memberId
    return
  }
  leaving.value = { ...page.value, branches: [...(page.value.branches || [])] }
  flipDir.value = dir || (nextNo > pageNo.value ? 'next' : 'prev')
  flipping.value = true
  await loadPage(nextNo, memberId)
  setTimeout(() => {
    flipping.value = false
    leaving.value = null
    flipDir.value = ''
  }, 720)
}

const flip = (dir: number) => startFlip(pageNo.value + dir, undefined, dir > 0 ? 'next' : 'prev')
const jumpTo = (n: number, memberId?: number) => startFlip(n, memberId)

const onGen = (e: any) => {
  genIdx.value = Number(e.detail.value)
  const g = meta.value.generations?.[genIdx.value]
  if (g) jumpTo(g.pageNo)
}

const doSearch = async () => {
  const word = kw.value.trim()
  if (!word) {
    hits.value = []
    return
  }
  if (!isLoggedIn()) {
    uni.showToast({ title: '定位人物需先登录', icon: 'none' })
    return
  }
  try {
    const resp = await GenealogyBookApi.search(word)
    if (resp?.loginRequired) {
      uni.showToast({ title: '定位人物需先登录', icon: 'none' })
      return
    }
    hits.value = resp?.list || []
    if (!hits.value.length) {
      uni.showToast({ title: '未找到匹配', icon: 'none' })
      return
    }
    const first = hits.value[0]
    await jumpTo(first.pageNo, first.memberId)
  } catch {
    /* toast in request */
  }
}

const openMember = (id: number) => {
  if (!isLoggedIn()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/member/member?id=' + id })
}

const onTouchStart = (e: any) => {
  touchX.value = e.changedTouches?.[0]?.clientX || 0
}
const onTouchEnd = (e: any) => {
  const x = e.changedTouches?.[0]?.clientX || 0
  const d = x - touchX.value
  if (d > 50) flip(-1)
  else if (d < -50) flip(1)
}

onLoad(async (q: any) => {
  try {
    meta.value = (await GenealogyBookApi.meta()) || {}
  } catch {
    meta.value = {}
  }
  const n = q?.page ? Number(q.page) : 1
  const mid = q?.memberId ? Number(q.memberId) : undefined
  await loadPage(n, mid)
})
</script>
<style scoped>
.book-wrap { padding-bottom: 40rpx; }
.bar { display: flex; gap: 12rpx; margin-bottom: 16rpx; }
.search { flex: 1; background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12rpx; padding: 16rpx 20rpx; font-size: 26rpx; }
.bar-btn { background: #a63d2f; color: #fff; border-radius: 12rpx; padding: 16rpx 28rpx; font-size: 26rpx; }
.picker { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12rpx; padding: 16rpx 20rpx; margin-bottom: 16rpx; color: #a63d2f; font-size: 26rpx; }
.hits { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 16rpx; }
.hit { background: #f4e3de; color: #a63d2f; border-radius: 999rpx; padding: 8rpx 20rpx; font-size: 22rpx; }
.hit.on { background: #a63d2f; color: #fff; }
.book-frame { display: flex; min-height: 70vh; }
.spine { width: 16rpx; flex-shrink: 0; background: #8d3a30; border-radius: 8rpx 0 0 8rpx; }
.book-viewport {
  flex: 1;
  perspective: 1200px;
  position: relative;
  overflow: hidden;
  min-height: 70vh;
}
.sheet {
  background: #fbf6e8;
  border: 1px solid #e0d3b4;
  border-left: none;
  padding: 40rpx 36rpx 80rpx;
  min-height: 70vh;
  box-sizing: border-box;
  position: relative;
  transform-origin: left center;
}
.sheet.current { position: relative; z-index: 1; }
.sheet.leaving { position: absolute; left: 0; right: 0; top: 0; bottom: 0; z-index: 3; }
.sheet.turn-next { animation: bookFlipNext .7s ease forwards; }
.sheet.turn-prev { transform-origin: right center; animation: bookFlipPrev .7s ease forwards; }
@keyframes bookFlipNext {
  0% { transform: rotateY(0deg); }
  100% { transform: rotateY(-180deg); }
}
@keyframes bookFlipPrev {
  0% { transform: rotateY(0deg); }
  100% { transform: rotateY(180deg); }
}
.cover { text-align: center; padding: 80rpx 0 40rpx; }
.cover-en { letter-spacing: 8rpx; color: #a63d2f; font-size: 22rpx; }
.cover-title { font-size: 48rpx; font-weight: 700; margin: 28rpx 0 20rpx; letter-spacing: 10rpx; }
.cover-line { width: 80rpx; height: 4rpx; background: #a63d2f; margin: 0 auto 28rpx; }
.cover-rev { margin-top: 60rpx; color: #a63d2f; }
.chapter { font-size: 34rpx; font-weight: 700; margin-bottom: 24rpx; border-left: 6rpx solid #a63d2f; padding-left: 16rpx; }
.poem-scroll { width: 100%; }
.poem-table { min-width: 1280rpx; border: 1px solid #e8dfcc; }
.poem-table .tr { display: flex; }
.poem-table .head { background: #f4ece0; }
.poem-table .th, .poem-table .td {
  flex: 1;
  min-width: 150rpx;
  padding: 12rpx 6rpx;
  text-align: center;
  font-size: 20rpx;
  border-right: 1px solid #eadfcb;
  border-bottom: 1px solid #eadfcb;
  box-sizing: border-box;
}
.poem-table .th { font-weight: 700; color: #5c5144; }
.poem-table .td:last-child, .poem-table .th:last-child { border-right: 0; }
.branch { margin-bottom: 28rpx; }
.branch-title { color: #a63d2f; font-weight: 700; margin-bottom: 8rpx; }
.person { padding: 16rpx 0; border-bottom: 1px dashed #e8dfcc; }
.person.on { background: #f4e3de; margin: 0 -16rpx; padding: 16rpx; }
.name { font-weight: 700; }
.intro { margin-top: 8rpx; font-size: 24rpx; line-height: 1.7; color: #5c5348; }
.album { height: 280rpx; margin-top: 16rpx; background: #f3ead4; }
.album-img { width: 100%; height: 280rpx; }
.deeds { margin-top: 16rpx; padding-top: 12rpx; border-top: 1px dashed #e8dfcc; }
.deed-h { color: #a63d2f; font-weight: 700; font-size: 24rpx; margin-bottom: 8rpx; }
.deed { margin-bottom: 12rpx; }
.deed-title { font-weight: 600; }
.folio { position: absolute; right: 24rpx; bottom: 16rpx; color: #8b8273; font-size: 22rpx; }
.flip { display: flex; align-items: center; gap: 16rpx; margin-top: 20rpx; }
.flip button { flex: 1; }
.pager-text { color: #8b8273; font-size: 24rpx; min-width: 80rpx; text-align: center; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.35); z-index: 20; display: flex; justify-content: flex-end; }
.drawer { width: 70%; background: #fffdf7; height: 100%; padding: 40rpx 28rpx; overflow: auto; }
.toc-item { display: flex; justify-content: space-between; padding: 20rpx 0; border-bottom: 1px solid #f0e6d4; }
.toc-item.on { color: #a63d2f; font-weight: 700; }
.empty { text-align: center; padding: 40rpx 0; }
</style>
