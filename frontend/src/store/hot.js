import { defineStore } from 'pinia'

const WS_PATH = '/ws/hotdata'

function buildWsUrl() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  return `${proto}://${location.host}${WS_PATH}`
}

export const useHotStore = defineStore('hot', {
  state: () => ({
    snapshot: null,
    aggregate: [],
    trendCache: {},
    selected: null,
    connection: 'idle',
    lastError: null,
    ws: null,
    reconnectTimer: null,
    theme: localStorage.getItem('theme') || 'dark'
  }),

  getters: {
    boards: (s) => s.snapshot?.boards ?? [],
    timestamp: (s) => s.snapshot?.timestamp ?? 0
  },

  actions: {
    async loadSnapshot() {
      const r = await fetch('/api/hot/snapshot')
      if (!r.ok) throw new Error('snapshot failed')
      this.snapshot = await r.json()
    },

    async loadAggregate(top = 20) {
      const r = await fetch(`/api/hot/aggregate?top=${top}`)
      if (!r.ok) return
      this.aggregate = await r.json()
    },

    async loadTrend(platform, title) {
      const key = `${platform}::${title}`
      const r = await fetch(
        `/api/hot/trend?platform=${encodeURIComponent(platform)}&title=${encodeURIComponent(title)}`
      )
      if (!r.ok) return []
      const data = await r.json()
      this.trendCache[key] = data
      return data
    },

    async manualRefresh() {
      this.connection = 'refreshing'
      try {
        const r = await fetch('/api/hot/refresh', { method: 'POST' })
        if (r.ok) this.snapshot = await r.json()
        await this.loadAggregate()
      } finally {
        this.connection = this.ws && this.ws.readyState === 1 ? 'open' : 'idle'
      }
    },

    select(platform, title) {
      this.selected = { platform, title }
      const key = `${platform}::${title}`
      if (!this.trendCache[key] || this.trendCache[key].length === 0) {
        const board = this.snapshot?.boards?.find(b => b.platform === platform)
        const item = board?.items?.find(i => i.title === title)
        if (item) {
          this.trendCache[key] = [{
            timestamp: this.snapshot.timestamp || Date.now(),
            rank: item.rank,
            hotScore: item.hotScore ?? null,
            hotValue: item.hotValue ?? null
          }]
        }
      }
      this.loadTrend(platform, title)
    },

    connect() {
      if (this.ws) return
      this.connection = 'connecting'
      const ws = new WebSocket(buildWsUrl())
      this.ws = ws
      ws.onopen = () => { this.connection = 'open' }
      ws.onmessage = (ev) => {
        try {
          const snap = JSON.parse(ev.data)
          this.snapshot = snap
          this.loadAggregate()
          if (this.selected) {
            this.loadTrend(this.selected.platform, this.selected.title)
          }
        } catch (e) {
          this.lastError = e.message
        }
      }
      ws.onclose = () => {
        this.connection = 'closed'
        this.ws = null
        this.scheduleReconnect()
      }
      ws.onerror = () => {
        this.lastError = 'websocket error'
      }
    },

    scheduleReconnect() {
      if (this.reconnectTimer) return
      this.reconnectTimer = setTimeout(() => {
        this.reconnectTimer = null
        this.connect()
      }, 5000)
    },

    setTheme(theme) {
      this.theme = theme
      localStorage.setItem('theme', theme)
      document.documentElement.setAttribute('data-theme', theme)
    }
  }
})
