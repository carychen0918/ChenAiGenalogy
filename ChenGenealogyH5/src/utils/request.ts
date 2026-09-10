import { getBaseUrl, tenantEnable } from '@/config'
import { getAccessToken, getRefreshToken, getTenantId, removeToken, setToken } from '@/utils/auth'
import { getTerminal } from '@/utils/terminal'

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

export interface RequestOption {
  url: string
  method?: Method
  params?: Record<string, any>
  data?: any
  header?: Record<string, any>
  /** 默认 true；登录等接口可设为 false */
  isToken?: boolean
  timeout?: number
}

const whiteList = ['/login', '/refresh-token']
const ignoreMsgs = ['无效的刷新令牌', '刷新令牌已过期']

let isRefreshToken = false
let requestList: Array<() => void> = []

const buildQuery = (params?: Record<string, any>) => {
  if (!params) return ''
  const parts: string[] = []
  Object.keys(params).forEach((key) => {
    const value = params[key]
    if (value === undefined || value === null || value === '') return
    parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
  })
  return parts.length ? `?${parts.join('&')}` : ''
}

const joinUrl = (url: string, params?: Record<string, any>) => {
  if (!params) return url
  if (url.includes('?')) {
    const extra = buildQuery(params)
    return extra ? `${url}&${extra.slice(1)}` : url
  }
  return url + buildQuery(params)
}

const toast = (msg: string) => {
  if (!msg || ignoreMsgs.includes(msg)) return
  uni.showToast({ title: msg, icon: 'none', duration: 2500 })
}

const toLogin = () => {
  removeToken()
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current ? `/${(current as any).route}` : '/pages/index/index'
  if (route.includes('pages/login/login')) return
  uni.navigateTo({ url: `/pages/login/login?redirect=${encodeURIComponent(route)}` })
}

const refreshToken = () => {
  return new Promise<void>((resolve, reject) => {
    const rt = getRefreshToken()
    if (!rt) {
      reject(new Error('无刷新令牌'))
      return
    }
    uni.request({
      url: getBaseUrl() + '/system/auth/refresh-token?refreshToken=' + encodeURIComponent(rt),
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        terminal: String(getTerminal()),
        ...(tenantEnable() ? { 'tenant-id': getTenantId() } : {})
      },
      success: (res: any) => {
        const body = res.data || {}
        if (body.code === 0 && body.data?.accessToken) {
          setToken(body.data)
          resolve()
        } else {
          reject(new Error(body.msg || '刷新令牌失败'))
        }
      },
      fail: reject
    })
  })
}

const request = <T = any>(option: RequestOption): Promise<T> => {
  return new Promise((resolve, reject) => {
    const method = (option.method || 'GET').toUpperCase() as Method
    let url = option.url.startsWith('http') ? option.url : getBaseUrl() + option.url
    url = joinUrl(url, option.params)

    const header: Record<string, any> = {
      'Content-Type': 'application/json',
      terminal: String(getTerminal()),
      ...(option.header || {})
    }
    let needToken = option.isToken !== false
    if (needToken && whiteList.some((item) => option.url.includes(item))) needToken = false
    const token = getAccessToken()
    if (needToken && token) header.Authorization = 'Bearer ' + token
    if (tenantEnable()) header['tenant-id'] = getTenantId()

    uni.request({
      url,
      method: method as any,
      data: method === 'GET' ? undefined : option.data,
      header,
      timeout: option.timeout || 30000,
      success: async (res: any) => {
        const body = res.data || {}
        const code = body.code
        const msg = body.msg || '请求失败'
        if (code === 401) {
          if (!getRefreshToken()) {
            toLogin()
            reject(new Error(msg))
            return
          }
          if (!isRefreshToken) {
            isRefreshToken = true
            try {
              await refreshToken()
              requestList.forEach((cb) => cb())
              requestList = []
              resolve(await request(option))
            } catch {
              requestList = []
              toLogin()
              reject(new Error(msg))
            } finally {
              isRefreshToken = false
            }
          } else {
            requestList.push(() => resolve(request(option)))
          }
          return
        }
        if (code !== 0 && code !== 200) {
          toast(msg)
          reject(new Error(msg))
          return
        }
        resolve((body.data ?? body) as T)
      },
      fail: (err) => {
        toast('网络异常，请稍后重试')
        reject(err)
      }
    })
  })
}

export const upload = (filePath: string, name = 'file') => {
  return new Promise<string>((resolve, reject) => {
    const header: Record<string, any> = { terminal: String(getTerminal()) }
    const token = getAccessToken()
    if (token) header.Authorization = 'Bearer ' + token
    if (tenantEnable()) header['tenant-id'] = getTenantId()
    uni.uploadFile({
      url: getBaseUrl() + '/infra/file/upload',
      filePath,
      name,
      header,
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 0) resolve(body.data)
          else {
            toast(body.msg || '上传失败')
            reject(new Error(body.msg))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: reject
    })
  })
}

export default {
  get: <T = any>(option: Omit<RequestOption, 'method'>) => request<T>({ ...option, method: 'GET' }),
  post: <T = any>(option: Omit<RequestOption, 'method'>) => request<T>({ ...option, method: 'POST' }),
  put: <T = any>(option: Omit<RequestOption, 'method'>) => request<T>({ ...option, method: 'PUT' }),
  delete: <T = any>(option: Omit<RequestOption, 'method'>) => request<T>({ ...option, method: 'DELETE' }),
  upload
}
