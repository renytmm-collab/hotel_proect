<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckOutList, getCheckOutDetail } from '@/api/reception'

const router = useRouter()
const loading = ref(true)
const detailLoading = ref(false)
const checkOutDialogVisible = ref(false)
const currentCheckOut = ref(null)

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  guestName: '',
  phone: '',
  startDate: '',
  endDate: ''
})

// 退房记录列表
const checkOutList = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  fetchCheckOutList()
})

// 获取退房记录列表
const fetchCheckOutList = async () => {
  loading.value = true
  try {
    const params = {
      roomNumber: searchForm.roomNumber,
      guestName: searchForm.guestName,
      phone: searchForm.phone,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const res = await getCheckOutList(params)
    console.log('获取退房记录响应:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理返回的数据格式，支持list和records字段
      const recordsList = res.data.list || res.data.records || [];
      
      if (Array.isArray(recordsList)) {
        // 处理日期和金额格式，防止toFixed错误
        checkOutList.value = recordsList.map(item => ({
          ...item,
          // 确保金额字段有默认值
          actualAmount: item.actualAmount || 0,
          extraFee: item.extraFee || 0,
          additionalFees: item.additionalFees || item.extraFee || 0, // 兼容两种命名
          damageFee: item.damageFee || 0,
          returnDeposit: item.returnDeposit || item.depositRefund || 0, // 兼容两种命名
          actualPayAmount: item.actualPayAmount || item.totalFee || 0 // 兼容两种命名
        }));
        console.log('处理后的退房记录:', checkOutList.value)
      } else {
        console.warn('返回的退房记录列表不是数组:', res.data)
        checkOutList.value = []
      }
      
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || res.message || '获取退房记录失败')
      checkOutList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取退房记录错误:', error)
    ElMessage.error('获取退房记录失败，请稍后再试')
    checkOutList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchCheckOutList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.roomNumber = ''
  searchForm.guestName = ''
  searchForm.phone = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.currentPage = 1
  fetchCheckOutList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchCheckOutList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchCheckOutList()
}

// 获取退房详情
const viewCheckOutDetail = async (row) => {
  detailLoading.value = true
  try {
    const res = await getCheckOutDetail(row.id)
    console.log('获取退房详情响应:', res)
    
    if (res.code === 200 && res.data) {
      // 处理数据，确保所有属性都有默认值
      currentCheckOut.value = {
        ...res.data,
        // 金额字段添加默认值
        actualAmount: res.data.actualAmount || 0,
        extraFee: res.data.extraFee || 0,
        additionalFees: res.data.additionalFees || res.data.extraFee || 0,
        damageFee: res.data.damageFee || 0,
        returnDeposit: res.data.returnDeposit || res.data.depositRefund || 0,
        actualPayAmount: res.data.actualPayAmount || res.data.totalFee || 0,
        // 文本字段添加默认值
        additionalFeesReason: res.data.additionalFeesReason || res.data.remark || ''
      }
      checkOutDialogVisible.value = true
    } else {
      ElMessage.error(res.msg || res.message || '获取退房详情失败')
    }
  } catch (error) {
    console.error('获取退房详情错误:', error)
    ElMessage.error('获取退房详情失败，请稍后再试')
  } finally {
    detailLoading.value = false
  }
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/staff/order?orderId=${orderId}`)
}
</script>

<template>
  <div class="checkout-list-container">
    <div class="checkout-list-content">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><List /></el-icon>
              <span>退房记录</span>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="router.push('/staff/rooms')">
                <el-icon><Back /></el-icon>
                返回房态管理
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
            <el-form-item label="退房日期">
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
        
        <!-- 退房记录表格 -->
        <div v-loading="loading">
          <el-table
            :data="checkOutList"
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
              prop="checkOutTime"
              label="退房时间"
              width="180"
            />
            <el-table-column
              prop="actualAmount"
              label="实际消费"
              width="100"
            >
              <template #default="{ row }">
                ¥{{ (row.actualAmount || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="additionalFees"
              label="额外费用"
              width="100"
            >
              <template #default="{ row }">
                ¥{{ (row.additionalFees || row.extraFee || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="returnDeposit"
              label="退还押金"
              width="100"
            >
              <template #default="{ row }">
                ¥{{ (row.returnDeposit || row.depositRefund || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="actualPayAmount"
              label="实收金额"
              width="100"
            >
              <template #default="{ row }">
                ¥{{ (row.actualPayAmount || row.totalFee || 0).toFixed(2) }}
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
              width="120"
            >
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  @click="viewCheckOutDetail(row)"
                >
                  详情
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
    
    <!-- 退房详情对话框 -->
    <el-dialog
      v-model="checkOutDialogVisible"
      title="退房详情"
      width="650px"
      v-loading="detailLoading"
    >
      <div v-if="currentCheckOut" class="checkout-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">房间号:</span>
              <span class="value">{{ currentCheckOut.roomNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">房型:</span>
              <span class="value">{{ currentCheckOut.roomTypeName }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">入住时间:</span>
              <span class="value">{{ currentCheckOut.checkInTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">退房时间:</span>
              <span class="value">{{ currentCheckOut.checkOutTime }}</span>
            </div>
          </div>
        </div>
        
        <!-- 客人信息 -->
        <div class="detail-section">
          <h3>客人信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">姓名:</span>
              <span class="value">{{ currentCheckOut.guestName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话:</span>
              <span class="value">{{ currentCheckOut.guestPhone }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">身份证号:</span>
              <span class="value">{{ currentCheckOut.guestIdCard }}</span>
            </div>
          </div>
        </div>
        
        <!-- 费用信息 -->
        <div class="detail-section">
          <h3>费用信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">实际消费:</span>
              <span class="value">¥{{ (currentCheckOut.actualAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">额外费用:</span>
              <span class="value">¥{{ (currentCheckOut.additionalFees || currentCheckOut.extraFee || 0).toFixed(2) }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">额外费用原因:</span>
              <span class="value">{{ currentCheckOut.additionalFeesReason || currentCheckOut.remark || '无' }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">退还押金:</span>
              <span class="value">¥{{ (currentCheckOut.returnDeposit || currentCheckOut.depositRefund || 0).toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">实收金额:</span>
              <span class="value highlight">¥{{ (currentCheckOut.actualPayAmount || currentCheckOut.totalFee || 0).toFixed(2) }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ currentCheckOut.paymentMethod || '现金' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 其他信息 -->
        <div class="detail-section">
          <h3>其他信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">订单号:</span>
              <span class="value">
                <el-button 
                  size="small"
                  type="primary" 
                  @click="viewOrderDetail(currentCheckOut.orderId)"
                >
                  {{ currentCheckOut.orderNo }}
                </el-button>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">操作员:</span>
              <span class="value">{{ currentCheckOut.operatorName }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">备注:</span>
              <span class="value">{{ currentCheckOut.remark || '无' }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkOutDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.checkout-list-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.checkout-list-content {
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

.checkout-detail .detail-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.checkout-detail .detail-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #409EFF;
}

.checkout-detail .detail-row {
  display: flex;
  margin-bottom: 10px;
}

.checkout-detail .detail-item {
  flex: 1;
  min-width: 0;
}

.checkout-detail .label {
  color: #606266;
  margin-right: 8px;
}

.checkout-detail .value {
  font-weight: 500;
  color: #303133;
}

.checkout-detail .highlight {
  color: #f56c6c;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .checkout-detail .detail-row {
    flex-direction: column;
  }
  
  .checkout-detail .detail-item {
    margin-bottom: 8px;
  }
  
  .search-area .el-form-item {
    margin-right: 0;
  }
}
</style>