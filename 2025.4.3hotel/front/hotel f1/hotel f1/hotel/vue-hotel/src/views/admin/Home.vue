<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getPerformanceList,
  getExpenseList,
  getRoomSalesStats,
  getIncomeStats,
  getExpenseStats,
  getProfitStats,
  getOccupancyStats
} from '@/api/admin'

const router = useRouter()

// 统计数据
const statistics = ref({
  totalIncome: 0,
  totalExpense: 0,
  totalProfit: 0,
  occupancyRate: 0
})

// 收入数据
const incomeData = ref([])

// 入住率数据
const occupancyData = ref([])

// 员工绩效数据
const performanceData = ref([])

// 最近支出数据
const recentExpenses = ref([])

// 获取当前月份的开始和结束日期
const getCurrentMonthDates = () => {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  const startDate = `${start.getFullYear()}-${(start.getMonth() + 1).toString().padStart(2, '0')}-${start.getDate().toString().padStart(2, '0')}`;
  const endDate = `${end.getFullYear()}-${(end.getMonth() + 1).toString().padStart(2, '0')}-${end.getDate().toString().padStart(2, '0')}`;
  // console.log(startDate,endDate)
  return {
    start: startDate,
    end: endDate    
  }
}

// 获取最近7天的日期
const getLastSevenDays = () => {
  const dates = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toISOString().split('T')[0])
  }
  return dates
}

// 获取收入统计
const fetchIncomeStats = async () => {
  try {
    const { start, end } = getCurrentMonthDates()
    const res = await getIncomeStats({
      startDate: start,
      endDate: end,
      groupBy: 'month'
    })
    if (res.code === 200 && res.data.length > 0) {
      statistics.value.totalIncome = res.data[0].totalAmount
    }
  } catch (error) {
    console.error('获取收入统计错误:', error)
    ElMessage.error('获取收入统计失败')
  }
}

// 获取支出统计
const fetchExpenseStats = async () => {
  try {
    const { start, end } = getCurrentMonthDates()
    const res = await getExpenseStats({
      startDate: start,
      endDate: end,
      groupBy: 'month'
    })
    if (res.code === 200 && res.data.length > 0) {
      statistics.value.totalExpense = res.data[0].totalAmount
    }
  } catch (error) {
    console.error('获取支出统计错误:', error)
    ElMessage.error('获取支出统计失败')
  }
}

// 获取利润统计
const fetchProfitStats = async () => {
  try {
    const { start, end } = getCurrentMonthDates()
    const res = await getProfitStats({
      startDate: start,
      endDate: end,
      groupBy: 'month'
    })
    if (res.code === 200 && res.data.length > 0) {
      statistics.value.totalProfit = res.data[0].profit
    }
  } catch (error) {
    console.error('获取利润统计错误:', error)
    ElMessage.error('获取利润统计失败')
  }
}

// 获取入住率统计
const fetchOccupancyStats = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await getOccupancyStats({
      startDate: today,
      endDate: today,
      groupBy: 'day'
    })
    if (res.code === 200 && res.data.length > 0) {
      statistics.value.occupancyRate = res.data[0].occupancyRate
    }
  } catch (error) {
    console.error('获取入住率统计错误:', error)
    ElMessage.error('获取入住率统计失败')
  }
}

// 获取收入趋势数据
const fetchIncomeTrend = async () => {
  try {
    const dates = getLastSevenDays()
    const res = await getIncomeStats({
      startDate: dates[0],
      endDate: dates[6],
      groupBy: 'day'
    })
    if (res.code === 200) {
      incomeData.value = res.data.map(item => ({
        date: item.date.split('-').slice(1).join('月') + '日',
        income: item.totalAmount
      }))
    }
  } catch (error) {
    console.error('获取收入趋势错误:', error)
    ElMessage.error('获取收入趋势失败')
  }
}

// 获取房型入住率
const fetchRoomOccupancy = async () => {
  try {
    const { start, end } = getCurrentMonthDates()
    const res = await getRoomSalesStats({
      startDate: start,
      endDate: end
    })
    if (res.code === 200) {
      occupancyData.value = res.data.map(item => ({
        roomType: item.roomTypeName,
        rate: item.occupancyRate
      }))
    }
  } catch (error) {
    console.error('获取房型入住率错误:', error)
    ElMessage.error('获取房型入住率失败')
  }
}

// 获取员工绩效
const fetchPerformance = async () => {
  try {
    const res = await getPerformanceList({
      pageNum: 1,
      pageSize: 5
    })
    if (res.code === 200) {
      const sortedRecords = res.data.records.sort((a, b) => b.score - a.score);
      performanceData.value = sortedRecords.map(item => ({
        name: item.employeeName,
        department: item.emp_no,
        score: item.score
      }))
    }
  } catch (error) {
    console.error('获取员工绩效错误:', error)
    ElMessage.error('获取员工绩效失败')
  }
}

// 获取最近支出
const fetchRecentExpenses = async () => {
  try {
    const res = await getExpenseList({
      pageNum: 1,
      pageSize: 5
    })
    if (res.code === 200) {
      const typeMap = {
        0: '退款',
        1: '采购',
        2: '工资',
        3: '水电',
        4: '其他'
      }
      recentExpenses.value = res.data.records.map(item => ({ 
        id: item.id,
        type: typeMap[item.type] || '其他',
        amount: item.amount,
        date: item.createTime.split(' ')[0],
        remark: item.remark
      }))
    }
  } catch (error) {
    console.error('获取最近支出错误:', error)
    ElMessage.error('获取最近支出失败')
  }
}

// 初始化数据
const initData = async () => {
  await Promise.all([
    fetchIncomeStats(),
    fetchExpenseStats(),
    fetchProfitStats(),
    fetchOccupancyStats(),
    fetchIncomeTrend(),
    fetchRoomOccupancy(),
    fetchPerformance(),
    fetchRecentExpenses()
  ])
}

// 组件加载时获取数据
onMounted(() => {
  initData()
})
</script>

<template>
  <div class="admin-home-container">
    <!-- 管理仪表盘 -->
    <div class="dashboard">
      <h2>当月统计</h2>
      
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-icon" style="background-color: #67C23A;">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-title">总收入</div>
                <div class="stats-value">¥{{ statistics.totalIncome.toLocaleString() }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-icon" style="background-color: #F56C6C;">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-title">总支出</div>
                <div class="stats-value">¥{{ statistics.totalExpense.toLocaleString() }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-icon" style="background-color: #409EFF;">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-title">总利润</div>
                <div class="stats-value">¥{{ statistics.totalProfit.toLocaleString() }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-icon" style="background-color: #E6A23C;">
                <el-icon><House /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-title">入住率</div>
                <div class="stats-value">{{ statistics.occupancyRate }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 收入趋势图 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>收入趋势</span>
              </div>
            </template>
            <div class="income-chart">
              <div class="chart-container">
                <div v-for="(item, index) in incomeData" :key="index" class="chart-bar-container">
                  <div class="chart-value">{{ item.income.toLocaleString() }}</div>
                  <div class="chart-bar" :style="{ height: (item.income / 25000 * 100) + '%' }"></div>
                  
                  <div class="chart-label">{{ item.date }}</div>
                </div>
              </div>
              <div class="chart-values">
                <div>¥25,000</div>
                <div>¥20,000</div>
                <div>¥15,000</div>
                <div>¥10,000</div>
                <div>¥5,000</div>
                <div>¥0</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 入住率 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>各房型入住率</span>
              </div>
            </template>
            <div class="occupancy-chart">
              <div v-for="item in occupancyData" :key="item.roomType" class="occupancy-item">
                <div class="occupancy-info">
                  <span class="occupancy-type">{{ item.roomType }}</span>
                  <!-- <span class="occupancy-rate">{{ item.rate }}%</span> -->
                </div>
                <el-progress 
                  :percentage="item.rate" 
                  :color="item.rate > 80 ? '#67C23A' : item.rate > 60 ? '#E6A23C' : '#F56C6C'"
                  :stroke-width="15"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 员工绩效和最近支出 -->
      <el-row :gutter="20" class="data-row">
        <el-col :xs="24" :lg="12">
          <el-card class="performance-card">
            <template #header>
              <div class="card-header">
                <span>员工绩效排名</span>
                <el-button text @click="router.push('/admin/performance')">查看全部</el-button>
              </div>
            </template>
            <div class="performance-list">
              <div v-for="(item, index) in performanceData" :key="index" class="performance-item">
                <div class="rank-number">{{ index + 1 }}</div>
                <div class="performance-info">
                  <div class="employee-name">{{ item.name }} <span class="department">({{ item.department }})</span></div>
                  <el-progress 
                    :percentage="item.score" 
                    :color="item.score > 90 ? '#67C23A' : item.score > 80 ? '#409EFF' : '#F56C6C'"
                    :stroke-width="10"
                  />
                </div>
                <!-- <div class="score">{{ item.score }}</div> -->
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="expense-card">
            <template #header>
              <div class="card-header">
                <span>最近支出</span>
                <el-button text @click="router.push('/admin/finance')">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentExpenses" style="width: 100%" stripe>
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column prop="amount" label="金额" width="80">
                <template #default="scope">
                  ¥{{ scope.row.amount.toLocaleString() }}
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="200" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.admin-home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard h2 {
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.dashboard h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.stats-row, .chart-row, .data-row {
  margin-bottom: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-card-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
}

.stats-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stats-info {
  flex: 1;
}

.stats-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.income-chart {
  height: 340px;
  display: flex;
  /* flex-direction: column; */
}

.chart-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex: 1;
  height: 300px;
  padding-bottom: 0px;
  position: relative;
  border-bottom: 1px solid #dcdfe6;
}

.chart-values {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-top: 10px;
  padding-bottom: 50px;
  color: #909399;
  font-size: 12px;
  text-align: right;
  width: 70px;
  border-right: 1px solid #dcdfe6;
}

.chart-bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  position: relative;
  justify-content: flex-end;
}

.chart-bar {
  width: 60%;
  background-color: #409EFF;
  border-radius: 4px 4px 0 0;
  margin-bottom: 8px;
  min-height: 1px;
}

.chart-label {
  position: absolute;
  bottom: -25px;
  font-size: 12px;
  color: #909399;
  width: 100%;
  text-align: center;
}

.occupancy-chart {
  padding: 10px 0;
}

.occupancy-item {
  margin-bottom: 20px;
}

.occupancy-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.occupancy-type {
  font-size: 14px;
  color: #606266;
}

.occupancy-rate {
  font-weight: bold;
  color: #303133;
}

.performance-list {
  padding: 10px 0;
}

.performance-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rank-number {
  width: 30px;
  height: 30px;
  background-color: #f5f7fa;
  color: #606266;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  margin-right: 15px;
  font-weight: bold;
}

.performance-item:nth-child(1) .rank-number {
  background-color: #F56C6C;
  color: white;
}

.performance-item:nth-child(2) .rank-number {
  background-color: #E6A23C;
  color: white;
}

.performance-item:nth-child(3) .rank-number {
  background-color: #409EFF;
  color: white;
}

.performance-info {
  flex: 1;
  margin-right: 15px;
}

.employee-name {
  margin-bottom: 5px;
  font-weight: bold;
}

.department {
  font-weight: normal;
  color: #909399;
  font-size: 12px;
}

.score {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.expense-card {
  height: 100%;
}
</style> 