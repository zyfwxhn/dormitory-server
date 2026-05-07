/**
 * WebSocket 客户端 
 */

let socket = null
let reconnectTimer = null
let currentSid = null

export function connectWebSocket(sid) {
  if (!sid) return
  currentSid = sid

  const baseUrl = import.meta.env.VITE_APP_WS_URL
    || import.meta.env.VITE_APP_SERVER_URL
    || 'http://localhost:8080'
  const baseWs = baseUrl.replace(/^http/, 'ws')
  const url = `${baseWs}/ws/${sid}`

  socket = new WebSocket(url)

  socket.onopen = () => {
    console.log('[WS] 已连接到通知服务')
    clearReconnect()
  }

  socket.onmessage = (event) => {
    console.log('[WS] 收到推送:', event.data)
    // 广播自定义事件，各页面按需监听
    window.dispatchEvent(new CustomEvent('ws-message', { detail: event.data }))
  }

  socket.onclose = () => {
    console.log('[WS] 连接断开，3秒后重连...')
    scheduleReconnect()
  }

  socket.onerror = (e) => {
    console.error('[WS] 连接错误', e)
  }
}

function scheduleReconnect() {
  clearReconnect()
  reconnectTimer = setTimeout(() => {
    if (currentSid) connectWebSocket(currentSid)
  }, 3000)
}

function clearReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

export function disconnectWebSocket() {
  clearReconnect()
  currentSid = null
  if (socket) {
    socket.close()
    socket = null
  }
}
