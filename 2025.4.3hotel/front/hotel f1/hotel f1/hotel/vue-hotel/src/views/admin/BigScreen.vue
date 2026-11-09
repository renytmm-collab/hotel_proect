<template>
  <div class="bigscreen">
    <!-- 顶部标题 -->
    <header class="bs-header">
      <h1 class="bs-title">酒店经营数据大屏</h1>
      <div class="bs-time">{{ nowStr }}</div>
    </header>

    <!-- 核心指标卡片 -->
    <div class="bs-kpi">
      <div class="kpi-item" v-for="k in kpis" :key="k.label">
        <div class="kpi-value" :style="{ color: k.color }">{{ k.value }}</div>
        <div class="kpi-label">{{ k.label }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="bs-grid">
      <div class="panel">
        <div class="panel-title">近30天入住率趋势</div>
        <div ref="occChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">近30天营收趋势</div>
        <div ref="incomeChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">房态分布</div>
        <div ref="roomChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">支付方式占比</div>
        <div ref="payChart" class="chart"></div>
      </div>

      <div class="panel panel-wide">
        <div class="panel-title">热门房型 Top5</div>
        <div ref="salesChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  getStatsOverview,
  getIncomeStats,
  getOccupancyStats,
  getRoomSalesStats
} from '@/api/admin'

// 房态数字 -> 中文
const ROOM_STATUS = {
  0: '已预定',
  1: '空闲',
  2: '已入住',
  3: '清洁中',
  4: '维修中',
  5: '下架'
}

const nowStr = ref('')
const kpis = ref([])

const occChart = ref(null)
const incomeChart = ref(null)
const roomChart = ref(null)
const payChart = ref(null)
const salesChart = ref(null)

let charts = []
let timer = null

// 格式化日期为 YYYY-MM-DD
function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 金额格式化
function fmtMoney(v) {
  if (v === null || v === undefined) return '¥0'
  return '¥' + Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

// 初始化单个图表
function mountChart(elRef, option) {
  if (!elRef.value) return
  const chart = echarts.init(elRef.value)
  chart.setOption(option)
  charts.push(chart)
}

// 深色大屏通用坐标轴配色
const axisText = { color: '#9fb3d1' }
const splitLine = { lineStyle: { color: 'rgba(255,255,255,0.08)' } }

async function loadData() {
  const today = new Date()
  const endDate = fmtDate(today)
  const startDate = fmtDate(new Date(today.getTime() - 29 * 24 * 3600 * 1000))

  try {
    const [overviewRes, incomeRes, occRes, salesRes] = await Promise.all([
      getStatsOverview(),
      getIncomeStats({ startDate, endDate, groupBy: 'day' }),
      getOccupancyStats({ startDate, endDate, groupBy: 'day' }),
      getRoomSalesStats({ startDate, endDate })
    ])

    const overview = overviewRes.data || {}
    const income = incomeRes.data || []
    const occ = occRes.data || []
    const sales = salesRes.data || []

    // 顶部卡片
    kpis.value = [
      { label: '今日营收', value: fmtMoney(overview.todayIncome), color: '#00d2ff' },
      { label: '今日订单', value: overview.todayOrderCount ?? 0, color: '#7b9cff' },
      { label: '在住房间', value: overview.stayingCount ?? 0, color: '#38e0a6' },
      { label: '累计营收', value: fmtMoney(overview.totalIncome), color: '#ffd666' }
    ]

    // 房态分布饼图
    const roomData = (overview.roomStatus || []).map(i => ({
      name: ROOM_STATUS[i.name] || String(i.name),
      value: Number(i.value)
    }))
    mountChart(roomChart, {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 间 ({d}%)' },
      legend: { bottom: 0, textStyle: axisText },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '45%'],
          data: roomData,
          label: { color: '#c8d3e8', formatter: '{b}\n{c}间' },
          itemStyle: { borderColor: '#0a1a33', borderWidth: 2 }
        }
      ]
    })

    // 支付方式占比
    const payData = (overview.payment || []).map(i => ({
      name: i.name,
      value: Number(i.value)
    }))
    mountChart(payChart, {
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      legend: { bottom: 0, textStyle: axisText },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '45%'],
          data: payData,
          label: { color: '#c8d3e8', formatter: '{b}\n{d}%' },
          itemStyle: { borderColor: '#0a1a33', borderWidth: 2 }
        }
      ]
    })

    // 入住率趋势折线
    const occDates = occ.map(i => i.date)
    const occRates = occ.map(i => Number(i.occupancyRate || 0))
    mountChart(occChart, {
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      tooltip: { trigger: 'axis', formatter: p => `${p[0].axisValue}<br/>入住率：${p[0].value}%` },
      xAxis: { type: 'category', data: occDates, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      yAxis: { type: 'value', axisLabel: { ...axisText, formatter: '{value}%' }, splitLine },
      series: [
        {
          type: 'line',
          smooth: true,
          data: occRates,
          itemStyle: { color: '#00d2ff' },
          lineStyle: { color: '#00d2ff', width: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,210,255,0.35)' },
              { offset: 1, color: 'rgba(0,210,255,0)' }
            ])
          }
        }
      ]
    })

    // 营收趋势堆叠柱状图（房费/押金/其他）
    const incomeDates = income.map(i => i.date)
    const roomFees = income.map(i => Number(i.roomFee || 0))
    const deposits = income.map(i => Number(i.depositFee || 0))
    const others = income.map(i => Number(i.otherFee || 0))
    mountChart(incomeChart, {
      grid: { left: 50, right: 20, top: 40, bottom: 30 },
      tooltip: { trigger: 'axis' },
      legend: { top: 0, textStyle: axisText },
      xAxis: { type: 'category', data: incomeDates, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      yAxis: { type: 'value', axisLabel: axisText, splitLine },
      series: [
        { name: '房费', type: 'bar', stack: 'total', data: roomFees, itemStyle: { color: '#00d2ff' } },
        { name: '押金', type: 'bar', stack: 'total', data: deposits, itemStyle: { color: '#7b9cff' } },
        { name: '其他', type: 'bar', stack: 'total', data: others, itemStyle: { color: '#38e0a6' } }
      ]
    })

    // 热门房型 Top5（横向条形）
    const top5 = [...sales].sort((a, b) => Number(b.totalAmount || 0) - Number(a.totalAmount || 0)).slice(0, 5)
    const names = top5.map(i => i.roomTypeName).reverse()
    const amounts = top5.map(i => Number(i.totalAmount || 0)).reverse()
    mountChart(salesChart, {
      grid: { left: 90, right: 60, top: 20, bottom: 30 },
      tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>销售额：¥${p[0].value}` },
      xAxis: { type: 'value', axisLabel: axisText, splitLine },
      yAxis: { type: 'category', data: names, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      series: [
        {
          type: 'bar',
          data: amounts,
          barWidth: 16,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#7b9cff' },
              { offset: 1, color: '#00d2ff' }
            ])
          },
          label: { show: true, position: 'right', color: '#c8d3e8' }
        }
      ]
    })
  } catch (e) {
    console.error('加载大屏数据失败', e)
  }
}

function updateClock() {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  const p = n => String(n).padStart(2, '0')
  nowStr.value = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())} 星期${week}`
}

function handleResize() {
  charts.forEach(c => c.resize())
}

onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
  charts = []
})
</script>

<style scoped>
.bigscreen {
  min-height: 100vh;
  background: radial-gradient(ellipse at top, #0d2242 0%, #081428 55%, #050b16 100%);
  padding: 16px 20px 24px;
  color: #dce6f5;
}

.bs-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 14px;
}

.bs-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 4px;
  background: linear-gradient(90deg, #00d2ff, #7b9cff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0;
}

.bs-time {
  position: absolute;
  right: 4px;
  font-size: 13px;
  color: #9fb3d1;
  font-variant-numeric: tabular-nums;
}

.bs-kpi {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 14px;
}

.kpi-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 16px 18px;
  text-align: center;
}

.kpi-value {
  font-size: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.kpi-label {
  margin-top: 6px;
  font-size: 13px;
  color: #9fb3d1;
}

.bs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.panel {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 12px 14px;
}

.panel-wide {
  grid-column: span 2;
}

.panel-title {
  font-size: 14px;
  color: #c8d3e8;
  margin-bottom: 6px;
  padding-left: 8px;
  border-left: 3px solid #00d2ff;
}

.chart {
  height: 280px;
}

@media (max-width: 900px) {
  .bs-kpi {
    grid-template-columns: repeat(2, 1fr);
  }
  .bs-grid {
    grid-template-columns: 1fr;
  }
  .panel-wide {
    grid-column: span 1;
  }
}
</style>
