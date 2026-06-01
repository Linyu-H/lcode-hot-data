<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useHotStore } from './store/hot'
import HotBoard from './components/HotBoard.vue'
import AggregateBoard from './components/AggregateBoard.vue'
import TrendChart from './components/TrendChart.vue'
import StatsChart from './components/StatsChart.vue'
import logoUrl from '../hot.logo.png'

const store = useHotStore()
const { boards, snapshot, aggregate, selected, connection, selectedTrend } = storeToRefs(store)

const activeTab = ref('all')
const darkMode = ref(false)
const searchQuery = ref('')
const onlyWithData = ref(localStorage.getItem('onlyWithData') === 'true')
const compactMode = ref(localStorage.getItem('compactMode') === 'true')

onMounted(async () => {
  // 读取本地存储的主题设置
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    darkMode.value = true
    document.documentElement.classList.add('dark')
    document.documentElement.setAttribute('data-theme', 'dark')
  }

  await store.loadSnapshot()
  await store.loadAggregate()
  store.connect()
})

function toggleDarkMode() {
  darkMode.value = !darkMode.value
  if (darkMode.value) {
    document.documentElement.classList.add('dark')
    document.documentElement.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    document.documentElement.setAttribute('data-theme', 'light')
    localStorage.setItem('theme', 'light')
  }
}

const tabs = computed(() => {
  const allBoards = boards.value
  const makeTab = (id, name, list) => ({
    id,
    name,
    boards: list,
    sourceCount: list.length,
    itemCount: list.reduce((sum, b) => sum + b.items.length, 0)
  })
  return [
    makeTab('all', '全部', allBoards),
    makeTab('dev', '开发者', allBoards.filter(b => b.category === 'dev')),
    makeTab('tech', '科技', allBoards.filter(b => b.category === 'tech')),
    makeTab('finance', '财经', allBoards.filter(b => b.category === 'finance')),
    makeTab('news', '新闻', allBoards.filter(b => b.category === 'news')),
    makeTab('entertainment', '娱乐', allBoards.filter(b => b.category === 'entertainment' || b.category === 'game')),
    makeTab('forum', '论坛', allBoards.filter(b => b.category === 'forum')),
    makeTab('welfare', '羊毛福利', allBoards.filter(b => b.category === 'welfare' || b.category === 'shopping')),
    makeTab('comprehensive', '综合', allBoards.filter(b => b.category === 'comprehensive'))
  ]
})

const currentBoards = computed(() => {
  const tab = tabs.value.find(t => t.id === activeTab.value)
  let list = tab ? [...tab.boards] : []
  const keyword = searchQuery.value.trim().toLowerCase()
  if (onlyWithData.value) {
    list = list.filter(b => b.items.length > 0)
  }
  if (keyword) {
    list = list.filter(b => {
      const boardHit = `${b.platformName} ${b.platform} ${b.category}`.toLowerCase().includes(keyword)
      const itemHit = b.items.some(i => `${i.title} ${i.extra || ''} ${i.hotValue || ''}`.toLowerCase().includes(keyword))
      return boardHit || itemHit
    }).map(b => ({
      ...b,
      items: b.items.filter(i => {
        if (`${b.platformName} ${b.platform} ${b.category}`.toLowerCase().includes(keyword)) return true
        return `${i.title} ${i.extra || ''} ${i.hotValue || ''}`.toLowerCase().includes(keyword)
      })
    }))
  }
  return list.sort((a, b) => {
    if (a.items.length === 0 && b.items.length > 0) return 1
    if (a.items.length > 0 && b.items.length === 0) return -1
    if (a.fromCache !== b.fromCache) return a.fromCache ? 1 : -1
    return b.items.length - a.items.length
  })
})

const totalCount = computed(() => {
  return boards.value.reduce((sum, b) => sum + b.items.length, 0)
})

const statusText = {
  idle: '未连接',
  connecting: '连接中',
  open: '实时',
  refreshing: '刷新中',
  closed: '已断开'
}

function pickItem(payload) {
  store.select(payload.platform, payload.title)
}

function refresh() {
  store.manualRefresh()
}

function toggleOnlyWithData() {
  onlyWithData.value = !onlyWithData.value
  localStorage.setItem('onlyWithData', String(onlyWithData.value))
}

function toggleCompactMode() {
  compactMode.value = !compactMode.value
  localStorage.setItem('compactMode', String(compactMode.value))
}

// 判断是否是"全部"页面
const isAllView = computed(() => activeTab.value === 'all')
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="container">
        <div class="header-content">
          <div class="brand">
            <img class="brand-logo" :src="logoUrl" alt="Lcode HotData" />
            <div class="brand-copy">
              <h1 class="logo">Lcode</h1>
              <span class="tagline">热点聚合</span>
            </div>
          </div>
          <div class="header-actions">
            <div class="stats">
              <span class="stat">{{ boards.filter(b => b.items.length > 0).length }} 源</span>
              <span class="divider">·</span>
              <span class="stat">{{ totalCount }} 条</span>
            </div>
            <span class="status" :class="'status-' + connection">
              <i></i>{{ statusText[connection] }}
            </span>
            <button class="btn-theme" @click="toggleDarkMode" :title="darkMode ? '切换到浅色模式' : '切换到深色模式'">
              {{ darkMode ? '☀️' : '🌙' }}
            </button>
            <button class="btn-refresh" @click="refresh" :disabled="connection === 'refreshing'">
              <span v-if="connection === 'refreshing'" class="refresh-spinner"></span>
              <span>{{ connection === 'refreshing' ? '刷新中' : '刷新' }}</span>
            </button>
          </div>
        </div>
      </div>
    </header>

    <nav class="nav">
      <div class="container">
        <div class="nav-scroll">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            class="nav-item"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.name }}
            <span class="badge">{{ tab.sourceCount }}源 / {{ tab.itemCount }}条</span>
          </button>
        </div>
      </div>
    </nav>

    <main class="main">
      <div class="container">
        <section class="toolbar">
          <div class="search-wrap">
            <span class="search-icon">⌕</span>
            <input
              v-model="searchQuery"
              class="search-input"
              type="search"
              placeholder="搜索数据源 / 热点标题 / 分类"
            />
          </div>
          <div class="toolbar-actions">
            <button class="filter-pill" :class="{ active: onlyWithData }" @click="toggleOnlyWithData">
              只看有数据
            </button>
            <button class="filter-pill" :class="{ active: compactMode }" @click="toggleCompactMode">
              紧凑模式
            </button>
          </div>
        </section>

        <!-- 全部页面：特殊布局 -->
        <div v-if="isAllView" class="all-view">
          <div class="boards-masonry">
            <HotBoard
              v-for="board in currentBoards"
              :key="board.platform"
              :board="board"
              :selected="selected"
              :compact="compactMode"
              @pick="pickItem"
            />
          </div>

          <aside class="charts-sidebar">
            <StatsChart :boards="boards" />
            <TrendChart :selected="selected" :data="selectedTrend" />
            <AggregateBoard :items="aggregate" />
          </aside>
        </div>

        <!-- 其他分类页面：原布局 -->
        <div v-else class="layout">
          <div class="boards">
            <HotBoard
              v-for="board in currentBoards"
              :key="board.platform"
              :board="board"
              :selected="selected"
              :compact="compactMode"
              @pick="pickItem"
            />
          </div>

          <aside class="sidebar">
            <TrendChart :selected="selected" :data="selectedTrend" />
            <AggregateBoard :items="aggregate" />
          </aside>
        </div>
      </div>
    </main>

    <footer class="footer">
      <div class="container">
        <p class="copyright">© 2026 Lcode · 热点聚合平台 · 仅供学习交流</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  transition: background 0.3s ease;
}

.dark .app {
  background: #000;
}

.container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

/* Header - Apple风格 */
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .header {
  background: rgba(29, 29, 31, 0.8);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.header-content {
  height: 52px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(0, 113, 227, 0.18);
}

.brand-copy {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.logo {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.02em;
  transition: color 0.3s ease;
}

.dark .logo {
  color: #f5f5f7;
}

.tagline {
  font-size: 13px;
  color: #86868b;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .tagline {
  color: #a1a1a6;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #86868b;
  transition: color 0.3s ease;
}

.dark .stats {
  color: #a1a1a6;
}

.stat {
  font-weight: 400;
}

.divider {
  color: #d2d2d7;
}

.dark .divider {
  color: #424245;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  background: #f5f5f7;
  color: #86868b;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
}

.status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.75;
}

.dark .status {
  background: #1d1d1f;
  color: #a1a1a6;
}

.status-open {
  background: #d1f4e0;
  color: #0d7a3f;
}

.dark .status-open {
  background: #0d3818;
  color: #30d158;
}

.status-refreshing {
  background: #fff4e5;
  color: #c77700;
}

.dark .status-refreshing {
  background: #2d1f00;
  color: #ffd60a;
}

.btn-theme {
  padding: 6px 10px;
  background: #f5f5f7;
  border-radius: 12px;
  font-size: 16px;
  transition: background 0.2s ease;
}

.dark .btn-theme {
  background: #1d1d1f;
}

.btn-theme:hover {
  background: #e8e8ed;
}

.dark .btn-theme:hover {
  background: #2d2d2f;
}

.btn-refresh {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 6px 14px;
  background: #0071e3;
  color: #fff;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s ease, transform 0.2s ease;
}

.btn-refresh:hover:not(:disabled) {
  background: #0077ed;
  transform: translateY(-1px);
}

.refresh-spinner {
  width: 13px;
  height: 13px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  animation: refresh-spin 0.8s linear infinite;
}

@keyframes refresh-spin {
  to { transform: rotate(360deg); }
}

.btn-refresh:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Navigation */
.nav {
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .nav {
  background: #1d1d1f;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.nav-scroll {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding: 8px 0;
  -webkit-overflow-scrolling: touch;
}

.nav-scroll::-webkit-scrollbar {
  display: none;
}

.nav-item {
  padding: 6px 14px;
  font-size: 13px;
  color: #1d1d1f;
  border-radius: 16px;
  transition: background 0.2s ease, color 0.3s ease;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 400;
}

.dark .nav-item {
  color: #f5f5f7;
}

.nav-item:hover {
  background: #f5f5f7;
}

.dark .nav-item:hover {
  background: #2d2d2f;
}

.nav-item.active {
  background: #e8e8ed;
  font-weight: 500;
}

.dark .nav-item.active {
  background: #3d3d3f;
}

.badge {
  font-size: 11px;
  color: #86868b;
  background: rgba(0, 0, 0, 0.04);
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .badge {
  color: #a1a1a6;
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.active .badge {
  background: rgba(0, 0, 0, 0.08);
  color: #1d1d1f;
}

.dark .nav-item.active .badge {
  background: rgba(255, 255, 255, 0.15);
  color: #f5f5f7;
}

/* Main */
.main {
  flex: 1;
  padding: 24px 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 20px;
}

.search-wrap {
  flex: 1;
  min-width: 0;
  height: 42px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .search-wrap {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.1);
}

.search-icon {
  color: #86868b;
  font-size: 18px;
}

.search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #1d1d1f;
  font-size: 14px;
  font-family: inherit;
}

.dark .search-input {
  color: #f5f5f7;
}

.search-input::placeholder {
  color: #86868b;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.filter-pill {
  height: 42px;
  padding: 0 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #1d1d1f;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.dark .filter-pill {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.1);
  color: #f5f5f7;
}

.filter-pill.active {
  background: rgba(0, 113, 227, 0.12);
  border-color: rgba(0, 113, 227, 0.24);
  color: #0071e3;
}

.dark .filter-pill.active {
  background: rgba(10, 132, 255, 0.18);
  border-color: rgba(100, 210, 255, 0.24);
  color: #64d2ff;
}

/* 全部页面布局 */
.all-view {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.boards-masonry {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  align-items: start;
}

.charts-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 90px;
}

/* 其他分类页面布局 */
.layout {
  display: flex;
  gap: 24px;
}

.boards {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.sidebar {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 90px;
}

/* Footer */
.footer {
  background: #f5f5f7;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding: 20px 0;
  margin-top: auto;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .footer {
  background: #1d1d1f;
  border-top-color: rgba(255, 255, 255, 0.1);
}

.copyright {
  text-align: center;
  font-size: 12px;
  color: #86868b;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .copyright {
  color: #a1a1a6;
}

/* 响应式 */
@media (max-width: 1400px) {
  .all-view {
    grid-template-columns: 1fr 340px;
  }

  .boards-masonry {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 1200px) {
  .all-view {
    grid-template-columns: 1fr;
  }

  .charts-sidebar {
    position: static;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }

  .layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: static;
    flex-direction: row;
  }

  .sidebar > * {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .boards-masonry {
    grid-template-columns: 1fr;
  }

  .boards {
    grid-template-columns: 1fr;
  }

  .charts-sidebar {
    grid-template-columns: 1fr;
  }

  .sidebar {
    flex-direction: column;
  }

  .header-content {
    height: auto;
    flex-direction: column;
    padding: 12px 0;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions {
    overflow-x: auto;
  }
}

@media (max-width: 375px) {
  .container {
    padding: 0 16px;
  }

  .logo {
    font-size: 19px;
  }

  .boards-masonry {
    gap: 16px;
  }
}
</style>
