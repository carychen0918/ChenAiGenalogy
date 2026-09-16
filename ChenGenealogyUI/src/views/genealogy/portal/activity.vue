<template>
  <div v-loading="loading" v-if="act">
    <el-card>
      <div class="text-22px font-bold">{{ act.title }}</div>
      <dict-tag :type="DICT_TYPE.GENEALOGY_ACTIVITY_STATUS" :value="act.status" class="mt-8px" />
      <el-descriptions :column="2" class="mt-16px" border>
        <el-descriptions-item label="时间">{{ format(act.startTime) }} ~ {{ format(act.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ act.place }}</el-descriptions-item>
        <el-descriptions-item label="集合地点">{{ act.gatherPlace }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ format(act.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ act.joinedCount || 0 }} / {{ act.maxCount || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ act.contactName }} {{ act.contactMobile }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt-16px whitespace-pre-wrap">{{ act.processDesc }}</div>
      <el-alert v-if="act.notice" class="mt-12px" :title="act.notice" type="info" :closable="false" />
      <div class="mt-16px">
        <el-button v-if="act.myRegStatus === 1" disabled>已报名</el-button>
        <el-button v-else-if="act.myRegStatus === 2" disabled>已加入候补</el-button>
        <el-button v-else type="primary" :disabled="act.status === 3" @click="regVisible = true">
          {{ full ? '已满，加入候补' : '我要报名' }}
        </el-button>
        <el-button class="ml-8px" :disabled="!act.longitude" @click="nav">
          {{ act.longitude ? '高德导航前往' : '地点信息待管理员配置' }}
        </el-button>
      </div>
      <AmapView v-if="act.longitude && act.latitude" class="mt-16px" :points="mapPoints" height="320px" />
    </el-card>
    <el-card class="mt-16px" header="祭扫记录">
      <el-space class="mb-12px">
        <el-button type="primary" @click="worshipVisible = true" :disabled="act.myRegStatus !== 1">上传祭扫记录</el-button>
        <el-button @click="onlineVisible = true">线上祭扫</el-button>
      </el-space>
      <div v-for="w in worships" :key="w.id" class="py-12px border-b">
        <el-tag v-if="w.pinned" type="danger" size="small">精选</el-tag>
        <el-tag v-if="w.online" type="info" size="small" class="ml-4px">线上</el-tag>
        <span class="ml-8px font-bold">{{ w.userName }}</span>
        <span v-if="w.ancestorName" class="text-gray-500"> · 致 {{ w.ancestorName }}</span>
        <div class="mt-6px">{{ w.content }}</div>
        <el-image v-for="(img, i) in w.images || []" :key="i" :src="img" class="w-72px h-72px mr-8px mt-8px" :preview-src-list="w.images" />
      </div>
      <el-empty v-if="!worships.length" description="暂无祭扫记录" />
    </el-card>
    <el-dialog v-model="regVisible" title="活动报名" width="460px">
      <el-form :model="reg" label-width="100px">
        <el-form-item label="参与人数"><el-input-number v-model="reg.peopleCount" :min="1" :max="20" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="reg.mobile" /></el-form-item>
        <el-form-item label="是否乘车"><el-switch v-model="reg.needBus" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="doRegister">确认报名</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="worshipVisible" title="上传祭扫记录" width="520px">
      <el-input type="textarea" v-model="worship.content" placeholder="留言" />
      <UploadImgs v-model="worship.images" :limit="9" class="mt-12px" />
      <template #footer>
        <el-button type="primary" @click="doWorship(false)">提交</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="onlineVisible" title="线上祭扫" width="460px">
      <el-form label-width="80px">
        <el-form-item label="先人"><el-input v-model="online.ancestorName" placeholder="请输入先人姓名" /></el-form-item>
        <el-form-item label="心意">
          <el-radio-group v-model="online.gift">
            <el-radio label="蜡烛">点亮蜡烛</el-radio>
            <el-radio label="鲜花">献花</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="留言"><el-input type="textarea" v-model="online.content" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="doOnline">完成祭扫</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyActivityApi, GenealogyMemberApi } from '@/api/genealogy'
import AmapView from '@/views/genealogy/components/AmapView.vue'
import { openAmapNav, type AmapPoint } from '@/utils/amap'
defineOptions({ name: 'PortalActivity' })
const route = useRoute()
const message = useMessage()
const loading = ref(false)
const act = ref<any>()
const worships = ref<any[]>([])
const regVisible = ref(false)
const worshipVisible = ref(false)
const onlineVisible = ref(false)
const reg = ref({ peopleCount: 1, mobile: '', needBus: false })
const worship = ref<{ content: string; images: string[] }>({ content: '', images: [] })
const online = ref({ ancestorName: '', gift: '蜡烛', content: '' })
const full = computed(() => act.value?.maxCount && (act.value.joinedCount || 0) >= act.value.maxCount)
const mapPoints = computed<AmapPoint[]>(() => {
  if (!act.value?.longitude || !act.value?.latitude) return []
  return [{
    lng: Number(act.value.longitude),
    lat: Number(act.value.latitude),
    title: act.value.place || act.value.title,
    content: [act.value.gatherPlace, act.value.address].filter(Boolean).join('\n')
  }]
})
const format = (t: number) => (t ? new Date(t).toLocaleString() : '')
const load = async () => {
  const id = Number(route.query.id)
  if (!id) return
  loading.value = true
  try {
    act.value = await GenealogyActivityApi.get(id)
    worships.value = await GenealogyActivityApi.worshipList(id)
    const me = await GenealogyMemberApi.me()
    if (me?.mobile && !reg.value.mobile) reg.value.mobile = me.mobile
  } finally { loading.value = false }
}
const doRegister = async () => {
  await GenealogyActivityApi.register({ activityId: act.value.id, ...reg.value })
  message.success(full.value ? '已加入候补队列' : '报名成功')
  regVisible.value = false
  await load()
}
const doWorship = async (isOnline: boolean) => {
  await GenealogyActivityApi.createWorship({
    activityId: act.value.id,
    content: worship.value.content,
    images: worship.value.images,
    online: isOnline
  })
  message.success('已提交')
  worshipVisible.value = false
  worship.value = { content: '', images: [] }
  await load()
}
const doOnline = async () => {
  await GenealogyActivityApi.createWorship({
    activityId: act.value.id,
    ancestorName: online.value.ancestorName,
    content: `线上祭扫：${online.value.gift}。${online.value.content || ''}`,
    images: [],
    online: true
  })
  message.success('线上祭扫完成')
  onlineVisible.value = false
  await load()
}
const nav = () => {
  openAmapNav(act.value.longitude, act.value.latitude, act.value.place || '祭祖地点')
}
onMounted(load)
</script>
