<template>
  <div class="g-login" :class="isAdmin ? 'is-admin' : 'is-client'">
    <div class="stage">
      <section class="visual">
        <div class="visual-mask" />
        <div class="visual-copy">
          <img class="seal" src="@/assets/imgs/logo.png" alt="陈氏族谱" />
          <h1>陈氏族谱</h1>
          <p>溯源 · 传承 · 凝聚</p>
          <div class="badge">{{ isAdmin ? '管理端' : '族人端' }}</div>
        </div>
      </section>
      <section class="panel">
        <div class="panel-inner">
          <div class="panel-head">
            <img class="mini-seal" src="@/assets/imgs/logo.png" alt="" />
            <div>
              <h2>{{ isAdmin ? '管理端登录' : '族人登录' }}</h2>
              <p>{{ isAdmin ? '仅系统管理员及省、市、县管理员可进入' : '登录后可查看族谱、申请资助、报名祭祖、发布动态' }}</p>
            </div>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="form.rememberMe">记住账号</el-checkbox>
            </el-form-item>
            <el-button class="submit" type="primary" :loading="loading" @click="submit">登录</el-button>
          </el-form>
          <div class="switch">
            <router-link v-if="isAdmin" to="/portal/login">返回族人登录</router-link>
            <router-link v-else to="/admin/login">管理员登录</router-link>
            <router-link to="/portal/home">先逛逛公开内容</router-link>
          </div>
        </div>
      </section>
    </div>
    <Verify
      v-if="captchaEnable"
      ref="verifyRef"
      captchaType="blockPuzzle"
      :imgSize="{ width: '400px', height: '200px' }"
      mode="pop"
      @success="doLogin"
    />
  </div>
</template>
<script setup lang="ts">
import { ElLoading } from 'element-plus'
import * as authUtil from '@/utils/auth'
import * as LoginApi from '@/api/login'

defineOptions({ name: 'GenealogyLogin' })

const route = useRoute()
const router = useRouter()
const message = useMessage()
const formRef = ref()
const verifyRef = ref()
const loading = ref(false)
const captchaEnable = import.meta.env.VITE_APP_CAPTCHA_ENABLE === 'true'
const isAdmin = computed(() => route.meta.loginType === 'admin' || route.path.startsWith('/admin/login'))
const form = reactive({
  tenantName: import.meta.env.VITE_APP_DEFAULT_LOGIN_TENANT || '',
  username: import.meta.env.VITE_APP_DEFAULT_LOGIN_USERNAME || '',
  password: '',
  rememberMe: true,
  captchaVerification: ''
})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const defaultRedirect = () => (isAdmin.value ? '/genealogy/home' : '/portal/home')

const submit = async () => {
  await formRef.value?.validate()
  if (captchaEnable) {
    verifyRef.value?.show()
    return
  }
  await doLogin({})
}

const doLogin = async (params: any) => {
  loading.value = true
  const loadingInst = ElLoading.service({ lock: true, text: '正在进入陈氏族谱…', background: 'rgba(42,37,31,0.55)' })
  try {
    if (import.meta.env.VITE_APP_TENANT_ENABLE === 'true' && form.tenantName) {
      const tenantId = await LoginApi.getTenantIdByName(form.tenantName)
      authUtil.setTenantId(tenantId)
    }
    const res = await LoginApi.login({
      username: form.username,
      password: form.password,
      captchaVerification: params?.captchaVerification || '',
      loginClient: isAdmin.value ? 'admin' : 'client'
    })
    if (form.rememberMe) {
      authUtil.setLoginForm({
        tenantName: form.tenantName,
        username: form.username,
        password: form.password,
        rememberMe: true
      })
    } else {
      authUtil.removeLoginForm()
    }
    authUtil.setToken(res)
    const redirect = (route.query.redirect as string) || defaultRedirect()
    await router.push(redirect)
  } catch (e: any) {
    if (e !== 'error') message.error(e?.message || '登录失败')
  } finally {
    loading.value = false
    loadingInst.close()
  }
}

onMounted(() => {
  const cached = authUtil.getLoginForm()
  if (cached?.username) {
    form.username = cached.username
    form.password = cached.password || ''
    form.rememberMe = cached.rememberMe
    form.tenantName = cached.tenantName || form.tenantName
  }
})
</script>
<style scoped>
.g-login { min-height: 100vh; background: #2a251f; color: #2a251f; }
.stage { min-height: 100vh; display: grid; grid-template-columns: 1.15fr 0.85fr; }
.visual { position: relative; background: #3a3126 url('@/assets/imgs/login-bg.png') center/cover no-repeat; }
.visual-mask { position: absolute; inset: 0; background: linear-gradient(180deg, rgba(42,37,31,.35), rgba(42,37,31,.72)); }
.visual-copy { position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; justify-content: center; padding: 64px; color: #f6f1e6; }
.seal { width: 84px; height: 84px; border-radius: 18px; box-shadow: 0 10px 30px rgba(0,0,0,.25); }
.visual-copy h1 { margin: 22px 0 8px; font-size: 40px; letter-spacing: 4px; }
.visual-copy p { opacity: .86; letter-spacing: 6px; }
.badge { margin-top: 28px; width: fit-content; padding: 6px 14px; border: 1px solid rgba(246,241,230,.35); border-radius: 999px; font-size: 12px; }
.panel { display: flex; align-items: center; justify-content: center; background: #f6f1e6; }
.panel-inner { width: min(420px, 92%); }
.panel-head { display: flex; gap: 14px; align-items: center; margin-bottom: 28px; }
.mini-seal { width: 48px; height: 48px; border-radius: 12px; }
.panel-head h2 { font-size: 24px; margin: 0 0 6px; }
.panel-head p { margin: 0; color: #8b8273; font-size: 13px; line-height: 1.6; }
.submit { width: 100%; height: 44px; background: #a63d2f; border-color: #a63d2f; }
.submit:hover { background: #8f3428; border-color: #8f3428; }
.switch { display: flex; justify-content: space-between; margin-top: 18px; font-size: 13px; }
.switch a { color: #a63d2f; text-decoration: none; }
@media (max-width: 960px) {
  .stage { grid-template-columns: 1fr; }
  .visual { min-height: 220px; }
  .visual-copy { padding: 32px 24px; }
  .visual-copy h1 { font-size: 28px; }
}
</style>
