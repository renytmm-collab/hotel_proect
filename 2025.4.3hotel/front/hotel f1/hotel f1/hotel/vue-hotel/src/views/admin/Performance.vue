<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPerformanceList, addPerformance, updatePerformance, getEmployeeList } from '@/api/admin'

// 搜索表单
const searchForm = ref({
  employeeName: '',
  empNo: '',
  month: ''
})

// 绩效表单
const performanceForm = ref({
  id: null,
  employeeId: '',
  month: '',
  score: 90,
  evaluation: ''
})

// 表格数据
const performanceList = ref([])
const employeeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗控制
const showDialog = ref(false)
const showDeleteDialog = ref(false)
const isEdit = ref(false)
const selectedItem = ref({})

// 表单验证规则
const formRules = {
  empNo: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  month: [
    { required: true, message: '请选择考核周期', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur' }
  ],
  evaluation: [
    { required: false, message: '请输入评价', trigger: 'blur' }
  ]
}

// 表单引用
const performanceFormRef = ref(null)

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑绩效' : '新增绩效'
})

// 获取员工列表
const fetchEmployeeList = async () => {
  try {
    const res = await getEmployeeList({
      pageNum: 1,
      pageSize: 100
    })
    if (res.code === 200) {
      employeeList.value = res.data.records
    } else {
      ElMessage.error(res.message || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}

// 获取绩效列表
const fetchPerformance = async () => {
  loading.value = true
  try {
    const res = await getPerformanceList({
      employeeName: searchForm.value.employeeName,
      empNo: searchForm.value.empNo,
      month: searchForm.value.month,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      performanceList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取绩效列表失败')
    }
  } catch (error) {
    console.error('获取绩效列表失败:', error)
    ElMessage.error('获取绩效列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化月份显示
const formatMonth = (month) => {
  if (!month) return ''
  const [year, monthNum] = month.split('-')
  return `${year}年${monthNum}月`
}

// 搜索绩效
const searchPerformance = () => {
  currentPage.value = 1
  fetchPerformance()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    employeeName: '',
    empNo: '',
    month: ''
  }
  currentPage.value = 1
  fetchPerformance()
}

// 打开添加弹窗
const openAddDialog = () => {
  isEdit.value = false
  const today = new Date()
  const currentMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`
  performanceForm.value = {
    id: null,
    empNo: '',
    month: currentMonth,
    score: 90,
    evaluation: ''
  }
  showDialog.value = true
}

// 打开编辑弹窗
const openEditDialog = (item) => {
  isEdit.value = true
  performanceForm.value = {
    id: item.id,
    empNo: item.empNo,
    month: item.month,
    score: item.score,
    evaluation: item.evaluation
  }
  showDialog.value = true
}

// 关闭弹窗
const closeDialog = () => {
  showDialog.value = false
}

// 提交表单
const submitForm = async () => {
  if (!performanceFormRef.value) return
  
  try {
    await performanceFormRef.value.validate()
    
    if (isEdit.value) {
      // 更新绩效
      const res = await updatePerformance({
        id: performanceForm.value.id,
        score: performanceForm.value.score,
        evaluation: performanceForm.value.evaluation
      })
      
      if (res.code === 200) {
        ElMessage.success('更新成功')
        closeDialog()
        fetchPerformance()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 添加绩效
      const res = await addPerformance({
        employeeId: performanceForm.value.employeeId,
        month: performanceForm.value.month,
        score: performanceForm.value.score,
        evaluation: performanceForm.value.evaluation
      })
      
      if (res.code === 200) {
        ElMessage.success('添加成功')
        closeDialog()
        fetchPerformance()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '操作失败')
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 打开删除确认弹窗
const openDeleteDialog = (item) => {
  selectedItem.value = item
  showDeleteDialog.value = true
}

// 关闭删除确认弹窗
const closeDeleteDialog = () => {
  showDeleteDialog.value = false
}

// 删除绩效
const deletePerformance = async () => {
  try {
    const response = await axios.delete(`/api/admin/performance/delete/${selectedItem.value.id}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      closeDeleteDialog()
      fetchPerformance()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除绩效失败:', error)
    ElMessage.error('删除失败')
  }
}

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}

// 生命周期钩子
onMounted(() => {
  fetchEmployeeList()
  fetchPerformance()
})
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">绩效管理</h1>
    <div class="breadcrumb">
      <span @click="goToHome">首页</span> / <span>绩效管理</span>
    </div>
    
    <!-- 搜索和过滤区域 -->
    <div class="bg-white p-4 mb-4 rounded shadow">
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">工号</label>
          <input
            v-model="searchForm.empNo"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入工号"
          />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">员工姓名</label>
          <input
            v-model="searchForm.employeeName"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入员工姓名"
          />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">考核周期</label>
          <input
            v-model="searchForm.month"
            type="month"
            class="w-full px-3 py-2 border border-gray-300 rounded-md"
          />
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button
            @click="searchPerformance"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            搜索
          </button>
          <button
            @click="resetSearch"
            class="ml-2 px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
          >
            重置
          </button>
          <button
            @click="openAddDialog"
            class="ml-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            新增绩效
          </button>
        </div>
      </div>
    </div>
    
    <!-- 绩效表格 -->
    <div class="bg-white p-4 rounded shadow overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              工号
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              姓名
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              考核周期
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              分数
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              评价人
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              操作
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="item in performanceList" :key="item.id">
            <td class="px-6 py-4 whitespace-nowrap">{{ item.empNo }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ item.employeeName }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ formatMonth(item.month) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ item.score }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ item.evaluatorName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button
                @click="openEditDialog(item)"
                class="text-indigo-600 hover:text-indigo-900"
              >
                编辑
              </button>
            </td>
          </tr>
          <tr v-if="!performanceList || performanceList.length === 0">
            <td colspan="6" class="px-6 py-4 text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
      
      <!-- 分页 -->
      <div class="flex flex-col items-center mt-4">
        <div class="mb-2">共 {{ total }} 条</div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchPerformance"
          @current-change="fetchPerformance"
        />
      </div>
    </div>
    
    <!-- 添加/编辑绩效弹窗 -->
    <div v-if="showDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">{{ dialogTitle }}</h2>
        <el-form
          ref="performanceFormRef"
          :model="performanceForm"
          :rules="formRules"
          @submit.prevent="submitForm"
        >
          <el-form-item label="员工" prop="employeeId">
            <el-select
              v-model="performanceForm.employeeId"
              class="w-full"
              :disabled="isEdit"
            >
              <el-option
                v-for="employee in employeeList"
                :key="employee.id"
                :label="`${employee.realName} (${employee.empNo})`"
                :value="employee.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="考核周期" prop="month">
            <el-date-picker
              v-model="performanceForm.month"
              type="month"
              class="w-full"
              :disabled="isEdit"
            />
          </el-form-item>
          <el-form-item label="分数" prop="score">
            <el-input-number
              v-model="performanceForm.score"
              :min="0"
              :max="100"
              :precision="2"
              :step="0.01"
              class="w-full"
            />
          </el-form-item>
          <el-form-item label="评价" prop="evaluation">
            <el-input
              v-model="performanceForm.evaluation"
              type="textarea"
              rows="3"
            />
          </el-form-item>
          <div class="flex justify-end space-x-2">
            <el-button @click="closeDialog">取消</el-button>
            <el-button type="primary" native-type="submit">确认</el-button>
          </div>
        </el-form>
      </div>
    </div>
    
    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">确认删除</h2>
        <p>确定要删除 {{ selectedItem.employeeName }} 在 {{ formatMonth(selectedItem.month) }} 的绩效记录吗？此操作不可撤销。</p>
        <div class="flex justify-end space-x-2 mt-4">
          <button
            @click="closeDeleteDialog"
            class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
          >
            取消
          </button>
          <button
            @click="deletePerformance"
            class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.el-pagination {
  justify-content: center;
}

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
</style>
