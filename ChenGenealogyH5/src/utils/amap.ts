export const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || 'c17cafc389010ee0c19c23edd06976da'
export const AMAP_SECRET = import.meta.env.VITE_AMAP_SECRET || 'b7aced10c8f7e1d7f4c10e92b2c422db'

let loadPromise: Promise<any> | null = null

export const loadAmap = (timeout = 12000): Promise<any> => {
  if (typeof window === 'undefined') return Promise.reject(new Error('当前环境不支持地图'))
  if ((window as any).AMap) return Promise.resolve((window as any).AMap)
  if (loadPromise) return loadPromise
  ;(window as any)._AMapSecurityConfig = { securityJsCode: AMAP_SECRET }
  loadPromise = new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      loadPromise = null
      reject(new Error('高德地图加载超时'))
    }, timeout)
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.async = true
    script.onload = () => {
      clearTimeout(timer)
      if ((window as any).AMap) resolve((window as any).AMap)
      else {
        loadPromise = null
        reject(new Error('高德地图加载失败'))
      }
    }
    script.onerror = () => {
      clearTimeout(timer)
      loadPromise = null
      reject(new Error('高德地图加载失败'))
    }
    document.head.appendChild(script)
  })
  return loadPromise
}

export const amapNavUrl = (lng: number, lat: number, name?: string) =>
  `https://uri.amap.com/navigation?to=${lng},${lat},${encodeURIComponent(name || '目的地')}&mode=car&src=chengenealogy&callnative=1`

export const escapeHtml = (s?: string) =>
  String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')

/** 中国范围：默认全国，禁止缩小到世界地图 */
export const CHINA_CENTER: [number, number] = [104.195397, 35.86166]
export const CHINA_SW: [number, number] = [73.66, 17.8]
export const CHINA_NE: [number, number] = [135.05, 53.55]
export const CHINA_ZOOMS: [number, number] = [4, 18]

export const chinaMapOptions = () => ({
  zoom: 4,
  center: CHINA_CENTER,
  zooms: CHINA_ZOOMS,
  viewMode: '2D' as const
})

export const limitToChina = (AMap: any, map: any) => {
  if (!AMap || !map?.setLimitBounds) return
  map.setLimitBounds(new AMap.Bounds(CHINA_SW, CHINA_NE))
}

export type AmapPoint = {
  lng: number
  lat: number
  title?: string
  content?: string
  id?: number | string
}
