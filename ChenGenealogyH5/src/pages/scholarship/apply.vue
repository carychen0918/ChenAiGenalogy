<template>
  <view class="page">
    <guest-login v-if="!logged" title="申请资助需登录" />
    <view class="card" v-else-if="!member?.id">
      <view class="card-title">尚未认证</view>
      <view class="muted">您的成员身份未认证，请联系管理员后再申请资助。</view>
    </view>
    <view class="card" v-else>
      <view class="card-title">申请资助</view>
      <view class="muted">{{ member.name }} · {{ member.generationNo }}世 · {{ member.regionName || '地区待完善' }}</view>
      <view v-if="config && !config.open" class="muted" style="margin: 12rpx 0">当前不在申请期内</view>
      <input class="input" v-model="form.school" placeholder="学校名称" />
      <input class="input" v-model="form.major" placeholder="专业" />
      <picker :range="GRADES" @change="(e: any) => form.grade = GRADES[e.detail.value]">
        <view class="input">{{ form.grade || '选择年级' }}</view>
      </picker>
      <input class="input" v-model="form.studentNo" placeholder="学号" />
      <picker :range="typeLabels" @change="onType">
        <view class="input">{{ SCHOLARSHIP_TYPE[form.type] || '资助类型' }}</view>
      </picker>
      <textarea class="input area" v-model="form.familySituation" placeholder="家庭经济情况" />
      <view class="muted">证明材料（图片）</view>
      <view class="row" @click="pick('enroll')">在校证明：{{ enroll ? '已上传' : '点击上传' }}</view>
      <view class="row" @click="pick('transcript')">成绩单：{{ transcript ? '已上传' : '点击上传' }}</view>
      <view class="row" @click="pick('admission')" v-if="needAdmission">录取通知书：{{ admission ? '已上传' : '点击上传' }}</view>
      <view class="row" @click="pick('hardship')">家庭困难证明：{{ hardship ? '已上传' : '选填' }}</view>
      <button class="ghost" :loading="loading" @click="save(false)">保存草稿</button>
      <button class="btn-primary" :loading="loading" :disabled="config && !config.open" @click="save(true)">提交申请</button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyMemberApi, GenealogyScholarshipApi } from '@/api/genealogy'
import { upload } from '@/utils/request'
import { GRADES, SCHOLARSHIP_TYPE } from '@/utils'
import { isLoggedIn } from '@/utils/auth'

const logged = ref(isLoggedIn())

const member = ref<any>()
const config = ref<any>()
const loading = ref(false)
const enroll = ref('')
const transcript = ref('')
const admission = ref('')
const hardship = ref('')
const typeLabels = Object.values(SCHOLARSHIP_TYPE)
const form = reactive<any>({ school: '', major: '', grade: '', studentNo: '', type: 1, familySituation: '' })
const needAdmission = computed(() => form.grade === '大一' || form.grade === '研一')

onLoad(async () => {
  logged.value = isLoggedIn()
  if (!logged.value) return
  try { member.value = await GenealogyMemberApi.me() } catch {}
  try { config.value = await GenealogyScholarshipApi.getConfig() } catch {}
})

const onType = (e: any) => {
  form.type = Number(Object.keys(SCHOLARSHIP_TYPE)[Number(e.detail.value)]) || 1
}
const pick = (key: 'enroll' | 'transcript' | 'admission' | 'hardship') => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const url = await upload(res.tempFilePaths[0])
      if (key === 'enroll') enroll.value = url
      if (key === 'transcript') transcript.value = url
      if (key === 'admission') admission.value = url
      if (key === 'hardship') hardship.value = url
    }
  })
}
const materials = () => {
  const list: any[] = []
  if (enroll.value) list.push({ type: 'ENROLL', name: '在校证明', url: enroll.value })
  if (transcript.value) list.push({ type: 'TRANSCRIPT', name: '成绩单', url: transcript.value })
  if (admission.value) list.push({ type: 'ADMISSION', name: '录取通知书', url: admission.value })
  if (hardship.value) list.push({ type: 'HARDSHIP', name: '家庭困难证明', url: hardship.value })
  return list
}
const save = async (submit: boolean) => {
  if (!member.value?.id) {
    uni.showToast({ title: '请先完成成员认证', icon: 'none' })
    return
  }
  if (!form.school || !form.major || !form.grade || !form.studentNo || !form.familySituation) {
    uni.showToast({ title: '请完整填写在校信息', icon: 'none' })
    return
  }
  if (submit && !member.value.provinceId && !member.value.cityId && !member.value.countyId) {
    uni.showToast({ title: '请先联系管理员完善省市区地址', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const payload = { ...form, materials: materials() }
    if (submit) {
      const id = await GenealogyScholarshipApi.submit(payload)
      uni.showToast({ title: '提交成功，编号 ' + id, icon: 'success' })
    } else {
      form.id = await GenealogyScholarshipApi.create(payload)
      uni.showToast({ title: '草稿已保存', icon: 'success' })
    }
    setTimeout(() => uni.redirectTo({ url: '/pages/scholarship/mine' }), 800)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '提交失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.input { background: #f4ece0; border-radius: 12rpx; padding: 22rpx 24rpx; margin-bottom: 20rpx; font-size: 28rpx; }
.area { height: 180rpx; }
.row { padding: 18rpx 0; border-bottom: 1px solid #e8dfcc; color: #5c554a; }
.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; margin: 24rpx 0 16rpx; }
</style>
