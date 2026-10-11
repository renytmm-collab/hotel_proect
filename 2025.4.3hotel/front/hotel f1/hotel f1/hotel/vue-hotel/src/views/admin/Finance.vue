<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getProfitStats,
  getIncomeList,
  getExpenseList,
  addExpense,
  updateExpense
} from '@/api/admin'

// 统计卡片数据
const stats = ref({
  currentMonth: {
    income: 0,
    expense: 0,
    profit: 0,
    incomeGrowth: 0,
    expenseGrowth: 0,
    profitGrowth: 0
  }
})

// 收入记录
const incomeList = ref([])
const incomeTotal = ref(0)
const incomePage = ref(1)
const incomePageSize = ref(10)
const incomeSearchForm = ref({
  type: '',
  startDate: '',
  endDate: ''
})

// 支出记录
const expenseList = ref([])
const expenseTotal = ref(0)
const expensePage = ref(1)
const expensePageSize = ref(10)
const expenseSearchForm = ref({
  type: '',
  startDate: '',
  endDate: ''
})

// 支出表单
const expenseForm = ref({
  id: null,
  amount: '',
  type: '',
  paymentMethod: '',
  remark: ''
})

// 弹窗控制
const showExpenseDialog = ref(false)
const isEditExpense = ref(false)

// 记录类型切换
const activeTab = ref('expense') // 'expense' 或 'income'

// 类型选项
const incomeTypes = [
  { value: 0, label: '房费' },
  { value: 1, label: '押金' },
  { value: 2, label: '其他' }
]

const expenseTypes = [
  { value: 0, label: '退款' },
  { value: 1, label: '采购' },
  { value: 2, label: '工资' },
  { value: 3, label: '水电' },
  { value: 4, label: '其他' }
]

// 获取统计数据
const fetchStats = async () => {
  try {
    const now = new Date()
    const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
    const lastMonthStr = `${lastMonth.getFullYear()}-${String(lastMonth.getMonth() + 1).padStart(2, '0')}`

    // 获取当前月份的最后一天
    const currentMonthLastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    const lastMonthLastDay = new Date(lastMonth.getFullYear(), lastMonth.getMonth() + 1, 0)

    const [currentRes, lastRes] = await Promise.all([
      getProfitStats({
        startDate: `${currentMonth}-01`,
        endDate: `${currentMonth}-${String(currentMonthLastDay.getDate()).padStart(2, '0')}`,
        groupBy: 'month'
      }),
      getProfitStats({
        startDate: `${lastMonthStr}-01`,
        endDate: `${lastMonthStr}-${String(lastMonthLastDay.getDate()).padStart(2, '0')}`,
        groupBy: 'month'
      })
    ])

    const currentData = currentRes.data[0]
    const lastData = lastRes.data[0]

    stats.value.currentMonth = {
      income: currentData.income,
      expense: currentData.expense,
      profit: currentData.profit,
      incomeGrowth: lastData ? ((currentData.income - lastData.income) / lastData.income * 100) : 0,
      expenseGrowth: lastData ? ((currentData.expense - lastData.expense) / lastData.expense * 100) : 0,
      profitGrowth: lastData ? ((currentData.profit - lastData.profit) / lastData.profit * 100) : 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取收入记录
const fetchIncomeList = async () => {
  try {
    const res = await getIncomeList({
      ...incomeSearchForm.value,
      pageNum: incomePage.value,
      pageSize: incomePageSize.value
    })
    incomeList.value = res.data.records
    incomeTotal.value = res.data.total
  } catch (error) {
    console.error('获取收入记录失败:', error)
    ElMessage.error('获取收入记录失败')
  }
}

// 获取支出记录
const fetchExpenseList = async () => {
  try {
    const res = await getExpenseList({
      ...expenseSearchForm.value,
      pageNum: expensePage.value,
      pageSize: expensePageSize.value
    })
    expenseList.value = res.data.records
    expenseTotal.value = res.data.total
  } catch (error) {
    console.error('获取支出记录失败:', error)
    ElMessage.error('获取支出记录失败')
  }
}

// 打开添加支出弹窗
const openAddExpenseDialog = () => {
  isEditExpense.value = false
  expenseForm.value = {
    id: null,
    amount: '',
    type: '',
    paymentMethod: '',
    remark: ''
  }
  showExpenseDialog.value = true
}

// 打开编辑支出弹窗
const openEditExpenseDialog = (expense) => {
  isEditExpense.value = true
  expenseForm.value = {
    id: expense.id,
    amount: expense.amount,
    type: expense.type,
    paymentMethod: expense.paymentMethod,
    remark: expense.remark
  }
  showExpenseDialog.value = true
}

// 提交支出表单
const submitExpenseForm = async () => {
  try {
    if (isEditExpense.value) {
      // 编辑时，确保传递id参数
      const updateData = {
        id: expenseForm.value.id,
        amount: expenseForm.value.amount,
        type: expenseForm.value.type,
        paymentMethod: expenseForm.value.paymentMethod,
        remark: expenseForm.value.remark
      }
      await updateExpense(updateData)
      ElMessage.success('更新成功')
    } else {
      // 添加时不需要id
      const addData = {
        amount: expenseForm.value.amount,
        type: expenseForm.value.type,
        paymentMethod: expenseForm.value.paymentMethod,
        remark: expenseForm.value.remark
      }
      await addExpense(addData)
      ElMessage.success('添加成功')
    }
    showExpenseDialog.value = false
    fetchExpenseList()
  } catch (error) {
    console.error('保存支出记录失败:', error)
    ElMessage.error('保存支出记录失败')
  }
}

// 格式化数字
const formatNumber = (num) => {
  return Number(num).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 生命周期钩子
onMounted(() => {
  fetchStats()
  fetchExpenseList()
})

// 切换记录类型
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'income') {
    fetchIncomeList()
  } else {
    fetchExpenseList()
  }
}

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">财务管理</h1>
    <div class="breadcrumb">
      <span @click="goToHome">首页</span> / <span>财务管理</span>
    </div>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="bg-white p-4 rounded shadow">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100 text-blue-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-gray-500 text-sm">当月收入</p>
            <p class="text-xl font-bold">￥{{ formatNumber(stats.currentMonth.income) }}</p>
            <p class="text-green-500 text-sm" v-if="stats.currentMonth.incomeGrowth > 0">
              +{{ stats.currentMonth.incomeGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
            <p class="text-red-500 text-sm" v-else>
              {{ stats.currentMonth.incomeGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
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
            <p class="text-gray-500 text-sm">当月支出</p>
            <p class="text-xl font-bold">￥{{ formatNumber(stats.currentMonth.expense) }}</p>
            <p class="text-green-500 text-sm" v-if="stats.currentMonth.expenseGrowth < 0">
              {{ stats.currentMonth.expenseGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
            <p class="text-red-500 text-sm" v-else>
              +{{ stats.currentMonth.expenseGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
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
            <p class="text-gray-500 text-sm">净利润</p>
            <p class="text-xl font-bold">￥{{ formatNumber(stats.currentMonth.profit) }}</p>
            <p class="text-green-500 text-sm" v-if="stats.currentMonth.profitGrowth > 0">
              +{{ stats.currentMonth.profitGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
            <p class="text-red-500 text-sm" v-else>
              {{ stats.currentMonth.profitGrowth.toFixed(1) }}% <span class="text-gray-500">同比上月</span>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 记录类型切换 -->
    <div class="flex justify-center mb-4">
      <div class="inline-flex rounded-md shadow-sm">
        <button
          @click="switchTab('expense')"
          :class="[
            'px-4 py-2 text-sm font-medium rounded-l-md',
            activeTab === 'expense'
              ? 'bg-blue-600 text-white'
              : 'bg-white text-gray-700 hover:bg-gray-50'
          ]"
        >
          支出记录
        </button>
        <button
          @click="switchTab('income')"
          :class="[
            'px-4 py-2 text-sm font-medium rounded-r-md',
            activeTab === 'income'
              ? 'bg-blue-600 text-white'
              : 'bg-white text-gray-700 hover:bg-gray-50'
          ]"
        >
          收入记录
        </button>
      </div>
    </div>

    <!-- 收入记录 -->
    <div v-if="activeTab === 'income'" class="bg-white p-4 mb-4 rounded shadow">
      <h2 class="text-lg font-semibold mb-4">收入记录</h2>
      
      <!-- 搜索区域 -->
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">类型</label>
          <select v-model="incomeSearchForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option v-for="type in incomeTypes" :key="type.value" :value="type.value">
              {{ type.label }}
            </option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">日期范围</label>
          <div class="flex gap-2">
            <input v-model="incomeSearchForm.startDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-md" />
            <span class="self-center">至</span>
            <input v-model="incomeSearchForm.endDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-md" />
          </div>
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button @click="fetchIncomeList" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            搜索
          </button>
        </div>
      </div>

      <!-- 收入表格 -->
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">金额</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">备注</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作人</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in incomeList" :key="item.id">
              <td class="px-6 py-4 whitespace-nowrap">{{ formatDate(item.createTime) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ incomeTypes.find(t => t.value === item.type)?.label || item.type }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.amount) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.remark || '-' }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.operatorName }}</td>
            </tr>
            <tr v-if="incomeList.length === 0">
              <td colspan="5" class="px-6 py-4 text-center">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="mt-4">
        <el-pagination
          v-model:current-page="incomePage"
          v-model:page-size="incomePageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="incomeTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchIncomeList"
          @current-change="fetchIncomeList"
        />
      </div>
    </div>

    <!-- 支出记录 -->
    <div v-else class="bg-white p-4 rounded shadow">
      <h2 class="text-lg font-semibold mb-4">支出记录</h2>
      
      <!-- 搜索区域 -->
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">类型</label>
          <select v-model="expenseSearchForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option v-for="type in expenseTypes" :key="type.value" :value="type.value">
              {{ type.label }}
            </option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">日期范围</label>
          <div class="flex gap-2">
            <input v-model="expenseSearchForm.startDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-md" />
            <span class="self-center">至</span>
            <input v-model="expenseSearchForm.endDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-md" />
          </div>
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button @click="fetchExpenseList" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            搜索
          </button>
          <button @click="openAddExpenseDialog" class="ml-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">
            添加支出
          </button>
        </div>
      </div>

      <!-- 支出表格 -->
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">金额</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">备注</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作人</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in expenseList" :key="item.id">
              <td class="px-6 py-4 whitespace-nowrap">{{ formatDate(item.createTime) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ expenseTypes.find(t => t.value === item.type)?.label || item.type }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">￥{{ formatNumber(item.amount) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.remark || '-' }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.operatorName }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <button @click="openEditExpenseDialog(item)" class="text-indigo-600 hover:text-indigo-900">
                  编辑
                </button>
              </td>
            </tr>
            <tr v-if="!expenseList || expenseList.length === 0">
              <td colspan="6" class="px-6 py-4 text-center">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="mt-4">
        <el-pagination
          v-model:current-page="expensePage"
          v-model:page-size="expensePageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="expenseTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchExpenseList"
          @current-change="fetchExpenseList"
        />
      </div>
    </div>

    <!-- 支出表单弹窗 -->
    <el-dialog
      v-model="showExpenseDialog"
      :title="isEditExpense ? '编辑支出' : '添加支出'"
      width="500px"
    >
      <el-form :model="expenseForm" label-width="100px">
        <el-form-item label="金额" required>
          <el-input-number v-model="expenseForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="expenseForm.type" style="width: 100%">
            <el-option
              v-for="type in expenseTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-input v-model="expenseForm.paymentMethod" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="expenseForm.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showExpenseDialog = false">取消</el-button>
          <el-button type="primary" @click="submitExpenseForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-pagination {
  justify-content: center;
}
</style>
<style scoped>
.breadcrumb {
  font-size: 14px;
  color: #666;
}

.breadcrumb span {
  cursor: pointer;
}

.breadcrumb span:hover {
  color: #409EFF;
}

.el-button--primary {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
}

.el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

/* 切换按钮样式 */
.inline-flex button {
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.inline-flex button:first-child {
  border-right: none;
}

.inline-flex button:last-child {
  border-left: none;
}

.inline-flex button:hover {
  z-index: 1;
}
</style>
