<template>
  <div v-loading="loading" v-if="m">
    <el-card>
      <div class="flex gap-20px items-center">
        <el-avatar :size="86" :src="m.avatar">{{ m.name?.[0] }}</el-avatar>
        <div>
          <h1 class="text-24px">{{ m.name }} <el-tag>{{ formatMemberGeneration(m) }}</el-tag></h1>
          <div class="text-gray-500 mt-8px">{{ life }} · {{ m.gender === 1 ? '男' : '女' }}</div>
          <el-button v-if="isSelf" class="mt-12px" type="primary" @click="applyVisible = true">提交档案补充</el-button>
        </div>
      </div>
    </el-card>
    <el-row :gutter="16" class="mt-16px">
      <el-col :span="12">
        <el-card header="生平简介">
          <p>{{ m.intro || '（暂无简介，等待补充）' }}</p>
          <el-divider />
          <div>联系方式：{{ m.mobile || '仅管理员可见' }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="flex justify-between items-center">
              <span>照片集</span>
              <el-button
                v-if="isSelf"
                type="primary"
                :loading="photoSaving"
                @click="savePhotos"
              >保存照片集</el-button>
            </div>
          </template>
          <div v-if="isSelf">
            <UploadImgs v-model="photos" :limit="20" :file-size="8" />
            <div class="mt-8px text-12px text-gray-500">点击加号上传，最多 20 张。增删后请点右上角保存。</div>
          </div>
          <div v-else>
            <el-image
              v-for="(p, i) in m.photoUrls || []"
              :key="i"
              :src="p"
              class="w-80px h-80px mr-8px mb-8px"
              fit="cover"
              preview-teleported
              :preview-src-list="m.photoUrls"
              :initial-index="i"
            />
            <el-empty v-if="!m.photoUrls?.length" description="暂无照片" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px" header="直系亲属">
      <el-space wrap>
        <el-card v-if="m.fatherId" shadow="hover" class="cursor-pointer" @click="go(m.fatherId)">父亲 {{ m.fatherName }}</el-card>
        <el-card v-for="(n, i) in m.spouseNames || []" :key="'s'+i" shadow="hover" class="cursor-pointer" @click="m.spouseIds?.[i] && go(m.spouseIds[i])">配偶 {{ n }}</el-card>
        <el-card v-for="c in m.children || []" :key="c.id" shadow="hover" class="cursor-pointer" @click="go(c.id)">子女 {{ c.name }}</el-card>
      </el-space>
    </el-card>
    <el-card class="mt-16px">
      <template #header>
        <div class="flex justify-between items-center">
          <span>事迹与荣誉</span>
          <el-button v-if="isSelf" type="primary" @click="openDeed()">新增事迹</el-button>
        </div>
      </template>
      <div v-for="d in m.deeds || []" :key="d.id" class="mb-12px pb-12px border-b last:border-0">
        <div class="flex justify-between gap-12px">
          <div>
            <div class="font-bold">{{ d.title }}
              <el-tag v-if="d.occurYear" size="small" class="ml-8px">{{ d.occurYear }}</el-tag>
            </div>
            <div v-if="d.source" class="text-13px text-gray-500 mt-4px">来源：{{ d.source }}</div>
            <div v-if="d.content" class="text-13px mt-6px whitespace-pre-wrap">{{ d.content }}</div>
          </div>
          <div v-if="isSelf" class="shrink-0">
            <el-button link type="primary" @click="openDeed(d)">编辑</el-button>
            <el-button link type="danger" @click="removeDeed(d)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!m.deeds?.length" description="暂无事迹记录" />
    </el-card>
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
  <el-empty v-else-if="!loading" description="尚未关联族谱成员，请联系管理员认证" />
</template>
<script setup lang="ts">
import { GenealogyMemberApi } from '@/api/genealogy'
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
