<script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { getAttendanceList, updateAttendanceStatus, getEmployeeList } from '@/api/admin'
  
  // 搜索表单
  const searchForm = ref({
    employeeName: '',
    date: '',
    status: '',
    startDate: '',
    endDate: ''
  })
  
  // 表格数据
  const attendanceList = ref([])
  const employeeList = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)
  
  // 状态选项
  const statusOptions = [
    { value: '', label: '全部' },
    { value: 0, label: '正常' },
    { value: 1, label: '迟到' },
    { value: 2, label: '早退' },
    { value: 3, label: '旷工' },
    { value: 4, label: '请假' }
  ]
  
  // 获取员工列表
  const fetchEmployeeList = async () => {
    try {
      const res = await getEmployeeList({
        pageNum: 1,
        pageSize: 100
      })
      if (res.code === 200) {
        employeeList.value = res.data.list
      } else {
        ElMessage.error(res.message || '获取员工列表失败')
      }
    } catch (error) {
      console.error('获取员工列表失败:', error)
      ElMessage.error('获取员工列表失败')
    }
  }
  // 格式化日期为 yyyy-MM-dd
  function formatDate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');  // 月份从 0 开始，所以需要 +1
    const day = String(d.getDate()).padStart(2, '0');
    // const hours = String(d.getHours()).padStart(2, '0');
    // const minutes = String(d.getMinutes()).padStart(2, '0');
    // const seconds = String(d.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  // 获取考勤列表
  const fetchAttendance = async () => {
    loading.value = true
    try {
      const res = await getAttendanceList({
        employeeName: searchForm.value.employeeName,
        date: searchForm.value.date ? formatDate(searchForm.value.date) : '',
        status: searchForm.value.status,
        startDate: searchForm.value.startDate ? formatDate(searchForm.value.startDate) : '',
        endDate: searchForm.value.endDate ? formatDate(searchForm.value.endDate) : '',
        pageNum: currentPage.value,
        pageSize: pageSize.value
      })
      if (res.code === 200) {
        attendanceList.value = res.data.records
        total.value = res.data.total
      } else {
        ElMessage.error(res.message || '获取考勤列表失败')
      }
    } catch (error) {
      console.error('获取考勤列表失败:', error)
      ElMessage.error('获取考勤列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 搜索考勤
  const searchAttendance = () => {
    currentPage.value = 1
    fetchAttendance()
  }
  
  // 重置搜索
  const resetSearch = () => {
    searchForm.value = {
      employeeName: '',
      date: '',
      status: '',
      startDate: '',
      endDate: ''
    }
    currentPage.value = 1
    fetchAttendance()
  }
  
  // 更新考勤状态
  const updateStatus = async (id, status) => {
    try {
      const res = await updateAttendanceStatus({
        id,
        status
      })
      if (res.code === 200) {
        ElMessage.success('更新成功')
        fetchAttendance()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } catch (error) {
      console.error('更新考勤状态失败:', error)
      ElMessage.error('更新失败')
    }
  }
  
  // 返回首页
  const goToHome = () => {
    window.location.href = '/admin'
  }
  
  // 生命周期钩子
  onMounted(() => {
    fetchEmployeeList()
    fetchAttendance()
  })
  </script>
  
  <template>
    <div class="container mx-auto p-4">
      <h1 class="text-2xl font-bold mb-4">考勤管理</h1>
      <div class="breadcrumb">
        <span @click="goToHome">首页</span> / <span>考勤管理</span>
      </div>
      
      <!-- 搜索和过滤区域 -->
      <div class="bg-white p-4 mb-4 rounded shadow">
        <div class="flex flex-wrap gap-4 mb-4">
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
            <label class="block text-sm font-medium text-gray-700 mb-1">考勤日期</label>
            <el-date-picker
              v-model="searchForm.date"
              type="date"
              class="w-full"
              placeholder="选择日期"
            />
          </div>
          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-1">考勤状态</label>
            <el-select
              v-model="searchForm.status"
              class="w-full"
              placeholder="请选择状态"
            >
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </div>
          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-1">开始日期</label>
            <el-date-picker
              v-model="searchForm.startDate"
              type="date"
              class="w-full"
              placeholder="选择开始日期"
            />
          </div>
          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-1">结束日期</label>
            <el-date-picker
              v-model="searchForm.endDate"
              type="date"
              class="w-full"
              placeholder="选择结束日期"
            />
          </div>
          <div class="flex-1 min-w-[200px] flex items-end">
            <button
              @click="searchAttendance"
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
          </div>
        </div>
      </div>
      
      <!-- 考勤表格 -->
      <div class="bg-white p-4 rounded shadow overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                头像
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                员工姓名
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                考勤日期
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                考勤状态
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in attendanceList" :key="item.id">
              <td class="px-6 py-4 whitespace-nowrap">
                <!-- <img
                  :src="item.avater"
                  class="h-10 w-10 rounded-full object-cover"
                  :alt="item.employeeName"
                /> -->
                <img v-if="item.avatar" :src="item.avatar" class="h-10 w-10 rounded-full" />
                <div v-else class="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                  <span class="text-gray-500">{{ item.employeeName?.charAt(0) }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.employeeName }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ item.date }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="{
                  'text-green-600': item.status === 0,
                  'text-yellow-600': item.status === 1 || item.status === 2,
                  'text-red-600': item.status === 3,
                  'text-blue-600': item.status === 4
                }">
                  {{ statusOptions.find(opt => opt.value === item.status)?.label || '未知' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <el-select
                  v-model="item.status"
                  class="w-32"
                  @change="(value) => updateStatus(item.id, value)"
                >
                  <el-option
                    v-for="option in statusOptions.filter(opt => opt.value !== '')"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </td>
            </tr>
            <tr v-if="!attendanceList || attendanceList.length === 0">
              <td colspan="5" class="px-6 py-4 text-center">暂无数据</td>
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
            @size-change="fetchAttendance"
            @current-change="fetchAttendance"
          />
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
  