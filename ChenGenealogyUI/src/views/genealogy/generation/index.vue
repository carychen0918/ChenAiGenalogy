<template>
  <ContentWrap>
    <el-alert
      class="mb-16px"
      type="info"
      show-icon
      :closable="false"
      title="同一世代可以并行多个字辈。例如四世同时使用「德」「万」，成员录入时先选世代，再选具体字辈。"
    />
    <div class="flex gap-8px mb-12px">
      <el-button type="primary" @click="openCreate()" v-hasPermi="['genealogy:content:update']">新增字辈</el-button>
    </div>
    <el-table :data="list" v-loading="loading" :span-method="spanMethod" border>
      <el-table-column label="世代" prop="generationNo" width="90">
        <template #default="s">{{ s.row.generationNo }}世</template>
      </el-table-column>
      <el-table-column label="字辈" prop="word" width="120">
        <template #default="s">
          <el-tag>{{ s.row.word }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110">
        <template #default="s">
          <dict-tag :type="DICT_TYPE.GENEALOGY_GENERATION_STATUS" :value="s.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="说明" prop="remark" min-width="180" />
      <el-table-column label="排序" prop="sort" width="80" />
      <el-table-column label="操作" width="240" align="center">
        <template #default="s">
          <el-button link type="primary" @click="openEdit(s.row)" v-hasPermi="['genealogy:content:update']">编辑</el-button>
          <el-button link type="primary" @click="openAddSame(s.row)" v-hasPermi="['genealogy:content:update']">再为该世添加</el-button>
          <el-button link type="danger" @click="remove(s.row)" v-hasPermi="['genealogy:content:update']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
  <Dialog v-model="visible" :title="dialogTitle" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="世代" prop="generationNo">
        <el-input-number v-model="form.generationNo" :min="1" :max="99" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="字辈" prop="word">
        <el-input v-model="form.word" maxlength="8" placeholder="单个字，如 德、万" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" class="!w-1/1">
          <el-option
            v-for="d in getIntDictOptions(DICT_TYPE.GENEALOGY_GENERATION_STATUS)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="form.remark" maxlength="255" placeholder="支系、并行字辈等说明" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" class="!w-1/1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="save">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { GenealogyContentApi } from '@/api/genealogy'

defineOptions({ name: 'GenealogyGeneration' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const visible = ref(false)
const dialogTitle = ref('新增字辈')
const formRef = ref()
const form = ref<any>({})
const rules = {
  generationNo: [{ required: true, message: '请填写世代', trigger: 'change' }],
  word: [{ required: true, message: '请填写字辈', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const spanMap = computed(() => {
  const map = new Map<number, number>()
  const firstIndex = new Map<number, number>()
  list.value.forEach((row, i) => {
    const no = row.generationNo
    map.set(no, (map.get(no) || 0) + 1)
    if (!firstIndex.has(no)) firstIndex.set(no, i)
  })
  return { map, firstIndex }
})

const spanMethod = ({ row, columnIndex, rowIndex }: any) => {
  if (columnIndex !== 0) return
  const first = spanMap.value.firstIndex.get(row.generationNo)
  if (first === rowIndex) {
    return { rowspan: spanMap.value.map.get(row.generationNo) || 1, colspan: 1 }
  }
  return { rowspan: 0, colspan: 0 }
}

const load = async () => {
  loading.value = true
  try {
    list.value = await GenealogyContentApi.generationList()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  form.value = { status: 1, sort: 0 }
  dialogTitle.value = '新增字辈'
  visible.value = true
}
const openEdit = (row: any) => {
  form.value = { ...row }
  dialogTitle.value = '编辑字辈'
  visible.value = true
}
const openAddSame = (row: any) => {
  form.value = {
    generationNo: row.generationNo,
    status: 1,
    sort: (row.sort || row.generationNo * 10) + 1,
    remark: '同一世并行字辈'
  }
  dialogTitle.value = `再为第${row.generationNo}世添加字辈`
  visible.value = true
}
const save = async () => {
  await formRef.value.validate()
  if (form.value.id) await GenealogyContentApi.updateGeneration(form.value)
  else await GenealogyContentApi.createGeneration(form.value)
  message.success('已保存')
  visible.value = false
  await load()
}
const remove = async (row: any) => {
  await message.delConfirm(`确认删除第${row.generationNo}世「${row.word}」字辈？若仍有成员使用该字辈则无法删除。`)
  await GenealogyContentApi.deleteGeneration(row.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>
