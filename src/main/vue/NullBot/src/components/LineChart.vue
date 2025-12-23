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
      left: '45px',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#ffffff',
      }
    },
    grid: {
      left: '30px',
      right: '18px',
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
      splitNumber: 2, // 刻度
      axisLine: {
        lineStyle: {
          color: '#3c3c3c',
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
        symbolSize: 6,
        showSymbol: false,
        lineStyle: {
          width: 2,
          color: gradientColors.line
        },
        itemStyle: {
          color: gradientColors.line,
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#fff',
            borderColor: '#FF6347',
            borderWidth: 2,
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
      backgroundColor: '#3f3f3f',
      borderColor: '#3f3f3f',
      borderRadius: 10,
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#3c3c3c',
          width: 1,
          type: 'dashed'
        }
      },
      textStyle: {
        color: '#fff',
        fontSize: 12
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
