<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getReviewList, deleteReview } from '@/api/review'
import { getUserInfo } from '@/api/user'
import { ChatDotRound, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const comments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)
const searchOrderId = ref('')
const userInfo = ref(null)
const userLoading = ref(false)

// 从环境变量获取基础URL
const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 处理图片URL
const processImageUrl = (url) => {
  if (!url) return ''
  
  try {
    // 如果已经是完整的URL（包含http或https），直接返回
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    
    // 如果是相对路径，拼接完整URL
    return `${baseUrl}${url.startsWith('/') ? url : `/${url}`}`
  } catch (error) {
    console.error('处理图片URL错误:', error)
    return ''
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  userLoading.value = true
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      console.log('获取到的用户信息:', res.data)
    } else {
      console.error('获取用户信息失败:', res.message || '未知错误')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
  } finally {
    userLoading.value = false
  }
}

// 评分选项
const rateTexts = {
  1: '非常差',
  2: '差',
  3: '一般',
  4: '好',
  5: '非常好'
}

// 加载评论数据
const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 如果有指定订单ID，添加到查询参数
    if (searchOrderId.value) {
      params.orderId = searchOrderId.value
    }
    
    const res = await getReviewList(params)
    
    if (res.code === 200 && res.data) {
      console.log('评价数据:', res.data)
      // 处理评论数据
      comments.value = (res.data.records || res.data.list || []).map(comment => {
        // 处理图片数组，确保它是一个数组
        const images = Array.isArray(comment.images) 
          ? comment.images 
          : (typeof comment.images === 'string' && comment.images) 
            ? JSON.parse(comment.images) 
            : []
        
        const commentData = {
          ...comment,
          userAvatar: comment.userAvatar ? processImageUrl(comment.userAvatar) : '',
          images: Array.isArray(images) 
            ? images.map(img => processImageUrl(img)).filter(url => url)
            : []
        }
        
        // 如果是当前用户的评论，使用用户信息中的头像和用户名
        if (userInfo.value && !comment.isAnonymous) {
          // 如果评论没有头像或用户名，使用用户信息中的数据
          if (!commentData.userAvatar) {
            commentData.userAvatar = userInfo.value.avatar ? processImageUrl(userInfo.value.avatar) : ''
          }
          if (!commentData.username) {
            commentData.username = userInfo.value.nickname || userInfo.value.username || '用户'
          }
        }
        
        return commentData
      })
      total.value = res.data.total || 0
      
      // 打印处理后的图片URL以便调试
      if (comments.value.length > 0) {
        console.log('处理后的第一条评价图片:', comments.value[0].images)
        console.log('处理后的第一条评价用户信息:', {
          username: comments.value[0].username,
          avatar: comments.value[0].userAvatar
        })
      }
    } else {
      comments.value = []
      total.value = 0
      ElMessage.error(res.msg || '获取评价列表失败')
    }
  } catch (error) {
    console.error('获取评价列表错误:', error)
    comments.value = []
    total.value = 0
    ElMessage.error('获取评价列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理页面变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchReviews()
}

// 删除评论
const handleDeleteComment = (id) => {
  ElMessageBox.confirm('确定要删除此评论吗？删除后无法恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteReview(id)
      
      if (res.code === 200) {
        ElMessage.success('评论已删除')
        // 重新加载评论列表
        fetchReviews()
      } else {
        ElMessage.error(res.message || '删除评论失败')
      }
    } catch (error) {
      console.error('删除评论错误:', error)
      ElMessage.error('删除评论失败，请稍后再试')
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 查看订单详情
const viewOrder = (orderId) => {
  router.push(`/order/detail/${orderId}`)
}

// 获取评分文字描述
const getRateText = (rate) => {
  return rateTexts[rate] || ''
}

// 重置筛选
const resetFilter = () => {
  searchOrderId.value = ''
  currentPage.value = 1
  fetchReviews()
}

onMounted(() => {
  // 先获取用户信息
  fetchUserInfo().then(() => {
    // 如果URL中有orderId参数，过滤显示该订单的评价
    const orderId = route.query.orderId
    if (orderId) {
      searchOrderId.value = orderId
    }
    fetchReviews()
  })
})
</script>

<template>
  <div class="comment-container">
    <h2 class="page-title">我的评价</h2>

    <!-- 筛选提示 -->
    <div v-if="searchOrderId" class="filter-info">
      <el-alert
        title="当前筛选指定订单的评价"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <span>订单ID: {{ searchOrderId }}</span>
          <el-button type="primary" link @click="resetFilter" style="margin-left: 20px">清除筛选</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 加载中 -->
    <div v-loading="loading" class="loading-container" v-if="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 评论列表 -->
    <div v-else>
      <!-- 空状态 -->
      <el-empty v-if="!comments || comments.length === 0" description="暂无评论记录">
        <el-button type="primary" @click="router.push('/order/list')">查看我的订单</el-button>
      </el-empty>

      <!-- 评论列表 -->
      <div v-else class="comment-list">
        <el-card v-for="comment in comments" :key="comment.id" class="comment-card" shadow="hover">
          <!-- 评论头部 -->
          <div class="review-header">
            <div class="reviewer-info">
              <div v-if="comment.userAvatar" class="avatar-container">
                <img :src="comment.userAvatar" class="avatar" alt="用户头像" />
              </div>
              <div v-else class="avatar-placeholder">
                {{ comment.isAnonymous ? '匿' : (comment.username?.charAt(0) || '用') }}
              </div>
              <div class="reviewer-name">
                {{ comment.isAnonymous ? '匿名用户' : (comment.username || (userInfo ? (userInfo.nickname || userInfo.username) : '用户')) }}
              </div>
            </div>
            <div class="comment-actions">
              <el-button type="primary" link @click="viewOrder(comment.orderId)">查看订单</el-button>
              <el-button type="danger" link @click="handleDeleteComment(comment.id)">删除</el-button>
            </div>
          </div>

          <!-- 评分 -->
          <div class="comment-rating">
            <el-rate
              v-model="comment.score"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value} 分"
            />
            <span class="rate-text">{{ getRateText(comment.score) }}</span>
            <span class="comment-time">{{ comment.createTime || '未知时间' }}</span>
          </div>

          <!-- 评论内容 -->
          <div class="comment-content">
            {{ comment.content || '暂无评论内容' }}
          </div>

          <!-- 评论图片 -->
          <div v-if="comment.images && comment.images.length > 0" class="comment-pictures">
            <div v-for="(pic, index) in comment.images" :key="index" class="picture-container">
              <el-image
                :src="pic"
                fit="cover"
                class="picture-item"
                :preview-src-list="comment.images"
                :initial-index="index"
                @error="() => console.error('图片加载失败:', pic)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <div>加载失败</div>
                  </div>
                </template>
              </el-image>
            </div>
          </div>

          <!-- 酒店回复 -->
          <div v-if="comment.reply" class="hotel-reply">
            <div class="reply-title">
              <el-icon><ChatDotRound /></el-icon>
              <span>酒店回复:</span>
              <span class="reply-time">{{ comment.replyTime || '未知时间' }}</span>
            </div>
            <div class="reply-content">
              {{ comment.reply }}
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-container {
  width: 100%;
  max-width: 100%;
}

.filter-info {
  margin-bottom: 20px;
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

.loading-container {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.comment-list {
  margin-top: 20px;
}

.comment-card {
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
}

.reviewer-avatar {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.reviewer-name {
  font-weight: bold;
  margin-bottom: 5px;
  cursor: pointer;
  color: #409EFF;
}

.reviewer-name:hover {
  text-decoration: underline;
}

.comment-rating {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.rate-text {
  margin-left: 10px;
  color: #ff9900;
}

.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.comment-content {
  line-height: 1.6;
  margin-bottom: 15px;
  color: #303133;
}

.comment-pictures {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.picture-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

.hotel-reply {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.reply-title {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
}

.reply-title .el-icon {
  margin-right: 5px;
}

.reply-time {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.reply-content {
  color: #606266;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .review-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-actions {
    margin-top: 10px;
  }

  .reviewer-avatar {
    width: 60px;
    height: 45px;
  }

  .picture-item {
    width: 80px;
    height: 80px;
  }
}

.avatar-container {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  color: #909399;
}

.picture-container {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
</style> 