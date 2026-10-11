<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, checkIn, getRoomStatusList } from '@/api/reception'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const roomsLoading = ref(false)

// 从路由获取订单ID
const orderId = route.query.orderId

// 入住表单
const checkInForm = reactive({
  orderId: 0,
  roomId: 0,
  roomNumber: '',
  guestName: '',
  guestPhone: '',
  guestIdCard: '',
  deposit: 500,
  remark: ''
})

// 订单信息
const orderInfo = ref({
  orderNo: '',
  roomTypeId: 0,
  roomTypeName: '',
  checkInDate: '',
  checkOutDate: '',
  days: 1,
  roomPrice: 0,
  totalAmount: 0
})

// 可选房间列表
const availableRooms = ref([])

// 入住规则
const rules = {
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '押金金额必须大于0', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

onMounted(() => {
  // 如果是从订单跳转过来的，加载订单数据
  if (orderId) {
    fetchOrderDetail(orderId)
  }
})

// 获取订单详情
const fetchOrderDetail = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    
    if (res.code === 200) {
      const orderData = res.data
      
      // 设置订单信息
      orderInfo.value = {
        orderNo: orderData.orderNo,
        roomTypeId: orderData.roomTypeId,
        roomTypeName: orderData.roomTypeName,
        checkInDate: orderData.checkInDate,
        checkOutDate: orderData.checkOutDate,
        days: orderData.days,
        roomPrice: orderData.roomPrice,
        totalAmount: orderData.totalAmount
      }
      
      // 填充入住表单数据
      checkInForm.orderId = orderData.id
      checkInForm.guestName = orderData.guestName
      checkInForm.guestPhone = orderData.guestPhone
      checkInForm.guestIdCard = orderData.guestIdCard
      
      // 计算押金为订单总额的五分之一（20%）
      checkInForm.deposit = Math.ceil(orderData.totalAmount / 5)
      
      // 加载可用房间
      loadAvailableRooms(orderData.roomTypeId)
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
      router.push('/staff/order')
    }
  } catch (error) {
    console.error('获取订单详情错误:', error)
    ElMessage.error('获取订单详情失败，请稍后再试')
    router.push('/staff/order')
  } finally {
    loading.value = false
  }
}

// 加载可用房间
const loadAvailableRooms = async (roomTypeId) => {
  roomsLoading.value = true
  try {
    const params = {
      roomTypeId: roomTypeId,
      status: 1  // 假设1是可用状态
    }
    
    const res = await getRoomStatusList(params)
    console.log('获取可用房间响应:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理返回的数据格式，支持list和records字段
      const roomsList = res.data.list || res.data.records || [];
      
      if (Array.isArray(roomsList)) {
        // 过滤可用的房间
        availableRooms.value = roomsList.filter(room => 
          room.status === 1 // 过滤空闲状态的房间
        )
        console.log('过滤后的可用房间:', availableRooms.value)
      } else {
        console.warn('返回的房间列表不是数组:', res.data)
        availableRooms.value = []
      }
    } else {
      ElMessage.warning(res.msg || res.message || '获取可用房间失败')
      availableRooms.value = []
    }
  } catch (error) {
    console.error('获取可用房间错误:', error)
    ElMessage.warning('获取可用房间失败，请稍后再试')
    availableRooms.value = []
  } finally {
    roomsLoading.value = false
  }
}

// 根据房间号获取房间ID
const getRoomIdByNumber = (roomNumber) => {
  const room = availableRooms.value.find(r => r.roomNumber === roomNumber)
  return room ? room.id : null
}

// 当选择房间时
const handleRoomChange = () => {
  const roomId = getRoomIdByNumber(checkInForm.roomNumber)
  if (roomId) {
    checkInForm.roomId = roomId
  }
}

// 提交入住登记
const submitCheckIn = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        // 确保填充了roomId
        const roomId = getRoomIdByNumber(checkInForm.roomNumber)
        if (!roomId) {
          ElMessage.error('无效的房间选择')
          submitting.value = false
          return
        }
        
        checkInForm.roomId = roomId
        
        const res = await checkIn(checkInForm)
        
        if (res.code === 200) {
          ElMessage.success('入住登记成功！房间号：' + res.data.roomNumber)
          
          // 跳转到订单列表页
          setTimeout(() => {
            router.push('/staff/checkin/list')
          }, 1500)
        } else {
          ElMessage.error(res.message || '入住登记失败')
        }
      } catch (error) {
        console.error('入住登记错误:', error)
        ElMessage.error('入住登记失败，请稍后再试')
      } finally {
        submitting.value = false
      }
    } else {
      return false
    }
  })
}

// 返回订单列表
const goBack = () => {
  router.push('/staff/order')
}
</script>

<template>
  <div class="checkin-container">
    <div class="checkin-content">
      <el-card shadow="hover" v-loading="loading">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><DocumentAdd /></el-icon>
              <span>入住登记</span>
            </div>
            <div>
              <el-button @click="goBack">
                <el-icon><Back /></el-icon>
                返回订单列表
              </el-button>
            </div>
          </div>
        </template>
        
        <el-form 
          ref="formRef"
          :model="checkInForm"
          :rules="rules"
          label-position="top"
          class="checkin-form"
        >
          <!-- 订单信息 -->
          <div class="form-section">
            <h3 class="section-title">预订信息</h3>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="订单号">
                  <el-input v-model="orderInfo.orderNo" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="房型">
                  <el-input v-model="orderInfo.roomTypeName" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="房间号" prop="roomNumber">
                  <el-select 
                    v-model="checkInForm.roomNumber" 
                    placeholder="请选择房间号"
                    filterable
                    class="full-width"
                    @change="handleRoomChange"
                    :loading="roomsLoading"
                  >
                    <template #empty>
                      <div v-if="roomsLoading">加载可用房间中...</div>
                      <div v-else>没有可用房间</div>
                    </template>
                    <el-option
                      v-for="room in availableRooms"
                      :key="room.id"
                      :label="room.roomNumber"
                      :value="room.roomNumber"
                    >
                      <div class="room-option">
                        <span>{{ room.roomNumber }}</span>
                        <span class="room-price">¥{{ room.price || orderInfo.roomPrice }}/晚</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="入住日期">
                  <el-input v-model="orderInfo.checkInDate" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="离店日期">
                  <el-input v-model="orderInfo.checkOutDate" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="住宿天数">
                  <el-input v-model="orderInfo.days" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="房间单价">
                  <el-input :value="`¥${orderInfo.roomPrice}`" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="订单总额">
                  <el-input :value="`¥${orderInfo.totalAmount}`" disabled />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <!-- 客人信息 -->
          <div class="form-section">
            <h3 class="section-title">入住人信息</h3>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="姓名" prop="guestName">
                  <el-input v-model="checkInForm.guestName" placeholder="请输入入住人姓名" disabled/>
                  <div class="field-tip">入住人信息已从订单获取</div>
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="身份证号" prop="guestIdCard">
                  <el-input v-model="checkInForm.guestIdCard" placeholder="请输入身份证号" disabled/>
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="联系电话" prop="guestPhone">
                  <el-input v-model="checkInForm.guestPhone" placeholder="请输入联系电话" disabled/>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <!-- 其他信息 -->
          <div class="form-section">
            <h3 class="section-title">其他信息</h3>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="押金" prop="deposit">
                  <el-input-number 
                    v-model="checkInForm.deposit" 
                    :min="0" 
                    :precision="2" 
                    :step="100"
                    class="full-width"
                  />
                  <div class="field-tip">默认押金为订单总额的五分之一</div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="checkInForm.remark" 
                type="textarea" 
                :rows="3"
                placeholder="请输入备注信息（可选）"
              />
            </el-form-item>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button 
              type="primary" 
              :loading="submitting"
              @click="submitCheckIn"
            >
              确认办理入住
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.checkin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.checkin-content {
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

.full-width {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}

.room-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.room-price {
  color: #f56c6c;
  font-size: 13px;
}

.guest-info {
  background-color: #f8f8f8;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.guest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.guest-title {
  font-weight: bold;
}

.field-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  font-style: italic;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style> 