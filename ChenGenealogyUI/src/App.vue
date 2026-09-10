<script lang="ts" setup>
import { isDark } from '@/utils/is'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import routerSearch from '@/components/RouterSearch/index.vue'

defineOptions({ name: 'APP' })

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('app')
const appStore = useAppStore()
const currentSize = computed(() => appStore.getCurrentSize)
const greyMode = computed(() => appStore.getGreyMode)
const { wsCache } = useCache()
const route = useRoute()
const isClientView = computed(() => {
  const path = route.path
  return path.startsWith('/portal') || path === '/login' || path.startsWith('/admin/login')
})

watch(
  isClientView,
  (enabled) => {
    document.documentElement.classList.toggle('is-client-view', enabled)
  },
  { immediate: true }
)
onUnmounted(() => {
  document.documentElement.classList.remove('is-client-view')
})

// 根据浏览器当前主题设置系统主题色
const setDefaultTheme = () => {
  let isDarkTheme = wsCache.get(CACHE_KEY.IS_DARK)
  if (isDarkTheme === null) {
    isDarkTheme = isDark()
  }
  appStore.setIsDark(isDarkTheme)
}
setDefaultTheme()
</script>
<template>
  <ConfigGlobal :size="currentSize">
    <RouterView :class="greyMode ? `${prefixCls}-grey-mode` : ''" />
    <routerSearch />
  </ConfigGlobal>
</template>
<style lang="scss">
$prefix-cls: #{$namespace}-app;

.size {
  width: 100%;
  height: 100%;
}

html,
body {
  @extend .size;

  padding: 0 !important;
  margin: 0;
  overflow: hidden;

  #app {
    @extend .size;
  }
}

/* 客户端不走管理端内部滚动，需恢复页面滚动，否则内容超长会被裁切 */
html.is-client-view,
html.is-client-view body {
  height: auto;
  min-height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
}

html.is-client-view #app {
  height: auto;
  min-height: 100%;
  overflow: visible;
}

.#{$prefix-cls}-grey-mode {
  filter: grayscale(100%);
}
</style>
