import { defineStore } from 'pinia'
import { AuthApi } from '@/api/login'
import { GenealogyMemberApi } from '@/api/genealogy'
import { getAccessToken, removeToken, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAccessToken() as string,
    user: null as any,
    member: null as any
  }),
  getters: {
    isLogin: (s) => !!s.token
  },
  actions: {
    restore() {
      this.token = getAccessToken()
    },
    async login(username: string, password: string) {
      const res: any = await AuthApi.login({ username, password, captchaVerification: '' })
      setToken(res)
      this.token = res.accessToken
      await this.fetchInfo()
    },
    async fetchInfo() {
      if (!this.token) return
      try {
        const info: any = await AuthApi.getInfo()
        this.user = info?.user || info
      } catch {}
      try {
        this.member = await GenealogyMemberApi.me()
      } catch {
        this.member = null
      }
    },
    async logout() {
      try {
        await AuthApi.logout()
      } catch {}
      removeToken()
      this.token = ''
      this.user = null
      this.member = null
    }
  }
})
