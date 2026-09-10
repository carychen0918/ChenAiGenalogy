<template>
  <ContentWrap>
    <el-tabs v-model="tab">
      <el-tab-pane label="姓氏源流" name="origin" />
      <el-tab-pane label="谱书" name="book" />
      <el-tab-pane label="迁徙节点" name="mig" />
      <el-tab-pane label="祖先事迹" name="deed" />
      <el-tab-pane label="文化指南" name="culture" />
    </el-tabs>
    <div v-show="tab === 'origin'">
      <Editor v-model="family.originContent" height="280px" />
      <el-button class="mt-12px" type="primary" @click="saveFamily">保存源流</el-button>
    </div>
    <div v-show="tab === 'book'">
      <el-alert
        class="mb-12px"
        type="info"
        :closable="false"
        title="谱书正文由成员世系、字辈自动生成，此处只维护封面与前言等公共信息。"
      />
      <el-form :model="family" label-width="90px" class="max-w-920px">
        <el-form-item label="谱书名称"><el-input v-model="family.bookTitle" placeholder="如 颍川陈氏族谱" /></el-form-item>
        <el-form-item label="重修记"><el-input v-model="family.bookRevision" placeholder="如 二〇二六年春重修" /></el-form-item>
        <el-form-item label="前言 / 谱序">
          <Editor v-model="family.bookPreface" height="280px" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="saveFamily">保存谱书信息</el-button>
    </div>
    <div v-show="tab === 'mig'">
      <el-button class="mb-8px" type="primary" @click="editMig()">新增节点</el-button>
      <el-table :data="migs">
        <el-table-column label="显示顺序" prop="sort" width="100" />
        <el-table-column label="时间" prop="nodeTime" />
        <el-table-column label="地点" prop="place" />
        <el-table-column label="事件" prop="eventTitle" />
        <el-table-column label="操作" width="220">
          <template #default="s">
            <el-button link :disabled="s.$index === 0" @click="moveMig(s.$index, -1)">上移</el-button>
            <el-button link :disabled="s.$index === migs.length - 1" @click="moveMig(s.$index, 1)">下移</el-button>
            <el-button link @click="editMig(s.row)">编辑</el-button>
            <el-button link type="danger" @click="GenealogyContentApi.deleteMigration(s.row.id).then(load)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-show="tab === 'deed'">
      <el-button class="mb-8px" type="primary" @click="editDeed()">新增事迹</el-button>
      <el-table :data="deeds">
        <el-table-column label="姓名" prop="name" />
        <el-table-column label="称号" prop="title" />
        <el-table-column label="分类" prop="category" />
        <el-table-column label="来源" prop="source" />
        <el-table-column label="操作"><template #default="s"><el-button link @click="editDeed(s.row)">编辑</el-button><el-button link type="danger" @click="GenealogyContentApi.deleteDeed(s.row.id).then(load)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <div v-show="tab === 'culture'">
      <el-button class="mb-8px" type="primary" @click="editCulture()">新增</el-button>
      <el-table :data="cultures">
        <el-table-column label="标题" prop="title" />
        <el-table-column label="操作"><template #default="s"><el-button link @click="editCulture(s.row)">编辑</el-button><el-button link type="danger" @click="GenealogyContentApi.deleteCulture(s.row.id).then(load)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
  </ContentWrap>
  <Dialog v-model="dlg" :title="dlgTitle">
    <el-form :model="cur" label-width="90px" v-if="tab === 'mig'">
      <el-form-item label="显示顺序">
        <el-input-number v-model="cur.sort" :min="1" class="!w-1/1" />
      </el-form-item>
      <el-form-item label="时间"><el-input v-model="cur.nodeTime" /></el-form-item>
      <el-form-item label="地点"><el-input v-model="cur.place" /></el-form-item>
      <el-form-item label="事件"><el-input v-model="cur.eventTitle" /></el-form-item>
      <el-form-item label="人物"><el-input v-model="cur.person" /></el-form-item>
      <el-form-item label="详情"><el-input type="textarea" v-model="cur.description" /></el-form-item>
      <el-form-item label="经度"><el-input v-model="cur.longitude" /></el-form-item>
      <el-form-item label="纬度"><el-input v-model="cur.latitude" /></el-form-item>
    </el-form>
    <el-form :model="cur" label-width="80px" v-else-if="tab === 'deed'">
      <el-form-item label="姓名"><el-input v-model="cur.name" /></el-form-item>
      <el-form-item label="称号"><el-input v-model="cur.title" /></el-form-item>
      <el-form-item label="分类"><el-select v-model="cur.category"><el-option v-for="d in getStrDictOptions(DICT_TYPE.GENEALOGY_DEED_CATEGORY)" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="来源"><el-input v-model="cur.source" /></el-form-item>
      <el-form-item label="正文"><el-input type="textarea" v-model="cur.content" rows="6" /></el-form-item>
    </el-form>
    <el-form :model="cur" label-width="80px" v-else>
      <el-form-item label="标题"><el-input v-model="cur.title" /></el-form-item>
      <el-form-item label="正文"><Editor v-model="cur.content" height="180px" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="saveCur">保存</el-button></template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyContent' })
const message = useMessage()
const tab = ref('origin')
const family = ref<any>({})
const migs = ref<any[]>([])
const deeds = ref<any[]>([])
const cultures = ref<any[]>([])
const dlg = ref(false)
const dlgTitle = ref('')
const cur = ref<any>({})
const load = async () => {
  family.value = await GenealogyContentApi.getFamily()
  migs.value = await GenealogyContentApi.migrationList()
  deeds.value = (await GenealogyContentApi.deedPage({ pageNo: 1, pageSize: 100 })).list
  cultures.value = await GenealogyContentApi.cultureList()
}
const saveFamily = async () => { await GenealogyContentApi.updateFamily(family.value); message.success('已保存') }
const nextMigSort = () => Math.max(0, ...migs.value.map((m) => Number(m.sort) || 0)) + 1
const editMig = (row?: any) => {
  cur.value = row ? { ...row } : { sort: nextMigSort() }
  dlgTitle.value = '迁徙节点'
  dlg.value = true
}
const moveMig = async (index: number, dir: number) => {
  const target = index + dir
  if (target < 0 || target >= migs.value.length) return
  const ordered = migs.value.map((m, i) => ({ ...m, sort: i + 1 }))
  const current = ordered[index]
  ordered[index] = ordered[target]
  ordered[target] = current
  await Promise.all(ordered.map((m, i) => GenealogyContentApi.updateMigration({ ...m, sort: i + 1 })))
  await load()
}
const editDeed = (row?: any) => { cur.value = row ? { ...row } : {}; dlgTitle.value = '事迹'; dlg.value = true }
const editCulture = (row?: any) => { cur.value = row ? { ...row } : {}; dlgTitle.value = '文化指南'; dlg.value = true }
const saveCur = async () => {
  if (tab.value === 'mig') cur.value.id ? await GenealogyContentApi.updateMigration(cur.value) : await GenealogyContentApi.createMigration(cur.value)
  else if (tab.value === 'deed') cur.value.id ? await GenealogyContentApi.updateDeed(cur.value) : await GenealogyContentApi.createDeed(cur.value)
  else cur.value.id ? await GenealogyContentApi.updateCulture(cur.value) : await GenealogyContentApi.createCulture(cur.value)
  message.success('已保存'); dlg.value = false; await load()
}
onMounted(load)
</script>
