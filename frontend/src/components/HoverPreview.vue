<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  item: { type: Object, default: null },
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const showIframe = ref(false)
const iframeLoaded = ref(false)

watch(() => props.item, (newItem) => {
  if (newItem) {
    showIframe.value = true
    iframeLoaded.value = false
  } else {
    showIframe.value = false
    iframeLoaded.value = false
  }
})

const style = computed(() => {
  if (!props.item) return { display: 'none' }
  const x = props.position.x
  const y = props.position.y
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const previewWidth = 480
  const previewHeight = 600

  let left = x + 20
  let top = y - 100

  // 如果右侧空间不够，显示在左侧
  if (left + previewWidth > windowWidth - 20) {
    left = x - previewWidth - 20
  }

  // 如果下方空间不够，调整位置
  if (top + previewHeight > windowHeight - 20) {
    top = windowHeight - previewHeight - 20
  }
  if (top < 20) top = 20

  return {
    left: left + 'px',
    top: top + 'px'
  }
})

function onIframeLoad() {
  iframeLoaded.value = true
}
</script>

<template>
  <Transition name="preview">
    <div v-if="item" class="hover-preview" :style="style">
      <div class="preview-header">
        <div class="header-left">
          <span class="rank-badge" :class="{ top: item.rank <= 3 }">#{{ item.rank }}</span>
          <span class="platform-name">{{ item.platform }}</span>
        </div>
        <a :href="item.url" target="_blank" rel="noopener" class="open-link" @click.stop title="在新标签页打开">
          ↗
        </a>
      </div>

      <div class="preview-title">{{ item.title }}</div>

      <div class="preview-meta" v-if="item.hotValue || item.extra">
        <span v-if="item.hotValue" class="meta-hot">🔥 {{ item.hotValue }}</span>
        <span v-if="item.extra" class="meta-tag">{{ item.extra }}</span>
      </div>

      <div class="preview-content">
        <div v-if="!iframeLoaded" class="loading">
          <div class="loading-spinner"></div>
          <p>加载预览中...</p>
        </div>
        <iframe
          v-if="showIframe"
          :src="item.url"
          frameborder="0"
          sandbox="allow-same-origin allow-scripts allow-popups allow-forms"
          @load="onIframeLoad"
          :class="{ loaded: iframeLoaded }"
        ></iframe>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.hover-preview {
  position: fixed;
  width: 480px;
  height: 600px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .hover-preview {
  background: #1d1d1f;
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
}

.preview-header {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .preview-header {
  background: #2d2d2f;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.rank-badge {
  font-size: 12px;
  font-weight: 700;
  color: #86868b;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 8px;
  border-radius: 6px;
  flex-shrink: 0;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .rank-badge {
  color: #a1a1a6;
  background: rgba(255, 255, 255, 0.1);
}

.rank-badge.top {
  background: linear-gradient(135deg, #ff3b30 0%, #ff6b58 100%);
  color: #fff;
}

.dark .rank-badge.top {
  background: linear-gradient(135deg, #ff453a 0%, #ff6961 100%);
}

.platform-name {
  font-size: 13px;
  color: #1d1d1f;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.dark .platform-name {
  color: #f5f5f7;
}

.open-link {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0071e3;
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.2s ease;
  flex-shrink: 0;
}

.open-link:hover {
  background: #0077ed;
}

.dark .open-link {
  background: #0a84ff;
}

.dark .open-link:hover {
  background: #409cff;
}

.preview-title {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  color: #1d1d1f;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease, border-color 0.3s ease;
}

.dark .preview-title {
  color: #f5f5f7;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.preview-meta {
  padding: 10px 16px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  transition: border-color 0.3s ease;
}

.dark .preview-meta {
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.meta-hot {
  font-size: 11px;
  padding: 3px 8px;
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .meta-hot {
  background: rgba(255, 69, 58, 0.15);
  color: #ff453a;
}

.meta-tag {
  font-size: 11px;
  padding: 3px 8px;
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease;
}

.dark .meta-tag {
  background: rgba(255, 159, 10, 0.15);
  color: #ff9f0a;
}

.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f5f5f7;
  transition: background 0.3s ease;
}

.dark .preview-content {
  background: #000;
}

.loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #86868b;
  font-size: 13px;
  transition: color 0.3s ease;
}

.dark .loading {
  color: #a1a1a6;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: #0071e3;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.dark .loading-spinner {
  border-color: rgba(255, 255, 255, 0.1);
  border-top-color: #0a84ff;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

iframe {
  width: 100%;
  height: 100%;
  border: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

iframe.loaded {
  opacity: 1;
}

/* 动画 */
.preview-enter-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.preview-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.preview-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}

.preview-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .hover-preview {
    width: 90vw;
    height: 70vh;
    max-width: 400px;
    max-height: 500px;
  }
}
</style>
