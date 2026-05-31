<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const props = defineProps({
  selected: { type: Object, default: null },
  data: { type: Array, default: () => [] }
})

const chartEl = ref(null)
let chart = null

// 检测深色模式
const isDark = computed(() => {
  return document.documentElement.classList.contains('dark')
})

const hasEnoughData = computed(() => props.data.length >= 2)
const pointLabel = computed(() => `${props.data.length} 个数据点`)
const currentRank = computed(() => props.data.at(-1)?.rank ?? null)
const bestRank = computed(() => props.data.length ? Math.min(...props.data.map(p => p.rank)) : null)
const rankDelta = computed(() => {
  if (props.data.length < 2) return null
  const prev = props.data.at(-2).rank
  const cur = props.data.at(-1).rank
  return prev - cur
})
const deltaText = computed(() => {
  if (rankDelta.value === null) return '等待对比'
  if (rankDelta.value > 0) return `上升 ${rankDelta.value}`
  if (rankDelta.value < 0) return `下降 ${Math.abs(rankDelta.value)}`
  return '持平'
})

const option = computed(() => {
  const points = props.data.map(p => [p.timestamp, p.rank])
  const maxRank = points.length
    ? Math.max(...points.map(p => p[1]), 10)
    : 10

  const dark = isDark.value

  return {
    backgroundColor: 'transparent',
    grid: { left: 42, right: 16, top: 22, bottom: 28 },
    tooltip: {
      trigger: 'axis',
      backgroundColor: dark ? 'rgba(29, 29, 31, 0.95)' : 'rgba(255, 255, 255, 0.95)',
      borderColor: dark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
      textStyle: { color: dark ? '#f5f5f7' : '#1d1d1f' },
      formatter: (arr) => {
        const p = arr[0]
        const dt = new Date(p.value[0]).toLocaleTimeString('zh-CN', { hour12: false })
        return `${dt}<br/>排名: <b>${p.value[1]}</b>`
      }
    },
    xAxis: {
      type: 'time',
      axisLine: { lineStyle: { color: dark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)' } },
      axisLabel: { color: dark ? '#a1a1a6' : '#86868b', fontSize: 10 },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      inverse: true,
      min: 1,
      max: Math.max(maxRank + 2, 10),
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: dark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)' } },
      axisLabel: { color: dark ? '#a1a1a6' : '#86868b', fontSize: 10 }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        showSymbol: true,
        data: points,
        lineStyle: {
          width: 2,
          color: dark ? '#0a84ff' : '#0071e3'
        },
        itemStyle: {
          color: dark ? '#0a84ff' : '#0071e3',
          borderColor: dark ? '#1d1d1f' : '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: dark ? 'rgba(10, 132, 255, 0.3)' : 'rgba(0, 113, 227, 0.3)' },
              { offset: 1, color: 'rgba(0, 0, 0, 0)' }
            ]
          }
        }
      }
    ]
  }
})

function resize() { chart && chart.resize() }

onMounted(() => {
  chart = echarts.init(chartEl.value)
  chart.setOption(option.value)
  window.addEventListener('resize', resize)

  // 监听主题变化
  const observer = new MutationObserver(() => {
    if (chart) {
      chart.setOption(option.value, true)
    }
  })
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class']
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart && chart.dispose()
})

watch(option, (v) => chart && chart.setOption(v, true))
</script>

<template>
  <section class="trend">
    <header>
      <div class="badge-icon">📈</div>
      <div class="title-wrap">
        <h3>排名趋势</h3>
        <span class="meta" v-if="selected">
          {{ selected.title }}
        </span>
        <span class="meta muted" v-else>选择任意热点查看排名变化</span>
      </div>
      <span v-if="selected" class="point-badge">{{ pointLabel }}</span>
    </header>
    <div class="chart" ref="chartEl"></div>
    <div v-if="selected && !hasEnoughData" class="single-state">
      <span>当前排名</span>
      <strong>#{{ currentRank || '-' }}</strong>
      <em>等待下次抓取后生成趋势线</em>
    </div>
    <div v-else-if="selected && hasEnoughData" class="trend-summary">
      <div><span>当前</span><strong>#{{ currentRank }}</strong></div>
      <div><span>最佳</span><strong>#{{ bestRank }}</strong></div>
      <div><span>变化</span><strong>{{ deltaText }}</strong></div>
    </div>
    <div v-if="!selected" class="placeholder">
      <div class="ph-icon">📊</div>
      <div>点击左侧任意热点条目</div>
    </div>
  </section>
</template>

<style scoped>
.trend {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 320px;
  position: relative;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dark .trend {
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
  background: #8b5cf6;
  transition: background 0.3s ease;
}

.dark .badge-icon {
  background: #bf5af2;
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
  color: #1d1d1f;
  font-size: 12px;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 400;
  transition: color 0.3s ease;
}

.dark .meta {
  color: #f5f5f7;
}

.meta.muted {
  color: #86868b;
}

.dark .meta.muted {
  color: #a1a1a6;
}

.point-badge {
  flex-shrink: 0;
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(139, 92, 246, 0.12);
  color: #6d28d9;
  font-size: 11px;
  font-weight: 600;
}

.dark .point-badge {
  background: rgba(191, 90, 242, 0.18);
  color: #dcb6ff;
}

.chart {
  flex: 1;
}

.single-state,
.trend-summary {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 14px;
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
  backdrop-filter: saturate(180%) blur(18px);
}

.dark .single-state,
.dark .trend-summary {
  background: rgba(29, 29, 31, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.34);
}

.single-state {
  grid-template-columns: auto 1fr;
  align-items: center;
}

.single-state span,
.trend-summary span {
  color: #86868b;
  font-size: 11px;
  font-weight: 600;
}

.dark .single-state span,
.dark .trend-summary span {
  color: #a1a1a6;
}

.single-state strong {
  color: #0071e3;
  font-size: 28px;
  line-height: 1;
  justify-self: end;
}

.dark .single-state strong {
  color: #64d2ff;
}

.single-state em {
  grid-column: 1 / -1;
  color: #86868b;
  font-size: 12px;
  font-style: normal;
}

.dark .single-state em {
  color: #a1a1a6;
}

.trend-summary {
  grid-template-columns: repeat(3, 1fr);
}

.trend-summary div {
  min-width: 0;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(0, 113, 227, 0.07);
}

.dark .trend-summary div {
  background: rgba(10, 132, 255, 0.12);
}

.trend-summary strong {
  display: block;
  margin-top: 3px;
  color: #1d1d1f;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dark .trend-summary strong {
  color: #f5f5f7;
}

.placeholder {
  position: absolute;
  inset: 64px 0 0;
  display: grid;
  place-items: center;
  color: #86868b;
  font-size: 14px;
  pointer-events: none;
  transition: color 0.3s ease;
}

.dark .placeholder {
  color: #a1a1a6;
}

.ph-icon {
  font-size: 40px;
  opacity: 0.4;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .trend {
    height: 280px;
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
