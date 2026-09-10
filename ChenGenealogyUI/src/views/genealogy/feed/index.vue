<template>
  <ContentWrap>
    <el-tabs v-model="tab">
      <el-tab-pane label="公告动态" name="feed" />
      <el-tab-pane label="评论审核" name="comment" />
    </el-tabs>
    <div v-show="tab === 'feed'">
      <el-button type="primary" class="mb-8px" @click="edit()">发布公告</el-button>
      <el-table :data="list">
        <el-table-column label="类型" prop="type"><template #default="s"><dict-tag :type="DICT_TYPE.GENEALOGY_FEED_TYPE" :value="s.row.type" /></template></el-table-column>
        <el-table-column label="标题" prop="title" />
        <el-table-column label="状态" prop="status"><template #default="s"><dict-tag :type="DICT_TYPE.GENEALOGY_FEED_STATUS" :value="s.row.status" /></template></el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="s">
            <el-button v-if="s.row.status === 0" link type="success" @click="audit(s.row.id, 1)">通过</el-button>
            <el-button v-if="s.row.status === 0" link type="danger" @click="audit(s.row.id, 2)">驳回</el-button>
            <el-button link @click="pin(s.row)">{{ s.row.pinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button link @click="offline(s.row.id)">下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-show="tab === 'comment'">
      <el-table :data="comments">
        <el-table-column label="内容" prop="content" />
        <el-table-column label="作者" prop="userName" />
        <el-table-column label="状态" prop="status" />
        <el-table-column label="操作">
          <template #default="s">
            <el-button v-if="s.row.status === 0" link type="success" @click="GenealogyFeedApi.auditComment(s.row.id, 1).then(load)">通过</el-button>
            <el-button v-if="s.row.status === 0" link type="danger" @click="GenealogyFeedApi.auditComment(s.row.id, 2).then(load)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </ContentWrap>
  <Dialog v-model="visible" title="发布">
    <el-form :model="form" label-width="80px">
      <el-form-item label="类型"><el-select v-model="form.type"><el-option :value="1" label="公告" /><el-option :value="2" label="动态" /><el-option :value="3" label="公示" /></el-select></el-form-item>
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="内容"><el-input type="textarea" v-model="form.content" /></el-form-item>
      <el-form-item label="置顶"><el-switch v-model="form.pinned" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="save">发布</el-button></template>
  </Dialog>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyFeedApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyFeed' })
const message = useMessage()
const tab = ref('feed')
const list = ref<any[]>([])
const comments = ref<any[]>([])
const visible = ref(false)
const form = ref<any>({ type: 1, pinned: true })
const load = async () => {
  list.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 50 })).list
  comments.value = (await GenealogyFeedApi.commentPage({ pageNo: 1, pageSize: 50, status: 0 })).list
}
const edit = () => { form.value = { type: 1, pinned: true }; visible.value = true }
const save = async () => { await GenealogyFeedApi.create(form.value); message.success('已发布'); visible.value = false; await load() }
const audit = async (id: number, status: number) => { await GenealogyFeedApi.audit(id, status); message.success('已处理'); await load() }
const pin = async (row: any) => { await GenealogyFeedApi.pin(row.id, !row.pinned); await load() }
const offline = async (id: number) => { await GenealogyFeedApi.audit(id, 3); await load() }
onMounted(load)
</script>
