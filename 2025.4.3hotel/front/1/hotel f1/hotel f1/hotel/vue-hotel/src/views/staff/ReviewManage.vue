<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, replyGuestReview, getRoomTypeNames } from '@/api/reception'

// 评价列表数据
const reviewList = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 房型筛选
const roomTypeFilter = ref('')
const roomTypeOptions = ref([
  { value: '', label: '全部房型' }
])
const roomTypeLoading = ref(false)

// 回复评论对话框
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')
const replying = ref(false)

// 获取房型列表
const fetchRoomTypes = async () => {
  roomTypeLoading.value = true
  try {
    const res = await getRoomTypeNames()
    console.log('获取房型列表响应:', res)
    
    if (res.code === 200 && res.data) {
      // 确保res.data是数组
      const types = Array.isArray(res.data) ? res.data : [res.data]
      
      // 将API返回的房型数据转换为选项格式
      const options = types.map(type => ({
        value: type.id,
        label: type.typeName
      }))
      
      // 保留"全部房型"选项
      roomTypeOptions.value = [
        { value: '', label: '全部房型' },
        ...options
      ]
      
      console.log('处理后的房型选项:', roomTypeOptions.value)
    } else {
      ElMessage.warning('获取房型列表失败，将使用默认选项')
    }
  } catch (error) {
    console.error('获取房型列表错误:', error)
    ElMessage.warning('获取房型列表失败，将使用默认选项')
  } finally {
    roomTypeLoading.value = false
  }
}

// 获取评价列表
const fetchReviewList = async () => {
  loading.value = true
  try {
    // 按照API文档要求设置参数
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    // 只有当有选择房型时才添加房型筛选参数
    if (roomTypeFilter.value) {
      params.roomTypeId = roomTypeFilter.value
    }
    
    console.log('获取评价列表请求参数:', params)
    const res = await getReviewList(params)
    console.log('获取评价列表响应:', res)
    
    if (res.code === 200 && res.data) {
      // 处理返回数据 - 兼容列表存储在records或list字段中的情况
      const recordsList = res.data.records || res.data.list || []
      console.log('解析的评价列表数据:', recordsList)
      
      if (Array.isArray(recordsList)) {
        // 按照API文档中的字段进行处理
        reviewList.value = recordsList.map(item => {
          // 查找房型名称 - 可能在不同的字段中
          let roomTypeName = item.roomTypeName || item.typeName || '';
          
          // 如果筛选了房型，并且返回的记录没有房型名称，则使用筛选的房型名称
          if (!roomTypeName && roomTypeFilter.value) {
            const selectedOption = roomTypeOptions.value.find(opt => opt.value === roomTypeFilter.value);
            if (selectedOption) {
              roomTypeName = selectedOption.label;
            }
          }
          
          return {
            id: item.id,
            content: item.content,
            score: item.score,
            isAnonymous: item.isAnonymous,
            roomTypeName: roomTypeName || '未知房型',
            roomTypeId: item.roomTypeId || item.typeId || roomTypeFilter.value || '',
            orderId: item.orderId,
            createTime: item.createTime,
            images: Array.isArray(item.reviewImages) ? item.reviewImages : (Array.isArray(item.images) ? item.images : []),
            reply: item.reply || '',
            replyTime: item.replyTime || ''
          }
        })
        
        // 输出调试信息
        console.log('处理后的评价列表(包含图片):', reviewList.value)
        
        // 设置总数
        pagination.total = res.data.total || 0
        
        if (reviewList.value.length === 0) {
          ElMessage.info('暂无评价记录')
        } else {
          console.log('处理后的评价列表:', reviewList.value)
        }
      } else {
        console.warn('返回的评价列表不是数组:', res.data)
        reviewList.value = []
        pagination.total = 0
        ElMessage.info('暂无评价记录')
      }
    } else {
      ElMessage.error(res.message || '获取评价列表失败')
      reviewList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取评价列表错误:', error)
    ElMessage.error('获取评价列表失败，请稍后再试')
    reviewList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  fetchReviewList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  fetchReviewList()
}

// 房型筛选变化
const handleRoomTypeFilterChange = () => {
  pagination.pageNum = 1
  fetchReviewList()
}

// 打开回复对话框
const openReplyDialog = (review) => {
  currentReview.value = review
  replyContent.value = review.reply || ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  replying.value = true
  try {
    // 按照API文档要求设置参数
    const data = {
      id: currentReview.value.id,
      reply: replyContent.value.trim()
    }
    
    console.log('提交回复请求数据:', data)
    const res = await replyGuestReview(data)
    console.log('提交回复响应:', res)
    
    if (res.code === 200) {
      ElMessage.success('回复成功')
      
      // 更新本地数据
      const index = reviewList.value.findIndex(item => item.id === currentReview.value.id)
      if (index !== -1) {
        reviewList.value[index].reply = replyContent.value
        reviewList.value[index].replyTime = new Date().toISOString().split('T')[0]
      }
      
      replyDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (error) {
    console.error('回复评论错误:', error)
    ElMessage.error('回复失败，请稍后再试')
  } finally {
    replying.value = false
  }
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  if (!orderId) {
    ElMessage.warning('无订单信息')
    return
  }
  
  // 跳转到订单详情页面
  window.open(`/staff/order?orderId=${orderId}`, '_blank')
}

// 初始化获取评价列表
onMounted(() => {
  fetchRoomTypes()
  fetchReviewList()
})
</script>

<template>
  <div class="review-management">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h2>客人评价管理</h2>
        </div>
      </template>
      
      <!-- 筛选栏 -->
      <div class="filter-row">
        <el-select
          v-model="roomTypeFilter"
          placeholder="按房型筛选"
          clearable
          style="width: 200px;"
          @change="handleRoomTypeFilterChange"
          :loading="roomTypeLoading"
        >
          <el-option
            v-for="item in roomTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-button type="primary" @click="fetchReviewList">刷新</el-button>
      </div>
      
      <!-- 评价列表 -->
      <div v-loading="loading">
        <el-table
          :data="reviewList"
          style="width: 100%"
          border
          stripe
          :header-cell-style="{ background: '#f5f7fa' }"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="roomTypeName" label="房型" width="150" />
          <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="score" label="评分" width="120">
            <template #default="{ row }">
              <el-rate
                v-model="row.score"
                disabled
                text-color="#ff9900"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评价时间" width="120" />
          <el-table-column prop="isAnonymous" label="匿名" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isAnonymous === 1 ? 'info' : 'success'" size="small">
                {{ row.isAnonymous === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="images" label="图片" width="100">
            <template #default="{ row }">
              <el-popover
                v-if="row.images && row.images.length > 0"
                placement="right"
                :width="320"
                trigger="hover"
              >
                <template #reference>
                  <el-button type="info" size="small" plain>
                    查看图片({{ row.images.length }})
                  </el-button>
                </template>
                <div class="image-preview">
                  <el-image
                    v-for="(img, index) in row.images"
                    :key="index"
                    :src="img"
                    :preview-src-list="row.images"
                    fit="cover"
                    style="width: 80px; height: 80px; margin: 5px;"
                  />
                </div>
              </el-popover>
              <span v-else class="text-muted">无图片</span>
            </template>
          </el-table-column>
          <el-table-column prop="reply" label="回复内容" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.reply">{{ row.reply }}</span>
              <span v-else class="text-muted">暂无回复</span>
            </template>
          </el-table-column>
          <el-table-column prop="replyTime" label="回复时间" width="120">
            <template #default="{ row }">
              <span v-if="row.replyTime">{{ row.replyTime }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="openReplyDialog(row)"
              >
                {{ row.reply ? '修改回复' : '回复' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty
          v-if="reviewList.length === 0 && !loading"
          description="暂无评价数据"
        />
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="pagination.total > 0">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 回复评论对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复客人评价"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentReview" class="reply-dialog-content">
        <div class="review-info">
          <div class="review-item">
            <span class="review-label">房型:</span>
            <span class="review-value">{{ currentReview.roomTypeName }}</span>
          </div>
          <div class="review-item">
            <span class="review-label">评分:</span>
            <span class="review-value">
              <el-rate v-model="currentReview.score" disabled text-color="#ff9900" />
            </span>
          </div>
          <div class="review-item">
            <span class="review-label">评价内容:</span>
            <span class="review-value">{{ currentReview.content }}</span>
          </div>
          <div class="review-item" v-if="currentReview.images && currentReview.images.length > 0">
            <span class="review-label">图片:</span>
            <div class="review-images">
              <el-image
                v-for="(img, index) in currentReview.images"
                :key="index"
                :src="img"
                :preview-src-list="currentReview.images"
                fit="cover"
                style="width: 60px; height: 60px; margin-right: 5px;"
              />
            </div>
          </div>
        </div>
        
        <el-form>
          <el-form-item label="回复内容" required>
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="replying"
          @click="submitReply"
        >
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.review-management {
  padding: 20px;
}

.page-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-row {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
  font-size: 13px;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.reply-dialog-content {
  padding: 0 20px;
}

.review-info {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.review-item {
  margin-bottom: 10px;
}

.review-label {
  color: #606266;
  font-weight: 600;
  display: inline-block;
  margin-right: 8px;
  min-width: 80px;
}

.review-value {
  color: #303133;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  margin-top: 5px;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-row .el-select {
    width: 100% !important;
  }
}
</style> 