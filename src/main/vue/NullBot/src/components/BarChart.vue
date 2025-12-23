<template>
  <div ref="chartRef" :style="{ width: width || '100%', height: height || '400px' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: String,
  y_name: String,
  xAxis: {
    type: Array,
    default: () => []
  },
  data: {
    type: Array,
    default: () => []
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '400px'
  }
})

const chartRef = ref(null)
let chartInstance = null

// 渐变色配置
const gradientColors = {
  bar: '#42d3fd',
  barStart: 'rgba(66, 211, 253, 0.8)',
  barEnd: 'rgba(66, 211, 253, 0.2)'
}

const initChart = () => {
  if (!chartRef.value || !props.xAxis.length || !props.data.length) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const option = {
    title: {
      text: props.title || '',
      left: '4%',
      textStyle: {
        fontSize: 15,
        fontWeight: 'bold',
        color: '#ffffff',
      }
    },
    grid: {
      left: '3%',
      right: '3%',
      bottom: '10%',
      top: props.title ? '25%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.xAxis,
      axisLine: {
        lineStyle: {
          color: '#3c3c3c'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      splitNumber: 3,
      axisLine: {
        lineStyle: {
          color: '#3c3c3c'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#3c3c3c',
          type: 'solid'
        }
      }
    },
    series: [
      {
        name: props.y_name || 'Data',
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: gradientColors.barStart },
            { offset: 0.9, color: gradientColors.barEnd }
          ]),
          borderRadius: [4, 4, 0, 0] // 柱子上方的圆角
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#7ee2ff' },
              { offset: 1, color: '#42d3fd' }
            ])
          }
        },
        data: props.data
      }
    ],
    tooltip: {
      backgroundColor: '#121212',
      trigger: 'axis',
      axisPointer: {
        type: 'shadow' // 阴影指示器更适合柱状图
      },
      textStyle: {
        color: '#fff'
      }
    }
  }

  chartInstance.setOption(option)

  // 响应窗口变化
  const resizeChart = () => {
    chartInstance && chartInstance.resize()
  }
  window.addEventListener('resize', resizeChart)

  // 组件卸载时移除监听器
  onUnmounted(() => {
    window.removeEventListener('resize', resizeChart)
    chartInstance && chartInstance.dispose()
  })
}

// 监听数据变化
watch(
    () => [props.xAxis, props.data],
    () => {
      nextTick(() => {
        initChart()
      })
    },
    { deep: true }
)

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

// 暴露方法给父组件
defineExpose({
  resize: () => {
    chartInstance && chartInstance.resize()
  },
  getInstance: () => chartInstance
})
</script>
