<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  url: { type: String, default: '' },
  title: { type: String, default: '' },
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

const loading = ref(true)
const loadError = ref(false)

watch(() => props.url, () => {
  if (props.url) {
    loading.value = true
    loadError.value = false
  }
})

function onLoad() {
  loading.value = false
}

function onError() {
  loading.value = false
  loadError.value = true
}

function close() {
  emit('close')
}

function openExternal() {
  if (props.url) window.open(props.url, '_blank')
}
</script>

<template>
  <transition name="fade">
    <div v-if="visible" class="preview-scrim" @click="close"></div>
  </transition>
  <transition name="slide">
    <div v-if="visible" class="preview-panel">
      <header class="preview-header">
        <div class="preview-title-wrap">
          <span class="live-dot"></span>
          <span class="preview-title" :title="title">{{ title || '页面预览' }}</span>
        </div>
        <div class="preview-actions">
          <button class="icon-btn" @click="openExternal" title="在新标签页打开">↗</button>
          <button class="icon-btn" @click="close" title="关闭">✕</button>
        </div>
      </header>

      <div class="preview-body">
        <div v-if="loading" class="preview-loading">
          <div class="spinner"></div>
          <span>加载中…</span>
        </div>

        <div v-if="loadError" class="preview-error">
          <div class="err-icon">🔒</div>
          <p>该网站不允许嵌入预览</p>
          <button class="btn-open" @click="openExternal">在新标签页打开 ↗</button>
        </div>

        <iframe
          v-show="!loadError"
          :src="url"
          class="preview-iframe"
          frameborder="0"
          sandbox="allow-scripts allow-same-origin allow-popups allow-forms"
          referrerpolicy="no-referrer"
          @load="onLoad"
          @error="onError"
        ></iframe>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.preview-scrim {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(2, 6, 23, 0.16);
  backdrop-filter: blur(2px);
}

.dark .preview-scrim {
  background: rgba(0, 0, 0, 0.36);
}

.preview-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 460px;
  max-width: 90vw;
  height: 100vh;
  background: #fff;
  border-left: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: -8px 0 40px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .preview-panel {
  background: #1d1d1f;
  border-left-color: rgba(255, 255, 255, 0.12);
  box-shadow: -8px 0 40px rgba(0, 0, 0, 0.5);
}

.preview-header {
  height: 52px;
  flex-shrink: 0;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(250, 250, 250, 0.9);
  backdrop-filter: saturate(180%) blur(20px);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .preview-header {
  background: rgba(45, 45, 47, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.preview-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #30d158;
  flex-shrink: 0;
  box-shadow: 0 0 0 0 rgba(48, 209, 88, 0.5);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(48, 209, 88, 0.5); }
  70% { box-shadow: 0 0 0 6px rgba(48, 209, 88, 0); }
  100% { box-shadow: 0 0 0 0 rgba(48, 209, 88, 0); }
}

.preview-title {
  font-size: 13px;
  font-weight: 500;
  color: #1d1d1f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.dark .preview-title {
  color: #f5f5f7;
}

.preview-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.icon-btn {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  font-size: 15px;
  color: #1d1d1f;
  background: rgba(0, 0, 0, 0.05);
  transition: background 0.2s ease, color 0.3s ease;
}

.dark .icon-btn {
  color: #f5f5f7;
  background: rgba(255, 255, 255, 0.1);
}

.icon-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.dark .icon-btn:hover {
  background: rgba(255, 255, 255, 0.18);
}

.preview-body {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f5f5f7;
}

.dark .preview-body {
  background: #000;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.dark .preview-iframe {
  background: #fff;
}

.preview-loading,
.preview-error {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #86868b;
  font-size: 14px;
  z-index: 2;
  background: inherit;
}

.dark .preview-loading,
.dark .preview-error {
  color: #a1a1a6;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(0, 113, 227, 0.2);
  border-top-color: #0071e3;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.err-icon {
  font-size: 42px;
  opacity: 0.7;
}

.btn-open {
  padding: 8px 16px;
  background: #0071e3;
  color: #fff;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s ease;
}

.btn-open:hover {
  background: #0077ed;
}

/* 滑入动画 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.32s cubic-bezier(0.32, 0.72, 0, 1);
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .preview-panel {
    width: 100vw;
    max-width: 100vw;
  }
}
</style>
