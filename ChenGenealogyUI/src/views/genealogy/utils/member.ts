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
