import type { App } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import remainingRouter from './modules/remaining'

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(), // createWebHashHistory URL带#，createWebHistory URL不带#
  strict: true,
  routes: remainingRouter as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 处理动态导入失败（如重新构建后 chunk 哈希变化），自动跳转到目标页面
router.onError((error, to) => {
  if (
    error.message.includes('Failed to fetch dynamically imported module') ||
    error.message.includes('Importing a module script failed')
  ) {
    window.location.assign(to.fullPath)
  }
})

export const resetRouter = (): void => {
  const resetWhiteNameList = ['Redirect', 'RedirectRoot', 'Login', 'PortalLogin', 'AdminLogin', 'NoFound', 'Home', 'GenealogyPortal', 'Root']
  router.getRoutes().forEach((route) => {
    const { name } = route
    if (name && !resetWhiteNameList.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
