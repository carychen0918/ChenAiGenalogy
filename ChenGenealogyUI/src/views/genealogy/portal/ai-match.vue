<template>
  <div class="ai-match" :class="{ logged }">
    <aside v-if="logged" class="sidebar">
      <el-button class="new-btn" type="primary" :disabled="loading" @click="createNew">新建对话</el-button>
      <div class="conv-list">
        <div
          v-for="item in conversations"
          :key="item.id"
          class="conv-item"
          :class="{ active: item.id === conversationId }"
          @click="openConversation(item.id)"
        >
          <div class="conv-title">{{ item.title || '新对话' }}</div>
          <el-icon class="conv-del" @click.stop="removeConversation(item.id)"><Delete /></el-icon>
        </div>
        <div v-if="!conversations.length" class="empty">暂无历史会话</div>
      </div>
    </aside>
    <div class="chat-wrap">
      <div class="head">
        <div class="text-22px font-bold">AI 寻宗问祖</div>
        <p class="text-13px text-gray-500 mt-6px">
          用自己的话描述姓氏、字辈、祖居地、迁徙或先人姓名，系统结合本支陈氏族谱资料实时分析是否同族。
          切换菜单不会中断当前对话。{{ logged ? '登录后每个寻宗会话都会保存在左侧列表。' : '未登录每天可咨询 10 次；登录后可保存多个寻宗会话。' }}
        </p>
      </div>
      <el-card class="chat-card">
        <div ref="listRef" class="messages">
          <div class="bubble bot">
            您好，我是陈氏族谱寻宗助手。请告诉我您了解的家族线索，例如：我姓陈，祖上从某地迁出，家中字辈有「某」字。
          </div>
          <div v-for="(m, i) in messages" :key="i" class="bubble" :class="m.role">
            <div class="whitespace-pre-wrap">{{ m.content }}</div>
            <div v-if="m.score != null && m.role === 'bot'" class="score">同族可能性约 {{ m.score }}%</div>
            <div v-if="m.contacts?.length" class="contacts">
              <div v-for="(c, ci) in m.contacts" :key="ci" class="contact">
                {{ c.levelName }} · {{ c.nickname || '管理员' }}
                <span v-if="c.regionName">（{{ c.regionName }}）</span>
                <span v-if="c.mobile"> {{ c.mobile }}</span>
              </div>
            </div>
          </div>
          <div v-if="loading" class="bubble bot typing">正在结合族谱资料分析…</div>
        </div>
        <div class="composer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="3"
            maxlength="1500"
            show-word-limit
            :disabled="loading"
            placeholder="例如：我姓陈，祖籍贵州贵阳，家谱字辈有「德」字…"
            @keydown.enter.exact.prevent="send"
          />
          <div class="mt-10px flex justify-between items-center">
            <el-button v-if="!logged" text @click="resetGuest">新开对话</el-button>
            <span v-else />
            <div class="flex items-center gap-12px">
              <span v-if="!logged" class="text-12px text-gray-400">未登录每天限 10 次</span>
              <el-button type="primary" :loading="loading" :disabled="!input.trim() || guestLimited" @click="send">发送</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup lang="ts">
import { Delete } from '@element-plus/icons-vue'
import { GenealogyAiMatchApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/portalAuth'

defineOptions({ name: 'PortalAiMatch' })

const GUEST_KEY = 'genealogy-ai-match-guest-session'
const ACTIVE_KEY = 'genealogy-ai-match-active-conversation'
const GUEST_LIMIT_KEY = 'genealogy-ai-match-guest-limited-on'

type ChatMsg = { role: 'user' | 'bot'; content: string; score?: number; contacts?: any[] }
type Conversation = { id: number; sessionId: string; title: string; lastScore?: number }

const message = useMessage()
const input = ref('')
const loading = ref(false)
const sessionId = ref('')
const conversationId = ref<number>()
const conversations = ref<Conversation[]>([])
const messages = ref<ChatMsg[]>([])
const listRef = ref<HTMLElement>()
const logged = computed(() => isLoggedIn())
const todayKey = () => new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Shanghai' })
const guestLimited = ref(!isLoggedIn() && localStorage.getItem(GUEST_LIMIT_KEY) === todayKey())
let ctrl: AbortController | null = null

const markGuestLimited = (msg?: string) => {
  if (logged.value || !msg || !msg.includes('上限')) return false
  guestLimited.value = true
  localStorage.setItem(GUEST_LIMIT_KEY, todayKey())
  message.error(msg)
  return true
}

const scrollBottom = () => {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  })
}

const mapMessages = (list: any[] = []): ChatMsg[] =>
  list
    .filter((m) => m?.content)
    .map((m) => ({
      role: m.role === 'user' ? 'user' : 'bot',
      content: m.content,
      score: m.score,
      contacts: m.contacts || []
    }))

const applySession = (data: any) => {
  rememberSession(data)
  messages.value = mapMessages(data?.messages)
  scrollBottom()
}

const refreshConversations = async () => {
  if (!logged.value) {
    conversations.value = []
    return
  }
  conversations.value = (await GenealogyAiMatchApi.listConversations()) || []
}

const openConversation = async (id: number) => {
  if (loading.value || id === conversationId.value) return
  ctrl?.abort()
  const data = await GenealogyAiMatchApi.getConversation(id)
  applySession(data)
}

const createNew = async () => {
  if (loading.value) return
  ctrl?.abort()
  const data = logged.value
    ? await GenealogyAiMatchApi.createConversation()
    : await GenealogyAiMatchApi.createSession()
  applySession({ ...data, messages: [] })
  await refreshConversations()
}

const removeConversation = async (id: number) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复该寻宗会话，确认删除？', '删除会话', { type: 'warning' })
  } catch {
    return
  }
  await GenealogyAiMatchApi.deleteConversation(id)
  if (conversationId.value === id) {
    sessionId.value = ''
    conversationId.value = undefined
    messages.value = []
    localStorage.removeItem(ACTIVE_KEY)
  }
  await refreshConversations()
  if (!conversationId.value) {
    if (conversations.value.length) {
      await openConversation(conversations.value[0].id)
    } else {
      await createNew()
    }
  }
}

const resetGuest = () => {
  ctrl?.abort()
  sessionId.value = ''
  conversationId.value = undefined
  messages.value = []
  input.value = ''
  loading.value = false
  localStorage.removeItem(GUEST_KEY)
}

const rememberSession = (data: any) => {
  sessionId.value = data?.sessionId || sessionId.value
  conversationId.value = data?.conversationId
  if (logged.value && conversationId.value) {
    localStorage.setItem(ACTIVE_KEY, String(conversationId.value))
    localStorage.removeItem(GUEST_KEY)
  } else if (sessionId.value) {
    localStorage.setItem(GUEST_KEY, sessionId.value)
  }
}

const ensureSession = async () => {
  if (sessionId.value) return
  const data = logged.value
    ? await GenealogyAiMatchApi.createConversation()
    : await GenealogyAiMatchApi.createSession()
  rememberSession(data)
  if (logged.value) await refreshConversations()
}

const restore = async () => {
  if (logged.value) {
    const guestSid = localStorage.getItem(GUEST_KEY)
    if (guestSid) {
      try {
        const adopted = await GenealogyAiMatchApi.getSession(guestSid)
        if (adopted?.conversationId) applySession(adopted)
      } catch {
        localStorage.removeItem(GUEST_KEY)
      }
    }
    await refreshConversations()
    const saved = Number(localStorage.getItem(ACTIVE_KEY) || conversationId.value || 0)
    const target = conversations.value.find((c) => c.id === saved) || conversations.value[0]
    if (target) {
      if (target.id !== conversationId.value) {
        await openConversation(target.id)
      }
    } else if (!sessionId.value) {
      await createNew()
    }
  } else {
    conversations.value = []
    conversationId.value = undefined
    const sid = localStorage.getItem(GUEST_KEY)
    if (!sid) return
    try {
      const data = await GenealogyAiMatchApi.getSession(sid)
      applySession(data)
    } catch {
      localStorage.removeItem(GUEST_KEY)
    }
  }
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value || guestLimited.value) return
  loading.value = true
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  const bot = reactive({ role: 'bot' as const, content: '', score: undefined as number | undefined, contacts: [] as any[] })
  messages.value.push(bot)
  scrollBottom()
  try {
    await ensureSession()
    ctrl = new AbortController()
    await GenealogyAiMatchApi.chatStream(
      sessionId.value,
      text,
      ctrl,
      (ev) => {
        if (!ev?.data) return
        const json = JSON.parse(ev.data)
        if (json.code !== 0) {
          markGuestLimited(json.msg) || message.error(json.msg || '分析失败')
          return
        }
        const d = json.data || {}
        if (d.content) bot.content += d.content
        if (d.done) {
          bot.score = d.score
          bot.contacts = d.contacts || []
        }
        scrollBottom()
      },
      (err) => {
        throw err
      },
      () => {}
    )
    if (logged.value) await refreshConversations()
  } catch (e: any) {
    if (e?.name !== 'AbortError') {
      const msg = e?.message || '暂时无法完成分析，请稍后重试'
      markGuestLimited(msg)
      bot.content = bot.content || msg
    }
  } finally {
    loading.value = false
    scrollBottom()
  }
}

watch(logged, (v) => {
  if (v) guestLimited.value = false
  else guestLimited.value = localStorage.getItem(GUEST_LIMIT_KEY) === todayKey()
  restore()
})

onMounted(() => {
  restore()
})

onBeforeUnmount(() => ctrl?.abort())
</script>
<style scoped>
.ai-match { max-width: 1280px; }
.ai-match.logged { max-width: 1680px; display: flex; gap: 16px; align-items: stretch; }
.sidebar {
  width: 232px;
  flex-shrink: 0;
  background: #fffdf7;
  border: 1px solid #e8dfcc;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 520px;
}
.new-btn { width: 100%; background: #a63d2f; border-color: #a63d2f; }
.conv-list { margin-top: 12px; flex: 1; overflow: auto; }
.conv-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  color: #5c554a;
  margin-bottom: 4px;
}
.conv-item:hover, .conv-item.active { background: #f4e3de; color: #a63d2f; }
.conv-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.conv-del { display: none; color: #8b8273; }
.conv-item:hover .conv-del { display: inline-flex; }
.empty { font-size: 12px; color: #8b8273; text-align: center; padding: 24px 8px; }
.chat-wrap { flex: 1; min-width: 0; }
.chat-card { background: #fffdf7; }
.messages { min-height: 360px; max-height: 520px; overflow: auto; padding: 8px 4px 16px; }
.bubble { max-width: 86%; padding: 12px 14px; border-radius: 12px; margin-bottom: 12px; line-height: 1.6; white-space: pre-wrap; }
.bubble.bot, .bubble:not(.user) { background: #f4ece0; color: #2a251f; }
.bubble.user { margin-left: auto; background: #a63d2f; color: #fff; }
.typing { color: #8b8273; font-style: italic; }
.score { margin-top: 8px; font-size: 12px; color: #a63d2f; }
.contacts { margin-top: 8px; font-size: 13px; }
.contact { padding: 4px 0; }
.composer { border-top: 1px solid #e8dfcc; padding-top: 12px; }
</style>
