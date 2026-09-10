import request from '@/config/axios'

export const GenealogyMemberApi = {
  getPage: (params: any) => request.get({ url: '/genealogy/member/page', params }),
  get: (id: number) => request.get({ url: '/genealogy/member/get?id=' + id }),
  create: (data: any) => request.post({ url: '/genealogy/member/create', data }),
  update: (data: any) => request.put({ url: '/genealogy/member/update', data }),
  delete: (id: number, confirm?: boolean) =>
    request.delete({ url: '/genealogy/member/delete', params: { id, confirm } }),
  restore: (id: number) => request.put({ url: '/genealogy/member/restore?id=' + id }),
  recycleList: () => request.get({ url: '/genealogy/member/recycle-list' }),
  simpleList: () => request.get({ url: '/genealogy/member/simple-list' }),
  tree: (params?: any) => request.get({ url: '/genealogy/member/tree', params }),
  me: () => request.get({ url: '/genealogy/member/me' }),
  updateMyPhotos: (photoUrls: string[]) =>
    request.put({ url: '/genealogy/member/me/photos', data: { photoUrls } }),
  createMyDeed: (data: any) => request.post({ url: '/genealogy/member/me/deed', data }),
  updateMyDeed: (data: any) => request.put({ url: '/genealogy/member/me/deed', data }),
  deleteMyDeed: (id: number) => request.delete({ url: '/genealogy/member/me/deed', params: { id } }),
  createDeed: (data: any) => request.post({ url: '/genealogy/member/deed', data }),
  updateDeed: (data: any) => request.put({ url: '/genealogy/member/deed', data }),
  deleteDeed: (id: number) => request.delete({ url: '/genealogy/member/deed', params: { id } }),
  updateArchive: (data: any) => request.put({ url: '/genealogy/member/archive', data }),
  importTemplate: () => request.download({ url: '/genealogy/member/get-import-template' }),
  import: (file: File, updateSupport: boolean) => {
    const form = new FormData()
    form.append('file', file)
    return request.upload({ url: '/genealogy/member/import?updateSupport=' + updateSupport, data: form })
  },
  export: (params: any) => request.download({ url: '/genealogy/member/export-excel', params }),
  createArchiveApply: (data: any) => request.post({ url: '/genealogy/member/archive-apply/create', data }),
  auditArchive: (id: number, status: number, reason?: string) =>
    request.put({ url: '/genealogy/member/archive-apply/audit', params: { id, status, reason } }),
  archivePage: (params: any) => request.get({ url: '/genealogy/member/archive-apply/page', params })
}

export const GenealogyContentApi = {
  getFamily: () => request.get({ url: '/genealogy/content/family/get' }),
  updateFamily: (data: any) => request.put({ url: '/genealogy/content/family/update', data }),
  generationList: () => request.get({ url: '/genealogy/content/generation/list' }),
  recommend: (fatherGenerationNo: number) =>
    request.get({ url: '/genealogy/content/generation/recommend', params: { fatherGenerationNo } }),
  createGeneration: (data: any) => request.post({ url: '/genealogy/content/generation/create', data }),
  updateGeneration: (data: any) => request.put({ url: '/genealogy/content/generation/update', data }),
  deleteGeneration: (id: number) => request.delete({ url: '/genealogy/content/generation/delete?id=' + id }),
  migrationList: () => request.get({ url: '/genealogy/content/migration/list' }),
  createMigration: (data: any) => request.post({ url: '/genealogy/content/migration/create', data }),
  updateMigration: (data: any) => request.put({ url: '/genealogy/content/migration/update', data }),
  deleteMigration: (id: number) => request.delete({ url: '/genealogy/content/migration/delete?id=' + id }),
  deedPage: (params: any) => request.get({ url: '/genealogy/content/deed/page', params }),
  getDeed: (id: number) => request.get({ url: '/genealogy/content/deed/get?id=' + id }),
  createDeed: (data: any) => request.post({ url: '/genealogy/content/deed/create', data }),
  updateDeed: (data: any) => request.put({ url: '/genealogy/content/deed/update', data }),
  deleteDeed: (id: number) => request.delete({ url: '/genealogy/content/deed/delete?id=' + id }),
  cultureList: () => request.get({ url: '/genealogy/content/culture/list' }),
  createCulture: (data: any) => request.post({ url: '/genealogy/content/culture/create', data }),
  updateCulture: (data: any) => request.put({ url: '/genealogy/content/culture/update', data }),
  deleteCulture: (id: number) => request.delete({ url: '/genealogy/content/culture/delete?id=' + id }),
  getTomb: () => request.get({ url: '/genealogy/content/tomb/get' }),
  saveTomb: (data: any) => request.put({ url: '/genealogy/content/tomb/save', data })
}

export const GenealogyScholarshipApi = {
  getConfig: (year?: number) => request.get({ url: '/genealogy/scholarship/config/get', params: { year } }),
  saveConfig: (data: any) => request.put({ url: '/genealogy/scholarship/config/save', data }),
  create: (data: any) => request.post({ url: '/genealogy/scholarship/create', data }),
  submit: (data: any) => request.post({ url: '/genealogy/scholarship/submit', data }),
  withdraw: (id: number) => request.put({ url: '/genealogy/scholarship/withdraw?id=' + id }),
  page: (params: any) => request.get({ url: '/genealogy/scholarship/page', params }),
  myPage: (params: any) => request.get({ url: '/genealogy/scholarship/my-page', params }),
  get: (id: number) => request.get({ url: '/genealogy/scholarship/get?id=' + id }),
  audit: (id: number, result: number, opinion?: string) =>
    request.put({ url: '/genealogy/scholarship/audit', params: { id, result, opinion } }),
  firstAudit: (id: number, result: number, opinion?: string) =>
    request.put({ url: '/genealogy/scholarship/first-audit', params: { id, result, opinion } }),
  batchFirst: (ids: number[], result: number, opinion?: string) =>
    request.put({ url: '/genealogy/scholarship/first-audit-batch', params: { ids: ids.join(','), result, opinion } }),
  finalAudit: (id: number, result: number, opinion?: string) =>
    request.put({ url: '/genealogy/scholarship/final-audit', params: { id, result, opinion } }),
  disburse: (data: any) => request.post({ url: '/genealogy/scholarship/disburse', data }),
  disbursePage: (params: any) => request.get({ url: '/genealogy/scholarship/disburse/page', params }),
  disburseExport: (params: any) => request.download({ url: '/genealogy/scholarship/disburse/export-excel', params }),
  stats: (year?: number) => request.get({ url: '/genealogy/scholarship/stats', params: { year } })
}

export const GenealogyAdminRegionApi = {
  list: () => request.get({ url: '/genealogy/admin-region/list' }),
  save: (data: { userId: number; areaId: number }) =>
    request.put({ url: '/genealogy/admin-region/save', data })
}

export const GenealogyActivityApi = {
  create: (data: any) => request.post({ url: '/genealogy/activity/create', data }),
  update: (data: any) => request.put({ url: '/genealogy/activity/update', data }),
  delete: (id: number) => request.delete({ url: '/genealogy/activity/delete?id=' + id }),
  cancel: (id: number) => request.put({ url: '/genealogy/activity/cancel?id=' + id }),
  get: (id: number) => request.get({ url: '/genealogy/activity/get?id=' + id }),
  page: (params: any) => request.get({ url: '/genealogy/activity/page', params }),
  register: (data: any) => request.post({ url: '/genealogy/activity/register', data }),
  myRegistrations: () => request.get({ url: '/genealogy/activity/registration/mine' }),
  registrations: (activityId: number) =>
    request.get({ url: '/genealogy/activity/registration/list', params: { activityId } }),
  exportReg: (activityId: number) =>
    request.download({ url: '/genealogy/activity/registration/export-excel', params: { activityId } }),
  createWorship: (data: any) => request.post({ url: '/genealogy/activity/worship/create', data }),
  deleteWorship: (id: number) => request.delete({ url: '/genealogy/activity/worship/delete?id=' + id }),
  pinWorship: (id: number, pinned: boolean) =>
    request.put({ url: '/genealogy/activity/worship/pin', params: { id, pinned } }),
  worshipList: (activityId?: number) =>
    request.get({ url: '/genealogy/activity/worship/list', params: { activityId } })
}

export const GenealogyFeedApi = {
  create: (data: any) => request.post({ url: '/genealogy/feed/create', data }),
  update: (data: any) => request.put({ url: '/genealogy/feed/update', data }),
  delete: (id: number) => request.delete({ url: '/genealogy/feed/delete?id=' + id }),
  audit: (id: number, status: number) => request.put({ url: '/genealogy/feed/audit', params: { id, status } }),
  pin: (id: number, pinned: boolean) => request.put({ url: '/genealogy/feed/pin', params: { id, pinned } }),
  page: (params: any) => request.get({ url: '/genealogy/feed/page', params }),
  like: (id: number) => request.post({ url: '/genealogy/feed/like?id=' + id }),
  comment: (feedId: number, content: string) =>
    request.post({ url: '/genealogy/feed/comment/create', params: { feedId, content } }),
  auditComment: (id: number, status: number) =>
    request.put({ url: '/genealogy/feed/comment/audit', params: { id, status } }),
  commentPage: (params: any) => request.get({ url: '/genealogy/feed/comment/page', params }),
  comments: (feedId: number) => request.get({ url: '/genealogy/feed/comment/list', params: { feedId } })
}

export const GenealogyDashboardApi = {
  summary: () => request.get({ url: '/genealogy/dashboard/summary' })
}

export const GenealogyAiMatchApi = {
  createSession: () => request.post({ url: '/genealogy/ai-match/session' }),
  getSession: (sessionId: string) =>
    request.get({ url: '/genealogy/ai-match/session/get', params: { sessionId } }),
  listConversations: () => request.get({ url: '/genealogy/ai-match/conversation/list' }),
  getConversation: (id: number) =>
    request.get({ url: '/genealogy/ai-match/conversation/get', params: { id } }),
  createConversation: () => request.post({ url: '/genealogy/ai-match/conversation/create' }),
  deleteConversation: (id: number) =>
    request.delete({ url: '/genealogy/ai-match/conversation/delete', params: { id } }),
  chatStream: async (
    sessionId: string,
    content: string,
    ctrl: AbortController,
    onMessage: (ev: any) => void,
    onError: (err: any) => void,
    onClose: () => void
  ) => {
    const { fetchEventSource } = await import('@microsoft/fetch-event-source')
    const { config } = await import('@/config/axios/config')
    const { getAccessToken } = await import('@/utils/auth')
    const clientId = ensureAiMatchClientId()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }
    const token = getAccessToken()
    if (token) headers.Authorization = `Bearer ${token}`
    return fetchEventSource(`${config.base_url}/genealogy/ai-match/chat-stream`, {
      method: 'post',
      headers,
      openWhenHidden: true,
      body: JSON.stringify({ sessionId, content, clientId }),
      async onopen(res) {
        if (res.ok) return
        let msg = '分析失败'
        try {
          const json = JSON.parse(await res.text())
          msg = json.msg || msg
        } catch {
          /* ignore */
        }
        throw new Error(msg)
      },
      onmessage: onMessage,
      onerror: onError,
      onclose: onClose,
      signal: ctrl.signal
    })
  }
}

const AI_MATCH_CLIENT_KEY = 'genealogy-ai-match-client-id'

export const ensureAiMatchClientId = () => {
  let id = localStorage.getItem(AI_MATCH_CLIENT_KEY)
  if (!id) {
    id = crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    localStorage.setItem(AI_MATCH_CLIENT_KEY, id)
  }
  return id
}
