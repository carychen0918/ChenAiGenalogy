<template>
  <view class="chat-page">
    <scroll-view scroll-y class="msgs" :scroll-into-view="bottomId">
      <view class="bubble bot">您好，我是陈氏族谱寻宗助手。请告诉我您了解的家族线索。</view>
      <view v-for="(m, i) in messages" :key="i" class="bubble" :class="m.role">
        <text>{{ m.content }}</text>
        <view v-if="m.score != null && m.role === 'bot'" class="score">同族可能性约 {{ m.score }}%</view>
        <view v-for="(c, ci) in m.contacts || []" :key="ci" class="contact">
          {{ c.levelName }} · {{ c.nickname || '管理员' }} {{ c.mobile || '' }}
        </view>
      </view>
      <view v-if="loading" class="bubble bot muted">正在结合族谱资料分析…</view>
      <view id="bottom" />
    </scroll-view>
    <view class="composer">
      <textarea v-model="input" :disabled="loading || limited" maxlength="1500" placeholder="例如：我姓陈，祖籍贵州贵阳…" />
      <view class="bar">
        <text class="muted" v-if="!logged">未登录每天限 10 次</text>
        <button class="btn-primary send" size="mini" :disabled="!input.trim() || loading || limited" @click="send">发送</button>
      </view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { GenealogyAiMatchApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'

const GUEST_KEY = 'genealogy-ai-match-guest-session'
const LIMIT_KEY = 'genealogy-ai-match-guest-limited-on'
const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const input = ref('')
const loading = ref(false)
const sessionId = ref('')
const messages = ref<{ role: string; content: string; score?: number; contacts?: any[] }[]>([])
const logged = computed(() => isLoggedIn())
const limited = ref(!isLoggedIn() && uni.getStorageSync(LIMIT_KEY) === today())
const bottomId = computed(() => 'bottom')

const ensureSession = async () => {
  if (sessionId.value) return
  const data: any = await GenealogyAiMatchApi.createSession()
  sessionId.value = data.sessionId
  if (!logged.value) uni.setStorageSync(GUEST_KEY, sessionId.value)
}

onShow(async () => {
  if (logged.value) {
    limited.value = false
    return
  }
  limited.value = uni.getStorageSync(LIMIT_KEY) === today()
  const sid = uni.getStorageSync(GUEST_KEY)
  if (!sid) return
  try {
    const data: any = await GenealogyAiMatchApi.getSession(sid)
    sessionId.value = data.sessionId || sid
    messages.value = (data.messages || [])
      .filter((m: any) => m.content)
      .map((m: any) => ({
        role: m.role === 'user' ? 'user' : 'bot',
        content: m.content,
        score: m.score,
        contacts: m.contacts || []
      }))
  } catch {
    uni.removeStorageSync(GUEST_KEY)
  }
})

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value || limited.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  loading.value = true
  try {
    await ensureSession()
    const data: any = await GenealogyAiMatchApi.chat(sessionId.value, text)
    messages.value.push({
      role: 'bot',
      content: data.content || '分析完成',
      score: data.score,
      contacts: data.contacts || []
    })
  } catch (e: any) {
    const msg = e?.message || '暂时无法完成分析'
    if (msg.includes('上限')) {
      limited.value = true
      uni.setStorageSync(LIMIT_KEY, today())
    }
    messages.value.push({ role: 'bot', content: msg })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.chat-page { height: 100vh; display: flex; flex-direction: column; background: #f6f1e6; }
.msgs { flex: 1; padding: 24rpx; }
.bubble { max-width: 86%; padding: 20rpx 24rpx; border-radius: 16rpx; margin-bottom: 20rpx; line-height: 1.6; white-space: pre-wrap; }
.bot { background: #f4ece0; }
.user { margin-left: auto; background: #a63d2f; color: #fff; }
.score { margin-top: 8rpx; font-size: 22rpx; color: #a63d2f; }
.contact { font-size: 24rpx; margin-top: 6rpx; }
.composer { background: #fffdf7; border-top: 1px solid #e8dfcc; padding: 16rpx 24rpx 32rpx; }
textarea { width: 100%; height: 140rpx; background: #f4ece0; border-radius: 12rpx; padding: 16rpx; box-sizing: border-box; }
.bar { display: flex; justify-content: space-between; align-items: center; margin-top: 12rpx; }
.send { background: #a63d2f; color: #fff; }
</style>
