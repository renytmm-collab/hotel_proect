<template>
  <div class="dashboard-container" v-loading="loading">
    <div class="dashboard-header">
      <h2>员工工作台</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshAllData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 签到卡片 -->
    <el-card class="attendance-card">
      <div class="attendance-status">
        <div class="status-info">
          <h3>今日考勤</h3>
          <div class="attendance-times">
            <p v-if="attendanceStatus.hasCheckedIn">
              <el-icon><Check /></el-icon>
              已签到: {{ attendanceStatus.checkInTime }}
            </p>
            <p v-else>未签到</p>
            <p v-if="attendanceStatus.hasCheckedOut">
              <el-icon><Check /></el-icon>
              已签退: {{ attendanceStatus.checkOutTime }}
            </p>
            <p v-else-if="attendanceStatus.hasCheckedIn">未签退</p>
          </div>
        </div>
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="handleCheckIn" 
            :disabled="attendanceStatus.hasCheckedIn"
          >签到</el-button>
          <el-button 
            type="warning" 
            @click="handleCheckOut" 
            :disabled="!attendanceStatus.hasCheckedIn || attendanceStatus.hasCheckedOut"
          >签退</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据概览 -->
    <div class="stats-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <el-icon class="stats-icon" color="#409EFF"><House /></el-icon>
              <div class="stats-info">
                <div class="stats-value">{{ roomStats.available }}</div>
                <div class="stats-label">可用房间</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <el-icon class="stats-icon" color="#67C23A"><Check /></el-icon>
              <div class="stats-info">
                <div class="stats-value">{{ roomStats.occupied }}</div>
                <div class="stats-label">已入住</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <el-icon class="stats-icon" color="#E6A23C"><Bell /></el-icon>
              <div class="stats-info">
                <div class="stats-value">{{ roomStats.reserved }}</div>
                <div class="stats-label">已预订</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <el-icon class="stats-icon" color="#F56C6C"><Menu /></el-icon>
              <div class="stats-info">
                <div class="stats-value">{{ roomStats.cleaning }}</div>
                <div class="stats-label">清扫中</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 待处理事项 -->
    <div class="tasks-row">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="task-card">
            <template #header>
              <div class="card-header">
                <h3>待处理订单</h3>
                <el-button text @click="goToOrderManagement">查看全部</el-button>
              </div>
            </template>
            <el-table :data="pendingOrders" stripe style="width: 100%">
              <el-table-column prop="orderNo" label="订单号" width="120" />
              <el-table-column prop="guestName" label="客人姓名" width="100" />
              <el-table-column prop="roomType" label="房型" width="120" />
              <el-table-column prop="checkInDate" label="入住日期" width="100" />
              <el-table-column fixed="right" label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handleViewOrder(scope.row.id)"
                  >处理</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="task-card">
            <template #header>
              <div class="card-header">
                <h3>最近客人记录</h3>
                <el-button text @click="goToGuestManagement">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentGuests" stripe style="width: 100%">
              <el-table-column prop="guestName" label="客人姓名" width="100" />
              <el-table-column prop="roomNo" label="房间号" width="100" />
              <el-table-column prop="checkInDate" label="入住日期" width="120" />
              <el-table-column prop="checkOutDate" label="退房日期" width="120" />
              <el-table-column fixed="right" label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handleViewGuest(scope.row.id)"
                  >查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="action-card" @click="goToOrderManagement">
            <el-icon><Tickets /></el-icon>
            <span>预订管理</span>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="action-card" @click="goToCheckInManagement">
            <el-icon><Right /></el-icon>
            <span>入住办理</span>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="action-card" @click="goToCheckOutManagement">
            <el-icon><Remove /></el-icon>
            <span>退房办理</span>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="action-card" @click="goToRoomManagement">
            <el-icon><House /></el-icon>
            <span>房态管理</span>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Calendar, 
  Check, 
  Bell, 
  House,
  Tickets,
  Right,
  Remove,
  Menu,
  Refresh
} from '@element-plus/icons-vue'
import { checkIn, checkOut, getAttendanceStatus } from '@/api/employee'
import { 
  getRoomStatusList, 
  getOrderList, 
  getCheckInList, 
  getCheckOutList,
  getGuestStayRecords
} from '@/api/reception'

const router = useRouter()
const loading = ref(false)

// 签到状态
const attendanceStatus = ref({
  hasCheckedIn: false,
  hasCheckedOut: false,
  checkInTime: '',
  checkOutTime: ''
})

// 房态统计
const roomStats = ref({
  available: 0,
  occupied: 0,
  reserved: 0,
  cleaning: 0
})

// 待处理订单
const pendingOrders = ref([])

// 今日入住和退房
const todayCheckIns = ref([])
const todayCheckOuts = ref([])

// 最近客人记录
const recentGuests = ref([])

// 获取签到状态
const fetchAttendanceStatus = async () => {
  try {
    const res = await getAttendanceStatus()
    if (res.code === 200) {
      attendanceStatus.value = res.data
    }
  } catch (error) {
    console.error('获取签到状态失败:', error)
    ElMessage.error('获取签到状态失败')
  }
}

// 处理签到
const handleCheckIn = async () => {
  try {
    const res = await checkIn()
    if (res.code === 200) {
      ElMessage.success('签到成功')
      fetchAttendanceStatus()
    }
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  }
}

// 处理签退
const handleCheckOut = async () => {
  try {
    const res = await checkOut()
    if (res.code === 200) {
      ElMessage.success('签退成功')
      fetchAttendanceStatus()
    }
  } catch (error) {
    console.error('签退失败:', error)
    ElMessage.error('签退失败')
  }
}

// 获取房态统计
const fetchRoomStats = async () => {
  try {
    const res = await getRoomStatusList()
    if (res.code === 200) {
      const data = res.data
      roomStats.value = {
        available: data.filter(room => room.status === 'AVAILABLE').length,
        occupied: data.filter(room => room.status === 'OCCUPIED').length,
        reserved: data.filter(room => room.status === 'RESERVED').length,
        cleaning: data.filter(room => room.status === 'CLEANING').length
      }
    }
  } catch (error) {
    console.error('获取房态统计失败:', error)
    ElMessage.error('获取房态统计失败')
  }
}

// 获取待处理订单
const fetchPendingOrders = async () => {
  try {
    const res = await getOrderList({ 
      status: 'PENDING',
      limit: 5,
      current: 1
    })
    if (res.code === 200) {
      pendingOrders.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取待处理订单失败:', error)
    ElMessage.error('获取待处理订单失败')
  }
}

// 获取今日入住信息
const fetchTodayCheckIns = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await getCheckInList({ 
      checkInDate: today,
      limit: 5,
      current: 1
    })
    if (res.code === 200) {
      todayCheckIns.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取今日入住信息失败:', error)
    ElMessage.error('获取今日入住信息失败')
  }
}

// 获取今日退房信息
const fetchTodayCheckOuts = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await getCheckOutList({ 
      checkOutDate: today,
      limit: 5,
      current: 1
    })
    if (res.code === 200) {
      todayCheckOuts.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取今日退房信息失败:', error)
    ElMessage.error('获取今日退房信息失败')
  }
}

// 获取最近客人记录
const fetchRecentGuests = async () => {
  try {
    const res = await getGuestStayRecords({
      limit: 5,
      current: 1
    })
    if (res.code === 200) {
      recentGuests.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取最近客人记录失败:', error)
    ElMessage.error('获取最近客人记录失败')
  }
}

// 刷新所有数据
const refreshAllData = () => {
  loading.value = true
  Promise.all([
    fetchAttendanceStatus(),
    fetchRoomStats(),
    fetchPendingOrders(),
    fetchTodayCheckIns(),
    fetchTodayCheckOuts(),
    fetchRecentGuests()
  ]).finally(() => {
    loading.value = false
    ElMessage.success('数据已更新')
  })
}

// 页面导航
const goToRoomManagement = () => router.push('/staff/rooms')
const goToOrderManagement = () => router.push('/staff/orders')
const goToCheckInManagement = () => router.push('/staff/check-ins')
const goToCheckOutManagement = () => router.push('/staff/check-outs')
const goToGuestManagement = () => router.push('/staff/guests')

// 查看详情
const handleViewOrder = (id) => router.push(`/staff/orders/${id}`)
const handleViewCheckIn = (id) => router.push(`/staff/check-in-detail/${id}`)
const handleViewCheckOut = (id) => router.push(`/staff/check-out/${id}`)
const handleViewGuest = (id) => router.push(`/staff/guest/${id}`)

// 处理操作
const handleProcessCheckIn = (id) => router.push(`/staff/check-in/${id}`)
const handleProcessCheckOut = (id) => router.push(`/staff/check-out/${id}`)

onMounted(() => {
  refreshAllData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.attendance-card {
  margin-bottom: 20px;
}

.attendance-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.attendance-times {
  color: #606266;
}

.attendance-times p {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 100%;
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stats-icon {
  font-size: 48px;
  margin-right: 15px;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.tasks-row {
  margin-bottom: 20px;
}

.task-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.quick-actions {
  margin-top: 20px;
}

.quick-actions h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.action-card {
  text-align: center;
  cursor: pointer;
  padding: 20px;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.action-card .el-icon {
  font-size: 32px;
  color: #409EFF;
  margin-bottom: 10px;
}

.action-card span {
  display: block;
  color: #606266;
}
</style> 