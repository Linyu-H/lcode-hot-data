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

onMounted(async () => {
  // 读取本地存储的主题设置
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    darkMode.value = true
    document.documentElement.classList.add('dark')
  }

  await store.loadSnapshot()
  await store.loadAggregate()
  store.connect()
})

function toggleDarkMode() {
  darkMode.value = !darkMode.value
  if (darkMode.value) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}

const tabs = computed(() => {
  const allBoards = boards.value
  return [
    { id: 'all', name: '全部', boards: allBoards },
    { id: 'dev', name: '开发者', boards: allBoards.filter(b => b.category === 'dev') },
    { id: 'tech', name: '科技', boards: allBoards.filter(b => b.category === 'tech') },
    { id: 'finance', name: '财经', boards: allBoards.filter(b => b.category === 'finance') },
    { id: 'news', name: '新闻', boards: allBoards.filter(b => b.category === 'news') },
    { id: 'entertainment', name: '娱乐', boards: allBoards.filter(b => b.category === 'entertainment' || b.category === 'game') },
    { id: 'forum', name: '论坛', boards: allBoards.filter(b => b.category === 'forum') },
    { id: 'welfare', name: '羊毛福利', boards: allBoards.filter(b => b.category === 'welfare' || b.category === 'shopping') },
    { id: 'comprehensive', name: '综合', boards: allBoards.filter(b => b.category === 'comprehensive') }
  ]
})

const currentBoards = computed(() => {
  const tab = tabs.value.find(t => t.id === activeTab.value)
  return tab ? tab.boards : []
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
            <span class="status" :class="'status-' + connection">{{ statusText[connection] }}</span>
            <button class="btn-theme" @click="toggleDarkMode" :title="darkMode ? '切换到浅色模式' : '切换到深色模式'">
              {{ darkMode ? '☀️' : '🌙' }}
            </button>
            <button class="btn-refresh" @click="refresh" :disabled="connection === 'refreshing'">
              刷新
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
            <span class="badge">{{ tab.boards.length }}</span>
          </button>
        </div>
      </div>
    </nav>

    <main class="main">
      <div class="container">
        <!-- 全部页面：特殊布局 -->
        <div v-if="isAllView" class="all-view">
          <div class="boards-masonry">
            <HotBoard
              v-for="board in currentBoards"
              :key="board.platform"
              :board="board"
              :selected="selected"
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
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  background: #f5f5f7;
  color: #86868b;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
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
  padding: 6px 14px;
  background: #0071e3;
  color: #fff;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 400;
  transition: background 0.2s ease;
}

.btn-refresh:hover:not(:disabled) {
  background: #0077ed;
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
