<script setup>
import { computed } from 'vue'

const props = defineProps({
  boards: { type: Array, default: () => [] }
})

const categoryNames = {
  dev: '开发者',
  tech: '科技',
  finance: '财经',
  news: '新闻',
  entertainment: '娱乐',
  game: '游戏',
  forum: '论坛',
  welfare: '福利',
  shopping: '购物',
  comprehensive: '综合',
  other: '其他'
}

const stats = computed(() => {
  const map = {}
  props.boards.forEach(board => {
    const cat = board.category || 'other'
    if (!map[cat]) map[cat] = { key: cat, name: categoryNames[cat] || cat, sources: 0, items: 0 }
    map[cat].sources++
    map[cat].items += board.items.length
  })
  return Object.values(map).sort((a, b) => b.items - a.items)
})

const maxItems = computed(() => Math.max(1, ...stats.value.map(s => s.items)))
</script>

<template>
  <section class="stats-chart">
    <header>
      <div class="badge-icon">📊</div>
      <div class="title-wrap">
        <h3>数据源分布</h3>
        <span class="meta">按分类统计热点数量</span>
      </div>
    </header>
    <div class="stats-list">
      <div v-for="item in stats" :key="item.key" class="stat-row">
        <div class="stat-row-head">
          <span class="cat-name">{{ item.name }}</span>
          <span class="cat-value">{{ item.sources }}源 / {{ item.items }}条</span>
        </div>
        <div class="bar-track">
          <div class="bar-fill" :style="{ width: `${Math.max(6, item.items / maxItems * 100)}%` }"></div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.stats-chart {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 360px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .stats-chart {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fafafa;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark header {
  background: #2d2d2f;
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.badge-icon {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  font-size: 16px;
  background: #0071e3;
  transition: background 0.3s ease;
}

.dark .badge-icon {
  background: #0a84ff;
}

.title-wrap {
  flex: 1;
  min-width: 0;
}

h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  transition: color 0.3s ease;
}

.dark h3 {
  color: #f5f5f7;
}

.meta {
  display: block;
  color: #86868b;
  font-size: 12px;
  margin-top: 2px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .meta {
  color: #a1a1a6;
}

.stats-list {
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: grid;
  gap: 7px;
}

.stat-row-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.cat-name {
  color: #1d1d1f;
  font-size: 13px;
  font-weight: 600;
}

.dark .cat-name {
  color: #f5f5f7;
}

.cat-value {
  color: #86868b;
  font-size: 12px;
  font-weight: 500;
}

.dark .cat-value {
  color: #a1a1a6;
}

.bar-track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
}

.dark .bar-track {
  background: rgba(255, 255, 255, 0.1);
}

.bar-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #0071e3, #64d2ff);
  box-shadow: 0 0 18px rgba(0, 113, 227, 0.22);
}

.dark .bar-fill {
  background: linear-gradient(90deg, #0a84ff, #bf5af2);
}

.stats-list::-webkit-scrollbar {
  width: 6px;
}

.stats-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.14);
  border-radius: 3px;
}

.dark .stats-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .stats-chart {
    height: 320px;
  }

  header {
    padding: 14px 16px;
  }

  h3 {
    font-size: 14px;
  }

  .meta {
    font-size: 11px;
  }
}
</style>
