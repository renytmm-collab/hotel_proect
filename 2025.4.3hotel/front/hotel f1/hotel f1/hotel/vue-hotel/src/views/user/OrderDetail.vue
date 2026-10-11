<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order'
import { 
  ArrowLeft,
  House,
  User,
  Calendar,
  Money,
  Wallet,
  Close,
  ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const order = ref(null)

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
    default: ''
  }
}

// 获取订单详情
const fetchOrderDetail = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    
    if (res.code === 200 && res.data) {
      order.value = {
        ...res.data,
        statusText: getStatusText(res.data.status),
        statusType: getStatusType(res.data.status),
        canCancel: res.data.status === 1, // 只有待支付状态可以取消
        canPay: res.data.status === 1, // 只有待支付状态可以支付
        canReview: res.data.status === 4 && !res.data.hasReview // 只有已完成状态且未评价的订单可以评价
      }
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
      router.push('/order/list')
    }
  } catch (error) {
    console.error('获取订单详情错误:', error)
    ElMessage.error('获取订单详情失败，请稍后再试')
    router.push('/order/list')
  } finally {
    loading.value = false
  }
}

// 支付订单
const handlePayOrder = () => {
  ElMessageBox.confirm('确定要支付此订单吗？', '支付确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await payOrder({
        orderId: order.value.id,
        paymentMethod: '支付宝' // 默认使用支付宝支付
      })
      
      if (res.code === 200) {
        ElMessage.success(res.message || '支付成功')
        // 重新获取订单详情
        fetchOrderDetail(order.value.id)
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

// 取消订单
const handleCancelOrder = () => {
  ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入取消原因',
    inputValidator: (value) => {
      return value.trim() !== ''
    },
    inputErrorMessage: '取消原因不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await cancelOrder({
        orderId: order.value.id,
        cancelReason: value
      })
      
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        // 重新获取订单详情
        fetchOrderDetail(order.value.id)
      } else {
        ElMessage.error(res.message || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单错误:', error)
      ElMessage.error('取消订单失败，请稍后再试')
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 去评价
const goToReview = () => {
  router.push(`/review/create/${order.value.id}`)
}

// 返回订单列表
const goBack = () => {
  router.push('/order/list')
}

onMounted(() => {
  const orderId = route.params.id
  if (orderId) {
    fetchOrderDetail(orderId)
  } else {
    ElMessage.error('未指定订单ID')
    router.push('/order/list')
  }
})
</script>

<template>
  <div class="order-detail-container">
    <div v-loading="loading" class="order-content">
      <div class="page-header">
        <el-page-header @back="goBack" :icon="ArrowLeft">
          <template #content>
            <span class="page-header-title">订单详情</span>
          </template>
        </el-page-header>
      </div>
      
      <div v-if="order" class="order-detail">
        <!-- 订单状态卡片 -->
        <el-card class="status-card">
          <div class="status-header">
            <div class="status-info">
              <el-tag :type="order.statusType" size="large" effect="light" class="status-tag">
                {{ order.statusText }}
              </el-tag>
              <div class="order-no">订单号：{{ order.orderNo }}</div>
            </div>
            <div class="action-buttons" v-if="order.canPay || order.canCancel || order.canReview">
              <el-button 
                v-if="order.canPay" 
                type="primary" 
                @click="handlePayOrder"
                :icon="Wallet"
                class="action-button"
              >
                立即支付
              </el-button>
              <el-button 
                v-if="order.canCancel" 
                type="danger" 
                @click="handleCancelOrder"
                :icon="Close"
                class="action-button"
              >
                取消订单
              </el-button>
              <el-button 
                v-if="order.canReview" 
                type="success" 
                @click="goToReview"
                :icon="ChatDotRound"
                class="action-button"
              >
                评价订单
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 房型信息卡片 -->
        <el-card class="info-card room-info-card">
          <template #header>
            <div class="card-header">
              <el-icon><House /></el-icon>
              <span>房型信息</span>
            </div>
          </template>
          <div class="room-info">
            <div class="room-image" v-if="order.roomImage">
              <img :src="order.roomImage" :alt="order.roomTypeName">
            </div>
            <div class="room-details">
              <h3 class="room-name">{{ order.roomTypeName }}</h3>
              <div class="room-specs">
                <div class="spec-item">
                  <el-icon><Money /></el-icon>
                  <span>¥{{ order.roomPrice }}/晚</span>
                </div>
                <div class="spec-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ order.checkInDate }} 至 {{ order.checkOutDate }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 入住人信息卡片 -->
        <el-card class="info-card guest-info-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>入住人信息</span>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">入住人</span>
              <span class="value">{{ order.guestName || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话</span>
              <span class="value">{{ order.guestPhone || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="label">身份证号</span>
              <span class="value">{{ order.guestIdCard || '未提供' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 支付信息卡片 -->
        <el-card class="info-card payment-info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Wallet /></el-icon>
              <span>支付信息</span>
            </div>
          </template>
          <div class="payment-info">
            <div class="price-details">
              <div class="price-item">
                <span class="label">房费总价</span>
                <span class="value">¥{{ order.totalAmount }}</span>
              </div>
              <div class="price-item" v-if="order.paymentMethod">
                <span class="label">支付方式</span>
                <span class="value">{{ order.paymentMethod }}</span>
              </div>
              <div class="price-item" v-if="order.paymentTime">
                <span class="label">支付时间</span>
                <span class="value">{{ order.paymentTime }}</span>
              </div>
            </div>
            <div class="total-price">
              <span class="label">应付金额</span>
              <span class="value price-highlight">¥{{ order.totalAmount }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
}

.page-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.status-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-tag {
  font-size: 14px;
  padding: 8px 16px;
}

.order-no {
  color: #606266;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.room-info {
  display: flex;
  gap: 20px;
}

.room-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
}

.room-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-details {
  flex: 1;
}

.room-name {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #303133;
}

.room-specs {
  display: flex;
  gap: 24px;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-size: 14px;
}

.payment-info {
  padding: 16px;
}

.price-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  color: #606266;
}

.total-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
  font-size: 16px;
}

.price-highlight {
  color: #ff6600;
  font-size: 24px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 10px;
  }

  .status-header {
    flex-direction: column;
    gap: 16px;
  }

  .room-info {
    flex-direction: column;
  }

  .room-image {
    width: 100%;
    height: 200px;
  }

  .action-buttons {
    width: 100%;
    flex-wrap: wrap;
  }

  .action-button {
    flex: 1;
  }
}
</style> 