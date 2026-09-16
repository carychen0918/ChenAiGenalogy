import request from '@/utils/request'

export const GenealogyMemberApi = {
  me: () => request.get({ url: '/genealogy/member/me' }),
  get: (id: number) => request.get({ url: '/genealogy/member/get', params: { id } }),
  tree: (params?: any) => request.get({ url: '/genealogy/member/tree', params }),
  simpleList: () => request.get({ url: '/genealogy/member/simple-list' }),
  updateMyPhotos: (photoUrls: string[]) =>
    request.put({ url: '/genealogy/member/me/photos', data: { photoUrls } }),
  createMyDeed: (data: any) => request.post({ url: '/genealogy/member/me/deed', data }),
  updateMyDeed: (data: any) => request.put({ url: '/genealogy/member/me/deed', data }),
  deleteMyDeed: (id: number) => request.delete({ url: '/genealogy/member/me/deed', params: { id } }),
  createArchiveApply: (data: any) => request.post({ url: '/genealogy/member/archive-apply/create', data })
}

export const GenealogyContentApi = {
  getFamily: () => request.get({ url: '/genealogy/content/family/get', isToken: false }),
  generationList: () => request.get({ url: '/genealogy/content/generation/list', isToken: false }),
  poemTable: () => request.get({ url: '/genealogy/content/generation/poem-table', isToken: false }),
  recommend: (fatherGenerationNo: number) =>
    request.get({ url: '/genealogy/content/generation/recommend', params: { fatherGenerationNo }, isToken: false }),
  migrationList: () => request.get({ url: '/genealogy/content/migration/list', isToken: false }),
  deedPage: (params: any) => request.get({ url: '/genealogy/content/deed/page', params, isToken: false }),
  getDeed: (id: number) => request.get({ url: '/genealogy/content/deed/get', params: { id }, isToken: false }),
  cultureList: () => request.get({ url: '/genealogy/content/culture/list', isToken: false }),
  getTomb: () => request.get({ url: '/genealogy/content/tomb/get' })
}

export const GenealogyBookApi = {
  meta: () => request.get({ url: '/genealogy/book/meta', isToken: false }),
  page: (pageNo: number) => request.get({ url: '/genealogy/book/page', params: { pageNo } }),
  search: (keyword: string) => request.get({ url: '/genealogy/book/search', params: { keyword } })
}

export const GenealogyScholarshipApi = {
  getConfig: (year?: number) => request.get({ url: '/genealogy/scholarship/config/get', params: { year } }),
  create: (data: any) => request.post({ url: '/genealogy/scholarship/create', data }),
  submit: (data: any) => request.post({ url: '/genealogy/scholarship/submit', data }),
  withdraw: (id: number) => request.put({ url: '/genealogy/scholarship/withdraw', params: { id } }),
  myPage: (params: any) => request.get({ url: '/genealogy/scholarship/my-page', params }),
  get: (id: number) => request.get({ url: '/genealogy/scholarship/get', params: { id } })
}

export const GenealogyActivityApi = {
  get: (id: number) => request.get({ url: '/genealogy/activity/get', params: { id } }),
  page: (params: any) => request.get({ url: '/genealogy/activity/page', params }),
  register: (data: any) => request.post({ url: '/genealogy/activity/register', data }),
  myRegistrations: () => request.get({ url: '/genealogy/activity/registration/mine' }),
  createWorship: (data: any) => request.post({ url: '/genealogy/activity/worship/create', data }),
  worshipList: (activityId?: number) =>
    request.get({ url: '/genealogy/activity/worship/list', params: { activityId } })
}

export const GenealogyShowcaseApi = {
  home: () => request.get({ url: '/genealogy/showcase/home', isToken: false }),
  gallery: (limit?: number) =>
    request.get({ url: '/genealogy/showcase/gallery', params: { limit }, isToken: false }),
  calendar: (year?: number, month?: number) =>
    request.get({ url: '/genealogy/showcase/calendar', params: { year, month }, isToken: false }),
  deeds: () => request.get({ url: '/genealogy/showcase/deeds', isToken: false }),
  scholarshipWall: () => request.get({ url: '/genealogy/showcase/scholarship-wall', isToken: false }),
  search: (keyword: string) =>
    request.get({ url: '/genealogy/showcase/search', params: { keyword }, isToken: false }),
  relation: (fromId: number, toId: number) =>
    request.get({ url: '/genealogy/showcase/relation', params: { fromId, toId } }),
  miniFamily: (memberId: number) =>
    request.get({ url: '/genealogy/showcase/mini-family', params: { memberId }, isToken: false })
}

export const GenealogyFeedApi = {
  create: (data: any) => request.post({ url: '/genealogy/feed/create', data }),
  page: (params: any) => request.get({ url: '/genealogy/feed/page', params }),
  like: (id: number) => request.post({ url: '/genealogy/feed/like', params: { id } }),
  comment: (feedId: number, content: string) =>
    request.post({ url: '/genealogy/feed/comment/create', params: { feedId, content } }),
  comments: (feedId: number) =>
    request.get({ url: '/genealogy/feed/comment/list', params: { feedId }, isToken: false })
}

const CLIENT_KEY = 'genealogy-ai-match-client-id'
export const ensureAiMatchClientId = () => {
  let id = uni.getStorageSync(CLIENT_KEY)
  if (!id) {
    id = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    uni.setStorageSync(CLIENT_KEY, id)
  }
  return id
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
  chat: (sessionId: string, content: string) =>
    request.post({
      url: '/genealogy/ai-match/chat',
      data: { sessionId, content, clientId: ensureAiMatchClientId() },
      timeout: 120000
    })
}
