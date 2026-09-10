import { getTerminal } from '@/utils/terminal'

export const getBaseUrl = () => {
  const prefix = import.meta.env.VITE_APP_API_PREFIX || '/app-api'
  // #ifdef H5
  if (import.meta.env.DEV) return prefix
  // #endif
  return (import.meta.env.VITE_APP_BASE_URL || '') + prefix
}

export const tenantEnable = () => import.meta.env.VITE_APP_TENANT_ENABLE === 'true'

export { getTerminal }
