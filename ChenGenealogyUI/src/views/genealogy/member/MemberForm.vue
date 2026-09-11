<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" v-loading="formLoading">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="姓名" prop="name"><el-input v-model="formData.name" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="世代">
            <el-select v-model="selectedGenNo" class="!w-1/1" placeholder="先选世代" @change="onGenNoChange">
              <el-option v-for="n in generationNos" :key="n" :label="n + '世'" :value="n" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="字辈" prop="generationId">
            <el-select v-model="formData.generationId" class="!w-1/1" :disabled="!selectedGenNo" placeholder="再选字辈">
              <el-option
                v-for="g in wordsOfSelected"
                :key="g.id"
                :label="generationOptionLabel(g)"
                :value="g.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="父亲">
            <el-select v-model="formData.fatherId" filterable clearable class="!w-1/1">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="母亲">
            <el-select v-model="formData.motherId" filterable clearable class="!w-1/1">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="配偶">
            <el-select v-model="formData.spouseIds" multiple filterable clearable class="!w-1/1">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出生日期">
            <el-date-picker v-model="formData.birthDate" value-format="x" class="!w-1/1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="逝世日期">
            <el-date-picker v-model="formData.deathDate" value-format="x" class="!w-1/1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话"><el-input v-model="formData.mobile" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所在地区">
            <el-cascader
              v-model="areaPath"
              :options="areaTree"
              :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true }"
              clearable
              filterable
              class="!w-1/1"
              placeholder="省 / 市 / 县"
              @change="onAreaChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="详细地址"><el-input v-model="formData.address" placeholder="街道、村寨等" /></el-form-item>
        </el-col>
        <el-col :span="12" v-if="formData.userId">
          <el-form-item label="登录账号">
            <el-input :model-value="formData.loginUsername" disabled placeholder="已绑定系统用户" />
          </el-form-item>
        </el-col>
        <template v-else>
          <el-col :span="12">
            <el-form-item label="开通登录">
              <el-switch
                v-model="formData.createLoginAccount"
                :disabled="!!formData.deathDate"
                active-text="同步创建账号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="formData.createLoginAccount && !formData.deathDate">
            <el-form-item label="登录账号">
              <el-input v-model="formData.loginUsername" placeholder="选填，默认按姓名拼音生成" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="formData.createLoginAccount && !formData.deathDate">
            <el-form-item label=" ">
              <div class="text-12px text-gray-500">在世成员将同步创建客户端登录账号，默认密码保存后展示（一般为 Chen123456）</div>
            </el-form-item>
          </el-col>
        </template>
        <el-col :span="24">
          <el-form-item label="简介"><el-input type="textarea" :rows="4" v-model="formData.intro" maxlength="4000" show-word-limit /></el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="头像"><UploadImg v-model="formData.avatar" /></el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="照片集">
            <UploadImgs v-model="formData.photoUrls" :limit="20" :file-size="8" />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.id">
          <el-form-item label="事迹荣誉">
            <div class="w-full">
              <el-button type="primary" class="mb-8px" @click="openDeed()">新增事迹</el-button>
              <div v-for="d in formData.deeds || []" :key="d.id" class="mb-8px pb-8px border-b last:border-0">
                <div class="flex justify-between gap-8px">
                  <div>
                    <div class="font-bold">{{ d.title }}
                      <el-tag v-if="d.occurYear" size="small" class="ml-8px">{{ d.occurYear }}</el-tag>
                    </div>
                    <div v-if="d.source" class="text-12px text-gray-500 mt-2px">来源：{{ d.source }}</div>
                    <div v-if="d.content" class="text-12px mt-4px whitespace-pre-wrap">{{ d.content }}</div>
                  </div>
                  <div class="shrink-0">
                    <el-button link type="primary" @click="openDeed(d)">编辑</el-button>
                    <el-button link type="danger" @click="removeDeed(d)">删除</el-button>
                  </div>
                </div>
              </div>
              <div v-if="!formData.deeds?.length" class="text-12px text-gray-400">暂无事迹，可点击新增</div>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-else>
          <el-form-item label="事迹荣誉">
            <div class="text-12px text-gray-400">请先保存成员，再补充事迹与荣誉</div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-dialog v-model="deedVisible" append-to-body :title="deedForm.id ? '编辑事迹荣誉' : '新增事迹荣誉'" width="480px">
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
          <el-input type="textarea" :rows="4" v-model="deedForm.content" maxlength="2000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deedVisible = false">取消</el-button>
        <el-button type="primary" :loading="deedSaving" @click="saveDeed">保存</el-button>
      </template>
    </el-dialog>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { GenealogyContentApi, GenealogyMemberApi } from '@/api/genealogy'
import { getAreaTree } from '@/api/system/area'
import { formatGenerationWord } from '@/views/genealogy/utils/generation'

defineOptions({ name: 'GenealogyMemberForm' })
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<any>({})
const areaTree = ref<any[]>([])
const areaPath = ref<number[]>([])
const formRules = { name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }], gender: [{ required: true, message: '性别不能为空', trigger: 'change' }], generationId: [{ required: true, message: '字辈不能为空', trigger: 'change' }] }
const formRef = ref()
const generations = ref<any[]>([])
const members = ref<any[]>([])
const selectedGenNo = ref<number>()
const generationNos = computed(() =>
  [...new Set(generations.value.map((g) => g.generationNo))].sort((a, b) => a - b)
)
const wordsOfSelected = computed(() =>
  generations.value.filter((g) => g.generationNo === selectedGenNo.value)
)
const generationOptionLabel = (g: any) => {
  const label = formatGenerationWord(g)
  return g.remark ? `${label}（${g.remark}）` : label
}
const onAreaChange = (val?: number[]) => {
  const path = val || []
  formData.value.provinceId = path[0]
  formData.value.cityId = path[1]
  formData.value.countyId = path[2]
}
const onGenNoChange = () => {
  const still = wordsOfSelected.value.find((g) => g.id === formData.value.generationId)
  if (still) return
  formData.value.generationId = wordsOfSelected.value.length === 1 ? wordsOfSelected.value[0].id : undefined
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增成员' : '编辑成员'
  formType.value = type
  formData.value = { gender: 1, spouseIds: [], photoUrls: [], deeds: [], createLoginAccount: true, loginUsername: '' }
  areaPath.value = []
  selectedGenNo.value = undefined
  generations.value = await GenealogyContentApi.generationList()
  members.value = await GenealogyMemberApi.simpleList()
  if (!areaTree.value.length) areaTree.value = await getAreaTree()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await GenealogyMemberApi.get(id)
      formData.value.photoUrls = formData.value.photoUrls || []
      formData.value.deeds = formData.value.deeds || []
      selectedGenNo.value = formData.value.generationNo
      areaPath.value = [formData.value.provinceId, formData.value.cityId, formData.value.countyId].filter(Boolean)
      if (!formData.value.userId) {
        formData.value.createLoginAccount = true
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })
const emit = defineEmits(['success'])
const deedVisible = ref(false)
const deedSaving = ref(false)
const deedForm = ref<any>({})
const reloadMember = async () => {
  if (!formData.value.id) return
  const data = await GenealogyMemberApi.get(formData.value.id)
  formData.value = { ...formData.value, ...data, photoUrls: data.photoUrls || [], deeds: data.deeds || [] }
}
const openDeed = (row?: any) => {
  if (!formData.value.id) {
    message.warning('请先保存成员，再补充事迹')
    return
  }
  deedForm.value = row ? { ...row } : { title: '', occurYear: '', source: '', content: '', memberId: formData.value.id }
  deedVisible.value = true
}
const saveDeed = async () => {
  if (!deedForm.value.title) {
    message.warning('请填写标题')
    return
  }
  deedSaving.value = true
  try {
    const payload = { ...deedForm.value, memberId: formData.value.id }
    if (payload.id) await GenealogyMemberApi.updateDeed(payload)
    else await GenealogyMemberApi.createDeed(payload)
    message.success('事迹已保存')
    deedVisible.value = false
    await reloadMember()
  } finally {
    deedSaving.value = false
  }
}
const removeDeed = async (row: any) => {
  await message.delConfirm()
  await GenealogyMemberApi.deleteDeed(row.id)
  message.success('已删除')
  await reloadMember()
}
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = { ...formData.value, confirmSpouseConflict: true, createLoginAccount: !!formData.value.createLoginAccount }
    const result = formType.value === 'create'
      ? await GenealogyMemberApi.create(data)
      : await GenealogyMemberApi.update(data)
    if (result?.username && result?.defaultPassword) {
      await message.alert(
        `已开通客户端登录账号：${result.username}，默认密码：${result.defaultPassword}。请通知该成员前往客户端登录后修改密码`
      )
    } else {
      message.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
