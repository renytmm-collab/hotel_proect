<template>
  <div class="bigscreen" ref="screenRef">
    <!-- 顶部标题与交互控制条 -->
    <header class="bs-header">
      <div class="bs-controls-left">
        <div class="range-group">
          <button
            class="range-btn"
            :class="{ active: rangeDays === 7 }"
            @click="switchRange(7)"
          >近 7 天</button>
          <button
            class="range-btn"
            :class="{ active: rangeDays === 30 }"
            @click="switchRange(30)"
          >近 30 天</button>
        </div>
        <button class="ctrl-btn" :disabled="loading" @click="loadData">
          {{ loading ? '刷新中…' : '↻ 刷新数据' }}
        </button>
        <button class="ctrl-btn" :class="{ active: autoRefresh }" @click="toggleAutoRefresh">
          {{ autoRefresh ? '● 自动刷新(30s)' : '○ 自动刷新' }}
        </button>
      </div>

      <h1 class="bs-title">逸云酒店经营数据大屏</h1>

      <div class="bs-controls-right">
        <div class="bs-time">{{ nowStr }}</div>
        <button class="ctrl-btn" @click="toggleFullscreen">
          {{ isFullscreen ? '退出全屏' : '⛶ 全屏' }}
        </button>
      </div>
    </header>

    <!-- 核心指标卡片（5 项含实时入住率） -->
    <div class="bs-kpi">
      <div class="kpi-item" v-for="k in kpis" :key="k.label">
        <div class="kpi-top">
          <span class="kpi-label">{{ k.label }}</span>
          <span class="kpi-tag" :style="{ color: k.color, borderColor: k.color + '44' }">{{ k.tag }}</span>
        </div>
        <div class="kpi-value" :style="{ color: k.color }">{{ k.value }}</div>
        <div class="kpi-sub">{{ k.sub }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="bs-grid">
      <div class="panel">
        <div class="panel-title">近 {{ rangeDays }} 天入住率趋势</div>
        <div ref="occChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">近 {{ rangeDays }} 天营收构成趋势</div>
        <div ref="incomeChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">实时房态分布</div>
        <div ref="roomChart" class="chart"></div>
      </div>

      <div class="panel">
        <div class="panel-title">收款支付方式占比</div>
        <div ref="payChart" class="chart"></div>
      </div>

      <div class="panel panel-wide">
        <div class="panel-title">近 {{ rangeDays }} 天热门房型销售额 Top 5</div>
        <div ref="salesChart" class="chart chart-short"></div>
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

const ROOM_STATUS = {
  0: '已预定',
  1: '空闲',
  2: '已入住',
  3: '清洁中',
  4: '维修中',
  5: '下架'
}

const ROOM_COLORS = {
  已入住: '#38e0a6',
  空闲: '#00d2ff',
  已预定: '#7b9cff',
  清洁中: '#ffd666',
  维修中: '#ff7875',
  下架: '#64748b'
}

const screenRef = ref(null)
const nowStr = ref('')
const kpis = ref([])
const rangeDays = ref(30)
const loading = ref(false)
const autoRefresh = ref(false)
const isFullscreen = ref(false)

const occChart = ref(null)
const incomeChart = ref(null)
const roomChart = ref(null)
const payChart = ref(null)
const salesChart = ref(null)

let charts = []
let clockTimer = null
let refreshTimer = null

function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function fmtMoney(v) {
  if (v === null || v === undefined) return '¥0'
  return '¥' + Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

function disposeCharts() {
  charts.forEach(c => c.dispose())
  charts = []
}

function mountChart(elRef, option) {
  if (!elRef.value) return
  const existing = echarts.getInstanceByDom(elRef.value)
  if (existing) existing.dispose()
  const chart = echarts.init(elRef.value)
  chart.setOption(option)
  charts.push(chart)
}

const axisText = { color: '#9fb3d1', fontSize: 11 }
const splitLine = { lineStyle: { color: 'rgba(255,255,255,0.08)' } }

function switchRange(days) {
  if (rangeDays.value === days) return
  rangeDays.value = days
  loadData()
}

function toggleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (autoRefresh.value) {
    refreshTimer = setInterval(loadData, 30000)
  }
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    const el = screenRef.value || document.documentElement
    el.requestFullscreen?.().catch(() => {})
  } else {
    document.exitFullscreen?.().catch(() => {})
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
  setTimeout(handleResize, 120)
}

async function loadData() {
  loading.value = true
  const today = new Date()
  const endDate = fmtDate(today)
  const startDate = fmtDate(new Date(today.getTime() - (rangeDays.value - 1) * 24 * 3600 * 1000))

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

    const staying = Number(overview.stayingCount ?? 0)
    const totalRooms = Number(overview.totalRooms ?? 0)
    const liveOccRate = totalRooms > 0 ? ((staying / totalRooms) * 100).toFixed(1) : '0.0'

    kpis.value = [
      { label: '今日营收', value: fmtMoney(overview.todayIncome), color: '#00d2ff', tag: '实时', sub: '含房费、押金与附加消费' },
      { label: '今日订单', value: `${overview.todayOrderCount ?? 0} 笔`, color: '#7b9cff', tag: '当日', sub: '线上预订与前台接待合计' },
      { label: '当前在住', value: `${staying} / ${totalRooms} 间`, color: '#38e0a6', tag: `入住率 ${liveOccRate}%`, sub: '实时在住房间占比' },
      { label: '累计总营收', value: fmtMoney(overview.totalIncome), color: '#ffd666', tag: '历史累计', sub: '已入账全部经营收入' }
    ]

    // 房态分布环形图
    const roomData = (overview.roomStatus || []).map(i => {
      const name = ROOM_STATUS[i.name] || String(i.name)
      return {
        name,
        value: Number(i.value),
        itemStyle: { color: ROOM_COLORS[name] || '#60a5fa' }
      }
    })
    mountChart(roomChart, {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 间 ({d}%)' },
      legend: { bottom: 0, textStyle: axisText },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '44%'],
          data: roomData,
          label: { color: '#c8d3e8', formatter: '{b}\n{c}间' },
          itemStyle: { borderColor: '#0a1a33', borderWidth: 2 }
        }
      ]
    })

    // 支付方式占比环形图
    const payColors = ['#38e0a6', '#00d2ff', '#7b9cff', '#ffd666', '#ff9f7f']
    const payData = (overview.payment || []).map((i, idx) => ({
      name: i.name,
      value: Number(i.value),
      itemStyle: { color: payColors[idx % payColors.length] }
    }))
    mountChart(payChart, {
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      legend: { bottom: 0, textStyle: axisText },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '44%'],
          data: payData,
          label: { color: '#c8d3e8', formatter: '{b}\n{d}%' },
          itemStyle: { borderColor: '#0a1a33', borderWidth: 2 }
        }
      ]
    })

    // 入住率趋势折线面积图
    const occDates = occ.map(i => (i.date || '').slice(5))
    const occRates = occ.map(i => Number(i.occupancyRate || 0))
    mountChart(occChart, {
      grid: { left: 44, right: 18, top: 26, bottom: 28 },
      tooltip: { trigger: 'axis', formatter: p => `${p[0].axisValue}<br/>入住率：${p[0].value}%` },
      xAxis: { type: 'category', data: occDates, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      yAxis: { type: 'value', max: 100, axisLabel: { ...axisText, formatter: '{value}%' }, splitLine },
      series: [
        {
          type: 'line',
          smooth: true,
          symbolSize: 6,
          data: occRates,
          itemStyle: { color: '#00d2ff' },
          lineStyle: { color: '#00d2ff', width: 2.5 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,210,255,0.38)' },
              { offset: 1, color: 'rgba(0,210,255,0.02)' }
            ])
          }
        }
      ]
    })

    // 营收趋势堆叠柱状图
    const incomeDates = income.map(i => (i.date || '').slice(5))
    const roomFees = income.map(i => Number(i.roomFee || 0))
    const deposits = income.map(i => Number(i.depositFee || 0))
    const others = income.map(i => Number(i.otherFee || 0))
    mountChart(incomeChart, {
      grid: { left: 52, right: 18, top: 36, bottom: 28 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { top: 0, textStyle: axisText },
      xAxis: { type: 'category', data: incomeDates, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      yAxis: { type: 'value', axisLabel: axisText, splitLine },
      series: [
        { name: '房费', type: 'bar', stack: 'total', barMaxWidth: 20, data: roomFees, itemStyle: { color: '#00d2ff', borderRadius: [0, 0, 2, 2] } },
        { name: '押金', type: 'bar', stack: 'total', barMaxWidth: 20, data: deposits, itemStyle: { color: '#7b9cff' } },
        { name: '其他', type: 'bar', stack: 'total', barMaxWidth: 20, data: others, itemStyle: { color: '#38e0a6', borderRadius: [3, 3, 0, 0] } }
      ]
    })

    // 热门房型 Top5 横向条形图
    const top5 = [...sales].sort((a, b) => Number(b.totalAmount || 0) - Number(a.totalAmount || 0)).slice(0, 5)
    const names = top5.map(i => i.roomTypeName).reverse()
    const amounts = top5.map(i => Number(i.totalAmount || 0)).reverse()
    mountChart(salesChart, {
      grid: { left: 96, right: 72, top: 14, bottom: 26 },
      tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>销售额：¥${Number(p[0].value).toLocaleString()}` },
      xAxis: { type: 'value', axisLabel: axisText, splitLine },
      yAxis: { type: 'category', data: names, axisLabel: axisText, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } } },
      series: [
        {
          type: 'bar',
          data: amounts,
          barWidth: 18,
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#7b9cff' },
              { offset: 1, color: '#00d2ff' }
            ])
          },
          label: { show: true, position: 'right', color: '#dce6f5', formatter: p => `¥${Number(p.value).toLocaleString()}` }
        }
      ]
    })
  } catch (e) {
    console.error('加载大屏数据失败', e)
  } finally {
    loading.value = false
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
  clockTimer = setInterval(updateClock, 1000)
  loadData()
  window.addEventListener('resize', handleResize)
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  disposeCharts()
})
</script>

<style scoped>
.bigscreen {
  min-height: calc(100vh - 60px);
  background: radial-gradient(ellipse at top, #0f2950 0%, #08152b 55%, #050c18 100%);
  padding: 16px 20px 24px;
  color: #dce6f5;
  overflow-y: auto;
}

.bs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.bs-controls-left,
.bs-controls-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-group {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  padding: 2px;
}

.range-btn,
.ctrl-btn {
  background: transparent;
  color: #9fb3d1;
  border: none;
  border-radius: 6px;
  padding: 5px 10px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.ctrl-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.range-btn.active,
.ctrl-btn.active,
.ctrl-btn:hover {
  background: rgba(0, 210, 255, 0.18);
  color: #00d2ff;
  border-color: rgba(0, 210, 255, 0.45);
}

.bs-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #38e0a6, #00d2ff, #7b9cff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0;
}

.bs-time {
  font-size: 12.5px;
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
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.07), rgba(255, 255, 255, 0.02));
  border: 1px solid rgba(255, 255, 255, 0.09);
  border-radius: 12px;
  padding: 14px 18px;
  transition: transform 0.2s, border-color 0.2s;
}

.kpi-item:hover {
  transform: translateY(-2px);
  border-color: rgba(0, 210, 255, 0.35);
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.kpi-label {
  font-size: 13px;
  color: #9fb3d1;
}

.kpi-tag {
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 999px;
  border: 1px solid;
  background: rgba(255, 255, 255, 0.04);
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.25;
}

.kpi-sub {
  margin-top: 4px;
  font-size: 11px;
  color: #64748b;
}

.bs-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.panel {
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid rgba(255, 255, 255, 0.09);
  border-radius: 12px;
  padding: 12px 14px;
  min-width: 0;
  overflow: hidden;
}

.panel-wide {
  grid-column: span 2;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #dce6f5;
  margin-bottom: 6px;
  padding-left: 9px;
  border-left: 3px solid #00d2ff;
}

.chart {
  height: 255px;
}

.chart-short {
  height: 220px;
}

@media (max-width: 960px) {
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
