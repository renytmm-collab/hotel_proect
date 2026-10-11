<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Promotion, Back, DocumentRemove, Plus, Refresh, Edit, Delete, CircleClose } from '@element-plus/icons-vue'
import { getCheckInList, getCheckInDetail, handleCheckOut } from '@/api/reception'

const router = useRouter()
const route = useRoute()

// 从路由获取房间号
const roomNumber = ref(route.query.roomNumber || '')

// 控制状态
const pageLoading = ref(false)
const tableLoading = ref(false)
const formLoading = ref(false)
const submitting = ref(false)

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  guestName: '',
  status: 0  // 默认查询在住的房间
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 在住房间列表
const occupiedRooms = ref([])

// 退房表单
const checkoutForm = reactive({
  checkInId: 0,
  roomNumber: '',
  roomTypeName: '',
  guestName: '',
  checkInTime: '',
  checkOutDate: '',
  days: 0,
  deposit: 0,
  orderAmount: 0,  // 订单金额
  actualAmount: 0,  // 损耗金额
  additionalFees: 0,  // 额外费用
  additionalReason: '',  // 额外费用原因
  remark: '',
  paymentMethod: '支付宝' // 支付方式
})

// 支付方式选项
const paymentOptions = [
  { value: '现金', label: '现金' },
  { value: '支付宝', label: '支付宝' },
  { value: '微信', label: '微信支付' },
  { value: '银行卡', label: '银行卡' },
  { value: '信用卡', label: '信用卡' }
]

// 表单验证规则
const rules = {
  actualAmount: [
    { required: true, message: '请输入损耗金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '金额不能小于0', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 计算最终应收金额
const finalAmount = computed(() => {
  // 额外费用 + 损耗金额 - 押金
  const total = (checkoutForm.additionalFees || 0) + (checkoutForm.actualAmount || 0) - (checkoutForm.deposit || 0)
  return Number(total.toFixed(2))
})

// 判断最终金额是否为正数（需要客户支付）
const isPositiveFinalAmount = computed(() => {
  return finalAmount.value >= 0
})

// 格式化最终金额显示
const finalAmountDisplay = computed(() => {
  if (isPositiveFinalAmount.value) {
    return `¥${finalAmount.value.toFixed(2)}`
  } else {
    return `¥${Math.abs(finalAmount.value).toFixed(2)}`
  }
})

// 计算需要退还的押金金额
const depositRefundAmount = computed(() => {
  if (isPositiveFinalAmount.value) {
    // 客户需要支付，押金全部抵扣
    return 0
  } else {
    // 需要退还押金
    return Math.abs(finalAmount.value)
  }
})

// 初始化
onMounted(() => {
  pageLoading.value = true
  // 如果有房间号，则直接查询房间信息并进入退房表单
  if (roomNumber.value) {
    searchRoomByNumber(roomNumber.value)
  } else {
    // 否则加载在住房间列表
    loadOccupiedRooms()
  }
  pageLoading.value = false
})

// 通过房间号搜索入住记录
const searchRoomByNumber = async (roomNo) => {
  formLoading.value = true
  try {
    // 先使用房间号查询在住记录
    const params = {
      roomNumber: roomNo,
      status: 0, // 在住状态
      pageNum: 1,
      pageSize: 10
    }
    
    const res = await getCheckInList(params)
    
    if (res.code === 200 && res.data) {
      const records = res.data.list || res.data.records || []
      if (records.length > 0) {
        // 找到记录，获取详情
        const checkInRecord = records[0]
        loadRoomDetail(checkInRecord.id)
      } else {
        ElMessage.warning(`未找到房间号为 ${roomNo} 的在住记录`)
        resetRoomSelection()
      }
    } else {
      ElMessage.error(res.msg || res.message || '搜索房间记录失败')
      resetRoomSelection()
    }
  } catch (error) {
    console.error('搜索房间记录错误:', error)
    ElMessage.error('搜索房间记录失败，请稍后再试')
    resetRoomSelection()
  } finally {
    formLoading.value = false
  }
}

// 加载在住房间列表
const loadOccupiedRooms = async () => {
  tableLoading.value = true
  try {
    const params = {
      roomNumber: searchForm.roomNumber,
      guestName: searchForm.guestName,
      status: 0, // 在住状态
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const res = await getCheckInList(params)
    console.log('获取在住房间响应:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理返回的数据格式，支持list和records字段
      const roomsList = res.data.list || res.data.records || [];
      
      if (Array.isArray(roomsList)) {
        occupiedRooms.value = roomsList
        console.log('处理后的在住房间列表:', occupiedRooms.value)
      } else {
        console.warn('返回的房间列表不是数组:', res.data)
        occupiedRooms.value = []
      }
      
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || res.message || '获取在住房间失败')
      occupiedRooms.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取在住房间错误:', error)
    ElMessage.error('获取在住房间失败，请稍后再试')
    occupiedRooms.value = []
    pagination.total = 0
  } finally {
    tableLoading.value = false
  }
}

// 搜索在住房间
const handleSearch = () => {
  pagination.currentPage = 1
  loadOccupiedRooms()
}

// 重置搜索
const resetSearch = () => {
  searchForm.roomNumber = ''
  searchForm.guestName = ''
  searchForm.status = 0
  pagination.currentPage = 1
  loadOccupiedRooms()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadOccupiedRooms()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadOccupiedRooms()
}

// 选择房间进行退房
const selectRoom = (room) => {
  roomNumber.value = room.roomNumber
  loadRoomDetail(room.id) // 使用id查询详情，而不是roomNumber
}

// 加载房间详细信息
const loadRoomDetail = async (id) => {
  formLoading.value = true
  try {
    const res = await getCheckInDetail(id)
    
    if (res.code === 200) {
      const data = res.data
      // 填充退房表单
      checkoutForm.checkInId = data.id
      checkoutForm.roomNumber = data.roomNumber
      checkoutForm.roomTypeName = data.roomTypeName
      checkoutForm.guestName = data.guestName
      checkoutForm.checkInTime = data.checkInTime
      checkoutForm.checkOutDate = data.checkOutDate
      checkoutForm.days = data.days
      checkoutForm.deposit = data.deposit
      checkoutForm.orderAmount = data.orderAmount
      checkoutForm.actualAmount = 0  // 默认损耗金额为0
      checkoutForm.additionalFees = 0
      checkoutForm.additionalReason = ''
      checkoutForm.returnDeposit = data.deposit  // 默认退还全部押金
      checkoutForm.remark = ''
      checkoutForm.paymentMethod = '支付宝'  // 默认支付方式为支付宝
    } else {
      ElMessage.error(res.message || '获取入住详情失败')
      resetRoomSelection()
    }
  } catch (error) {
    console.error('获取入住详情错误:', error)
    ElMessage.error('获取入住详情失败，请稍后再试')
    resetRoomSelection()
  } finally {
    formLoading.value = false
  }
}

// 重置房间选择
const resetRoomSelection = () => {
  roomNumber.value = ''
  router.replace('/staff/checkout')
}

// 返回房间列表
const backToRoomList = () => {
  resetRoomSelection()
}

// 提交退房
const submitCheckout = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      let messageText = `确认为房间 ${checkoutForm.roomNumber} 办理退房吗？\n`
      
      if (isPositiveFinalAmount.value) {
        messageText += `应收金额: ${finalAmountDisplay.value}\n`
        messageText += `支付方式: ${checkoutForm.paymentMethod || '支付宝'}`
      } else {
        messageText += `应退还押金: ¥${depositRefundAmount.value.toFixed(2)}\n`
        messageText += `退还方式: ${checkoutForm.paymentMethod || '支付宝'}`
      }
      
      ElMessageBox.confirm(
        messageText,
        '确认退房',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        submitting.value = true
        try {
          // 严格按API文档要求构建参数
          const params = {
            checkInId: checkoutForm.checkInId,
            extraFee: checkoutForm.additionalFees || 0,
            damageFee: checkoutForm.actualAmount || 0,  // 使用损耗金额
            paymentMethod: checkoutForm.paymentMethod || '支付宝',  // 使用用户选择的支付方式
            remark: checkoutForm.remark || (checkoutForm.additionalReason ? `额外费用原因: ${checkoutForm.additionalReason}` : "")
          }
          
          console.log('提交退房参数:', params)
          const res = await handleCheckOut(params)
          console.log('退房响应:', res)
          
          if (res.code === 200) {
            ElMessage.success('退房成功！')
            
            // 跳转到退房记录列表
            setTimeout(() => {
              router.push('/staff/checkout/list')
            }, 1000)
          } else {
            ElMessage.error(res.msg || res.message || '退房失败')
          }
        } catch (error) {
          console.error('退房错误:', error)
          ElMessage.error('退房失败，请稍后再试')
        } finally {
          submitting.value = false
        }
      }).catch(() => {
        // 取消操作
      })
    } else {
      return false
    }
  })
}

// 处理行点击事件
const handleRowClick = (row) => {
  selectRoom(row)
}

// 返回房态管理
const backToRoomStatus = () => {
  router.push('/staff/room-status')
}
</script>

<template>
  <div class="checkout-page staff-page">
    <div class="checkout-container" v-loading="pageLoading">
      <!-- 房间选择 -->
      <el-card v-if="!roomNumber" shadow="hover" class="page-card">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><Promotion /></el-icon>
              <span>退房结算</span>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="backToRoomStatus">
                <el-icon><Back /></el-icon>
                返回房态管理
              </el-button>
              <el-button type="success" @click="router.push('/staff/checkout/list')">
                <el-icon><DocumentRemove /></el-icon>
                查看退房记录
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item label="房间号">
              <el-input 
                v-model="searchForm.roomNumber" 
                placeholder="请输入房间号" 
                clearable 
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="客人姓名">
              <el-input 
                v-model="searchForm.guestName" 
                placeholder="请输入客人姓名" 
                clearable 
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 在住房间列表 -->
        <div v-loading="tableLoading">
          <el-table
            v-loading="tableLoading"
            :data="occupiedRooms || []"
            style="width: 100%"
            stripe
            border
            highlight-current-row
            @row-click="handleRowClick"
          >
            <el-table-column
              prop="roomNumber"
              label="房间号"
              width="100"
              align="center"
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
              align="center"
            />
            <el-table-column
              label="已住天数"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                {{ row.days || 1 }} 天
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="120"
              align="center"
            >
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  @click="selectRoom(row)"
                >
                  办理退房
                </el-button>
              </template>
            </el-table-column>
            
            <!-- 无数据提示 -->
            <template #empty>
              <div class="empty-data">
                <el-icon><CircleClose /></el-icon>
                <span>暂无在住记录</span>
              </div>
            </template>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handleCurrentChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-card>
      
      <!-- 退房表单 -->
      <el-card v-else shadow="hover" v-loading="formLoading" class="page-card">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><Promotion /></el-icon>
              <span>办理退房 - 房间{{ checkoutForm.roomNumber }}</span>
            </div>
            <div class="header-actions">
              <el-button @click="backToRoomList">
                <el-icon><Back /></el-icon>
                返回房间列表
              </el-button>
            </div>
          </div>
        </template>
        
        <el-form 
          ref="formRef"
          :model="checkoutForm"
          :rules="rules"
          label-position="top"
          class="checkout-form"
        >
          <!-- 房间信息 -->
          <div class="form-section">
            <h3 class="section-title">房间与入住信息</h3>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="房间号">
                  <el-input v-model="checkoutForm.roomNumber" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="房型">
                  <el-input v-model="checkoutForm.roomTypeName" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="客人姓名">
                  <el-input v-model="checkoutForm.guestName" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="入住时间">
                  <el-input v-model="checkoutForm.checkInTime" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="预计离店">
                  <el-input v-model="checkoutForm.checkOutDate" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="押金">
                  <el-input :value="`¥${checkoutForm.deposit?.toFixed(2)}`" disabled />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <!-- 结算信息 -->
          <div class="form-section">
            <h3 class="section-title">结算信息</h3>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="损耗金额" prop="actualAmount">
                  <el-input-number 
                    v-model="checkoutForm.actualAmount" 
                    :min="0" 
                    :precision="2" 
                    :step="100"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="额外费用" prop="additionalFees">
                  <el-input-number 
                    v-model="checkoutForm.additionalFees" 
                    :min="0" 
                    :precision="2" 
                    :step="50"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="额外费用原因" prop="additionalReason">
                  <el-input 
                    v-model="checkoutForm.additionalReason" 
                    placeholder="请说明额外费用原因（如有）"
                  />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="支付方式" prop="paymentMethod">
                  <el-select v-model="checkoutForm.paymentMethod" style="width: 100%">
                    <el-option 
                      v-for="option in paymentOptions" 
                      :key="option.value" 
                      :label="option.label" 
                      :value="option.value" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 结算金额概览 -->
            <div class="settlement-summary">
              <el-descriptions :column="1" border>
                <el-descriptions-item :label="isPositiveFinalAmount ? '应收金额' : '应退还押金'">
                  <span class="final-amount" :class="{ 'refund-amount': !isPositiveFinalAmount }">
                    {{ finalAmountDisplay }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="金额计算">
                  <div class="amount-calculation">
                    <div>押金：¥{{ (checkoutForm.deposit || 0).toFixed(2) }}</div>
                    <div>损耗金额：¥{{ (checkoutForm.actualAmount || 0).toFixed(2) }}</div>
                    <div>额外费用：¥{{ (checkoutForm.additionalFees || 0).toFixed(2) }}</div>
                    <div class="calculation-formula">
                      结算金额 = 损耗金额 + 额外费用 - 押金
                    </div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item :label="isPositiveFinalAmount ? '支付方式' : '退还方式'">
                  <span>{{ checkoutForm.paymentMethod || '支付宝' }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
            
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="checkoutForm.remark" 
                type="textarea" 
                rows="3"
                placeholder="请输入备注信息（可选）"
              />
            </el-form-item>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button @click="backToRoomList" :disabled="submitting">取消</el-button>
            <el-button 
              type="primary" 
              :loading="submitting"
              @click="submitCheckout"
            >
              确认办理退房
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.checkout-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.checkout-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-card {
  margin-bottom: 20px;
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

.header-actions {
  display: flex;
  gap: 10px;
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

.form-section {
  margin-bottom: 30px;
  border-bottom: 1px dashed #ebeef5;
  padding-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #409EFF;
  display: flex;
  align-items: center;
}

.settlement-summary {
  margin: 20px 0;
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
}

.final-amount {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.refund-amount {
  color: #67c23a !important;
}

.amount-calculation {
  font-size: 14px;
  color: #606266;
}

.calculation-formula {
  margin-top: 8px;
  font-weight: bold;
  color: #303133;
  border-top: 1px dashed #dcdfe6;
  padding-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}

@media (max-width: 768px) {
  .search-area .el-form {
    display: flex;
    flex-direction: column;
  }
  
  .search-area .el-form-item {
    margin-right: 0;
    margin-bottom: 15px;
  }
}

.empty-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.empty-data .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.empty-data span {
  display: block;
  font-size: 14px;
}
</style> 