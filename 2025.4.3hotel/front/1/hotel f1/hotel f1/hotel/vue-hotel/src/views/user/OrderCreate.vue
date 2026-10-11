<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomTypeDetail } from '@/api/roomType'
import { createOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const submitting = ref(false)
const orderFormRef = ref(null)

// 模拟房型数据
const roomsData = [
  {
    id: 1,
    name: '标准单人间',
    image: 'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 258,
    discount: 0.8,
    size: '18平方米',
    bedType: '单人床',
    capacity: 1,
    breakfast: false
  },
  {
    id: 2,
    name: '标准双床房',
    image: 'https://img1.baidu.com/it/u=1187860984,2261105129&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 328,
    discount: 0.8,
    size: '25平方米',
    bedType: '两张单人床',
    capacity: 2,
    breakfast: false
  },
  {
    id: 3,
    name: '豪华大床房',
    image: 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 398,
    discount: 0.9,
    size: '30平方米',
    bedType: '特大号床',
    capacity: 2,
    breakfast: true
  },
  {
    id: 4,
    name: '行政套房',
    image: 'https://img1.baidu.com/it/u=1979540962,1457794044&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 598,
    discount: 0.9,
    size: '45平方米',
    bedType: '特大号床',
    capacity: 2,
    breakfast: true
  },
  {
    id: 5,
    name: '家庭房',
    image: 'https://img2.baidu.com/it/u=3482414296,1599253536&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 498,
    discount: 0.8,
    size: '40平方米',
    bedType: '一张大床和一张单人床',
    capacity: 3,
    breakfast: true
  }
]

// 当前选中的房型
const selectedRoom = ref(null)

// 订单表单数据
const orderForm = reactive({
  roomTypeId: null,
  guestName: '',
  guestPhone: '',
  guestIdCard: '',
  checkInDate: '',
  checkOutDate: '',
  guestCount: 1,
  breakfast: false,
  remarks: ''
})

// 表单校验规则
const rules = {
  guestName: [
    { required: true, message: '请输入入住人姓名', trigger: 'blur' }
  ],
  guestPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入联系电话'))
        } else if (!/^1[3-9]\d{9}$/.test(value.trim())) {
          callback(new Error('请输入正确的手机号码'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur'
    }
  ],
  guestIdCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择离店日期', trigger: 'change' }
  ],
  guestCount: [
    { required: true, message: '请选择入住人数', trigger: 'change' },
    { type: 'number', min: 1, message: '入住人数至少为1人', trigger: 'change' }
  ]
}

// 获取今天的日期
const today = new Date()
const tomorrow = new Date()
tomorrow.setDate(today.getDate() + 1)
const dayAfterTomorrow = new Date()
dayAfterTomorrow.setDate(today.getDate() + 2)

// 设置默认入住和离店日期
orderForm.checkInDate = formatDate(tomorrow)
orderForm.checkOutDate = formatDate(dayAfterTomorrow)

// 日期格式化
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 计算住宿天数
const stayDays = computed(() => {
  if (!orderForm.checkInDate || !orderForm.checkOutDate) return 0
  const checkin = new Date(orderForm.checkInDate)
  const checkout = new Date(orderForm.checkOutDate)
  const diffTime = checkout.getTime() - checkin.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 0
})

// 计算基础价格
const basePrice = computed(() => {
  if (!selectedRoom.value) return 0
  return selectedRoom.value.price
})

// 计算早餐价格
const breakfastPrice = computed(() => {
  if (!selectedRoom.value || !orderForm.breakfast || (selectedRoom.value.facilities && selectedRoom.value.facilities.includes('早餐'))) return 0
  return 38 * orderForm.guestCount * stayDays.value
})

// 计算总价
const totalPrice = computed(() => {
  return basePrice.value * stayDays.value + breakfastPrice.value
})

// 检查日期是否有效
const validateDates = () => {
  if (!orderForm.checkInDate || !orderForm.checkOutDate) return false
  const checkin = new Date(orderForm.checkInDate)
  const checkout = new Date(orderForm.checkOutDate)
  
  // 检查入住日期是否早于今天
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  if (checkin < today) {
    ElMessage.error('入住日期不能早于今天')
    return false
  }
  
  // 检查离店日期是否晚于入住日期
  if (checkout <= checkin) {
    ElMessage.error('离店日期必须晚于入住日期')
    return false
  }
  
  return true
}

// 表单提交
const submitOrder = async () => {
  if (!orderFormRef.value) return
  
  if (!selectedRoom.value) {
    ElMessage.error('未找到房型信息，请返回重新选择')
    return
  }
  
  if (!validateDates()) {
    return
  }
  
  // 表单验证
  await orderFormRef.value.validate(async (valid) => {
    if (valid) {
      // 弹出确认框
      ElMessageBox.confirm(
        `您将预订${selectedRoom.value.name}，入住日期${orderForm.checkInDate}，离店日期${orderForm.checkOutDate}，共${stayDays.value}晚，总价￥${totalPrice.value}。确认预订吗？`,
        '确认预订',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        // 用户确认预订
        submitting.value = true
        try {
          // 准备提交数据
          const orderData = {
            roomTypeId: orderForm.roomTypeId,
            checkInDate: orderForm.checkInDate,
            checkOutDate: orderForm.checkOutDate,
            guestName: orderForm.guestName,
            guestPhone: orderForm.guestPhone,
            guestIdCard: orderForm.guestIdCard
          }
          
          // 调用创建订单API
          const res = await createOrder(orderData)
          
          if (res.code === 200) {
            ElMessage.success('订单创建成功')
            // 如果创建成功，跳转到订单详情页
            router.push({
              path: `/order/detail/${res.data.orderId}`,
              query: { orderId: res.data.orderId }
            })
          } else if (res.code === 400 && res.message.includes('房间不足')) {
            ElMessage.error('当前房型房间不足，请选择其他房型或日期')
          } else {
            ElMessage.error(res.message || '创建订单失败')
          }
        } catch (error) {
          console.error('创建订单错误:', error)
          ElMessage.error(error.message || '创建订单失败，请稍后再试')
        } finally {
          submitting.value = false
        }
      }).catch(() => {
        // 用户取消预订
      })
    }
  })
}

const cancelOrder = () => {
  router.push('/room/list')
}

// 获取房型详情
const fetchRoomTypeDetail = async (id) => {
  loading.value = true
  try {
    const res = await getRoomTypeDetail(id)
    if (res.code === 200) {
      // 处理响应数据
      const roomData = res.data
      selectedRoom.value = {
        id: roomData.id,
        name: roomData.typeName,
        image: roomData.images.find(img => img.isMain === 1)?.imageUrl || roomData.images[0]?.imageUrl,
        price: roomData.price,
        size: `${roomData.area}平方米`,
        bedType: roomData.bedType,
        maxGuests: roomData.maxGuests,
        description: roomData.description,
        facilities: roomData.facilities.split(',')
      }
      
      // 更新订单表单的房型ID
      orderForm.roomTypeId = selectedRoom.value.id
      
      // 设置默认入住人数为最大入住人数
      orderForm.guestCount = selectedRoom.value.maxGuests || 1
    } else {
      ElMessage.error('获取房型详情失败')
      router.push('/room/list')
    }
  } catch (error) {
    console.error('获取房型详情失败:', error)
    ElMessage.warning('连接后端服务失败，使用本地模拟数据展示')
    // 使用模拟数据
    selectedRoom.value = getBackupRoomData(id)
    orderForm.roomTypeId = selectedRoom.value.id
    orderForm.guestCount = selectedRoom.value.maxGuests || 1
  } finally {
    loading.value = false
  }
}

// 备用房型数据
const getBackupRoomData = (id) => {
  const roomsData = [
    {
      id: 1,
      name: '标准单人间',
      image: 'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
      price: 258,
      size: '18平方米',
      bedType: '单人床',
      maxGuests: 1,
      description: '舒适的单人间，配备基本设施，适合商务出行或单人旅游。',
      facilities: ['WiFi', '电视', '空调']
    },
    {
      id: 2,
      name: '标准双床房',
      image: 'https://img1.baidu.com/it/u=1187860984,2261105129&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
      price: 328,
      size: '25平方米',
      bedType: '两张单人床',
      maxGuests: 2,
      description: '实用的双床房，配有两张单人床，简约舒适，适合商务或结伴出行。',
      facilities: ['WiFi', '电视', '空调', '电热水壶']
    },
    {
      id: 3,
      name: '豪华大床房',
      image: 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
      price: 398,
      size: '30平方米',
      bedType: '特大号床',
      maxGuests: 2,
      description: '宽敞明亮的客房配备特大号床，提供舒适的睡眠体验，配有现代化设施。',
      facilities: ['WiFi', '电视', '空调', '电热水壶', '冰箱', '早餐']
    },
    {
      id: 4,
      name: '行政套房',
      image: 'https://img1.baidu.com/it/u=1979540962,1457794044&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
      price: 598,
      size: '45平方米',
      bedType: '特大号床',
      maxGuests: 2,
      description: '豪华行政套房，配有独立起居室和卧室，享有城市美景，提供高端服务。',
      facilities: ['WiFi', '电视', '空调', '冰箱', '浴缸', '吹风机', 'minibar', '书桌', '保险箱', '熨斗']
    },
    {
      id: 5,
      name: '家庭房',
      image: 'https://img2.baidu.com/it/u=3482414296,1599253536&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
      price: 498,
      size: '40平方米',
      bedType: '一张大床和一张单人床',
      maxGuests: 3,
      description: '适合家庭入住的亲子房，配有1张大床和1张儿童床，有儿童游乐区和舒适的休息空间。',
      facilities: ['WiFi', '电视', '空调', '冰箱', '淋浴', '吹风机']
    }
  ]
  
  const parsedId = parseInt(id)
  return roomsData.find(r => r.id === parsedId) || roomsData[0]
}

// 初始化页面，获取房型信息
onMounted(() => {
  const roomId = route.query.roomId
  if (roomId) {
    fetchRoomTypeDetail(roomId)
  } else {
    ElMessage.error('未指定房型，请重新选择')
    router.push('/room/list')
  }
})
</script>

<template>
  <div class="order-create-container">
    <div v-loading="loading" class="order-content">
      <h2 class="page-title">填写预订信息</h2>
      
      <!-- 房型信息卡片 -->
      <el-card v-if="selectedRoom" class="room-card">
        <div class="room-info">
          <div class="room-image-container">
            <img :src="selectedRoom.image" :alt="selectedRoom.name" class="room-image" />
          </div>
          <div class="room-details">
            <h3 class="room-name">{{ selectedRoom.name }}</h3>
            <div class="room-specs">
              <span class="spec-item">{{ selectedRoom.size }}</span>
              <span class="spec-item">{{ selectedRoom.bedType }}</span>
              <span class="spec-item">最多{{ selectedRoom.maxGuests }}人</span>
            </div>
            <div class="room-price">
              <span class="price">¥{{ selectedRoom.price }}</span>
              <span class="unit">/晚</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 订单表单 -->
      <el-card class="order-form-card">
        <el-form 
          ref="orderFormRef"
          :model="orderForm"
          :rules="rules"
          label-position="top"
        >
          <h3 class="form-section-title">预订日期</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入住日期" prop="checkInDate">
                <el-date-picker 
                  v-model="orderForm.checkInDate"
                  type="date"
                  placeholder="选择入住日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="(time) => time.getTime() < Date.now() - 8.64e7"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="离店日期" prop="checkOutDate">
                <el-date-picker 
                  v-model="orderForm.checkOutDate"
                  type="date"
                  placeholder="选择离店日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="(time) => time.getTime() <= new Date(orderForm.checkInDate).getTime()"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <h3 class="form-section-title">入住人信息</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入住人姓名" prop="guestName">
                <el-input 
                  v-model="orderForm.guestName"
                  placeholder="请输入入住人姓名"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="入住人数" prop="guestCount">
                <el-input-number 
                  v-model="orderForm.guestCount"
                  :min="1"
                  :max="selectedRoom?.maxGuests || 1"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="guestPhone">
                <el-input 
                  v-model="orderForm.guestPhone"
                  placeholder="请输入联系电话"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="guestIdCard">
                <el-input 
                  v-model="orderForm.guestIdCard"
                  placeholder="请输入身份证号"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <h3 class="form-section-title">其他服务</h3>
          <el-form-item label="添加早餐" v-if="!selectedRoom?.facilities?.includes('早餐')">
            <el-switch 
              v-model="orderForm.breakfast"
              active-text="添加早餐"
              inactive-text="不需要早餐"
            />
            <div class="service-description" v-if="orderForm.breakfast">
              早餐费用: ¥38/人/天 × {{ orderForm.guestCount }}人 × {{ stayDays }}天 = ¥{{ breakfastPrice }}
            </div>
          </el-form-item>
          
          <el-form-item label="备注" prop="remarks">
            <el-input 
              v-model="orderForm.remarks"
              type="textarea"
              :rows="3"
              placeholder="如有特殊要求，请在此备注"
            />
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 订单摘要 -->
      <el-card class="order-summary-card">
        <h3 class="summary-title">费用明细</h3>
        <div class="summary-item">
          <span>房间费用</span>
          <span>¥{{ basePrice }} × {{ stayDays }}晚 = ¥{{ basePrice * stayDays }}</span>
        </div>
        <div class="summary-item" v-if="breakfastPrice > 0">
          <span>早餐费用</span>
          <span>¥{{ breakfastPrice }}</span>
        </div>
        <div class="summary-total">
          <span>总计</span>
          <span class="total-price">¥{{ totalPrice }}</span>
        </div>
        
        <div class="order-actions">
          <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">提交订单</el-button>
          <el-button size="large" @click="cancelOrder">取消</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.order-create-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-title {
  margin: 20px 0;
  font-size: 24px;
  font-weight: bold;
}

.order-content {
  min-height: 600px;
}

.room-card, .order-form-card, .order-summary-card {
  margin-bottom: 20px;
}

.room-info {
  display: flex;
  align-items: center;
}

.room-image-container {
  width: 200px;
  height: 130px;
  margin-right: 20px;
  overflow: hidden;
  border-radius: 8px;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-details {
  flex: 1;
}

.room-name {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
}

.room-specs {
  margin-bottom: 10px;
}

.spec-item {
  margin-right: 15px;
  color: #606266;
  font-size: 14px;
}

.room-price {
  color: #ff6600;
}

.price {
  font-size: 22px;
  font-weight: bold;
}

.unit {
  font-size: 14px;
  color: #909399;
}

.form-section-title {
  margin: 10px 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
  font-size: 18px;
  color: #303133;
}

.service-description {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.summary-title {
  margin-top: 0;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
  font-size: 18px;
  color: #303133;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #606266;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
  font-weight: bold;
  color: #303133;
}

.total-price {
  font-size: 20px;
  color: #ff6600;
}

.order-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

@media (max-width: 768px) {
  .room-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .room-image-container {
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 15px;
  }
}
</style> 