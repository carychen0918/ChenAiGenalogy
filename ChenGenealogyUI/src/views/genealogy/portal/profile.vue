<template>
  <div>
    <div class="text-22px font-bold mb-16px">个人中心</div>
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card header="我的谱位">
          <div v-if="me">
            <div class="flex items-center gap-12px">
              <el-avatar :size="56" :src="me.avatar">{{ me.name?.[0] }}</el-avatar>
              <div>
                <div class="text-18px font-bold">{{ me.name }}</div>
                <div class="text-13px text-gray-500 mt-4px">{{ formatMemberGeneration(me) }}</div>
              </div>
            </div>
            <div class="seat mt-12px" v-if="hasMini">
              <div v-if="mini.ancestors?.length" class="seat-row">
                <span class="seat-k">上三代</span>
                <span>{{ mini.ancestors.map((a: any) => a.name).join(' → ') }}</span>
              </div>
              <div v-if="mini.mother" class="seat-row"><span class="seat-k">母亲</span>{{ mini.mother.name }}</div>
              <div v-if="(mini.spouses || []).length" class="seat-row"><span class="seat-k">配偶</span>{{ mini.spouses.map((s: any) => s.name).join('、') }}</div>
              <div v-if="(mini.children || []).length" class="seat-row"><span class="seat-k">子女</span>{{ mini.children.map((s: any) => s.name).join('、') }}</div>
            </div>
            <el-space class="mt-12px" wrap>
              <el-button type="primary" @click="$router.push('/portal/member?id=' + me.id)">查看完整档案</el-button>
              <el-button @click="$router.push('/portal/tree')">在世系图定位</el-button>
            </el-space>
            <div class="text-12px text-gray-500 mt-8px">可在完整档案中上传照片集、新增事迹与荣誉</div>
          </div>
          <el-empty v-else description="尚未关联族谱成员，请联系管理员认证" />
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card header="近期日历">
          <div v-for="c in upcoming" :key="c.type + c.title + c.date" class="cal-row" @click="c.link && $router.push(c.link)">
            <el-tag size="small">{{ calType(c.type) }}</el-tag>
            <span class="font-bold ml-8px">{{ c.title }}</span>
            <span class="text-12px text-gray-500 ml-8px">{{ String(c.date || '').slice(0, 10) }}</span>
          </div>
          <el-empty v-if="!upcoming.length" description="近期暂无纪念日" :image-size="48" />
        </el-card>
        <el-card header="发布动态" class="mt-16px">
          <el-input v-model="feed.title" placeholder="标题" class="mb-8px" />
          <el-input type="textarea" v-model="feed.content" placeholder="分享家族近况，发布后需管理员审核" />
          <el-button class="mt-12px" type="primary" @click="publish">发布</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="mt-16px" header="我的动态">
      <div v-for="f in feeds" :key="f.id" class="py-10px border-b">
        <dict-tag :type="DICT_TYPE.GENEALOGY_FEED_STATUS" :value="f.status" />
        <span class="ml-8px font-bold">{{ f.title }}</span>
        <div class="text-13px text-gray-500 mt-4px">{{ f.content }}</div>
      </div>
      <el-empty v-if="!feeds.length" description="暂无动态" />
    </el-card>
    <el-card class="mt-16px" header="我的报名">
      <el-table :data="regs">
        <el-table-column label="活动ID" prop="activityId" width="90" />
        <el-table-column label="人数" prop="peopleCount" />
        <el-table-column label="乘车" prop="needBus"><template #default="s">{{ s.row.needBus ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="状态"><template #default="s">{{ s.row.status === 2 ? '候补' : '已报名' }}</template></el-table-column>
        <el-table-column label="操作">
          <template #default="s">
            <el-button link type="primary" @click="$router.push('/portal/activity?id=' + s.row.activityId)">查看活动</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!regs.length" description="暂无报名记录" />
    </el-card>
    <el-card class="mt-16px" header="账号设置">
      <el-button @click="$router.push('/user/profile')">前往系统账号设置</el-button>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { useUserStore } from '@/store/modules/user'
import { formatMemberGeneration } from '@/views/genealogy/utils/generation'
import { GenealogyActivityApi, GenealogyFeedApi, GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
defineOptions({ name: 'PortalProfile' })
const message = useMessage()
const userStore = useUserStore()
const me = ref<any>()
const mini = ref<any>({})
const upcoming = ref<any[]>([])
const feeds = ref<any[]>([])
const regs = ref<any[]>([])
const feed = ref({ title: '', content: '' })
const hasMini = computed(() =>
  !!(mini.value?.ancestors?.length || mini.value?.mother || mini.value?.spouses?.length || mini.value?.children?.length)
)
const calType = (t: string) => ({ birthday: '寿辰', memorial: '忌日', festival: '节气', activity: '活动', scholarship: '资助' }[t] || t)
const load = async () => {
  me.value = await GenealogyMemberApi.me()
  if (me.value?.id) {
    mini.value = (await GenealogyShowcaseApi.miniFamily(me.value.id).catch(() => null)) || {}
  }
  const now = new Date().toISOString().slice(0, 10)
  try {
    upcoming.value = ((await GenealogyShowcaseApi.calendar()) || [])
      .filter((c: any) => String(c.date || '').slice(0, 10) >= now)
      .slice(0, 6)
  } catch {
    upcoming.value = []
  }
  feeds.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 20, authorUserId: userStore.getUser.id })).list
  regs.value = await GenealogyActivityApi.myRegistrations()
}
const publish = async () => {
  if (!feed.value.title || !feed.value.content) {
    message.warning('请填写标题和内容')
    return
  }
  await GenealogyFeedApi.create({ type: 2, ...feed.value })
  message.success('已提交，等待管理员审核')
  feed.value = { title: '', content: '' }
  await load()
}
onMounted(load)
</script>
<style scoped>
.seat { font-size: 13px; color: #5c5348; line-height: 1.7; }
.seat-row { display: flex; gap: 8px; }
.seat-k { color: #8b8273; width: 48px; flex-shrink: 0; }
.cal-row { padding: 8px 0; border-bottom: 1px solid #f0e8d8; cursor: pointer; }
.cal-row:last-child { border-bottom: 0; }
</style>
