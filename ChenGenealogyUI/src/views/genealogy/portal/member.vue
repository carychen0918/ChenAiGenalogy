<template>
  <div v-loading="loading" v-if="m" class="page">
    <section class="hero">
      <el-avatar :size="72" :src="m.avatar">{{ m.name?.[0] }}</el-avatar>
      <div class="hero-meta">
        <h1>{{ m.name }} <el-tag size="small">{{ formatMemberGeneration(m) }}</el-tag></h1>
        <div class="sub">{{ life }} · {{ m.gender === 1 ? '男' : '女' }}</div>
        <el-button v-if="isSelf" class="mt-8px" type="primary" size="small" @click="applyVisible = true">提交档案补充</el-button>
      </div>
    </section>

    <section class="band">
      <article class="panel">
        <div class="panel-h">生平简介</div>
        <p class="intro">{{ m.intro || '暂无简介，等待补充' }}</p>
        <div class="contact">联系方式：{{ m.mobile || '仅管理员可见' }}</div>
      </article>
      <article class="panel">
        <div class="panel-h">
          <span>照片集</span>
          <el-button v-if="isSelf" type="primary" size="small" :loading="photoSaving" @click="savePhotos">保存照片集</el-button>
        </div>
        <div v-if="isSelf">
          <UploadImgs v-model="photos" :limit="20" :file-size="8" />
          <div class="hint">点击加号上传，最多 20 张。增删后请点右上角保存。</div>
        </div>
        <div v-else-if="m.photoUrls?.length" class="photos">
          <el-image
            v-for="(p, i) in m.photoUrls"
            :key="i"
            :src="p"
            fit="cover"
            preview-teleported
            :preview-src-list="m.photoUrls"
            :initial-index="i"
          />
        </div>
        <div v-else class="quiet">暂无照片</div>
      </article>
    </section>

    <section class="panel">
      <div class="panel-h">家庭小谱 · 上三代下两代</div>
      <div class="mf" v-if="hasMini">
        <div v-if="mini.ancestors?.length" class="mf-row">
          <div class="mf-label">上三代</div>
          <el-space wrap>
            <el-tag
              v-for="(a, i) in mini.ancestors"
              :key="a.id"
              class="cursor-pointer"
              @click="go(a.id)"
            >{{ ancestorLabel(i, mini.ancestors.length) }} {{ a.name }}</el-tag>
          </el-space>
        </div>
        <div v-if="mini.mother" class="mf-row">
          <div class="mf-label">母亲</div>
          <el-tag class="cursor-pointer" @click="go(mini.mother.id)">{{ mini.mother.name }}</el-tag>
        </div>
        <div v-if="(mini.spouses || []).length || (mini.siblings || []).length" class="mf-row">
          <div class="mf-label">同辈</div>
          <el-space wrap>
            <el-tag v-for="s in mini.spouses || []" :key="'sp'+s.id" type="warning" class="cursor-pointer" @click="go(s.id)">配偶 {{ s.name }}</el-tag>
            <el-tag v-for="s in mini.siblings || []" :key="'sb'+s.id" type="info" class="cursor-pointer" @click="go(s.id)">兄弟姐妹 {{ s.name }}</el-tag>
          </el-space>
        </div>
        <div v-if="(mini.children || []).length" class="mf-row">
          <div class="mf-label">下一代</div>
          <el-space wrap>
            <el-tag v-for="c in mini.children" :key="c.id" type="success" class="cursor-pointer" @click="go(c.id)">子女 {{ c.name }}</el-tag>
          </el-space>
        </div>
        <div v-if="(mini.grandchildren || []).length" class="mf-row">
          <div class="mf-label">下两代</div>
          <el-space wrap>
            <el-tag v-for="c in mini.grandchildren" :key="c.id" class="cursor-pointer" @click="go(c.id)">孙 {{ c.name }}</el-tag>
          </el-space>
        </div>
      </div>
      <div v-else class="quiet sm">暂无家庭关系</div>
    </section>

    <section class="panel">
      <div class="panel-h">
        <span>事迹与荣誉</span>
        <el-button v-if="isSelf" type="primary" size="small" @click="openDeed()">新增事迹</el-button>
      </div>
      <div v-if="m.deeds?.length" class="deeds">
        <div v-for="d in m.deeds" :key="d.id" class="deed">
          <div>
            <div class="deed-title">{{ d.title }}
              <el-tag v-if="d.occurYear" size="small">{{ d.occurYear }}</el-tag>
            </div>
            <div v-if="d.source" class="hint">来源：{{ d.source }}</div>
            <div v-if="d.content" class="deed-body">{{ d.content }}</div>
          </div>
          <div v-if="isSelf" class="shrink-0">
            <el-button link type="primary" @click="openDeed(d)">编辑</el-button>
            <el-button link type="danger" @click="removeDeed(d)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="quiet sm">暂无事迹记录</div>
    </section>

    <el-dialog v-model="applyVisible" title="提交档案补充">
      <el-input type="textarea" v-model="applyContent" placeholder="请描述需要补充的内容" />
      <template #footer>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="deedVisible" :title="deedForm.id ? '编辑事迹荣誉' : '新增事迹荣誉'" width="520px">
      <el-form :model="deedForm" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="deedForm.title" maxlength="64" />
        </el-form-item>
        <el-form-item label="年份">
          <el-input v-model="deedForm.occurYear" placeholder="如 2018 或 清光绪三年" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="deedForm.source" placeholder="如 族谱记载、奖状、报纸" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" :rows="5" v-model="deedForm.content" maxlength="2000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deedVisible = false">取消</el-button>
        <el-button type="primary" :loading="deedSaving" @click="saveDeed">保存</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else-if="!loading" class="quiet">尚未关联族谱成员，请联系管理员认证</div>
</template>
<script setup lang="ts">
import { GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { useUserStore } from '@/store/modules/user'
import { formatMemberGeneration } from '@/views/genealogy/utils/generation'
import { formatLifeSpan } from '@/views/genealogy/utils/member'
defineOptions({ name: 'PortalMember' })
const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)
const m = ref<any>()
const applyVisible = ref(false)
const applyContent = ref('')
const photoSaving = ref(false)
const photos = ref<string[]>([])
const deedVisible = ref(false)
const deedSaving = ref(false)
const deedForm = ref<any>({})
const selfMemberId = ref<number>()
const mini = ref<any>({})
const hasMini = computed(() =>
  !!(mini.value?.ancestors?.length || mini.value?.mother || mini.value?.spouses?.length || mini.value?.siblings?.length || mini.value?.children?.length || mini.value?.grandchildren?.length)
)
const ancestorLabel = (i: number, total: number) => ['曾祖', '祖父', '父亲'][3 - total + i] || '先祖'
const isSelf = computed(() => {
  if (!m.value) return false
  if (selfMemberId.value && Number(m.value.id) === Number(selfMemberId.value)) return true
  const uid = Number(m.value.userId)
  const loginId = Number(userStore.getUser?.id)
  return !!uid && !!loginId && uid === loginId
})
const life = computed(() => (m.value ? formatLifeSpan(m.value) : ''))
const load = async () => {
  loading.value = true
  try {
    const id = Number(route.query.id)
    const self = await GenealogyMemberApi.me().catch(() => null)
    selfMemberId.value = self?.id
    m.value = id ? await GenealogyMemberApi.get(id) : self
    photos.value = [...(m.value?.photoUrls || [])]
    mini.value = m.value?.id ? ((await GenealogyShowcaseApi.miniFamily(m.value.id).catch(() => null)) || {}) : {}
  } finally { loading.value = false }
}
const go = (id: number) => router.push('/portal/member?id=' + id)
const submitApply = async () => {
  await GenealogyMemberApi.createArchiveApply({ memberId: m.value.id, content: applyContent.value })
  message.success('补充申请已提交，等待管理员审核')
  applyVisible.value = false
}
const savePhotos = async () => {
  photoSaving.value = true
  try {
    await GenealogyMemberApi.updateMyPhotos(photos.value || [])
    message.success('照片集已保存')
    await load()
  } finally {
    photoSaving.value = false
  }
}
const openDeed = (row?: any) => {
  deedForm.value = row ? { ...row } : { title: '', occurYear: '', source: '', content: '' }
  deedVisible.value = true
}
const saveDeed = async () => {
  if (!deedForm.value.title) {
    message.warning('请填写标题')
    return
  }
  deedSaving.value = true
  try {
    if (deedForm.value.id) await GenealogyMemberApi.updateMyDeed(deedForm.value)
    else await GenealogyMemberApi.createMyDeed(deedForm.value)
    message.success('已保存')
    deedVisible.value = false
    await load()
  } finally {
    deedSaving.value = false
  }
}
const removeDeed = async (row: any) => {
  await message.delConfirm()
  await GenealogyMemberApi.deleteMyDeed(row.id)
  message.success('已删除')
  await load()
}
watch(() => route.query.id, load)
onMounted(load)
</script>
<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.hero {
  display: flex; align-items: center; gap: 18px;
  background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px; padding: 18px 20px;
}
.hero h1 { margin: 0; font-size: 22px; display: flex; align-items: center; gap: 8px; }
.sub { color: #8b8273; font-size: 13px; margin-top: 6px; }
.band { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: stretch; }
.panel {
  background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px;
  padding: 16px 18px; display: flex; flex-direction: column; min-height: 180px;
}
.panel-h { display: flex; align-items: center; justify-content: space-between; font-weight: 700; margin-bottom: 12px; }
.intro { margin: 0; line-height: 1.75; color: #5c5348; flex: 1; }
.contact { margin-top: 14px; padding-top: 12px; border-top: 1px dashed #eadfcb; color: #8b8273; font-size: 13px; }
.photos { display: grid; grid-template-columns: repeat(auto-fill, minmax(72px, 1fr)); gap: 8px; }
.photos :deep(.el-image) { width: 100%; height: 72px; border-radius: 8px; overflow: hidden; background: #efe7d6; }
.hint { margin-top: 8px; font-size: 12px; color: #8b8273; }
.quiet {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: #8b8273; font-size: 13px; background: #f8f3e8; border-radius: 8px; min-height: 88px;
}
.quiet.sm { min-height: 56px; }
.mf-row { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 10px; }
.mf-row:last-child { margin-bottom: 0; }
.mf-label { width: 56px; flex-shrink: 0; color: #8b8273; font-size: 13px; line-height: 24px; }
.deeds { display: flex; flex-direction: column; }
.deed { display: flex; justify-content: space-between; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0e8d8; }
.deed:last-child { border-bottom: 0; }
.deed-title { font-weight: 700; display: flex; align-items: center; gap: 8px; }
.deed-body { margin-top: 6px; font-size: 13px; color: #5c5348; white-space: pre-wrap; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
@media (max-width: 860px) {
  .band { grid-template-columns: 1fr; }
}
</style>
