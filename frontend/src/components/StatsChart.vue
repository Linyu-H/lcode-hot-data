<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const props = defineProps({
  boards: { type: Array, default: () => [] }
})

const chartEl = ref(null)
let chart = null

// 检测深色模式
const isDark = computed(() => {
  return document.documentElement.classList.contains('dark')
})

const option = computed(() => {
  const dark = isDark.value

  // 按分类统计数据源数量
  const categoryStats = {}
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
    comprehensive: '综合'
  }

  props.boards.forEach(board => {
    if (board.items.length > 0) {
      const cat = board.category || 'other'
      if (!categoryStats[cat]) {
        categoryStats[cat] = { count: 0, items: 0 }
      }
      categoryStats[cat].count++
      categoryStats[cat].items += board.items.length
    }
  })

  const pieData = Object.entries(categoryStats).map(([key, val]) => ({
    name: categoryNames[key] || key,
    value: val.items
  }))

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: dark ? 'rgba(29, 29, 31, 0.95)' : 'rgba(255, 255, 255, 0.95)',
      borderColor: dark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
      textStyle: { color: dark ? '#f5f5f7' : '#1d1d1f' },
      formatter: '{b}: {c} 条 ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      textStyle: {
        color: dark ? '#a1a1a6' : '#86868b',
        fontSize: 11
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: dark ? '#1d1d1f' : '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{c}',
          fontSize: 11,
          color: dark ? '#f5f5f7' : '#1d1d1f'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 13,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data: pieData,
        color: [
          '#0071e3',
          '#5856d6',
          '#ff9500',
          '#ff3b30',
          '#34c759',
          '#00c7be',
          '#ff2d55',
          '#af52de',
          '#ffcc00',
          '#ff6482'
        ]
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
  <section class="stats-chart">
    <header>
      <div class="badge-icon">📊</div>
      <div class="title-wrap">
        <h3>数据源分布</h3>
        <span class="meta">按分类统计热点数量</span>
      </div>
    </header>
    <div class="chart" ref="chartEl"></div>
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

.chart {
  flex: 1;
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
