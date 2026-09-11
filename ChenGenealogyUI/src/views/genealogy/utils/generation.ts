export const HOUSE_LABEL: Record<string, string> = {
  '1': '长房',
  '2': '二房',
  '3': '三房',
  '4': '三房织金',
  '5': '四五房'
}

export const houseLabel = (house?: string) => {
  if (!house) return ''
  return HOUSE_LABEL[house] || house
}

const CN_GEN = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
export const chituOrder = (no?: number) => {
  if (!no || no <= 0) return ''
  if (no <= 10) return `${CN_GEN[no]}世`
  return `${no}世`
}

export const POEM_COLUMNS = [
  { key: 'chituOrder', label: '赤土官庄世序' },
  { key: 'nationalSource', label: '全国统一字派' },
  { key: 'jiaofangSource', label: '高安椒坊字派' },
  { key: 'house1', label: '长房' },
  { key: 'house2', label: '二房' },
  { key: 'house3', label: '三房' },
  { key: 'house3Zhijin', label: '三房织金' },
  { key: 'house45', label: '四五房' }
] as const

export const poemCell = (v?: string) => (v && String(v).trim() ? String(v).trim() : '—')

export const formatGenerationWord = (item?: {
  word?: string
  generationWord?: string
  house?: string
  generationHouse?: string
  nationalSource?: string
  generationNationalSource?: string
  remark?: string
}) => {
  if (!item) return ''
  const word = item.word || item.generationWord || ''
  const house = houseLabel(item.house || item.generationHouse)
  const src = item.nationalSource || item.generationNationalSource || ''
  const parts = [word, house]
  if (src && src !== word) parts.push('源' + src)
  return parts.filter(Boolean).join(' · ')
}

export const formatMemberGeneration = (m?: {
  generationNo?: number
  generationWord?: string
  generationHouse?: string
  generationNationalSource?: string
}) => {
  if (!m) return ''
  const no = m.generationNo ? m.generationNo + '世' : ''
  const word = formatGenerationWord(m)
  if (no && word) return `${no} · ${word}字辈`
  if (no) return no
  return word ? word + '字辈' : ''
}
