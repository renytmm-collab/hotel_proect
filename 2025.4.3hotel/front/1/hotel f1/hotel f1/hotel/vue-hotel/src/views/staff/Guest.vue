<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ElMessage, 
  ElMessageBox 
} from 'element-plus'
import { getGuestStayRecords, getReviewList, replyGuestReview } from '@/api/reception'
import { getGuestList, getGuestDetail } from '@/api/staff'

const router = useRouter()
const loading = ref(false)

// 搜索和筛选条件
const filterForm = reactive({
  searchQuery: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 状态选项
const statusOptions = [
  { value: '在住', label: '在住' },
  { value: '已离店', label: '已离店' }
]

// 表格数据
const tableData = ref([])

// 过滤后的表格数据
const filteredTableData = computed(() => {
  return tableData.value
})

// 重置筛选
const resetFilters = () => {
  filterForm.searchQuery = ''
  pagination.pageNum = 1
  loadGuestData()
}

// 加载客人数据
const loadGuestData = async () => {
  // 身份证号码是必须的参数
  if (!filterForm.searchQuery || !isValidIDCard(filterForm.searchQuery)) {
    // 不在页面初始加载时显示警告
    if (loading.value) { // 只有当手动触发搜索时才显示提示
      ElMessage.warning('请输入有效的身份证号码进行查询')
    }
    return
  }
  
  loading.value = true
  try {
    // 按照API文档要求设置参数
    const params = {
      guestIdCard: filterForm.searchQuery, // 必填参数
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    const res = await getGuestList(params)
    console.log('获取客人数据响应:', res)
    
    if (res.code === 200 && res.data) {
      // 处理返回数据 - 兼容处理list和records字段
      const recordsList = res.data.list || res.data.records || []
      
      if (Array.isArray(recordsList) && recordsList.length > 0) {
        console.log('解析的入住记录:', recordsList)
        // 处理客人列表数据
        tableData.value = recordsList.map(item => ({
          id: item.id || 0,
          name: item.guestName || '未知',
          idCard: params.guestIdCard,
          phone: item.guestPhone || '',
          status: item.checkOutTime ? '已离店' : '在住',
          checkInDate: item.checkInTime,
          checkOutDate: item.checkOutTime,
          roomNumber: item.roomNumber || '',
          orderId: item.orderId,
          orderNo: item.orderNo,
          roomTypeName: item.roomTypeName,
          days: item.days,
          totalAmount: item.totalAmount
        }))
        console.log('处理后的客人数据:', tableData.value)
        
        pagination.total = res.data.total || recordsList.length
        ElMessage.success(`成功加载${tableData.value.length}条入住记录`)
      } else {
        console.warn('未找到入住记录:', recordsList)
        tableData.value = []
        pagination.total = 0
        ElMessage.info('未找到相关客人入住记录')
      }
    } else {
      ElMessage.error(res.msg || res.message || '客人数据加载失败')
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载客人数据错误:', error)
    ElMessage.error('客人数据加载失败，请稍后再试')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 验证身份证号
const isValidIDCard = (idCard) => {
  // 简单的身份证号验证，实际项目中可能需要更复杂的验证
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  return reg.test(idCard);
}

// 客人详情对话框
const guestDetailVisible = ref(false)
const currentGuest = ref(null)

// 入住记录对话框
const stayRecordsVisible = ref(false)
const stayRecords = ref([])
const stayRecordsLoading = ref(false)
const stayRecordsPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const currentIdCard = ref('')

// 获取模拟客人详情
const getMockGuestDetail = (guest) => {
  return {
    ...guest,
    id: guest.id,
    name: guest.name,
    phone: guest.phone,
    idCard: guest.idCard,
    status: guest.status || '在住',
    roomNumber: guest.roomNumber || '',
    orderNo: guest.orderNo || '',
    roomTypeName: guest.roomTypeName || '',
    checkInDate: guest.checkInDate || '',
    checkOutDate: guest.checkOutDate || '',
    days: guest.days || 0,
    totalAmount: guest.totalAmount || 0,
    orderId: guest.orderId || 0
  }
}

// 获取客人详情
const viewGuestDetail = async (guest) => {
  try {
    loading.value = true
    // 修复API路径问题
    const res = await getGuestDetail(guest.id)
    
    if (res.code === 200) {
      currentGuest.value = res.data
      guestDetailVisible.value = true
    } else {
      // 如果API返回错误，使用模拟数据
      currentGuest.value = getMockGuestDetail(guest)
      guestDetailVisible.value = true
      ElMessage.warning('使用临时客人信息，API返回错误')
    }
  } catch (error) {
    console.error('获取客人详情错误:', error)
    // 如果API调用失败，使用模拟数据
    currentGuest.value = getMockGuestDetail(guest)
    guestDetailVisible.value = true
    ElMessage.warning('使用临时客人信息，详细信息获取失败')
  } finally {
    loading.value = false
  }
}

// 查看入住记录
const viewStayRecords = (guest) => {
  stayRecordsVisible.value = true
  currentIdCard.value = guest.idCard
  fetchStayRecords()
}

// 获取客人入住记录
const fetchStayRecords = async () => {
  if (!currentIdCard.value) return
  
  stayRecordsLoading.value = true
  try {
    const params = {
      guestIdCard: currentIdCard.value,
      pageNum: stayRecordsPagination.pageNum,
      pageSize: stayRecordsPagination.pageSize
    }
    
    const res = await getGuestStayRecords(params)
    console.log('获取入住记录响应:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理数据格式，支持list和records字段
      const recordsList = res.data.list || res.data.records || []
      
      if (Array.isArray(recordsList)) {
        stayRecords.value = recordsList.map(item => ({
          ...item,
          // 处理可能不存在的字段
          id: item.id || 0,
          orderId: item.orderId || 0,
          orderNo: item.orderNo || '未知',
          roomNumber: item.roomNumber || '未知',
          roomTypeName: item.roomTypeName || '未知房型',
          checkInTime: item.checkInTime || '未知',
          checkOutTime: item.checkOutTime || '未知',
          days: item.days || 0,
          totalAmount: item.totalAmount || 0
        }))
        stayRecordsPagination.total = res.data.total || 0
        
        if (stayRecords.value.length === 0) {
          ElMessage.info('该客人暂无入住记录')
        }
      } else {
        console.warn('返回的入住记录不是数组:', res.data)
        stayRecords.value = []
        stayRecordsPagination.total = 0
        ElMessage.info('该客人暂无入住记录')
      }
    } else {
      ElMessage.error(res.msg || res.message || '获取入住记录失败')
      stayRecords.value = []
      stayRecordsPagination.total = 0
    }
  } catch (error) {
    console.error('获取入住记录错误:', error)
    ElMessage.error('获取入住记录失败，请稍后再试')
    stayRecords.value = []
    stayRecordsPagination.total = 0
  } finally {
    stayRecordsLoading.value = false
  }
}

// 处理入住记录页码变化
const handleStayRecordsPageChange = (page) => {
  stayRecordsPagination.pageNum = page
  fetchStayRecords()
}

// 处理入住记录每页条数变化
const handleStayRecordsSizeChange = (size) => {
  stayRecordsPagination.pageSize = size
  stayRecordsPagination.pageNum = 1
  fetchStayRecords()
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/staff/order?orderId=${orderId}`)
}

// 评价列表对话框
const reviewListVisible = ref(false)
const reviewList = ref([])
const reviewListLoading = ref(false)
const reviewListPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const roomTypeFilter = ref('')
const roomTypeOptions = [
  { value: '', label: '全部房型' },
  { value: 1, label: '豪华大床房' },
  { value: 2, label: '商务双床房' },
  { value: 3, label: '标准大床房' },
  { value: 4, label: '高级套房' }
]

// 查看评价列表
const viewReviewList = () => {
  reviewListVisible.value = true
  fetchReviewList()
}

// 获取评价列表
const fetchReviewList = async () => {
  reviewListLoading.value = true
  try {
    // 按照API文档要求设置参数
    const params = {
      pageNum: reviewListPagination.pageNum,
      pageSize: reviewListPagination.pageSize
    }
    
    // 只有当有选择房型时才添加房型筛选参数
    if (roomTypeFilter.value) {
      params.roomTypeId = roomTypeFilter.value
    }
    
    console.log('获取评价列表请求参数:', params)
    const res = await getReviewList(params)
    console.log('获取评价列表响应:', res)
    
    if (res.code === 200 && res.data) {
      // 处理返回数据
      const recordsList = res.data.list || []
      
      if (Array.isArray(recordsList)) {
        // 按照API文档中的字段进行处理
        reviewList.value = recordsList.map(item => ({
          id: item.id,
          content: item.content,
          score: item.score,
          isAnonymous: item.isAnonymous,
          roomTypeName: item.roomTypeName || '未知房型',
          orderId: item.orderId,
          createTime: item.createTime,
          images: Array.isArray(item.images) ? item.images : [],
          reply: item.reply || '',
          replyTime: item.replyTime || ''
        }))
        
        // 设置总数
        reviewListPagination.total = res.data.total || 0
        
        if (reviewList.value.length === 0) {
          ElMessage.info('暂无评价记录')
        }
      } else {
        console.warn('返回的评价列表不是数组:', res.data)
        reviewList.value = []
        reviewListPagination.total = 0
        ElMessage.info('暂无评价记录')
      }
    } else {
      ElMessage.error(res.message || '获取评价列表失败')
      reviewList.value = []
      reviewListPagination.total = 0
    }
  } catch (error) {
    console.error('获取评价列表错误:', error)
    ElMessage.error('获取评价列表失败，请稍后再试')
    reviewList.value = []
    reviewListPagination.total = 0
  } finally {
    reviewListLoading.value = false
  }
}

// 处理评价列表页码变化
const handleReviewListPageChange = (page) => {
  reviewListPagination.pageNum = page
  fetchReviewList()
}

// 处理评价列表每页条数变化
const handleReviewListSizeChange = (size) => {
  reviewListPagination.pageSize = size
  reviewListPagination.pageNum = 1
  fetchReviewList()
}

// 房型筛选变化
const handleRoomTypeFilterChange = () => {
  reviewListPagination.pageNum = 1
  fetchReviewList()
}

// 回复评论对话框
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')
const replying = ref(false)

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

// 处理分页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadGuestData()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadGuestData()
}

// 办理退房
const handleCheckout = (guest) => {
  if (guest && guest.roomNumber) {
    router.push(`/staff/checkout?roomNumber=${guest.roomNumber}`)
  } else {
    ElMessage.warning('该记录没有关联房间号，无法办理退房')
  }
}

// 初始化加载数据
onMounted(() => {
  // 不在页面初始加载时调用loadGuestData
})
</script>

<template>
  <div class="guest-management">
    <!-- 页面主体 -->
    <div class="page-content">
      <h2>客人入住记录</h2>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-card>
          <div class="filter-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-input
                  v-model="filterForm.searchQuery"
                  placeholder="请输入客人身份证号"
                  clearable
                >
                  <template #prefix>
                    <el-icon><search /></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="12">
                <el-button type="primary" @click="loadGuestData">搜索</el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <!-- 移除客人评价列表按钮 -->
      </div>

      <!-- 客人列表表格 -->
      <el-card class="table-card">
        <div v-if="tableData.length === 0 && !loading" class="empty-tip">
          <el-empty description="请输入客人身份证号码进行查询" />
        </div>
        
        <el-table
          :data="filteredTableData"
          style="width: 100%"
          border
          stripe
          :header-cell-style="{ background: '#f5f7fa' }"
          v-loading="loading"
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="orderNo" label="订单号" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="idCard" label="身份证号" width="180" />
          <el-table-column prop="roomNumber" label="房间号" width="80" />
          <el-table-column prop="roomTypeName" label="房型" width="150" />
          <el-table-column prop="checkInDate" label="入住时间" width="180" />
          <el-table-column prop="checkOutDate" label="退房时间" width="180" />
          <el-table-column prop="days" label="天数" width="80" />
          <el-table-column prop="totalAmount" label="消费金额" width="120">
            <template #default="scope">
              ¥{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag
                :type="
                  scope.row.status === '在住'
                    ? 'success'
                    : 'info'
                "
              >
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button size="small" type="info" @click="viewStayRecords(row)">入住记录</el-button>
              <el-button 
                size="small" 
                type="primary"
                @click="viewOrderDetail(row.orderId)"
              >
                订单详情
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                v-if="row.status === '在住'"
                @click="handleCheckout(row)"
              >
                办理退房
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页器 -->
        <div class="pagination-container">
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
      </el-card>
    </div>

    <!-- 对话框等内容保持不变 -->
    <!-- 客人详情对话框 -->
    <el-dialog
      v-model="guestDetailVisible"
      :title="currentGuest?.id ? '客人详情' : '客人详情'"
      width="50%"
    >
      <el-descriptions
        v-if="currentGuest"
        :column="2"
        border
      >
        <el-descriptions-item label="姓名">{{ currentGuest.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentGuest.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentGuest.idCard }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentGuest.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentGuest.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ currentGuest.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ currentGuest.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房时间">{{ currentGuest.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ currentGuest.days }}</el-descriptions-item>
        <el-descriptions-item label="消费金额">¥{{ currentGuest.totalAmount ? currentGuest.totalAmount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentGuest.status === '在住' ? 'success' : 'info'">
            {{ currentGuest.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="guestDetailVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            v-if="currentGuest && currentGuest.orderId"
            @click="viewOrderDetail(currentGuest.orderId)"
          >
            查看订单详情
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 入住记录对话框 -->
    <el-dialog
      v-model="stayRecordsVisible"
      title="客人入住记录"
      width="900px"
      destroy-on-close
    >
      <div v-loading="stayRecordsLoading">
        <el-table
          :data="stayRecords"
          style="width: 100%"
          border
          stripe
          v-if="stayRecords.length > 0"
        >
          <el-table-column prop="orderNo" label="订单号" width="120" />
          <el-table-column prop="roomNumber" label="房间号" width="100" />
          <el-table-column prop="roomTypeName" label="房型" width="150" />
          <el-table-column prop="checkInTime" label="入住时间" width="180" />
          <el-table-column prop="checkOutTime" label="退房时间" width="180" />
          <el-table-column prop="days" label="天数" width="80" />
          <el-table-column prop="totalAmount" label="消费金额" width="120">
            <template #default="{ row }">
              ¥{{ row.totalAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="viewOrderDetail(row.orderId)"
              >
                订单详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty
          v-else
          description="暂无入住记录"
        />
        
        <div class="pagination-container" v-if="stayRecords.length > 0">
          <el-pagination
            v-model:current-page="stayRecordsPagination.pageNum"
            v-model:page-size="stayRecordsPagination.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="stayRecordsPagination.total"
            @size-change="handleStayRecordsSizeChange"
            @current-change="handleStayRecordsPageChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 评价列表对话框 -->
    <el-dialog
      v-model="reviewListVisible"
      title="客人评价列表"
      width="1000px"
      destroy-on-close
    >
      <div class="filter-row" style="margin-bottom: 15px;">
        <el-select
          v-model="roomTypeFilter"
          placeholder="按房型筛选"
          clearable
          style="width: 200px;"
          @change="handleRoomTypeFilterChange"
        >
          <el-option
            v-for="item in roomTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      
      <div v-loading="reviewListLoading">
        <el-table
          :data="reviewList"
          style="width: 100%"
          border
          stripe
          v-if="reviewList.length > 0"
        >
          <el-table-column prop="roomTypeName" label="房型" width="150" />
          <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
          <el-table-column prop="score" label="评分" width="80">
            <template #default="{ row }">
              <el-rate
                v-model="row.score"
                disabled
                text-color="#ff9900"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评价时间" width="100" />
          <el-table-column prop="isAnonymous" label="匿名" width="80">
            <template #default="{ row }">
              {{ row.isAnonymous === 1 ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column prop="reply" label="回复" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.reply">{{ row.reply }}</span>
              <span v-else class="text-muted">暂无回复</span>
            </template>
          </el-table-column>
          <el-table-column prop="replyTime" label="回复时间" width="100" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="openReplyDialog(row)"
              >
                {{ row.reply ? '修改回复' : '回复' }}
              </el-button>
              <el-button
                size="small"
                type="info"
                @click="viewOrderDetail(row.orderId)"
              >
                查看订单
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty
          v-else
          description="暂无评价"
        />
        
        <div class="pagination-container" v-if="reviewList.length > 0">
          <el-pagination
            v-model:current-page="reviewListPagination.pageNum"
            v-model:page-size="reviewListPagination.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="reviewListPagination.total"
            @size-change="handleReviewListSizeChange"
            @current-change="handleReviewListPageChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 回复评论对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复客人评价"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentReview">
        <div class="review-info" style="margin-bottom: 20px;">
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
            <span class="review-value">
              <el-image
                v-for="(img, index) in currentReview.images"
                :key="index"
                :src="img"
                :preview-src-list="currentReview.images"
                fit="cover"
                style="width: 60px; height: 60px; margin-right: 5px;"
              />
            </span>
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
.guest-management {
  width: 100%;
  padding: 0 20px;
}

.page-content {
  width: 100%;
  margin-bottom: 30px;
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  position: relative;
  padding-bottom: 10px;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.filter-bar {
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 自适应响应式布局 */
@media (max-width: 1200px) {
  .page-content {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .filter-form .el-row {
    flex-direction: column;
  }
  
  .filter-form .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
}

.text-muted {
  color: #909399;
}

.review-info {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
}

.review-item {
  margin-bottom: 10px;
}

.review-label {
  font-weight: bold;
  margin-right: 10px;
}

.review-value {
  word-break: break-all;
}

.order-footer {
  flex-wrap: wrap;
}

.empty-tip {
  padding: 40px 0;
  text-align: center;
}

.cancel-dialog-content {
  padding: 10px 0;
}
</style> 