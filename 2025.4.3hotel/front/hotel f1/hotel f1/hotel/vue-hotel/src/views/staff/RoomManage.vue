<template>
  <div class="room-manage-container">
    <h2>房间管理</h2>
    <div class="content">
      <!-- 临时内容 -->
      <el-empty description="房间管理功能开发中..." />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomStatusList, updateRoomStatus, getRoomDetailStatus } from '@/api/reception'

const router = useRouter()
const loading = ref(true)
const detailLoading = ref(false)
const roomDetailDialogVisible = ref(false)
const currentRoom = ref(null)

// 房间状态选项
const roomStatusOptions = [
  { value: '', label: '全部状态' },
  { value: 1, label: '空闲', color: '#67C23A' },
  { value: 2, label: '已入住', color: '#409EFF' },
  { value: 3, label: '清洁中', color: '#E6A23C' },
  { value: 4, label: '维修中', color: '#F56C6C' },
  { value: 5, label: '下架', color: '#909399' }
]

// 获取状态标签颜色
const getStatusColor = (status) => {
  const statusOption = roomStatusOptions.find(option => option.value === status)
  return statusOption ? statusOption.color : '#909399'
}

// 获取状态显示文本
const getStatusText = (status) => {
  const statusOption = roomStatusOptions.find(option => option.value === status)
  return statusOption ? statusOption.label : '未知状态'
}

// 房型选项（实际应从API获取）
const roomTypeOptions = [
  { value: '', label: '全部房型' },
  { value: 1, label: '豪华大床房' },
  { value: 2, label: '商务双床房' },
  { value: 3, label: '标准大床房' },
  { value: 4, label: '高级套房' }
]

// 楼层选项（实际应从API获取或根据实际情况定制）
const floorOptions = [
  { value: '', label: '全部楼层' },
  { value: '1', label: '1楼' },
  { value: '2', label: '2楼' },
  { value: '3', label: '3楼' },
  { value: '4', label: '4楼' },
  { value: '5', label: '5楼' },
  { value: '6', label: '6楼' },
  { value: '7', label: '7楼' },
  { value: '8', label: '8楼' },
  { value: '9', label: '9楼' }
]

// 搜索表单
const searchForm = reactive({
  floor: '',
  status: '',
  roomTypeId: ''
})

// 房间列表
const roomList = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 统计数据
const statistics = computed(() => {
  return {
    total: roomList.value.length,
    vacant: roomList.value.filter(room => room.status === 1).length,
    occupied: roomList.value.filter(room => room.status === 2).length,
    cleaning: roomList.value.filter(room => room.status === 3).length,
    maintenance: roomList.value.filter(room => room.status === 4).length,
    offShelf: roomList.value.filter(room => room.status === 5).length
  }
})

onMounted(() => {
  fetchRoomStatusList()
})

// 获取房态列表
const fetchRoomStatusList = async () => {
  loading.value = true
  try {
    const params = {
      floor: searchForm.floor,
      status: searchForm.status,
      roomTypeId: searchForm.roomTypeId,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const res = await getRoomStatusList(params)
    
    if (res.code === 200) {
      roomList.value = res.data.list || res.data
      pagination.total = res.data.total || res.data.length
    } else {
      ElMessage.error(res.message || '获取房态列表失败')
    }
  } catch (error) {
    console.error('获取房态列表错误:', error)
    ElMessage.error('获取房态列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchRoomStatusList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.floor = ''
  searchForm.status = ''
  searchForm.roomTypeId = ''
  pagination.currentPage = 1
  fetchRoomStatusList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchRoomStatusList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchRoomStatusList()
}

// 查看房间详情
const viewRoomDetail = async (roomId) => {
  detailLoading.value = true
  roomDetailDialogVisible.value = true
  
  try {
    const res = await getRoomDetailStatus(roomId)
    
    if (res.code === 200) {
      currentRoom.value = {
        ...res.data,
        statusText: getStatusText(res.data.status)
      }
    } else {
      ElMessage.error(res.message || '获取房间详情失败')
    }
  } catch (error) {
    console.error('获取房间详情错误:', error)
    ElMessage.error('获取房间详情失败，请稍后再试')
  } finally {
    detailLoading.value = false
  }
}

// 更新房间状态
const changeRoomStatus = async (roomId, newStatus) => {
  try {
    const res = await updateRoomStatus({
      id: roomId,
      status: newStatus
    })
    
    if (res.code === 200) {
      ElMessage.success('更新房间状态成功')
      
      // 更新本地列表中的状态
      const roomIndex = roomList.value.findIndex(room => room.id === roomId)
      if (roomIndex !== -1) {
        roomList.value[roomIndex].status = newStatus
      }
      
      // 如果当前正在查看该房间的详情，同时更新详情中的状态
      if (currentRoom.value && currentRoom.value.id === roomId) {
        currentRoom.value.status = newStatus
        currentRoom.value.statusText = getStatusText(newStatus)
      }
    } else {
      ElMessage.error(res.message || '更新房间状态失败')
    }
  } catch (error) {
    console.error('更新房间状态错误:', error)
    ElMessage.error('更新房间状态失败，请稍后再试')
  }
}

// 办理入住
const handleCheckIn = (roomId) => {
  router.push(`/staff/checkin?roomId=${roomId}`)
}

// 办理退房
const handleCheckOut = (roomNumber) => {
  router.push(`/staff/checkout?roomNumber=${roomNumber}`)
}
</script>

<template>
  <div class="room-manage-container">
    <div class="room-manage-content">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><House /></el-icon>
              <span>房态管理</span>
            </div>
          </div>
        </template>
        
        <!-- 统计信息 -->
        <div class="statistics-section">
          <div class="stat-card" style="background-color: #ecf5ff; color: #409EFF; border-color: #d9ecff;">
            <div class="stat-icon"><el-icon><HomeFilled /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">总房间数</div>
            </div>
          </div>
          <div class="stat-card" style="background-color: #f0f9eb; color: #67C23A; border-color: #e1f3d8;">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.vacant }}</div>
              <div class="stat-label">空闲房间</div>
            </div>
          </div>
          <div class="stat-card" style="background-color: #fef0f0; color: #F56C6C; border-color: #fde2e2;">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.occupied }}</div>
              <div class="stat-label">已入住</div>
            </div>
          </div>
          <div class="stat-card" style="background-color: #fdf6ec; color: #E6A23C; border-color: #faecd8;">
            <div class="stat-icon"><el-icon><Brush /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.cleaning }}</div>
              <div class="stat-label">清洁中</div>
            </div>
          </div>
          <div class="stat-card" style="background-color: #f4f4f5; color: #909399; border-color: #e9e9eb;">
            <div class="stat-icon"><el-icon><Tools /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.maintenance }}</div>
              <div class="stat-label">维修中</div>
            </div>
          </div>
          <div class="stat-card" style="background-color: #f4f4f5; color: #909399; border-color: #e9e9eb;">
            <div class="stat-icon"><el-icon><Delete /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.offShelf }}</div>
              <div class="stat-label">下架房间</div>
            </div>
          </div>
        </div>
        
        <!-- 搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" label-width="80px" inline>
            <el-form-item label="楼层">
              <el-select v-model="searchForm.floor" placeholder="选择楼层" clearable style="width: 140px;">
                <el-option
                  v-for="item in floorOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="房间状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
                <el-option
                  v-for="item in roomStatusOptions.filter(item => item.value !== '')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="房型">
              <el-select v-model="searchForm.roomTypeId" placeholder="选择房型" clearable style="width: 160px;">
                <el-option
                  v-for="item in roomTypeOptions.filter(item => item.value !== '')"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 房态列表 -->
        <div v-loading="loading">
          <div class="room-grid">
            <div 
              v-for="room in roomList" 
              :key="room.id" 
              class="room-card"
              :class="{
                'room-vacant': room.status === 1,
                'room-occupied': room.status === 2,
                'room-cleaning': room.status === 3,
                'room-maintenance': room.status === 4,
                'room-offline': room.status === 5
              }"
            >
              <div class="room-header">
                <span class="room-number">{{ room.roomNumber }}</span>
                <span 
                  class="room-status" 
                  :style="{ backgroundColor: getStatusColor(room.status) }"
                >
                  {{ getStatusText(room.status) }}
                </span>
              </div>
              <div class="room-info">
                <div class="room-info-item">
                  <span class="label">房型:</span>
                  <span class="value">{{ room.roomTypeName }}</span>
                </div>
                <div class="room-info-item">
                  <span class="label">楼层:</span>
                  <span class="value">{{ room.floor }}楼</span>
                </div>
              </div>
              <div class="room-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="viewRoomDetail(room.id)"
                >
                  详情
                </el-button>
                
                <el-dropdown @command="(command) => changeRoomStatus(room.id, command)">
                  <el-button
                    type="warning"
                    size="small"
                  >
                    更改状态
                    <el-icon class="el-icon--right">
                      <arrow-down />
                    </el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        v-if="room.status !== 1" 
                        :command="1"
                      >
                        <span style="color: #67C23A;">空闲</span>
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="room.status !== 3" 
                        :command="3"
                      >
                        <span style="color: #E6A23C;">清洁中</span>
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="room.status !== 4" 
                        :command="4"
                      >
                        <span style="color: #F56C6C;">维修中</span>
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="room.status !== 5" 
                        :command="5"
                      >
                        <span style="color: #909399;">下架</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                
                <el-button
                  v-if="room.status === 1"
                  type="success"
                  size="small"
                  @click="handleCheckIn(room.id)"
                >
                  办理入住
                </el-button>
                
                <el-button
                  v-if="room.status === 2"
                  type="danger"
                  size="small"
                  @click="handleCheckOut(room.roomNumber)"
                >
                  办理退房
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 房间详情对话框 -->
    <el-dialog
      v-model="roomDetailDialogVisible"
      title="房间详情"
      width="650px"
      v-loading="detailLoading"
    >
      <div v-if="currentRoom" class="room-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">房间号:</span>
              <span class="value">{{ currentRoom.roomNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">房型:</span>
              <span class="value">{{ currentRoom.roomTypeName }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">楼层:</span>
              <span class="value">{{ currentRoom.floor }}楼</span>
            </div>
            <div class="detail-item">
              <span class="label">状态:</span>
              <span class="value">
                <el-tag :color="getStatusColor(currentRoom.status)" effect="dark">
                  {{ currentRoom.statusText }}
                </el-tag>
              </span>
            </div>
          </div>
        </div>
        
        <div v-if="currentRoom.status === 2" class="detail-section">
          <h3>入住信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">客人姓名:</span>
              <span class="value">{{ currentRoom.guestName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话:</span>
              <span class="value">{{ currentRoom.guestPhone }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">身份证号:</span>
              <span class="value">{{ currentRoom.guestIdCard }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">入住时间:</span>
              <span class="value">{{ currentRoom.checkInTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预计离店:</span>
              <span class="value">{{ currentRoom.checkOutDate }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-actions">
          <el-button type="primary" @click="changeRoomStatus(currentRoom.id, 1)" v-if="currentRoom.status !== 1">
            设为空闲
          </el-button>
          <el-button type="warning" @click="changeRoomStatus(currentRoom.id, 3)" v-if="currentRoom.status !== 3">
            设为清洁中
          </el-button>
          <el-button type="danger" @click="changeRoomStatus(currentRoom.id, 4)" v-if="currentRoom.status !== 4">
            设为维修中
          </el-button>
          <el-button type="info" @click="changeRoomStatus(currentRoom.id, 5)" v-if="currentRoom.status !== 5">
            设为下架
          </el-button>
          
          <el-button type="success" @click="handleCheckIn(currentRoom.id)" v-if="currentRoom.status === 1">
            办理入住
          </el-button>
          <el-button type="danger" @click="handleCheckOut(currentRoom.roomNumber)" v-if="currentRoom.status === 2">
            办理退房
          </el-button>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roomDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.room-manage-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.room-manage-content {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.header-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.statistics-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  display: flex;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  border-left: 4px solid;
}

.stat-icon {
  font-size: 40px;
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
}

.search-area {
  margin: 15px 0;
  padding: 15px;
  background-color: #f8f9fb;
  border-radius: 4px;
}

.search-area .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.search-area .el-select {
  width: 100%;
}

.search-area .el-button {
  margin-right: 10px;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.room-card {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0;
  overflow: hidden;
  transition: all 0.3s;
  border-top: 3px solid #909399;
}

.room-vacant {
  border-top-color: #67C23A;
}

.room-occupied {
  border-top-color: #409EFF;
}

.room-cleaning {
  border-top-color: #E6A23C;
}

.room-maintenance {
  border-top-color: #F56C6C;
}

.room-offline {
  border-top-color: #909399;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
}

.room-number {
  font-size: 18px;
  font-weight: bold;
}

.room-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
}

.room-info {
  padding: 10px 15px;
}

.room-info-item {
  display: flex;
  margin-bottom: 5px;
}

.room-info-item .label {
  color: #606266;
  margin-right: 5px;
  min-width: 50px;
}

.room-info-item .value {
  color: #303133;
  flex: 1;
}

.room-actions {
  padding: 10px 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.room-detail .detail-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.room-detail .detail-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #409EFF;
}

.room-detail .detail-row {
  display: flex;
  margin-bottom: 10px;
}

.room-detail .detail-item {
  flex: 1;
  min-width: 0;
}

.room-detail .label {
  color: #606266;
  margin-right: 8px;
}

.room-detail .value {
  font-weight: 500;
  color: #303133;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .room-detail .detail-row {
    flex-direction: column;
  }
  
  .room-detail .detail-item {
    margin-bottom: 8px;
  }
  
  .search-area .el-form-item {
    margin-right: 0;
  }
  
  .room-grid {
    grid-template-columns: repeat(auto-fill, minmax(100%, 1fr));
  }
  
  .statistics-section {
    flex-direction: column;
  }
  
  .stat-card {
    width: 100%;
  }
}
</style> 