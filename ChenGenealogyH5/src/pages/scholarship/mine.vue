<template>
  <view class="page">
    <guest-login v-if="!logged" title="查看申请需登录" />
    <template v-else>
      <view class="card" v-for="a in list" :key="a.id" @click="open(a)">
        <view class="between">
          <view class="card-title">{{ a.applyNo || a.school }}</view>
          <text class="tag">{{ status(a.status) }}</text>
        </view>
        <view>{{ a.school }} {{ a.major || '' }} {{ a.grade || '' }}</view>
        <view class="muted">{{ SCHOLARSHIP_TYPE[a.type] || '' }} · {{ a.year || '' }}</view>
        <view v-if="a.rejectReason" class="muted">驳回原因：{{ a.rejectReason }}</view>
        <button v-if="canWithdraw(a.status)" class="ghost" size="mini" @click.stop="withdraw(a)">撤回</button>
      </view>
      <view v-if="!list.length" class="muted empty">暂无申请记录</view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyScholarshipApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { SCHOLARSHIP_STATUS, SCHOLARSHIP_TYPE } from '@/utils'

const logged = ref(isLoggedIn())
const list = ref<any[]>([])
const status = (s: number) => SCHOLARSHIP_STATUS[s] || '处理中'
const canWithdraw = (s: number) => [1, 2, 10, 11, 12, 13].includes(s)
const load = async () => {
  logged.value = isLoggedIn()
  if (!logged.value) return
  try {
    const data: any = await GenealogyScholarshipApi.myPage({ pageNo: 1, pageSize: 50 })
    list.value = data.list || []
  } catch {}
}
const open = async (row: any) => {
  const d: any = await GenealogyScholarshipApi.get(row.id)
  uni.showModal({
    title: d.applyNo || '申请详情',
    content: `${d.school} / ${d.major} / ${d.grade}\n学号 ${d.studentNo || ''}\n${d.familySituation || ''}${d.rejectReason ? '\n驳回：' + d.rejectReason : ''}`,
    showCancel: false
  })
}
const withdraw = async (row: any) => {
  uni.showModal({
    title: '撤回申请',
    content: '撤回后可修改再提交。',
    success: async (res) => {
      if (!res.confirm) return
      await GenealogyScholarshipApi.withdraw(row.id)
      uni.showToast({ title: '已撤回', icon: 'success' })
      load()
    }
  })
}
onShow(load)
</script>
<style scoped>
.between { display: flex; justify-content: space-between; align-items: center; }
.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; margin-top: 16rpx; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
