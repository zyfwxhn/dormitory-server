const pad = (n) => String(n).padStart(2, '0')

/**
 * 格式化时间，输出 "yyyy-MM-dd HH:mm"
 */
export const formatTime = (t) => {
  if (!t) return ''

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

/**
 * 相对时间：刚刚 / X分钟前 / X小时前 / MM-dd HH:mm / yyyy-MM-dd
 */
export const formatRelativeTime = (t) => {
  if (!t) return ''
  const s = String(t)
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (!m) return formatTime(t)

  const d = new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5])
  const now = new Date()
  const diff = now - d
  if (diff < 0) return formatTime(t)
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (d.getFullYear() === now.getFullYear()) return `${pad(m[2])}-${pad(m[3])} ${m[4]}:${m[5]}`
  return `${m[1]}-${m[2]}-${m[3]}`
}