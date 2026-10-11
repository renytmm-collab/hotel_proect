<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitReview } from '@/api/review'
import { getOrderDetail } from '@/api/order'
import { uploadFile } from '@/api/common'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const submitting = ref(false)
const reviewFormRef = ref(null)
const order = ref(null)
// 上传的图片列表
const imageList = ref([])

// 基础URL配置 - 从环境变量获取
const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// token处理
const token = computed(() => localStorage.getItem('token') || '')

// 初始化表单数据
const reviewForm = reactive({
  orderId: 0,
  roomTypeId: 0,
  content: '',
  score: 5,
  isAnonymous: false,
  images: []
})

// 评分描述
const rateDescriptions = {
  1: '非常差',
  2: '差',
  3: '一般',
  4: '好',
  5: '非常好'
}

// 获取订单详情
const fetchOrderDetail = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    if (res.code === 200) {
      order.value = res.data
      reviewForm.orderId = order.value.id
      reviewForm.roomTypeId = order.value.roomTypeId
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

// 文件上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 处理图片上传成功
const handleUploadSuccess = (res, file) => {
  console.log('图片上传成功:', res)
  if (res.code === 200 && res.data) {
    // 获取图片路径 - 可能在不同字段
    let imagePath = res.data.fileUrl || res.data;
    console.log('上传返回的图片路径:', imagePath)
    
    // 保存相对路径用于提交给后端
    if (typeof imagePath === 'string') {
      // 统一处理路径格式 - 保存原始路径，不做额外处理
      reviewForm.images.push(imagePath)
      
      // 创建完整URL用于前端显示
      const fullUrl = imagePath.startsWith('http') 
        ? imagePath 
        : baseUrl + (imagePath.startsWith('/') ? imagePath : `/${imagePath}`)
      
      // 记录上传的图片，用于界面展示
      imageList.value.push({
        name: file.name,
        url: fullUrl,
        path: imagePath // 保存原始路径
      })
      
      console.log('当前图片列表:', reviewForm.images)
    } else {
      ElMessage.error('图片路径格式错误')
    }
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 移除图片
const handleRemove = (file, fileList) => {
  // 查找对应的索引 - 使用path或url匹配
  const path = file.path || file.url
  const index = reviewForm.images.findIndex(url => url === path)
  
  if (index !== -1) {
    // 从表单数据中移除
    reviewForm.images.splice(index, 1)
    console.log('移除图片后的列表:', reviewForm.images)
  } else {
    console.warn('未找到要移除的图片:', path)
  }
}

// 处理头像上传
const handleAvatarSuccess = (res, file) => {
  // 移除头像上传功能
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  // 移除头像上传验证
  return false
}

// 提交评价
const submitReviewForm = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        // 确保图片数组正确传递
        const data = {
          orderId: reviewForm.orderId,
          roomTypeId: reviewForm.roomTypeId,
          content: reviewForm.content,
          score: reviewForm.score,
          isAnonymous: reviewForm.isAnonymous ? 1 : 0,
          images: reviewForm.images // 已经是从文件上传响应中获取的路径数组
        }
        
        console.log('提交的评价数据:', data)
        
        const res = await submitReview(data)
        
        if (res.code === 200) {
          ElMessage.success('评价提交成功')
          // 跳转到评价列表页
          router.push('/comment')
        } else {
          ElMessage.error(res.message || '评价提交失败')
        }
      } catch (error) {
        console.error('提交评价错误:', error)
        ElMessage.error('评价提交失败，请稍后再试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消评价
const cancelReview = () => {
  ElMessageBox.confirm('确定要取消评价吗？已填写的内容将不会保存。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '返回编辑',
    type: 'warning'
  }).then(() => {
    router.push(`/order/detail/${reviewForm.orderId}`)
  }).catch(() => {
    // 继续编辑
  })
}

onMounted(() => {
  const orderId = route.params.id || route.query.orderId
  
  if (orderId) {
    fetchOrderDetail(orderId)
  } else {
    ElMessage.error('未指定订单ID')
    router.push('/order/list')
  }
})
</script>

<template>
  <div class="review-create-container">
    <div v-loading="loading" class="review-content">
      <div class="page-header">
        <el-page-header :title="'订单评价'" @back="() => router.push(`/order/detail/${reviewForm.orderId}`)" />
      </div>
      
      <div v-if="order" class="review-form-container">
        <!-- 订单信息卡片 -->
        <el-card class="order-info-card">
          <div class="room-info">
            <h3>{{ order.roomTypeName }}</h3>
            <div class="stay-info">
              <span>入住: {{ order.checkInDate }}</span>
              <span>离店: {{ order.checkOutDate }}</span>
              <span>{{ order.days }}晚</span>
            </div>
          </div>
        </el-card>
        
        <!-- 评价表单 -->
        <el-card class="review-form-card">
          <el-form
            ref="reviewFormRef"
            :model="reviewForm"
            label-position="top"
            :rules="{
              content: [
                { required: true, message: '请输入评价内容', trigger: 'blur' },
                { min: 5, max: 500, message: '评价内容长度在5到500个字符之间', trigger: 'blur' }
              ]
            }"
          >
            <h3 class="form-section-title">您的评价</h3>
            
            <el-form-item label="满意度评分" prop="score">
              <div class="rating-container">
                <el-rate
                  v-model="reviewForm.score"
                  show-score
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  :texts="[rateDescriptions[1], rateDescriptions[2], rateDescriptions[3], rateDescriptions[4], rateDescriptions[5]]"
                  text-color="#FF9900"
                  score-template="{value} 分"
                />
                <span class="rating-text">{{ rateDescriptions[reviewForm.score] }}</span>
              </div>
            </el-form-item>
            
            <el-form-item label="评价内容" prop="content">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="6"
                placeholder="请分享您的住宿体验，如房间设施、服务态度、卫生情况等"
              />
            </el-form-item>
            
            <el-form-item label="上传图片">
              <el-upload
                action="/api/admin/common/upload"
                list-type="picture-card"
                :limit="6"
                :on-error="(err, file, fileList) => { console.error('上传图片错误:', err) }"
                :on-success="handleUploadSuccess"
                :on-remove="handleRemove"
                :before-upload="beforeUpload"
                :headers="{
                  Authorization: `Bearer ${token}`
                }"
                name="file"
                :file-list="imageList"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">
                最多可上传6张图片，单张不超过2MB
              </div>
              <!-- 显示已上传的图片URL -->
              <div v-if="reviewForm.images.length > 0" class="uploaded-images-info">
                <div class="uploaded-title">已上传的图片路径 (将提交给服务器)</div>
                <div v-for="(url, index) in reviewForm.images" :key="index" class="uploaded-url">
                  <el-tag size="small" type="info" class="url-tag">{{ index + 1 }}</el-tag>
                  <span>{{ url }}</span>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
              <div class="checkbox-tip" v-if="reviewForm.isAnonymous">
                您的评价将以匿名方式展示
              </div>
            </el-form-item>
            
            <div class="form-actions">
              <el-button type="primary" :loading="submitting" @click="submitReviewForm">提交评价</el-button>
              <el-button @click="cancelReview">取消</el-button>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-create-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.review-form-container {
  max-width: 800px;
  margin: 0 auto;
}

.order-info-card {
  margin-bottom: 20px;
}

.room-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
}

.stay-info {
  display: flex;
  gap: 20px;
  color: #606266;
}

.review-form-card {
  margin-bottom: 20px;
}

.form-section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
}

.rating-container {
  display: flex;
  align-items: center;
}

.rating-text {
  margin-left: 15px;
  font-size: 16px;
  color: #FF9900;
}

.upload-tip, .checkbox-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.uploaded-images-info {
  margin-top: 10px;
  border: 1px solid #ebeef5;
  padding: 10px;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.uploaded-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #606266;
}

.uploaded-url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
  margin-bottom: 3px;
  display: flex;
  align-items: center;
}

.url-tag {
  margin-right: 8px;
  min-width: 22px;
  text-align: center;
}
</style> 