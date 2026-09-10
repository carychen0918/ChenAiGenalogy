/** 芋道 TerminalEnum：10 微信小程序 / 11 微信公众号 / 20 H5 */
export const getTerminal = (): number => {
  // #ifdef MP-WEIXIN
  return 10
  // #endif
  // #ifdef H5
  const ua = typeof navigator !== 'undefined' ? navigator.userAgent.toLowerCase() : ''
  if (ua.includes('micromessenger')) return 11
  return 20
  // #endif
  return 20
}
