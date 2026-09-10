<template>
  <el-drawer v-model="visible" :title="title" size="640px" destroy-on-close>
    <div v-loading="loading">
      <el-card class="mb-16px" header="生平简介">
        <el-input
          v-model="form.intro"
          type="textarea"
          :rows="8"
          maxlength="4000"
          show-word-limit
          placeholder="可直接撰写或修订该成员的生平简介"
        />
      </el-card>
      <el-card class="mb-16px" header="照片集">
        <UploadImgs v-model="form.photoUrls" :limit="20" :file-size="8" />
        <div class="mt-8px text-12px text-gray-500">支持 jpg / png，最多 20 张。修改后请点底部保存。</div>
      </el-card>
      <el-card header="事迹与荣誉">
        <el-button type="primary" class="mb-12px" @click="openDeed()">新增事迹</el-button>
        <div v-for="d in form.deeds || []" :key="d.id" class="mb-12px pb-12px border-b last:border-0">
          <div class="flex justify-between gap-12px">
            <div>
              <div class="font-bold">
                {{ d.title }}
                <el-tag v-if="d.occurYear" size="small" class="ml-8px">{{ d.occurYear }}</el-tag>
              </div>
              <div v-if="d.source" class="text-13px text-gray-500 mt-4px">来源：{{ d.source }}</div>
              <div v-if="d.content" class="text-13px mt-6px whitespace-pre-wrap">{{ d.content }}</div>
            </div>
            <div class="shrink-0">
              <el-button link type="primary" @click="openDeed(d)">编辑</el-button>
              <el-button link type="danger" @click="removeDeed(d)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!form.deeds?.length" description="暂无事迹记录，可点击新增" />
      </el-card>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveArchive">保存简介与照片集</el-button>
    </template>
    <el-dialog v-model="deedVisible" append-to-body :title="deedForm.id ? '编辑事迹荣誉' : '新增事迹荣誉'" width="520px">
      <el-form :model="deedForm" label-width="80px">
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
          <el-input type="textarea" :rows="5" v-model="deedForm.content" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deedVisible = false">取消</el-button>
        <el-button type="primary" :loading="deedSaving" @click="saveDeed">保存</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>
<script setup lang="ts">
import { GenealogyMemberApi } from '@/api/genealogy'

defineOptions({ name: 'GenealogyMemberArchiveForm' })
const message = useMessage()
const emit = defineEmits(['success'])
const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const title = ref('维护档案')
const form = ref<any>({ intro: '', photoUrls: [], deeds: [] })
const deedVisible = ref(false)
const deedSaving = ref(false)
const deedForm = ref<any>({})

const load = async (id: number) => {
  loading.value = true
  try {
    const data = await GenealogyMemberApi.get(id)
    form.value = {
      id: data.id,
      name: data.name,
      intro: data.intro || '',
      photoUrls: data.photoUrls || [],
      deeds: data.deeds || []
    }
    title.value = `维护档案 · ${data.name || ''}`
  } finally {
    loading.value = false
  }
}

const open = async (id: number) => {
  visible.value = true
  await load(id)
}

const saveArchive = async () => {
  saving.value = true
  try {
    await GenealogyMemberApi.updateArchive({
      id: form.value.id,
      intro: form.value.intro,
      photoUrls: form.value.photoUrls || []
    })
    message.success('生平简介与照片集已保存')
    emit('success')
    await load(form.value.id)
  } finally {
    saving.value = false
  }
}

const openDeed = (row?: any) => {
  deedForm.value = row
    ? { ...row }
    : { title: '', occurYear: '', source: '', content: '', memberId: form.value.id }
  deedVisible.value = true
}

const saveDeed = async () => {
  if (!deedForm.value.title) {
    message.warning('请填写标题')
    return
  }
  deedSaving.value = true
  try {
    const payload = { ...deedForm.value, memberId: form.value.id }
    if (payload.id) await GenealogyMemberApi.updateDeed(payload)
    else await GenealogyMemberApi.createDeed(payload)
    message.success('事迹已保存')
    deedVisible.value = false
    emit('success')
    await load(form.value.id)
  } finally {
    deedSaving.value = false
  }
}

const removeDeed = async (row: any) => {
  await message.delConfirm()
  await GenealogyMemberApi.deleteDeed(row.id)
  message.success('已删除')
  emit('success')
  await load(form.value.id)
}

defineExpose({ open })
</script>
