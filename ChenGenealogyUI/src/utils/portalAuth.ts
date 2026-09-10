import { getAccessToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'

export const PORTAL_LOGIN = '/portal/login'
export const ADMIN_LOGIN = '/admin/login'

/** 允许进入管理端的角色编码 */
export const ADMIN_ROLE_CODES = [
  'super_admin',
  'tenant_admin',
  'genealogy_patriarch',
  'genealogy_admin',
  'genealogy_province_admin',
  'genealogy_city_admin',
  'genealogy_county_admin'
]

const PUBLIC_PATHS = [
  '/403',
  '/404',
  '/500',
  '/social-login',
  '/sso',
  '/bind',
  '/register',
  '/auth-redirect'
]

export const isLoggedIn = () => !!getAccessToken()

export const isAuthLoginPath = (path: string) =>
  path === '/login' || path === PORTAL_LOGIN || path === ADMIN_LOGIN

export const isAdminConsolePath = (path: string) => {
  if (!path || isAuthLoginPath(path) || path.startsWith('/portal')) {
    return false
  }
  if (PUBLIC_PATHS.some((item) => path === item || path.startsWith(`${item}/`))) {
    return false
  }
  return !path.startsWith('/oauthLogin')
}

export const hasAdminAccess = () => {
  const userStore = useUserStore()
  const roles = userStore.getRoles || []
  if (roles.some((role) => ADMIN_ROLE_CODES.includes(role))) {
    return true
  }
  const permissions = userStore.getPermissions
  if (!permissions) {
    return false
  }
  return permissions.has('genealogy:dashboard:query') || permissions.has('*:*:*')
}

export const loginPathFor = (targetPath?: string) => {
  if (
    targetPath &&
    (targetPath.startsWith('/admin') ||
      targetPath.startsWith('/genealogy') ||
      targetPath === '/index' ||
      targetPath.startsWith('/user'))
  ) {
    return ADMIN_LOGIN
  }
  return PORTAL_LOGIN
}
