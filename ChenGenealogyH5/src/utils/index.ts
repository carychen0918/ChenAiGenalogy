import { getAccessToken } from '@/utils/auth'

export const TAB_PAGES = [
  '/pages/index/index',
  '/pages/tree/tree',
  '/pages/roots/roots',
  '/pages/ancestor/ancestor',
  '/pages/mine/mine'
]

export const goOpenHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

export const requireLogin = (redirect?: string) => {
  if (getAccessToken()) return true
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const currentRoute = current ? `/${(current as any).route}` : ''
  if (currentRoute.includes('pages/login/login')) return false
  const route = redirect || currentRoute
  uni.navigateTo({ url: `/pages/login/login?redirect=${encodeURIComponent(route)}` })
  return false
}

export const formatTime = (t?: number | string) => {
  if (!t) return ''
  const d = new Date(typeof t === 'number' && t < 1e12 ? t * 1000 : t)
  if (Number.isNaN(d.getTime())) return String(t)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

export const formatDate = (t?: number | string) => formatTime(t).slice(0, 10)

export const stripHtml = (html?: string) => (html || '').replace(/<[^>]+>/g, '')

export const openNav = (lng?: number, lat?: number, name?: string) => {
  if (!lng || !lat) {
    uni.showToast({ title: '地点信息待管理员配置', icon: 'none' })
    return
  }
  uni.openLocation({ longitude: Number(lng), latitude: Number(lat), name: name || '目的地' })
}

export const SCHOLARSHIP_TYPE: Record<number, string> = { 1: '助学金', 2: '奖学金', 3: '临时困难补助' }
export const SCHOLARSHIP_STATUS: Record<number, string> = {
  0: '草稿', 1: '待初审', 2: '待终审', 4: '已通过', 5: '已发放', 6: '已驳回', 7: '已撤回',
  10: '待县级审核', 11: '待市级审核', 12: '待省级审核', 13: '待家族审核'
}
export const FEED_TYPE: Record<number, string> = { 1: '公告', 2: '动态', 3: '公示' }
export const GEN_STATUS: Record<number, string> = { 1: '在用', 2: '已用', 3: '备用' }
export const GRADES = ['大一', '大二', '大三', '大四', '研一', '研二', '研三', '专科']
