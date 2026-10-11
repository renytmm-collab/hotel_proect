<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckInList, getCheckInDetail } from '@/api/reception'

const router = useRouter()
const loading = ref(true)
const detailLoading = ref(false)
const checkInDialogVisible = ref(false)
const currentCheckIn = ref(null)

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  guestName: '',
  phone: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 入住记录列表
const checkInList = ref([])

// 入住状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 0, label: '在住' },
  { value: 1, label: '已退房' }
]

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  fetchCheckInList()
})

// 获取入住记录列表
const fetchCheckInList = async () => {
  loading.value = true
  try {
    const params = {
      roomNumber: searchForm.roomNumber,
      guestName: searchForm.guestName,
      phone: searchForm.phone,
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const res = await getCheckInList(params)
    console.log('获取入住记录响应:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理返回的数据格式，支持list和records字段
      const recordsList = res.data.list || res.data.records || [];
      
      if (Array.isArray(recordsList)) {
        checkInList.value = recordsList.map(item => ({
          ...item,
          statusText: getStatusText(item.status)
        }))
        console.log('处理后的入住记录:', checkInList.value)
      } else {
        console.warn('返回的入住记录列表不是数组:', res.data)
        checkInList.value = []
      }
      
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || res.message || '获取入住记录失败')
      checkInList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取入住记录错误:', error)
    ElMessage.error('获取入住记录失败，请稍后再试')
    checkInList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchCheckInList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.roomNumber = ''
  searchForm.guestName = ''
  searchForm.phone = ''
  searchForm.status = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.currentPage = 1
  fetchCheckInList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchCheckInList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchCheckInList()
}

// 获取入住详情
const viewCheckInDetail = async (row) => {
  detailLoading.value = true
  try {
    const res = await getCheckInDetail(row.id)
    
    if (res.code === 200) {
      currentCheckIn.value = {
        ...res.data,
        statusText: getStatusText(res.data.status)
      }
      checkInDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取入住详情失败')
    }
  } catch (error) {
    console.error('获取入住详情错误:', error)
    ElMessage.error('获取入住详情失败，请稍后再试')
  } finally {
    detailLoading.value = false
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '在住'
    case 1: return '已退房'
    case 2: return '已取消'
    default: return '未知状态'
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'primary'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

// 办理退房
const handleCheckOut = (row) => {
  router.push(`/staff/checkout?roomNumber=${row.roomNumber}`)
}
</script>

<template>
  <div class="checkin-list-container">
    <div class="checkin-list-content">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><Tickets /></el-icon>
              <span>入住记录</span>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="router.push('/staff/order')">
                <el-icon><ArrowLeft /></el-icon>
                返回订单管理
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" label-width="80px" inline>
            <el-form-item label="房间号">
              <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" clearable />
            </el-form-item>
            <el-form-item label="客人姓名">
              <el-input v-model="searchForm.guestName" placeholder="请输入客人姓名" clearable />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="searchForm.phone" placeholder="请输入联系电话" clearable />
            </el-form-item>
            <el-form-item label="入住状态">
              <el-select 
                v-model="searchForm.status" 
                placeholder="请选择状态" 
                clearable 
                style="width: 160px;"
                popper-class="status-select-dropdown"
                placement="bottom-start"
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="入住日期">
              <el-date-picker
                v-model="searchForm.startDate"
                type="date"
                placeholder="开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-form-item>
            <el-form-item label="至">
              <el-date-picker
                v-model="searchForm.endDate"
                type="date"
                placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 入住记录表格 -->
        <div v-loading="loading">
          <el-table
            :data="checkInList"
            style="width: 100%"
            border
            stripe
          >
            <el-table-column
              prop="roomNumber"
              label="房间号"
              width="100"
            />
            <el-table-column
              prop="roomTypeName"
              label="房型"
              width="150"
            />
            <el-table-column
              prop="guestName"
              label="客人姓名"
              width="120"
            />
            <el-table-column
              prop="guestPhone"
              label="联系电话"
              width="140"
            />
            <el-table-column
              prop="checkInTime"
              label="入住时间"
              width="180"
            />
            <el-table-column
              prop="checkOutDate"
              label="预计离店"
              width="120"
            />
            <el-table-column
              prop="deposit"
              label="押金"
              width="100"
            >
              <template #default="{ row }">
                ¥{{ row.deposit.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="status"
              label="状态"
              width="100"
            >
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="operatorName"
              label="操作员"
              width="120"
            />
            <el-table-column
              label="操作"
              fixed="right"
              width="180"
            >
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  @click="viewCheckInDetail(row)"
                >
                  详情
                </el-button>
                
                <el-button
                  v-if="row.status === 0"
                  size="small"
                  type="primary"
                  @click="handleCheckOut(row)"
                >
                  办理退房
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 入住详情对话框 -->
    <el-dialog
      v-model="checkInDialogVisible"
      title="入住详情"
      width="650px"
      v-loading="detailLoading"
    >
      <div v-if="currentCheckIn" class="checkin-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">房间号:</span>
              <span class="value">{{ currentCheckIn.roomNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">房型:</span>
              <span class="value">{{ currentCheckIn.roomTypeName }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">状态:</span>
              <span class="value">
                <el-tag :type="getStatusTagType(currentCheckIn.status)">
                  {{ currentCheckIn.statusText }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">押金:</span>
              <span class="value">¥{{ currentCheckIn.deposit?.toFixed(2) }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">入住时间:</span>
              <span class="value">{{ currentCheckIn.checkInTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预计离店:</span>
              <span class="value">{{ currentCheckIn.checkOutDate }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>客人信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">姓名:</span>
              <span class="value">{{ currentCheckIn.guestName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话:</span>
              <span class="value">{{ currentCheckIn.guestPhone }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">身份证号:</span>
              <span class="value">{{ currentCheckIn.guestIdCard }}</span>
            </div>
            <div class="detail-item">
              <span class="label">会员:</span>
              <span class="value">{{ currentCheckIn.username || '非会员' }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>订单信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">订单号:</span>
              <span class="value">{{ currentCheckIn.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">操作员:</span>
              <span class="value">{{ currentCheckIn.operatorName }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">备注:</span>
              <span class="value">{{ currentCheckIn.remark || '无' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">登记时间:</span>
              <span class="value">{{ currentCheckIn.createTime }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkInDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            v-if="currentCheckIn && currentCheckIn.status === 0"
            @click="handleCheckOut(currentCheckIn)"
          >
            办理退房
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style>
/* 全局样式，不使用scoped */
:deep(.status-select-dropdown) {
  min-width: 160px !important;
  margin-top: 5px !important;
}
</style>

<style scoped>
.checkin-list-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.checkin-list-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 20px 40px;
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

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.checkin-detail .detail-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.checkin-detail .detail-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #409EFF;
}

.checkin-detail .detail-row {
  display: flex;
  margin-bottom: 10px;
}

.checkin-detail .detail-item {
  flex: 1;
  min-width: 0;
}

.checkin-detail .label {
  color: #606266;
  margin-right: 8px;
}

.checkin-detail .value {
  font-weight: 500;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .checkin-detail .detail-row {
    flex-direction: column;
  }
  
  .checkin-detail .detail-item {
    margin-bottom: 8px;
  }
  
  .search-area .el-form-item {
    margin-right: 0;
  }
}
</style> 