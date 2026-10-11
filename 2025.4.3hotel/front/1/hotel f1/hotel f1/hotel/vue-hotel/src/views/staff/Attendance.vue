<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Position, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { checkIn, checkOut, getAttendanceList, getAttendanceStatus } from '@/api/employee'

const router = useRouter()
const loading = ref(true)
const isClockingIn = ref(false)
const isClockingOut = ref(false)
const currentMonth = ref(new Date().getMonth() + 1)
const currentYear = ref(new Date().getFullYear())

// 获取当前日期和时间
const now = new Date()
const today = ref(now.toISOString().split('T')[0])
const currentTime = ref(formatTime(now))

// 考勤状态
const attendanceStatus = ref({
  clockedIn: false,
  clockedOut: false,
  clockInTime: '',
  clockOutTime: '',
  status: -1 // 初始状态：-1（未签到，未签退）
})

// 考勤记录
const attendanceRecords = ref([])

// 分页参数
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 搜索参数
const searchParams = reactive({
  startDate: '',
  endDate: '',
  status: ''
})

// 考勤统计
const statistics = computed(() => {
  // 检查数组是否存在
  if (!attendanceRecords.value || !Array.isArray(attendanceRecords.value) || attendanceRecords.value.length === 0) {
    return {
      total: 0,
      normal: 0,
      late: 0,
      early: 0,
      absent: 0
    }
  }
  
  const total = attendanceRecords.value.length
  const normal = attendanceRecords.value.filter(record => record.status === 0).length
  const late = attendanceRecords.value.filter(record => record.status === 1).length
  const early = attendanceRecords.value.filter(record => record.status === 2).length
  const absent = attendanceRecords.value.filter(record => record.status === 3).length
  
  return {
    total,
    normal,
    late,
    early,
    absent
  }
})

// 模拟员工信息
const staffInfo = reactive({
  id: 'S001',
  name: '张三',
  department: '前台',
  position: '前台接待员',
  avatar: 'https://img1.baidu.com/it/u=1448553235,494400442&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500'
})

// 工作时间设置
const workTimeSettings = reactive({
  clockInTime: '09:00',
  clockOutTime: '18:00',
  lateThreshold: 15, // 迟到阈值（分钟）
  earlyLeaveThreshold: 15 // 早退阈值（分钟）
})

onMounted(() => {
  // 加载考勤记录
  fetchAttendanceList()
  
  // 检查今日是否已打卡
  checkTodayAttendanceStatus()
  
  // 自动更新当前时间
  setInterval(() => {
    const now = new Date()
    currentTime.value = formatTime(now)
  }, 1000)
})

// 加载考勤记录
const fetchAttendanceList = async () => {
  loading.value = true
  try {
    console.log('正在获取考勤记录，token状态：', localStorage.getItem('staffToken') ? '已设置' : '未设置')
    
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      startDate: searchParams.startDate,
      endDate: searchParams.endDate,
      status: searchParams.status
    }
    
    const res = await getAttendanceList(params)
    console.log('考勤记录响应数据:', res)
    
    if (res.code === 200 && res.data) {
      // 获取记录列表（兼容list和records字段）
      const recordsList = res.data.list || res.data.records || [];
      
      if (Array.isArray(recordsList)) {
        // 打印第一条记录以调试字段名
        if (recordsList.length > 0) {
          console.log('考勤记录第一条数据示例:', recordsList[0]);
        }
        
        attendanceRecords.value = recordsList.map(item => ({
          ...item,
          statusText: getStatusText(item.status)
        }))
        total.value = res.data.total || 0
      } else {
        // 如果不是数组，设置为空数组
        console.warn('返回的考勤记录格式不符合预期:', res.data)
        attendanceRecords.value = []
        total.value = 0
      }
    } else {
      console.error('获取考勤记录失败:', res)
      ElMessage.error(res.message || '获取考勤记录失败')
      attendanceRecords.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取考勤记录错误:', error)
    attendanceRecords.value = []
    total.value = 0
    
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      setTimeout(() => {
        router.push('/staff/login')
      }, 1500)
    } else {
      ElMessage.error('获取考勤记录失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

// 处理页面变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAttendanceList()
}

// 处理搜索条件变化
const handleSearchChange = () => {
  currentPage.value = 1
  fetchAttendanceList()
}

// 检查今日考勤状态
const checkTodayAttendanceStatus = async () => {
  try {
    console.log('正在获取今日考勤状态，token状态：', localStorage.getItem('staffToken') ? '已设置' : '未设置')
    const res = await getAttendanceStatus()
    console.log('今日考勤状态响应数据:', res)
    
    if (res.code === 200 && res.data) {
      // 根据API返回的状态更新考勤状态
      // 状态：-1（未签到，未签退），5（已签到未签退），0（正常），1（迟到），2（早退），3（旷工），4（请假）
      let status = -1
      
      if (typeof res.data === 'object' && 'status' in res.data) {
        status = res.data.status
      } else if (typeof res.data === 'number') {
        status = res.data
      }
      
      console.log('解析后的考勤状态:', status)
      
      attendanceStatus.value = {
        clockedIn: status !== -1 && status !== 4 && status !== 3, // 除了未签到、请假和旷工外，都算已签到
        clockedOut: status === 0 || status === 1 || status === 2, // 正常、迟到、早退状态表示已签退
        clockInTime: '', // API目前可能不返回具体时间
        clockOutTime: '',
        status: status
      }
    } else {
      console.error('获取考勤状态失败:', res)
      // 失败时使用默认状态
      attendanceStatus.value = {
        clockedIn: false,
        clockedOut: false,
        clockInTime: '',
        clockOutTime: '',
        status: -1
      }
    }
  } catch (error) {
    console.error('获取考勤状态错误:', error)
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      setTimeout(() => {
        router.push('/staff/login')
      }, 1500)
    } else {
      // 失败时使用默认状态
      attendanceStatus.value = {
        clockedIn: false,
        clockedOut: false,
        clockInTime: '',
        clockOutTime: '',
        status: -1
      }
    }
  }
}

// 签到打卡
const clockIn = async () => {
  if (attendanceStatus.value.clockedIn) {
    ElMessage.warning('今日已打卡签到')
    return
  }
  
  isClockingIn.value = true
  
  try {
    const res = await checkIn()
    
    if (res.code === 200) {
      // 更新状态
      attendanceStatus.value.clockedIn = true
      attendanceStatus.value.clockInTime = formatTime(new Date())
      attendanceStatus.value.status = 5 // 已签到未签退
      
      ElMessage.success('签到成功')
      
      // 重新获取考勤记录
      fetchAttendanceList()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (error) {
    console.error('签到错误:', error)
    ElMessage.error('签到失败，请稍后再试')
  } finally {
    isClockingIn.value = false
  }
}

// 签退打卡
const clockOut = async () => {
  if (!attendanceStatus.value.clockedIn) {
    ElMessage.warning('请先打卡签到')
    return
  }
  
  if (attendanceStatus.value.clockedOut) {
    ElMessage.warning('今日已打卡签退')
    return
  }
  
  isClockingOut.value = true
  
  try {
    const res = await checkOut()
    
    if (res.code === 200) {
      // 更新状态
      attendanceStatus.value.clockedOut = true
      attendanceStatus.value.clockOutTime = formatTime(new Date())
      
      // 暂时无法确定后端返回的具体状态，需要重新获取
      
      ElMessage.success('签退成功')
      
      // 重新获取当天状态和考勤记录
      checkTodayAttendanceStatus()
      fetchAttendanceList()
    } else {
      ElMessage.error(res.message || '签退失败')
    }
  } catch (error) {
    console.error('签退错误:', error)
    ElMessage.error('签退失败，请稍后再试')
  } finally {
    isClockingOut.value = false
  }
}

// 格式化时间
function formatTime(date) {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

// 获取考勤状态文本
function getStatusText(status) {
  switch (status) {
    case -1: return '未签到'
    case 0: return '正常'
    case 1: return '迟到'
    case 2: return '早退'
    case 3: return '旷工'
    case 4: return '请假'
    case 5: return '已签到'
    default: return '未知'
  }
}

// 获取考勤状态标签类型
function getStatusTagType(status) {
  switch (status) {
    case -1: return 'info' // 未签到
    case 0: return 'success' // 正常
    case 1: return 'warning' // 迟到
    case 2: return 'warning' // 早退
    case 3: return 'danger' // 旷工
    case 4: return 'info' // 请假
    case 5: return 'primary' // 已签到
    default: return 'info'
  }
}

// 获取月份天数
function getDaysInMonth(year, month) {
  return new Date(year, month, 0).getDate()
}

// 获取月份名称
function getMonthName(month) {
  const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  return monthNames[month - 1]
}

// 切换月份
function changeMonth(offset) {
  if (currentMonth.value + offset > 12) {
    currentMonth.value = 1
    currentYear.value += 1
  } else if (currentMonth.value + offset < 1) {
    currentMonth.value = 12
    currentYear.value -= 1
  } else {
    currentMonth.value += offset
  }
  
  // 更新搜索日期
  const startDate = new Date(currentYear.value, currentMonth.value - 1, 1)
  const endDate = new Date(currentYear.value, currentMonth.value, 0)
  
  searchParams.startDate = startDate.toISOString().split('T')[0]
  searchParams.endDate = endDate.toISOString().split('T')[0]
  
  handleSearchChange()
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-';
  
  // 尝试处理不同格式的日期
  try {
    // 支持多种日期格式
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr; // 如果无法转换，则返回原字符串
    
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error('日期格式化错误:', e);
    return dateStr;
  }
}
</script>

<template>
  <div class="attendance-container">
    <h2>我的考勤</h2>
    
    <!-- 打卡区域 -->
    <el-card shadow="hover" class="clock-card">
      <div class="clock-container">
        <div class="clock-info">
          <div class="current-time">{{ currentTime }}</div>
          <div class="current-date">{{ today }}</div>
          <div class="clock-status">
            <div class="status-overview">
              <span class="status-label">今日考勤状态:</span>
              <el-tag :type="getStatusTagType(attendanceStatus.status)">{{ getStatusText(attendanceStatus.status) }}</el-tag>
            </div>
            <div class="clock-in-status">
              <span class="status-label">签到:</span>
              <el-tag v-if="attendanceStatus.clockedIn" type="success">已签到</el-tag>
              <el-tag v-else type="info">未签到</el-tag>
              <span v-if="attendanceStatus.clockInTime" class="status-time">{{ attendanceStatus.clockInTime }}</span>
            </div>
            <div class="clock-out-status">
              <span class="status-label">签退:</span>
              <el-tag v-if="attendanceStatus.clockedOut" type="success">已签退</el-tag>
              <el-tag v-else type="info">未签退</el-tag>
              <span v-if="attendanceStatus.clockOutTime" class="status-time">{{ attendanceStatus.clockOutTime }}</span>
            </div>
          </div>
        </div>
        <div class="clock-actions">
          <el-button 
            type="primary" 
            :disabled="attendanceStatus.clockedIn" 
            :loading="isClockingIn"
            @click="clockIn"
          >
            <el-icon><Check /></el-icon>
            签到打卡
          </el-button>
          <el-button 
            type="warning" 
            :disabled="!attendanceStatus.clockedIn || attendanceStatus.clockedOut" 
            :loading="isClockingOut"
            @click="clockOut"
          >
            <el-icon><Position /></el-icon>
            签退打卡
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 考勤统计 -->
    <el-card shadow="hover" class="stats-card">
      <template #header>
        <div class="card-header">
          <span>考勤统计</span>
          <div class="month-selector">
            <el-button :icon="ArrowLeft" text circle @click="changeMonth(-1)" />
            <span class="month-display">{{ currentYear }}年 {{ getMonthName(currentMonth) }}</span>
            <el-button :icon="ArrowRight" text circle @click="changeMonth(1)" />
          </div>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="4" :xs="12">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.total }}</div>
            <div class="stat-label">总记录</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="stat-item normal">
            <div class="stat-value">{{ statistics.normal }}</div>
            <div class="stat-label">正常</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="stat-item late">
            <div class="stat-value">{{ statistics.late }}</div>
            <div class="stat-label">迟到</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="stat-item early">
            <div class="stat-value">{{ statistics.early }}</div>
            <div class="stat-label">早退</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="stat-item absent">
            <div class="stat-value">{{ statistics.absent }}</div>
            <div class="stat-label">缺勤</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="stat-item working-days">
            <div class="stat-value">{{ getDaysInMonth(currentYear, currentMonth) }}</div>
            <div class="stat-label">工作日</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 考勤记录 -->
    <el-card shadow="hover" class="records-card">
      <template #header>
        <div class="card-header">
          <span>考勤记录</span>
          <div class="search-actions">
            <el-date-picker
              v-model="searchParams.startDate"
              type="date"
              placeholder="开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleSearchChange"
            />
            <span class="date-separator">至</span>
            <el-date-picker
              v-model="searchParams.endDate"
              type="date"
              placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleSearchChange"
            />
            <el-select 
              v-model="searchParams.status" 
              placeholder="考勤状态" 
              clearable
              @change="handleSearchChange"
            >
              <el-option label="全部" value="" />
              <el-option label="正常" :value="0" />
              <el-option label="迟到" :value="1" />
              <el-option label="早退" :value="2" />
              <el-option label="旷工" :value="3" />
              <el-option label="请假" :value="4" />
            </el-select>
          </div>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-table :data="attendanceRecords" style="width: 100%" border stripe>
          <el-table-column label="日期">
            <template #default="{ row }">
              {{ formatDate(row.date) }}
            </template>
          </el-table-column>
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签到时间">
            <template #default="{ row }">
              {{ row.clockInTime || row.clock_in_time || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="签退时间">
            <template #default="{ row }">
              {{ row.clockOutTime || row.clock_out_time || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="工作时长">
            <template #default="{ row }">
              {{ row.workHours || row.work_hours || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="备注">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next, jumper"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.attendance-container {
  width: 100%;
}

h2 {
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.clock-card {
  margin-bottom: 20px;
}

.clock-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.clock-info {
  display: flex;
  flex-direction: column;
}

.current-time {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 5px;
}

.current-date {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.clock-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-overview {
  margin-bottom: 5px;
}

.clock-in-status, .clock-out-status, .status-overview {
  display: flex;
  align-items: center;
}

.status-label {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

.status-time {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.clock-actions {
  display: flex;
  gap: 15px;
}

.stats-card, .records-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.month-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.month-display {
  font-size: 16px;
  font-weight: 500;
}

.stat-item {
  text-align: center;
  padding: 15px 0;
  border-radius: 4px;
  margin-bottom: 10px;
  background-color: #f5f7fa;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-item.normal .stat-value {
  color: #67C23A;
}

.stat-item.late .stat-value,
.stat-item.early .stat-value {
  color: #E6A23C;
}

.stat-item.absent .stat-value {
  color: #F56C6C;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-separator {
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .clock-container {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  .clock-info {
    text-align: center;
  }
  
  .search-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-actions {
    width: 100%;
  }
}
</style> 