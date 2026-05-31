<script setup>
import { computed } from 'vue'

const props = defineProps({
  board: { type: Object, required: true },
  selected: { type: Object, default: null }
})

const emit = defineEmits(['pick'])

const updatedAt = computed(() => {
  if (!props.board.updatedAt) return ''
  const d = new Date(props.board.updatedAt)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return d.toLocaleDateString('zh-CN')
})

function pick(item) {
  emit('pick', { platform: props.board.platform, title: item.title })
}

function isActive(item) {
  return props.selected
    && props.selected.platform === props.board.platform
    && props.selected.title === item.title
}
</script>

<template>
  <div class="board">
    <div class="board-header">
      <h3 class="title">{{ board.platformName }}</h3>
      <div class="meta">
        <span v-if="board.error" class="error">缓存</span>
        <span v-else class="time">{{ updatedAt }}</span>
        <span class="count">{{ board.items.length }}</span>
      </div>
    </div>

    <div class="list">
      <div
        v-for="item in board.items"
        :key="item.rank"
        class="item"
        :class="{ active: isActive(item) }"
        @click="pick(item)"
      >
        <span class="rank" :class="{ top: item.rank <= 3 }">{{ item.rank }}</span>
        <div class="content">
          <a class="item-title" :href="item.url" target="_blank" @click.stop>
            {{ item.title }}
          </a>
          <div class="info" v-if="item.extra || item.hotValue">
            <span v-if="item.extra" class="tag">{{ item.extra }}</span>
            <span v-if="item.hotValue" class="hot">{{ item.hotValue }}</span>
          </div>
        </div>
      </div>

      <div v-if="board.items.length === 0" class="empty">
        暂无数据
      </div>
    </div>
  </div>
</template>

<style scoped>
.board {
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 540px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: background 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

.dark .board {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.board-header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .board-header {
  background: #2d2d2f;
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.01em;
  transition: color 0.3s ease;
}

.dark .title {
  color: #f5f5f7;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.time {
  color: #86868b;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .time {
  color: #a1a1a6;
}

.error {
  color: #ff3b30;
  font-size: 12px;
  font-weight: 500;
}

.dark .error {
  color: #ff453a;
}

.count {
  background: rgba(0, 0, 0, 0.04);
  padding: 3px 8px;
  border-radius: 10px;
  color: #1d1d1f;
  font-weight: 500;
  font-size: 11px;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .count {
  background: rgba(255, 255, 255, 0.1);
  color: #f5f5f7;
}

.list {
  flex: 1;
  overflow-y: auto;
}

.item {
  display: flex;
  padding: 12px 18px;
  gap: 12px;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.item:hover {
  background: #fafafa;
  border-left-color: #0071e3;
}

.dark .item:hover {
  background: #2d2d2f;
  border-left-color: #0a84ff;
}

.item.active {
  background: #f5f5f7;
  border-left-color: #0071e3;
}

.dark .item.active {
  background: #3d3d3f;
  border-left-color: #0a84ff;
}

.rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  background: #f5f5f7;
  border-radius: 8px;
  flex-shrink: 0;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .rank {
  color: #a1a1a6;
  background: #2d2d2f;
}

.rank.top {
  background: linear-gradient(135deg, #ff3b30 0%, #ff6b58 100%);
  color: #fff;
  font-size: 13px;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.25);
}

.dark .rank.top {
  background: linear-gradient(135deg, #ff453a 0%, #ff6961 100%);
  box-shadow: 0 2px 8px rgba(255, 69, 58, 0.4);
}

.content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  font-weight: 400;
  transition: color 0.2s ease;
}

.dark .item-title {
  color: #f5f5f7;
}

.item-title:hover {
  color: #0071e3;
}

.dark .item-title:hover {
  color: #0a84ff;
}

.info {
  display: flex;
  gap: 8px;
  margin-top: 6px;
  align-items: center;
}

.tag {
  font-size: 11px;
  padding: 2px 7px;
  background: #fff4e5;
  color: #c77700;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .tag {
  background: #2d1f00;
  color: #ffd60a;
}

.hot {
  font-size: 11px;
  color: #ff3b30;
  font-weight: 500;
  transition: color 0.3s ease;
}

.dark .hot {
  color: #ff453a;
}

.empty {
  padding: 60px 20px;
  text-align: center;
  color: #86868b;
  font-size: 14px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .empty {
  color: #a1a1a6;
}

/* 滚动条 - Apple风格 */
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

/* 响应式 */
@media (max-width: 768px) {
  .board {
    height: 500px;
  }

  .board-header {
    padding: 14px 16px;
  }

  .item {
    padding: 11px 16px;
  }
}

@media (max-width: 375px) {
  .board {
    height: 460px;
  }

  .board-header {
    padding: 12px 14px;
  }

  .title {
    font-size: 14px;
  }

  .item {
    padding: 10px 14px;
    gap: 10px;
  }

  .rank {
    width: 26px;
    height: 26px;
    font-size: 12px;
  }

  .item-title {
    font-size: 13px;
  }
}
</style>
