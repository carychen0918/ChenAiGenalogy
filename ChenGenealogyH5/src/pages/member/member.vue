<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看档案需登录" />
    <template v-else>
      <view class="card">
        <view class="card-title">{{ m.name }}</view>
        <view class="muted">{{ m.generationNo }}世{{ m.generationWord ? ' · ' + m.generationWord + '字辈' : '' }}</view>
        <view>生卒：{{ life }}</view>
        <view v-if="m.regionName">地区：{{ m.regionName }}</view>
        <view v-if="m.address">住址：{{ m.address }}</view>
        <view v-if="m.mobile">电话：{{ m.mobile }}</view>
      </view>
      <view class="card" v-if="m.intro">
        <view class="card-title">生平简介</view>
        <view>{{ m.intro }}</view>
      </view>
      <view class="card">
        <view class="card-title">家庭关系</view>
        <view v-if="m.fatherName">父亲：{{ m.fatherName }}</view>
        <view v-if="m.motherName">母亲：{{ m.motherName }}</view>
        <view v-if="(m.spouseNames || []).length">配偶：{{ (m.spouseNames || []).join('、') }}</view>
        <view v-if="(m.children || []).length">子女：{{ (m.children || []).map((c: any) => c.name).join('、') }}</view>
        <view v-if="!m.fatherName && !m.motherName && !(m.spouseNames || []).length && !(m.children || []).length" class="muted">暂无关系信息</view>
      </view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'

const logged = ref(isLoggedIn())
const m = ref<any>({})
const life = computed(() => {
  const birth = m.value.birthDate ? String(m.value.birthDate).slice(0, 10) : '?'
  const death = m.value.alive === false || m.value.alive === 0
    ? (m.value.deathDate ? String(m.value.deathDate).slice(0, 10) : '?')
    : '今'
  return `${birth} — ${death}`
})
onLoad(async (q) => {
  logged.value = isLoggedIn()
  if (!logged.value) return
  const id = Number(q?.id)
  if (!id) return
  m.value = await GenealogyMemberApi.get(id)
})
</script>
