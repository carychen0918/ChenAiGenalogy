import { formatGenerationWord } from '@/views/genealogy/utils/generation'

export const isDeceased = (m?: { alive?: boolean | number | null }) =>
  m?.alive === false || m?.alive === 0

export const formatLifeSpan = (m?: {
  birthDate?: string | number | Date | null
  deathDate?: string | number | Date | null
  alive?: boolean | number | null
}) => {
  if (!m) return ''
  const birth = m.birthDate ? String(m.birthDate).slice(0, 4) : '?'
  if (isDeceased(m)) {
    const death = m.deathDate ? String(m.deathDate).slice(0, 4) : '?'
    return `${birth} — ${death}`
  }
  return `${birth} — 今`
}

export const memberOptionLabel = (m?: {
  name?: string
  generationNo?: number
  generationWord?: string
  generationHouse?: string
  generationNationalSource?: string
  gender?: number
}) => {
  if (!m?.name) return ''
  const gen = formatGenerationWord(m)
  if (gen) return `${m.name} · ${gen}字辈`
  if (m.generationNo) return `${m.name} · ${m.generationNo}世`
  if (m.gender === 2) return `${m.name} · 配偶`
  return m.name
}

/** 同名成员的可区分标签：几世 / 字辈 / 父名 / 生卒 */
export const lineagePickLabel = (m?: any, byId?: Map<number, any>) => {
  if (!m?.name) return ''
  const parts = [m.name]
  if (m.generationNo) parts.push(m.generationNo + '世')
  const word = formatGenerationWord(m)
  if (word) parts.push(word + '字辈')
  const father = m.fatherName || (m.fatherId != null ? byId?.get(Number(m.fatherId))?.name : '')
  if (father) parts.push('父' + father)
  else if (m.gender === 2 && !m.fatherId) parts.push('配偶')
  if (m.birthDate) parts.push(formatLifeSpan(m))
  return parts.join(' · ')
}

export const paternalLineage = (id: number | string | undefined | null, members: any[]) => {
  if (id == null || !members?.length) return [] as any[]
  const byId = new Map(members.map((m) => [Number(m.id), m]))
  const chain: any[] = []
  let cur = byId.get(Number(id))
  const seen = new Set<number>()
  while (cur && !seen.has(Number(cur.id))) {
    chain.push(cur)
    seen.add(Number(cur.id))
    if (!cur.fatherId) break
    cur = byId.get(Number(cur.fatherId))
  }
  return chain
}

export const isSpouseOnlyMember = (m?: {
  gender?: number
  generationId?: number | null
  generationNo?: number | null
  generationWord?: string | null
  fatherId?: number | null
}) =>
  m?.gender === 2 &&
  !m?.generationId &&
  !m?.generationNo &&
  !m?.generationWord &&
  !m?.fatherId
