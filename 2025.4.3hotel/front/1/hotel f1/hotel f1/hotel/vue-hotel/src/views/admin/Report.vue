<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">报表统计</h1>
    
    <!-- 报表筛选区域 -->
    <div class="bg-white p-4 mb-4 rounded shadow">
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">报表类型</label>
          <select
            v-model="filterForm.reportType"
            class="w-full px-3 py-2 border border-gray-300 rounded-md"
          >
            <option value="revenue">营收报表</option>
            <option value="occupancy">入住率报表</option>
            <option value="staff">员工绩效报表</option>
            <option value="customer">客户分析报表</option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">时间范围</label>
          <select
            v-model="filterForm.timeRange"
            class="w-full px-3 py-2 border border-gray-300 rounded-md"
          >
            <option value="last7days">最近7天</option>
            <option value="last30days">最近30天</option>
            <option value="thisMonth">本月</option>
            <option value="lastMonth">上月</option>
            <option value="thisYear">今年</option>
            <option value="custom">自定义</option>
          </select>
        </div>
        <div v-if="filterForm.timeRange === 'custom'" class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">日期范围</label>
          <div class="flex gap-2">
            <input
              v-model="filterForm.startDate"
              type="date"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
            <span class="self-center">至</span>
            <input
              v-model="filterForm.endDate"
              type="date"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button
            @click="generateReport"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            生成报表
          </button>
          <button
            @click="exportReport"
            class="ml-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            导出Excel
          </button>
          <button
            @click="printReport"
            class="ml-2 px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
          >
            打印
          </button>
        </div>
      </div>
    </div>
    
    <!-- 报表概览卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white p-4 rounded shadow">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100 text-blue-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-gray-500 text-sm">总收入</p>
            <p class="text-xl font-bold">￥587,950</p>
            <p class="text-green-500 text-sm">+8.2% <span class="text-gray-500">同比</span></p>
          </div>
        </div>
      </div>
      
      <div class="bg-white p-4 rounded shadow">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-purple-100 text-purple-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-gray-500 text-sm">平均入住率</p>
            <p class="text-xl font-bold">85.6%</p>
            <p class="text-green-500 text-sm">+3.5% <span class="text-gray-500">同比</span></p>
          </div>
        </div>
      </div>
      
      <div class="bg-white p-4 rounded shadow">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100 text-green-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-gray-500 text-sm">客户满意度</p>
            <p class="text-xl font-bold">92.3%</p>
            <p class="text-green-500 text-sm">+1.8% <span class="text-gray-500">同比</span></p>
          </div>
        </div>
      </div>
      
      <div class="bg-white p-4 rounded shadow">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-red-100 text-red-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-gray-500 text-sm">平均每客消费</p>
            <p class="text-xl font-bold">￥1,258</p>
            <p class="text-green-500 text-sm">+10.5% <span class="text-gray-500">同比</span></p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图表展示区 -->
    <div v-if="filterForm.reportType === 'revenue'" class="bg-white p-4 rounded shadow mb-6">
      <h2 class="text-lg font-semibold mb-4">营收报表</h2>
      <div class="flex flex-col lg:flex-row gap-6">
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">月度收入趋势</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <!-- 实际项目中这里应该集成图表库，例如 ECharts 或 Chart.js -->
            <div class="text-gray-400">月度收入趋势图表</div>
          </div>
        </div>
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">收入来源分布</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">收入来源饼图</div>
          </div>
        </div>
      </div>
      
      <!-- 表格数据 -->
      <div class="mt-6">
        <h3 class="text-md font-medium mb-2">月度收入明细</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">月份</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">住宿收入</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">餐饮收入</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">其他收入</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">总收入</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">同比增长</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in revenueData" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">{{ item.month }}</td>
                <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.roomRevenue) }}</td>
                <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.foodRevenue) }}</td>
                <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.otherRevenue) }}</td>
                <td class="px-6 py-4 whitespace-nowrap font-medium">￥{{ formatNumber(item.totalRevenue) }}</td>
                <td class="px-6 py-4 whitespace-nowrap" :class="item.growth >= 0 ? 'text-green-600' : 'text-red-600'">
                  {{ item.growth >= 0 ? '+' : '' }}{{ item.growth }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <div v-else-if="filterForm.reportType === 'occupancy'" class="bg-white p-4 rounded shadow mb-6">
      <h2 class="text-lg font-semibold mb-4">入住率报表</h2>
      <div class="flex flex-col lg:flex-row gap-6">
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">每月入住率趋势</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">入住率趋势图表</div>
          </div>
        </div>
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">房型入住率对比</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">房型入住率条形图</div>
          </div>
        </div>
      </div>
      
      <!-- 表格数据 -->
      <div class="mt-6">
        <h3 class="text-md font-medium mb-2">入住率明细</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">月份</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">标准间</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">豪华间</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商务套房</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">总统套房</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">平均入住率</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in occupancyData" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">{{ item.month }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.standard }}%</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.deluxe }}%</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.business }}%</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.presidential }}%</td>
                <td class="px-6 py-4 whitespace-nowrap font-medium">{{ item.average }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <div v-else-if="filterForm.reportType === 'staff'" class="bg-white p-4 rounded shadow mb-6">
      <h2 class="text-lg font-semibold mb-4">员工绩效报表</h2>
      <div class="flex flex-col lg:flex-row gap-6">
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">部门绩效对比</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">部门绩效雷达图</div>
          </div>
        </div>
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">员工绩效分布</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">员工绩效分布图</div>
          </div>
        </div>
      </div>
      
      <!-- 表格数据 -->
      <div class="mt-6">
        <h3 class="text-md font-medium mb-2">部门绩效统计</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">部门</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">员工人数</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">平均出勤率</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">客户评分</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">平均绩效得分</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">同比增长</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in staffData" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">{{ item.department }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.count }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.attendance }}%</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.customerRating }}</td>
                <td class="px-6 py-4 whitespace-nowrap font-medium">{{ item.averageScore }}</td>
                <td class="px-6 py-4 whitespace-nowrap" :class="item.growth >= 0 ? 'text-green-600' : 'text-red-600'">
                  {{ item.growth >= 0 ? '+' : '' }}{{ item.growth }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <div v-else-if="filterForm.reportType === 'customer'" class="bg-white p-4 rounded shadow mb-6">
      <h2 class="text-lg font-semibold mb-4">客户分析报表</h2>
      <div class="flex flex-col lg:flex-row gap-6">
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">客户来源分布</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">客户来源饼图</div>
          </div>
        </div>
        <div class="flex-1">
          <h3 class="text-md font-medium mb-2">回头客趋势</h3>
          <div class="h-64 bg-gray-50 rounded flex items-center justify-center">
            <div class="text-gray-400">回头客趋势图</div>
          </div>
        </div>
      </div>
      
      <!-- 表格数据 -->
      <div class="mt-6">
        <h3 class="text-md font-medium mb-2">客户统计数据</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">月份</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">新客户数</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">回头客数</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">平均停留时间</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">平均消费</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">客户满意度</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in customerData" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">{{ item.month }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.newCustomers }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.returningCustomers }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ item.avgStay }} 天</td>
                <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.avgSpending) }}</td>
                <td class="px-6 py-4 whitespace-nowrap font-medium">{{ item.satisfaction }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminReport',
  data() {
    return {
      filterForm: {
        reportType: 'revenue',
        timeRange: 'thisMonth',
        startDate: '',
        endDate: ''
      },
      
      // 模拟收入数据
      revenueData: [
        { month: '2024-01', roomRevenue: 180500, foodRevenue: 42300, otherRevenue: 15200, totalRevenue: 238000, growth: 5.2 },
        { month: '2024-02', roomRevenue: 175800, foodRevenue: 38600, otherRevenue: 13500, totalRevenue: 227900, growth: 3.8 },
        { month: '2024-03', roomRevenue: 192300, foodRevenue: 45200, otherRevenue: 18900, totalRevenue: 256400, growth: 7.5 },
        { month: '2024-04', roomRevenue: 210500, foodRevenue: 52300, otherRevenue: 22100, totalRevenue: 284900, growth: 10.2 },
        { month: '2024-05', roomRevenue: 245700, foodRevenue: 61800, otherRevenue: 28300, totalRevenue: 335800, growth: 17.9 }
      ],
      
      // 模拟入住率数据
      occupancyData: [
        { month: '2024-01', standard: 82, deluxe: 78, business: 65, presidential: 45, average: 75 },
        { month: '2024-02', standard: 80, deluxe: 76, business: 62, presidential: 48, average: 73 },
        { month: '2024-03', standard: 85, deluxe: 80, business: 68, presidential: 50, average: 78 },
        { month: '2024-04', standard: 88, deluxe: 85, business: 75, presidential: 58, average: 83 },
        { month: '2024-05', standard: 92, deluxe: 89, business: 82, presidential: 65, average: 87 }
      ],
      
      // 模拟员工绩效数据
      staffData: [
        { department: '前台接待', count: 12, attendance: 96, customerRating: 4.5, averageScore: 92, growth: 3.2 },
        { department: '客房服务', count: 18, attendance: 94, customerRating: 4.3, averageScore: 89, growth: 2.5 },
        { department: '餐饮厨房', count: 15, attendance: 95, customerRating: 4.7, averageScore: 93, growth: 4.1 },
        { department: '安保部门', count: 8, attendance: 98, customerRating: 4.2, averageScore: 88, growth: 1.5 },
        { department: '维修部门', count: 5, attendance: 97, customerRating: 4.4, averageScore: 90, growth: 2.8 }
      ],
      
      // 模拟客户分析数据
      customerData: [
        { month: '2024-01', newCustomers: 324, returningCustomers: 156, avgStay: 2.3, avgSpending: 980, satisfaction: 90 },
        { month: '2024-02', newCustomers: 302, returningCustomers: 168, avgStay: 2.4, avgSpending: 1020, satisfaction: 91 },
        { month: '2024-03', newCustomers: 356, returningCustomers: 182, avgStay: 2.5, avgSpending: 1150, satisfaction: 92 },
        { month: '2024-04', newCustomers: 388, returningCustomers: 210, avgStay: 2.6, avgSpending: 1280, satisfaction: 93 },
        { month: '2024-05', newCustomers: 412, returningCustomers: 245, avgStay: 2.7, avgSpending: 1350, satisfaction: 94 }
      ]
    }
  },
  created() {
    // 初始化日期
    const today = new Date()
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
    const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0)
    
    this.filterForm.startDate = this.formatDate(firstDayOfMonth)
    this.filterForm.endDate = this.formatDate(lastDayOfMonth)
  },
  methods: {
    // 生成报表
    generateReport() {
      console.log('生成报表:', this.filterForm)
      // 实际项目中这里应该调用API获取数据
    },
    
    // 导出报表为Excel
    exportReport() {
      console.log('导出Excel报表')
      // 实际项目中应该集成导出Excel的功能
      // 例如使用SheetJS (xlsx) 库
      this.$message({
        message: '报表导出功能正在开发中',
        type: 'info'
      })
    },
    
    // 打印报表
    printReport() {
      console.log('打印报表')
      window.print()
    },
    
    // 格式化数字，加千位分隔符
    formatNumber(num) {
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")
    },
    
    // 格式化日期为yyyy-MM-dd
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style>
@media print {
  .no-print {
    display: none;
  }
  
  body {
    padding: 20px;
  }
}
</style> 