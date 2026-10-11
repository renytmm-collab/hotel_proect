<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'

const router = useRouter()
const loading = ref(true)
const orderStatus = ref('')
const orders = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 订单状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: '1', label: '待支付' },
  { value: '2', label: '已支付待入住' },
  { value: '3', label: '已入住' },
  { value: '4', label: '已完成' },
  { value: '0', label: '已取消' }
]

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已取消'
    case 1: return '待支付'
    case 2: return '已支付待入住'
    case 3: return '已入住'
    case 4: return '已完成'
    default: return '未知状态'
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'warning'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'success'
    default: return ''
  }
}

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (orderStatus.value !== '') {
      params.status = parseInt(orderStatus.value)
    }
    
    const res = await getOrderList(params)
    console.log('API返回的订单列表数据:', res)
    
    if (res.code === 200 && res.data && Array.isArray(res.data.records)) {
      orders.value = res.data.records.map(item => {
        console.log('处理订单项:', item)
        return {
          id: item.id,
          orderNo: item.orderNo,
          roomName: item.roomTypeName,
          roomPrice: item.roomPrice,
          checkInDate: item.checkInDate,
          checkOutDate: item.checkOutDate,
          days: item.days,
          totalAmount: item.totalAmount,
          status: parseInt(item.status), // 确保状态是数字类型
          statusText: getStatusText(parseInt(item.status)),
          statusType: getStatusType(parseInt(item.status)),
          createTime: item.createTime,
          roomImage: item.roomImage || 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
          roomTypeId: item.roomTypeId,
          hasReview: item.hasReview || false,
          canPay: parseInt(item.status) === 1, // 只有状态为1（待支付）时才显示支付按钮
          canCancel: parseInt(item.status) === 1, // 只有状态为1（待支付）时可以取消
          canReview: parseInt(item.status) === 4 && !item.hasReview // 只有已完成且未评价的订单可以评价
        }
      })
      total.value = res.data.total || 0
    } else {
      orders.value = []
      total.value = 0
      if (res.code !== 200) {
        ElMessage.error(res.msg || '获取订单列表失败')
      }
    }
  } catch (error) {
    console.error('获取订单列表错误:', error)
    orders.value = []
    total.value = 0
    ElMessage.error('获取订单列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理页面变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchOrderList()
}

// 处理状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrderList()
}

// 支付订单
const handlePayOrder = (orderId) => {
  ElMessageBox.confirm('确定要支付此订单吗？', '支付确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await payOrder({ 
        orderId: orderId,
        paymentMethod: '支付宝' // 默认使用支付宝支付
      })
      
      if (res.code === 200) {
        ElMessage.success(res.message || '支付成功')
        fetchOrderList() // 重新获取订单列表
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (error) {
      console.error('支付订单错误:', error)
      ElMessage.error('支付失败，请稍后再试')
    }
  }).catch(() => {
    ElMessage.info('已取消支付')
  })
}

// 取消订单相关变量
const cancelDialogVisible = ref(false)
const currentCancelOrderId = ref(null)
const selectedCancelReason = ref('')

// 预设的取消原因选项
const cancelReasons = [
  '行程有变，无法入住',
  '找到了更合适的酒店',
  '价格原因',
  '个人原因',
  '其他原因'
]

// 打开取消订单对话框
const handleCancelOrder = (orderId) => {
  currentCancelOrderId.value = orderId
  selectedCancelReason.value = cancelReasons[0] // 默认选择第一个原因
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancelOrder = async () => {
  if (!selectedCancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  
  try {
    const res = await cancelOrder({
      orderId: currentCancelOrderId.value,
      cancelReason: selectedCancelReason.value
    })
    
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      cancelDialogVisible.value = false
      fetchOrderList() // 重新获取订单列表
    } else {
      ElMessage.error(res.message || '取消订单失败')
    }
  } catch (error) {
    console.error('取消订单错误:', error)
    ElMessage.error('取消订单失败，请稍后再试')
  }
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/order/detail/${orderId}`)
}

// 创建新订单
const createNewOrder = () => {
  router.push('/room/list')
}

// 重置筛选条件
const resetFilters = () => {
  orderStatus.value = ''
  fetchOrderList()
}

// 去评价
const goToReview = (orderId) => {
  router.push(`/review/create/${orderId}`)
}

// 查看评价
const viewReview = (orderId) => {
  router.push(`/comment?orderId=${orderId}`)
}

// 联系客服
const contactCustomerService = () => {
  ElMessage.success('联系客服成功')
}

onMounted(() => {
  fetchOrderList()
})
</script>

<template>
  <div class="order-list-container">
    <h2 class="page-title">我的订单</h2>

    <!-- 筛选区域 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-container">
        <el-select
          v-model="orderStatus"
          placeholder="订单状态"
          clearable
          class="filter-item"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <div class="filter-buttons">
          <el-button type="primary" @click="handleStatusChange">筛选</el-button>
          <el-button type="info" @click="resetFilters">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 订单列表 -->
    <div v-loading="loading">
      <div v-if="orders.length === 0 && !loading" class="empty-result">
        <el-empty description="暂无符合条件的订单" />
      </div>
      
      <el-card
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        shadow="hover"
      >
        <div class="order-header">
          <div class="order-id">
            <span class="label">订单号：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="order-time">
            <span class="label">下单时间：</span>
            <span class="value">{{ order.createTime }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="order.statusType" effect="plain">{{ order.statusText }}</el-tag>
          </div>
        </div>
        
        <div class="order-content">
          <div class="room-info">
            <div class="room-image">
              <img :src="order.roomImage" alt="房间图片" />
            </div>
            <div class="room-details">
              <h3>{{ order.roomName }}</h3>
              <p>入住日期：{{ order.checkInDate }} 至 {{ order.checkOutDate }}</p>
            </div>
          </div>
          
          <div class="order-price">
            <div class="price-amount">
              <span class="price">¥{{ order.totalAmount }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <el-button type="primary" plain @click="viewOrderDetail(order.id)">查看详情</el-button>
          <el-button 
            type="success" 
            v-if="order.canPay" 
            @click="handlePayOrder(order.id)"
          >
            去支付
          </el-button>
          <el-button 
            type="danger" 
            plain 
            v-if="order.canCancel" 
            @click="handleCancelOrder(order.id)"
          >
            取消订单
          </el-button>
          <el-button 
            type="primary" 
            v-if="order.canReview && !order.hasReview" 
            @click="goToReview(order.id)"
          >
            去评价
          </el-button>
          <el-button 
            type="info" 
            plain 
            v-if="order.hasReview" 
            @click="viewReview(order.id)"
          >
            查看评价
          </el-button>
          <el-button
            type="warning"
            plain
            @click="contactCustomerService"
          >
            联系客服
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="cancel-dialog-content">
        <p>请选择取消原因：</p>
        <el-select v-model="selectedCancelReason" placeholder="请选择取消原因" style="width: 100%">
          <el-option
            v-for="reason in cancelReasons"
            :key="reason"
            :label="reason"
            :value="reason"
          />
        </el-select>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancelOrder">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-list-container {
  width: 100%;
  max-width: 100%;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-item {
  width: 200px;
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.empty-result {
  margin: 50px 0;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
}

.order-id, .order-time {
  color: #606266;
}

.label {
  color: #909399;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-info {
  display: flex;
  flex: 1;
}

.room-image {
  width: 120px;
  height: 80px;
  overflow: hidden;
  border-radius: 4px;
  margin-right: 15px;
}

.room-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-details h3 {
  margin-top: 0;
  margin-bottom: 5px;
}

.room-details p {
  margin: 5px 0;
  color: #606266;
}

.order-price {
  text-align: right;
  width: 150px;
}

.price {
  font-size: 1.5rem;
  color: #F56C6C;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

@media (max-width: 768px) {
  .filter-item {
    width: 100%;
  }
  
  .filter-buttons {
    width: 100%;
    margin-top: 10px;
  }
  
  .order-header {
    flex-direction: column;
    gap: 5px;
  }
  
  .order-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .order-price {
    width: 100%;
    text-align: left;
    margin-top: 10px;
  }
  
  .order-footer {
    flex-wrap: wrap;
  }
}

.cancel-dialog-content {
  padding: 10px 0;
}

.cancel-dialog-content p {
  margin-bottom: 15px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 