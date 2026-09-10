import request from '@/utils/request'

export const AuthApi = {
  login: (data: { username: string; password: string; captchaVerification?: string }) =>
    request.post({ url: '/system/auth/login', data, isToken: false }),
  logout: () => request.post({ url: '/system/auth/logout' }),
  getInfo: () => request.get({ url: '/system/auth/get-permission-info' }),
  getTenantIdByName: (name: string) =>
    request.get({ url: '/system/tenant/get-id-by-name', params: { name }, isToken: false })
}
