<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})
</script>

<template>
  <section class="aggregate">
    <header>
      <div class="head-left">
        <div class="badge-icon">★</div>
        <div>
          <h3>跨平台热点榜</h3>
          <span class="meta">综合权重 · 实时计算</span>
        </div>
      </div>
    </header>
    <ol class="list scrollbar">
      <li v-for="(item, idx) in items" :key="item.title">
        <span class="rank" :data-top="idx < 3">{{ idx + 1 }}</span>
        <div class="content">
          <div class="title">{{ item.title }}</div>
          <div class="tags">
            <a
              v-for="p in item.platforms"
              :key="p.platform"
              :href="p.url"
              target="_blank"
              rel="noopener"
              class="tag"
            >
              {{ p.platformName }} · #{{ p.rank }}
            </a>
          </div>
        </div>
        <span class="score">{{ Math.round(item.score) }}</span>
      </li>
      <li v-if="items.length === 0" class="empty">等待数据汇聚...</li>
    </ol>
  </section>
</template>

<style scoped>
.aggregate {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .aggregate {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: #fafafa;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark header {
  background: #2d2d2f;
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.head-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge-icon {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  font-size: 16px;
  background: #f59e0b;
  color: white;
  transition: background 0.3s ease;
}

.dark .badge-icon {
  background: #ffd60a;
  color: #000;
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
  color: #86868b;
  font-size: 12px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .meta {
  color: #a1a1a6;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 480px;
  overflow-y: auto;
}

.list li {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: background 0.15s ease, border-color 0.3s ease;
}

.dark .list li {
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.list li:last-child {
  border-bottom: none;
}

.list li:hover {
  background: #fafafa;
}

.dark .list li:hover {
  background: #2d2d2f;
}

.rank {
  width: 28px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  color: #86868b;
  padding-top: 2px;
  transition: color 0.3s ease;
}

.dark .rank {
  color: #a1a1a6;
}

.rank[data-top="true"] {
  color: white;
  background: #f59e0b;
  border-radius: 8px;
  height: 28px;
  display: grid;
  place-items: center;
  padding-top: 0;
}

.dark .rank[data-top="true"] {
  background: #ffd60a;
  color: #000;
}

.content {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
  word-break: break-word;
  color: #1d1d1f;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .title {
  color: #f5f5f7;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 10px;
  background: rgba(0, 113, 227, 0.1);
  color: #0071e3;
  transition: background 0.2s ease, color 0.3s ease;
  font-weight: 500;
}

.dark .tag {
  background: rgba(10, 132, 255, 0.15);
  color: #0a84ff;
}

.tag:hover {
  background: rgba(0, 113, 227, 0.15);
}

.dark .tag:hover {
  background: rgba(10, 132, 255, 0.25);
}

.score {
  font-size: 12px;
  color: #ff3b30;
  font-weight: 600;
  margin-top: 2px;
  transition: color 0.3s ease;
}

.dark .score {
  color: #ff453a;
}

.empty {
  color: #86868b;
  text-align: center;
  padding: 40px;
  font-size: 14px;
  transition: color 0.3s ease;
}

.dark .empty {
  color: #a1a1a6;
}

/* 滚动条 */
.list::-webkit-scrollbar {
  width: 6px;
}

.list::-webkit-scrollbar-track {
  background: transparent;
}

.list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.dark .list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
}

.list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

.dark .list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  header {
    padding: 14px 16px;
  }

  .list li {
    padding: 10px 16px;
  }

  .title {
    font-size: 13px;
  }

  .tag {
    font-size: 10px;
    padding: 2px 7px;
  }
}
</style>
