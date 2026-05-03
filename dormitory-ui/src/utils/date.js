const pad = (n) => String(n).padStart(2, '0')

/**
 * 格式化时间，输出 "yyyy-MM-dd HH:mm"
 * 兼容多种后端格式：
 *   "2026-05-02 21:39:18"      — Jackson with date-format
 *   "2026-05-02T21:39:18"      — ISO 8601
 *   "2026,5,2,21,39,18"        — 未加载jsr310模块时的数组toString
 *   [2026,5,2,21,39,18]        — 本身就是数组
 */
export const formatTime = (t) => {
  if (!t) return ''

  // 如果是数组（Jackson未加载jsr310时LocalDateTime序列化为数组）
  if (Array.isArray(t) && t.length >= 5) {
    return `${t[0]}-${pad(t[1])}-${pad(t[2])} ${pad(t[3])}:${pad(t[4])}`
  }

  const s = String(t)

  // 逗号分隔的数组toString: "2026,5,2,21,39,18"
  const commaMatch = s.match(/^(\d{4}),(\d{1,2}),(\d{1,2}),(\d{1,2}),(\d{1,2})/)
  if (commaMatch) {
    return `${commaMatch[1]}-${pad(+commaMatch[2])}-${pad(+commaMatch[3])} ${pad(+commaMatch[4])}:${pad(+commaMatch[5])}`
  }

  // ISO / date-format: "yyyy-MM-ddTHH:mm" 或 "yyyy-MM-dd HH:mm"
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (m) {
    return `${m[1]}-${m[2]}-${m[3]} ${m[4]}:${m[5]}`
  }

  return s
}
