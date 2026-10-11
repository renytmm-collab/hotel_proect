<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getFavoriteList, cancelFavorite } from '@/api/favorite'

const router = useRouter()
const loading = ref(true)
const favorites = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

// 从环境变量获取基础URL
const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 加载收藏数据
const fetchFavorites = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const res = await getFavoriteList(params)
    
    if (res.code === 200) {
      favorites.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('收藏列表数据:', favorites.value)
    } else {
      ElMessage.error(res.message || '获取收藏列表失败')
    }
  } catch (error) {
    console.error('获取收藏列表错误:', error)
    ElMessage.error('获取收藏列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理页面变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchFavorites()
}

// 移除收藏
const removeFavorite = (roomTypeId) => {
  ElMessageBox.confirm('确定要取消收藏该房型吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelFavorite({ roomTypeId })
      
      if (res.code === 200) {
        ElMessage.success('已取消收藏')
        // 重新加载收藏列表
        fetchFavorites()
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } catch (error) {
      console.error('取消收藏错误:', error)
      ElMessage.error('取消收藏失败，请稍后再试')
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 立即预订
const bookNow = (roomTypeId) => {
  router.push({
    path: 'order/create',
    query: { roomId: roomTypeId }
  })
}

// 查看房型详情
const viewRoomDetail = (roomTypeId) => {
  router.push(`room/detail/${roomTypeId}`)
}

// 浏览所有房型
const browseAllRooms = () => {
  router.push('room/list')
}

// 处理图片URL
const processImageUrl = (url) => {
  if (!url) return ''
  
  try {
    // 如果已经是完整的URL（包含http或https），直接返回
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    
    // 如果是相对路径，拼接完整URL
    const cleanUrl = url.startsWith('/') ? url : `/upload/${url}`
    return `${baseUrl}${cleanUrl}`
  } catch (error) {
    console.error('处理图片URL错误:', error)
    return ''
  }
}

// 图片加载错误处理
const handleImageError = (e) => {
  // 直接清除图片源
  e.target.src = ''
  // 清除错误处理器
  e.target.onerror = null
}

// 添加分页大小改变处理函数
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
  fetchFavorites()
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorite-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的收藏</span>
        </div>
      </template>
      
      <!-- 收藏列表 -->
      <div v-loading="loading" class="favorite-list">
        <template v-if="favorites && favorites.length > 0">
          <el-row :gutter="20">
            <el-col :span="8" v-for="item in favorites" :key="item.id">
              <el-card class="room-card" :body-style="{ padding: '0px' }">
                <div class="room-image-container">
                  <img 
                    v-if="item.mainImage" 
                    :src="processImageUrl(item.mainImage)" 
                    class="room-image" 
                    @error="handleImageError"
                  />
                  <div v-else class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <div>暂无图片</div>
                  </div>
                </div>
                <div class="room-info">
                  <h3>{{ item.typeName }}</h3>
                  <div class="price">¥{{ item.price }}/晚</div>
                  <div class="bed-type">{{ item.bedType }}</div>
                  <div class="operations">
                    <el-button type="primary" size="small" @click="viewRoomDetail(item.roomTypeId)">
                      查看详情
                    </el-button>
                    <el-button type="danger" size="small" @click="removeFavorite(item.roomTypeId)">
                      取消收藏
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[8, 16, 24]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </template>
        
        <el-empty v-else description="暂无收藏" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.favorite-container {
  padding: 20px;
}

.room-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.room-image-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  position: relative;
  background-color: #f5f7fa;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.image-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.room-info {
  padding: 14px;
}

.room-info h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.price {
  margin-top: 10px;
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.bed-type {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

.operations {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 