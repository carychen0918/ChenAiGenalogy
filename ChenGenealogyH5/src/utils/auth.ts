const ACCESS_TOKEN = 'ACCESS_TOKEN'
const REFRESH_TOKEN = 'REFRESH_TOKEN'
const TENANT_ID = 'TENANT_ID'

export const getAccessToken = () => uni.getStorageSync(ACCESS_TOKEN) || ''
export const getRefreshToken = () => uni.getStorageSync(REFRESH_TOKEN) || ''
export const getTenantId = () => uni.getStorageSync(TENANT_ID) || import.meta.env.VITE_APP_TENANT_ID || '1'

export const setToken = (token: { accessToken: string; refreshToken?: string }) => {
  uni.setStorageSync(ACCESS_TOKEN, token.accessToken)
  if (token.refreshToken) uni.setStorageSync(REFRESH_TOKEN, token.refreshToken)
}

export const removeToken = () => {
  uni.removeStorageSync(ACCESS_TOKEN)
  uni.removeStorageSync(REFRESH_TOKEN)
}

export const setTenantId = (id: string | number) => uni.setStorageSync(TENANT_ID, String(id))
export const isLoggedIn = () => !!getAccessToken()
