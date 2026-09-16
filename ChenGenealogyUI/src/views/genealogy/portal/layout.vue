<template>
  <div class="portal" :class="{ 'portal-fill': fillViewport }">
    <nav class="nav">
      <div class="nav-inner">
        <div class="brand" @click="go('/portal/home')">
          <img class="seal-img" src="@/assets/imgs/logo.png" alt="陈氏族谱" />
          <div>
            <div class="brand-text">陈氏族谱</div>
            <div class="brand-sub">溯源 · 传承 · 凝聚</div>
          </div>
        </div>
        <div class="links">
          <router-link to="/portal/home">首页</router-link>
          <router-link to="/portal/tree">族谱</router-link>
          <router-link to="/portal/book">谱书</router-link>
          <router-link to="/portal/roots">寻根问祖</router-link>
          <router-link to="/portal/ai-match">AI寻宗</router-link>
          <router-link to="/portal/scholarship">学海无涯</router-link>
          <router-link to="/portal/ancestor">清明祭祖</router-link>
        </div>
        <div class="right">
          <el-autocomplete
            v-model="kw"
            :fetch-suggestions="querySearch"
            placeholder="搜姓名 / 几世 / 字辈"
            class="nav-search"
            clearable
            @select="onPick"
          >
            <template #default="{ item }">
              <div>{{ item.name }} · {{ item.generationNo ? item.generationNo + '世' : '' }}{{ item.generationWord ? item.generationWord + '字辈' : '' }}</div>
            </template>
          </el-autocomplete>
          <template v-if="logged">
            <el-button text v-if="canAdmin" class="nav-btn" @click="$router.push('/genealogy/home')">管理入口</el-button>
            <span class="nav-user" :title="displayName" @click="go('/portal/profile')">{{ displayName }}</span>
            <el-button text class="nav-btn" @click="logout">退出</el-button>
          </template>
          <el-button v-else type="primary" class="login-btn" @click="go('/portal/login')">登录</el-button>
        </div>
      </div>
    </nav>
    <main class="main">
      <router-view v-slot="{ Component }">
        <keep-alive :include="['PortalAiMatch']">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
    <footer class="foot">陈氏族谱 · 宗族数字平台　寻根问祖 · 学海无涯 · 清明祭祖</footer>
  </div>
</template>
<script setup lang="ts">
import { GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { hasAdminAccess, isLoggedIn } from '@/utils/portalAuth'
import { useUserStore } from '@/store/modules/user'
import { useTagsViewStore } from '@/store/modules/tagsView'

const router = useRouter()
const route = useRoute()
const fillViewport = computed(() => route.path.startsWith('/portal/book'))
const userStore = useUserStore()
const tagsViewStore = useTagsViewStore()
const me = ref<any>()
const logged = computed(() => isLoggedIn())
const canAdmin = computed(() => logged.value && hasAdminAccess())
const nickname = computed(() => userStore.getUser?.nickname)
const displayName = computed(() => me.value?.name || nickname.value || '个人中心')
const kw = ref('')
const go = (p: string) => router.push(p)
const querySearch = async (q: string, cb: (rows: any[]) => void) => {
  if (!q?.trim()) return cb([])
  try {
    cb(((await GenealogyShowcaseApi.search(q.trim())) || []).map((x: any) => ({ ...x, value: x.name })))
  } catch {
    cb([])
  }
}
const onPick = (item: any) => {
  if (!item?.id) return
  kw.value = item.name
  router.push('/portal/member?id=' + item.id)
}
const logout = async () => {
  await userStore.loginOut()
  tagsViewStore.delAllViews()
  me.value = undefined
  await router.push('/portal/home')
}
onMounted(async () => {
  if (!logged.value) return
  try { me.value = await GenealogyMemberApi.me() } catch {}
})
</script>
<style scoped>
.portal {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #f6f1e6;
  color: #2a251f;
  font-family: 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.portal-fill {
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
}
.nav { position: sticky; top: 0; z-index: 20; background: #fffdf7; border-bottom: 1px solid #e8dfcc; flex-shrink: 0; }
.nav-inner { max-width: 1680px; margin: 0 auto; height: 62px; display: flex; align-items: center; gap: 20px; padding: 0 32px; flex-wrap: nowrap; }
.brand { display: flex; align-items: center; gap: 10px; cursor: pointer; flex-shrink: 0; }
.seal-img { width: 38px; height: 38px; border-radius: 9px; object-fit: cover; }
.brand-text { font-size: 18px; font-weight: 700; white-space: nowrap; }
.brand-sub { font-size: 11px; color: #8b8273; letter-spacing: 1px; white-space: nowrap; }
.links { display: flex; gap: 4px; flex: 1; min-width: 0; }
.links a { padding: 8px 12px; border-radius: 8px; color: #5c554a; text-decoration: none; white-space: nowrap; }
.links a.router-link-active { background: #f4e3de; color: #a63d2f; font-weight: 600; }
.right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; white-space: nowrap; }
.nav-search { width: 200px; flex-shrink: 0; }
.nav-user {
  max-width: 8.5em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  line-height: 1;
}
.nav-btn { flex-shrink: 0; }
.login-btn { background: #a63d2f; border-color: #a63d2f; flex-shrink: 0; }
.main { flex: 1; width: 100%; max-width: 1680px; margin: 0 auto; padding: 24px 32px 60px; }
.portal-fill .main {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  padding-bottom: 16px;
}
.foot { flex-shrink: 0; border-top: 1px solid #e8dfcc; background: #efe7d6; padding: 26px; text-align: center; color: #8b8273; font-size: 13px; }
</style>
