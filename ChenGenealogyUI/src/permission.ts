import router from './router'
import type { RouteRecordRaw } from 'vue-router'
import { isRelogin } from '@/config/axios/service'
import { getAccessToken } from '@/utils/auth'
import { useTitle } from '@/hooks/web/useTitle'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { useDictStoreWithOut } from '@/store/modules/dict'
import { useUserStoreWithOut } from '@/store/modules/user'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { ElMessage } from 'element-plus'
import { parseRouteLocation } from '@/utils/routeParams'
import { hasAdminAccess, isAdminConsolePath, isAuthLoginPath, loginPathFor } from '@/utils/portalAuth'

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

// 路由不重定向白名单
const whiteList = [
  '/login',
  '/portal/login',
  '/admin/login',
  '/social-login',
  '/auth-redirect',
  '/bind',
  '/register',
  '/oauthLogin/gitee'
]

const isPublicRoute = (path: string, matched: typeof router.currentRoute.value.matched) => {
  if (whiteList.includes(path) || path.startsWith('/portal/login') || path.startsWith('/admin/login')) {
    return true
  }
  if (matched.some((item) => item.meta?.requiresAuth)) {
    return false
  }
  return path === '/' || path.startsWith('/portal')
}

// 路由加载前
router.beforeEach(async (to, from, next) => {
  start()
  loadStart()
  if (getAccessToken()) {
    const dictStore = useDictStoreWithOut()
    const userStore = useUserStoreWithOut()
    const permissionStore = usePermissionStoreWithOut()
    const firstLoad = !userStore.getIsSetUser
    if (firstLoad) {
      isRelogin.show = true
      await userStore.setUserInfoAction()
      isRelogin.show = false
      await permissionStore.generateRoutes()
      permissionStore.getAddRouters.forEach((route) => {
        router.addRoute(route as unknown as RouteRecordRaw)
      })
    }
    if (isAuthLoginPath(to.path)) {
      if (to.path.startsWith('/admin') && hasAdminAccess()) {
        next({ path: '/genealogy/home' })
      } else {
        next({ path: '/portal/home' })
      }
      return
    }
    if (isAdminConsolePath(to.path) && !hasAdminAccess()) {
      ElMessage.warning('当前账号无权访问管理端')
      next({ path: '/portal/home', replace: true })
      return
    }
    if (!dictStore.getIsSetDict) {
      dictStore.setDictMap().then()
    }
    if (firstLoad) {
      const redirectPath = from.query.redirect
      const redirect = typeof redirectPath === 'string' ? redirectPath : to.fullPath
      const redirectLocation = parseRouteLocation(redirect)
      const nextData =
        to.fullPath === redirect ? { ...to, replace: true } : { ...redirectLocation, replace: true }
      next(nextData)
    } else {
      next()
    }
  } else {
    if (isPublicRoute(to.path, to.matched)) {
      next()
    } else {
      const loginPath = loginPathFor(to.fullPath)
      next(`${loginPath}?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})

router.afterEach((to) => {
  useTitle(to?.meta?.title as string)
  done() // 结束Progress
  loadDone()
})
