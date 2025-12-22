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
  line: '#42d3fd',
  shadowStart: 'rgb(255, 255, 255, 0.2)',
  shadowEnd: 'rgba(255, 255, 255, 0)'
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
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#ffffff',
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: props.title ? '20%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
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
      splitNumber: 3, // 包括底和顶一共4个刻度
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
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 2,
          color: gradientColors.line
        },
        itemStyle: {
          color: gradientColors.line
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#fff',
            borderColor: '#FF6347',
            borderWidth: 3,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
            shadowBlur: 10
          },
          symbolSize: 8
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0.3, color: gradientColors.shadowStart },
            { offset: 1, color: gradientColors.shadowEnd }
          ])
        },
        data: props.data
      }
    ],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line'
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
