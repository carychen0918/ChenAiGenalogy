<template>
  <div>
    <div class="text-22px font-bold mb-16px">资助申请</div>
    <el-alert v-if="!me?.id" type="error" :closable="false" class="mb-16px" title="您的成员身份未认证，请联系管理员" />
    <el-alert v-else-if="config && !config.open" type="warning" :closable="false" class="mb-16px"
      :title="'当前不在申请期内，申请开放时间为 ' + format(config.windowStart) + ' 至 ' + format(config.windowEnd)" />
    <el-form :model="form" label-width="130px" :disabled="!me?.id">
      <el-card header="个人信息（自动带入，不可修改）" class="mb-16px">
        <el-form-item label="姓名"><el-input :model-value="me?.name" disabled /></el-form-item>
        <el-form-item label="辈分">
          <el-input :model-value="(me?.generationNo || '') + '世 · ' + (me?.generationWord || '')" disabled />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input :model-value="me?.regionName || '未填写，请联系管理员补充后再提交'" disabled />
        </el-form-item>
      </el-card>
      <el-card header="在校信息" class="mb-16px">
        <el-form-item label="学校名称" required><el-input v-model="form.school" /></el-form-item>
        <el-form-item label="专业" required><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="form.grade" class="!w-240px">
            <el-option v-for="d in getStrDictOptions(DICT_TYPE.GENEALOGY_GRADE)" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号" required><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item label="资助类型" required>
          <el-select v-model="form.type" class="!w-240px">
            <el-option v-for="d in getIntDictOptions(DICT_TYPE.GENEALOGY_SCHOLARSHIP_TYPE)" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="家庭经济情况" required>
          <el-input type="textarea" :rows="5" v-model="form.familySituation" />
        </el-form-item>
      </el-card>
      <el-card header="证明材料">
        <el-form-item label="在校证明" required>
          <UploadFile v-model="enroll" :limit="1" :file-size="10" :file-type="['pdf', 'jpg', 'jpeg', 'png']" />
        </el-form-item>
        <el-form-item label="成绩单" required>
          <UploadFile v-model="transcript" :limit="1" :file-size="10" :file-type="['pdf', 'jpg', 'jpeg', 'png']" />
        </el-form-item>
        <el-form-item v-if="needAdmission" label="录取通知书" required>
          <UploadFile v-model="admission" :limit="1" :file-size="10" :file-type="['pdf', 'jpg', 'jpeg', 'png']" />
        </el-form-item>
        <el-form-item label="家庭困难证明">
          <UploadFile v-model="hardship" :limit="1" :file-size="10" :file-type="['pdf', 'jpg', 'jpeg', 'png']" />
        </el-form-item>
      </el-card>
      <div class="mt-16px flex justify-end gap-12px">
        <el-button @click="save(false)">保存草稿</el-button>
        <el-button type="primary" :disabled="!config?.open" @click="save(true)">提交申请</el-button>
      </div>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import { GenealogyMemberApi, GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'PortalScholarshipApply' })
const message = useMessage()
const router = useRouter()
const me = ref<any>()
const config = ref<any>()
const form = ref<any>({ type: 1 })
const enroll = ref<string>('')
const transcript = ref<string>('')
const admission = ref<string>('')
const hardship = ref<string>('')
const needAdmission = computed(() => form.value.grade === '大一' || form.value.grade === '研一')
const format = (t: number) => (t ? new Date(t).toLocaleDateString() : '')
const urlOf = (v: string | string[]) => (Array.isArray(v) ? v[0] : v) || ''
const buildMaterials = () => {
  const list: any[] = []
  if (urlOf(enroll.value)) list.push({ type: 'ENROLL', name: '在校证明', url: urlOf(enroll.value) })
  if (urlOf(transcript.value)) list.push({ type: 'TRANSCRIPT', name: '成绩单', url: urlOf(transcript.value) })
  if (urlOf(admission.value)) list.push({ type: 'ADMISSION', name: '录取通知书', url: urlOf(admission.value) })
  if (urlOf(hardship.value)) list.push({ type: 'HARDSHIP', name: '家庭困难证明', url: urlOf(hardship.value) })
  return list
}
const save = async (submit: boolean) => {
  if (!me.value?.id) {
    message.warning('您的成员身份未认证，请联系管理员')
    return
  }
  const payload = { ...form.value, materials: buildMaterials() }
  if (submit) {
    if (!me.value?.provinceId && !me.value?.cityId && !me.value?.countyId) {
      message.warning('请先联系管理员完善您的省市区地址后再提交')
      return
    }
    const id = await GenealogyScholarshipApi.submit(payload)
    const detail = await GenealogyScholarshipApi.get(id)
    message.success('提交成功，申请编号 ' + (detail.applyNo || id))
  } else {
    form.value.id = await GenealogyScholarshipApi.create(payload)
    message.success('草稿已保存')
  }
  await router.push('/portal/my-applications')
}
onMounted(async () => {
  me.value = await GenealogyMemberApi.me()
  config.value = await GenealogyScholarshipApi.getConfig()
})
</script>
