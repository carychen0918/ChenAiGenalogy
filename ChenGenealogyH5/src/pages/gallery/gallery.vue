<template>
  <view class="page">
    <view class="muted hint">点击照片查看大图，点姓名进档案</view>
    <view class="grid">
      <view class="item" v-for="(g, i) in list" :key="g.url + i">
        <image :src="g.url" class="img" mode="aspectFit" @click="preview(i)" />
        <view class="name" @click="goMember(g)">{{ g.memberName }}</view>
      </view>
    </view>
    <view v-if="!list.length" class="muted empty">暂无照片</view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyShowcaseApi } from '@/api/genealogy'

const list = ref<any[]>([])
const preview = (i: number) => {
  uni.previewImage({ urls: list.value.map((x) => x.url), current: list.value[i]?.url })
}
const goMember = (g: any) => {
  if (g.memberId) uni.navigateTo({ url: '/pages/member/member?id=' + g.memberId })
}
onLoad(async () => {
  try { list.value = (await GenealogyShowcaseApi.gallery(80)) || [] } catch {}
})
</script>
<style scoped>
.hint { margin-bottom: 16rpx; }
.grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.item { width: calc(33.33% - 12rpx); background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12rpx; overflow: hidden; }
.img { width: 100%; height: 240rpx; background: #f4ece0; }
.name { font-size: 22rpx; padding: 8rpx; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
