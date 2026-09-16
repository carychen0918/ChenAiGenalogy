<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看档案需登录" />
    <template v-else>
      <view class="card">
        <view class="card-title">{{ m.name }}</view>
        <view class="muted">{{ formatMemberGeneration(m) }}</view>
        <view>生卒：{{ life }}</view>
        <view v-if="m.regionName">地区：{{ m.regionName }}</view>
        <view v-if="m.address">住址：{{ m.address }}</view>
        <view v-if="m.mobile">电话：{{ m.mobile }}</view>
      </view>
      <view class="card" v-if="m.intro">
        <view class="card-title">生平简介</view>
        <view>{{ m.intro }}</view>
      </view>
      <view class="card" v-if="(m.photoUrls || []).length">
        <view class="card-title">照片集</view>
        <view class="photos">
          <image v-for="(p, i) in m.photoUrls" :key="p + i" :src="p" class="photo" mode="aspectFill" @click="preview(i)" />
        </view>
      </view>
      <view class="card">
        <view class="card-title">家庭小谱 · 上三代下两代</view>
        <view v-if="mini.ancestors?.length" class="mf">
          <text class="mf-k">上三代</text>
          <text v-for="(a, i) in mini.ancestors" :key="a.id" class="link" @click="go(a.id)">{{ i ? ' → ' : '' }}{{ a.name }}</text>
        </view>
        <view v-if="mini.mother" class="mf"><text class="mf-k">母亲</text><text class="link" @click="go(mini.mother.id)">{{ mini.mother.name }}</text></view>
        <view v-if="(mini.spouses || []).length" class="mf">
          <text class="mf-k">配偶</text>
          <text v-for="s in mini.spouses" :key="s.id" class="link" @click="go(s.id)">{{ s.name }} </text>
        </view>
        <view v-if="(mini.siblings || []).length" class="mf">
          <text class="mf-k">同辈</text>
          <text v-for="s in mini.siblings" :key="s.id" class="link" @click="go(s.id)">{{ s.name }} </text>
        </view>
        <view v-if="(mini.children || []).length" class="mf">
          <text class="mf-k">下一代</text>
          <text v-for="c in mini.children" :key="c.id" class="link" @click="go(c.id)">{{ c.name }} </text>
        </view>
        <view v-if="(mini.grandchildren || []).length" class="mf">
          <text class="mf-k">下两代</text>
          <text v-for="c in mini.grandchildren" :key="c.id" class="link" @click="go(c.id)">{{ c.name }} </text>
        </view>
        <view v-if="!hasMini" class="muted">暂无关系信息</view>
      </view>
      <view class="card" v-if="(m.deeds || []).length">
        <view class="card-title">事迹与荣誉</view>
        <view v-for="d in m.deeds" :key="d.id" class="deed">
          <view class="name">{{ d.title }} <text v-if="d.occurYear" class="muted">{{ d.occurYear }}</text></view>
          <view class="muted" v-if="d.source">来源：{{ d.source }}</view>
          <view class="muted" v-if="d.content">{{ d.content }}</view>
        </view>
      </view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { formatLifeSpan, formatMemberGeneration } from '@/utils'

const logged = ref(isLoggedIn())
const m = ref<any>({})
const mini = ref<any>({})
const hasMini = computed(() =>
  !!(mini.value?.ancestors?.length || mini.value?.mother || mini.value?.spouses?.length || mini.value?.siblings?.length || mini.value?.children?.length || mini.value?.grandchildren?.length)
)
const life = computed(() => formatLifeSpan(m.value))
const go = (id?: number) => id && uni.navigateTo({ url: '/pages/member/member?id=' + id })
const preview = (i: number) => {
  const urls = m.value.photoUrls || []
  uni.previewImage({ urls, current: urls[i] })
}
onLoad(async (q) => {
  logged.value = isLoggedIn()
  if (!logged.value) return
  const id = Number(q?.id)
  if (!id) return
  m.value = await GenealogyMemberApi.get(id)
  try { mini.value = (await GenealogyShowcaseApi.miniFamily(id)) || {} } catch { mini.value = {} }
})
</script>
<style scoped>
.photos { display: flex; flex-wrap: wrap; gap: 12rpx; }
.photo { width: 160rpx; height: 160rpx; border-radius: 8rpx; background: #f4ece0; }
.row { padding: 8rpx 0; }
.link { color: #a63d2f; margin-right: 12rpx; }
.deed { padding: 12rpx 0; border-bottom: 1px solid #e8dfcc; }
.name { font-weight: 700; }
.mf { display: flex; flex-wrap: wrap; align-items: baseline; padding: 8rpx 0; }
.mf-k { width: 100rpx; color: #8b8273; flex-shrink: 0; }
</style>
