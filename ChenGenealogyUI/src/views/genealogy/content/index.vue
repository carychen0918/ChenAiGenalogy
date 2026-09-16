<template>
  <ContentWrap>
    <el-tabs v-model="tab">
      <el-tab-pane label="姓氏源流" name="origin" />
      <el-tab-pane label="谱书" name="book" />
      <el-tab-pane label="迁徙节点" name="mig" />
      <el-tab-pane label="祖先事迹" name="deed" />
      <el-tab-pane label="文化指南" name="culture" />
      <el-tab-pane label="前台客厅" name="showcase" />
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
      <el-alert class="mb-12px" type="info" :closable="false" title="在此录入祖先事迹。录入后可点「前台置顶」，或到「前台客厅」里勾选。" />
      <el-button class="mb-8px" type="primary" @click="editDeed()">新增事迹</el-button>
      <el-table :data="deeds">
        <el-table-column label="姓名" prop="name" />
        <el-table-column label="称号" prop="title" />
        <el-table-column label="分类" prop="category" />
        <el-table-column label="来源" prop="source" />
        <el-table-column label="操作" width="280">
          <template #default="s">
            <el-button link type="primary" @click="pinAncestor(s.row)">{{ showcase.featuredAncestorDeedId === s.row.id ? '取消前台置顶' : '前台置顶' }}</el-button>
            <el-button link @click="editDeed(s.row)">编辑</el-button>
            <el-button link type="danger" @click="GenealogyContentApi.deleteDeed(s.row.id).then(load)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-show="tab === 'culture'">
      <el-button class="mb-8px" type="primary" @click="editCulture()">新增</el-button>
      <el-table :data="cultures">
        <el-table-column label="标题" prop="title" />
        <el-table-column label="操作"><template #default="s"><el-button link @click="editCulture(s.row)">编辑</el-button><el-button link type="danger" @click="GenealogyContentApi.deleteCulture(s.row.id).then(load)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <div v-show="tab === 'showcase'">
      <el-alert
        class="mb-12px"
        type="info"
        :closable="false"
        title="本页只配置前台展示，不改写成员、事迹、资助等业务数据。置顶事迹请先在「祖先事迹」或成员档案中录入，再在下面勾选。"
      />
      <el-form :model="showcase" label-width="140px" class="max-w-920px">
        <el-form-item label="首页人物">
          <el-select v-model="showcase.featuredMemberId" filterable clearable placeholder="自动选取有照片或简介的成员" class="!w-1/1">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶成员事迹">
          <el-select v-model="showcase.featuredDeedId" filterable clearable placeholder="请先在成员档案中新增事迹，再在此勾选" class="!w-1/1">
            <el-option v-for="d in memberDeeds" :key="d.id" :label="(d.name || '') + ' · ' + (d.title || '')" :value="d.id" />
          </el-select>
          <div class="form-tip">没有选项时，请到「家族成员 → 维护档案 → 事迹与荣誉」新增。</div>
        </el-form-item>
        <el-form-item label="置顶祖先事迹">
          <el-select v-model="showcase.featuredAncestorDeedId" filterable clearable placeholder="请先在「祖先事迹」页录入，再在此勾选" class="!w-1/1">
            <el-option v-for="d in ancestorDeeds" :key="d.id" :label="(d.name || '') + ' · ' + (d.title || '')" :value="d.id" />
          </el-select>
          <el-button class="mt-8px" @click="tab = 'deed'">去录入祖先事迹</el-button>
        </el-form-item>
        <el-form-item label="家族日历成员">
          <el-select
            v-model="showcase.calendarMemberIds"
            multiple
            filterable
            clearable
            placeholder="不选则前台日历不展示寿辰/忌日"
            class="!w-1/1"
          >
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
          <div class="form-tip">祖先多为公元前日期，公历无法对应月日。请只勾选需要出现在日历上的近人；清明、祭祖活动、资助窗口仍会自动出现。</div>
        </el-form-item>
        <el-form-item label="助学榜样">
          <el-select v-model="showcase.wallApplicationIds" multiple filterable clearable placeholder="不选则自动取待发放/已发放" class="!w-1/1">
            <el-option v-for="a in wallApps" :key="a.id" :label="(a.memberName || a.applyNo || a.id) + ' · ' + (a.year || '')" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="saveShowcase">保存客厅配置</el-button>
      </el-form>
      <el-divider />
      <div class="text-16px font-bold mb-12px">前台预览</div>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-card header="本周人物">{{ preview.featuredPerson?.name || '自动选取' }}</el-card>
        </el-col>
        <el-col :span="8">
          <el-card header="族影">{{ preview.galleryTotal || 0 }} 张</el-card>
        </el-col>
        <el-col :span="8">
          <el-card header="榜样墙">{{ (preview.scholarshipWall || []).length }} 人</el-card>
        </el-col>
      </el-row>
      <el-table class="mt-12px" :data="preview.calendar || []">
        <el-table-column label="日期" prop="date" width="120" />
        <el-table-column label="类型" prop="type" width="100" />
        <el-table-column label="事项" prop="title" />
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
import { GenealogyContentApi, GenealogyMemberApi, GenealogyScholarshipApi, GenealogyShowcaseApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyContent' })
const message = useMessage()
const tab = ref('origin')
const family = ref<any>({})
const migs = ref<any[]>([])
const deeds = ref<any[]>([])
const cultures = ref<any[]>([])
const members = ref<any[]>([])
const memberDeeds = ref<any[]>([])
const ancestorDeeds = ref<any[]>([])
const wallApps = ref<any[]>([])
const showcase = ref<any>({})
const preview = ref<any>({})
const dlg = ref(false)
const dlgTitle = ref('')
const cur = ref<any>({})
const load = async () => {
  family.value = await GenealogyContentApi.getFamily()
  migs.value = await GenealogyContentApi.migrationList()
  deeds.value = (await GenealogyContentApi.deedPage({ pageNo: 1, pageSize: 100 })).list
  cultures.value = await GenealogyContentApi.cultureList()
  try {
    members.value = await GenealogyMemberApi.simpleList()
    const cfg = await GenealogyShowcaseApi.getConfig()
    showcase.value = {
      featuredMemberId: cfg?.featuredMemberId,
      featuredDeedId: cfg?.featuredDeedId,
      featuredAncestorDeedId: cfg?.featuredAncestorDeedId,
      wallApplicationIds: cfg?.wallApplicationIds || [],
      calendarMemberIds: cfg?.calendarMemberIds || []
    }
    preview.value = (await GenealogyShowcaseApi.home()) || {}
    const deedsAll = (await GenealogyShowcaseApi.deeds()) || []
    memberDeeds.value = deedsAll.filter((d: any) => d.kind === 'member')
    ancestorDeeds.value = (deeds.value || []).map((d: any) => ({ id: d.id, name: d.name, title: d.title, kind: 'ancestor' }))
    if (!ancestorDeeds.value.length) {
      ancestorDeeds.value = deedsAll.filter((d: any) => d.kind === 'ancestor')
    }
    const pending = await GenealogyScholarshipApi.page({ pageNo: 1, pageSize: 50, status: 4 })
    const done = await GenealogyScholarshipApi.page({ pageNo: 1, pageSize: 50, status: 5 })
    wallApps.value = [...(pending?.list || []), ...(done?.list || [])]
  } catch {}
}
const saveShowcase = async () => {
  await GenealogyShowcaseApi.saveConfig(showcase.value)
  message.success('客厅配置已保存')
  preview.value = (await GenealogyShowcaseApi.home()) || {}
}
const pinAncestor = async (row: any) => {
  const id = showcase.value.featuredAncestorDeedId === row.id ? undefined : row.id
  showcase.value.featuredAncestorDeedId = id
  await saveShowcase()
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
<style scoped>
.form-tip { margin-top: 6px; font-size: 12px; color: #8b8273; line-height: 1.6; }
</style>
