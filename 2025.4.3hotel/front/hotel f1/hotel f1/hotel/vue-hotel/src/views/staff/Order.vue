<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElButton } from 'element-plus'
import { getOrderList, getOrderDetail, updateOrder, cancelOrder, getRoomTypeNames } from '@/api/reception'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const orderDetailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentOrder = ref(null)
const showDirectOrderDetail = ref(false)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  guestName: '',
  guestPhone: '',
  guestIdCard: '',
  status: '',
  checkInDate: '',
})

// 编辑表单
const editForm = reactive({
  orderId: 0,
  roomTypeId: 0,
  checkInDate: '',
  checkOutDate: '',
  guestName: '',
  guestPhone: '',
  guestIdCard: ''
})

// 编辑表单规则
const editRules = {
  guestName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  guestIdCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }]
}

// 订单列表
const orderList = ref([])

// 订单状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 1, label: '待支付' },
  { value: 2, label: '已支付待入住' },
  { value: 3, label: '已入住' },
  { value: 4, label: '已完成' }
]

// 房型选项（从API获取）
const roomTypeOptions = ref([])

// 获取房型选项列表
const fetchRoomTypeOptions = async () => {
  try {
    const res = await getRoomTypeNames()
    if (res.code === 200) {
      // 将API返回的数据转换为下拉框选项格式
      if (Array.isArray(res.data)) {
        // 处理数组格式的返回数据
        roomTypeOptions.value = res.data.map(item => ({
          value: parseInt(item.id),
          label: item.typeName
        }))
      } else if (res.data && typeof res.data === 'object') {
        // 处理单对象格式的返回数据
        if (res.data.typeName) {
          roomTypeOptions.value = [
            { value: parseInt(res.data.id), label: res.data.typeName }
          ]
        } else if (Array.isArray(res.data.list)) {
          // 处理可能包含在list字段中的数据
          roomTypeOptions.value = res.data.list.map(item => ({
            value: parseInt(item.id),
            label: item.typeName
          }))
        } else if (Array.isArray(res.data.records)) {
          // 处理可能包含在records字段中的数据
          roomTypeOptions.value = res.data.records.map(item => ({
            value: parseInt(item.id),
            label: item.typeName
          }))
        }
      }
      
      console.log('获取的房型选项:', roomTypeOptions.value)
      
      // 如果没有获取到数据，使用备用选项
      if (roomTypeOptions.value.length === 0) {
        console.warn('API没有返回有效的房型数据，使用备用选项')
        roomTypeOptions.value = [
          { value: 1, label: '豪华大床房' },
          { value: 2, label: '标准大床房' },
          { value: 3, label: '商务双床房' },
          { value: 4, label: '行政套房' }
        ]
      }
    } else {
      console.error('获取房型数据失败:', res.message || res.msg || '未知错误')
      ElMessage.error('获取房型数据失败，使用默认选项')
      // 使用备用选项
      roomTypeOptions.value = [
        { value: 1, label: '豪华大床房' },
        { value: 2, label: '标准大床房' },
        { value: 3, label: '商务双床房' },
        { value: 4, label: '行政套房' }
      ]
    }
  } catch (error) {
    console.error('获取房型选项错误:', error)
    ElMessage.error('获取房型选项失败，使用默认选项')
    // 使用备用选项
    roomTypeOptions.value = [
      { value: 1, label: '豪华大床房' },
      { value: 2, label: '标准大床房' },
      { value: 3, label: '商务双床房' },
      { value: 4, label: '行政套房' }
    ]
  }
}

// 表格列配置
const tableColumns = [
  { prop: 'orderNo', label: '订单号', width: '160' },
  { prop: 'guestName', label: '客户姓名', width: '120' },
  { prop: 'guestPhone', label: '联系电话', width: '140' },
  { prop: 'roomTypeName', label: '房型', width: '120' },
  { prop: 'checkInDate', label: '入住日期', width: '120' },
  { prop: 'checkOutDate', label: '离店日期', width: '120' },
  { prop: 'days', label: '天数', width: '80' },
  { prop: 'totalAmount', label: '金额', width: '100' },
  { prop: 'status', label: '状态', width: '100' }
]

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单引用
const editFormRef = ref(null)

// 直接通过ID获取订单详情
const fetchOrderById = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    
    if (res.code === 200) {
      currentOrder.value = res.data
      showDirectOrderDetail.value = true
    } else {
      ElMessage.error(res.message || res.msg || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情错误:', error)
    ElMessage.error('获取订单详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 获取房型选项列表
  fetchRoomTypeOptions()
  
  // 检查URL参数中是否有订单ID
  const orderId = route.query.orderId
  if (orderId) {
    // 如果有订单ID参数，直接获取订单详情
    fetchOrderById(orderId)
  } else {
    // 否则加载订单列表
    fetchOrderList()
  }
})

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    // 构造查询参数
    const params = {
      orderNo: searchForm.orderNo,
      guestName: searchForm.guestName,
      guestPhone: searchForm.guestPhone,
      guestIdCard: searchForm.guestIdCard,
      status: searchForm.status,
      checkInDate: searchForm.checkInDate, // 已经通过el-date-picker的value-format属性格式化为YYYY-MM-DD
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    // 打印参数方便调试
    console.log('发送查询参数:', params);
    
    const res = await getOrderList(params)
    console.log('订单列表响应数据:', res)
    
    if (res.code === 200 && res.data) {
      // 兼容处理返回的数据格式，支持list和records字段
      const ordersData = res.data.list || res.data.records || []
      orderList.value = ordersData
      pagination.total = res.data.total || 0
      
      console.log('处理后的订单数据:', orderList.value)
    } else {
      ElMessage.error(res.msg || res.message || '获取订单列表失败')
      orderList.value = []
    }
  } catch (error) {
    console.error('获取订单列表错误:', error)
    ElMessage.error('获取订单列表失败，请稍后再试')
    orderList.value = []
  } finally {
    loading.value = false
  }
}

// 搜索订单
const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrderList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.guestName = ''
  searchForm.guestPhone = ''
  searchForm.guestIdCard = ''
  searchForm.status = ''
  searchForm.checkInDate = ''
  
  // 重置分页并搜索
  pagination.currentPage = 1
  fetchOrderList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchOrderList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchOrderList()
}

// 查看订单详情
const viewOrderDetail = async (row) => {
  loading.value = true
  try {
    const res = await getOrderDetail(row.id)
    
    if (res.code === 200) {
      currentOrder.value = res.data
      orderDetailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情错误:', error)
    ElMessage.error('获取订单详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = async (row) => {
  loading.value = true
  try {
    const res = await getOrderDetail(row.id)
    
    if (res.code === 200) {
      const orderData = res.data
      
      // 填充表单数据
      editForm.orderId = orderData.id
      editForm.roomTypeId = orderData.roomTypeId
      editForm.checkInDate = orderData.checkInDate
      editForm.checkOutDate = orderData.checkOutDate
      editForm.guestName = orderData.guestName
      editForm.guestPhone = orderData.guestPhone
      editForm.guestIdCard = orderData.guestIdCard
      
      editDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情错误:', error)
    ElMessage.error('获取订单详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await updateOrder(editForm)
        
        if (res.code === 200) {
          ElMessage.success('修改预订信息成功')
          editDialogVisible.value = false
          fetchOrderList() // 刷新列表
        } else {
          ElMessage.error(res.message || '修改预订信息失败')
        }
      } catch (error) {
        console.error('修改预订信息错误:', error)
        ElMessage.error('修改预订信息失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 取消订单
const handleCancelOrder = (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消预订', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '请输入取消原因'
  }).then(async ({ value }) => {
    loading.value = true
    try {
      const data = {
        orderId: row.id,
        cancelReason: value
      }
      
      const res = await cancelOrder(data)
      
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        fetchOrderList() // 刷新列表
      } else {
        ElMessage.error(res.message || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单错误:', error)
      ElMessage.error('取消订单失败，请稍后再试')
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

// 办理入住
const checkIn = (row) => {
  router.push(`/staff/checkin?orderId=${row.id}`)
}

// 获取订单状态的标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'info'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'success'
    case 5: return 'danger'
    default: return 'info'
  }
}

// 获取订单状态的文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已取消'
    case 1: return '待支付'
    case 2: return '已支付待入住'
    case 3: return '已入住'
    case 4: return '已完成'
    case 5: return '已取消'
    default: return '未知状态'
  }
}

// 注销
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录信息
    localStorage.removeItem('staffLoggedIn')
    localStorage.removeItem('staffUsername')
    localStorage.removeItem('staffRole')
    
    // 跳转到登录页
    router.push('/staff/login')
    ElMessage.success('已退出登录')
  }).catch(() => {
    // 取消操作
  })
}

// 跳转到创建预订页面
const goToCreateOrder = () => {
  router.push('/staff/orders')
}
</script>

<template>
  <div class="staff-order-container">
    <!-- 直接显示订单详情 -->
    <template v-if="showDirectOrderDetail">
      <div class="page-header">
        <h2>订单详情</h2>
        <div class="header-actions">
          <el-button type="primary" @click="router.push('/staff/orders')">返回订单列表</el-button>
        </div>
      </div>
      
      <el-card v-if="currentOrder" class="order-detail-card">
        <div class="order-detail">
          <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">订单号:</span>
                <span class="value">{{ currentOrder.orderNo }}</span>
              </div>
              <div class="detail-item">
                <span class="label">状态:</span>
                <span class="value">
                  <el-tag :type="getStatusTagType(currentOrder.status)">
                    {{ getStatusText(currentOrder.status) }}
                  </el-tag>
                </span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">房型:</span>
                <span class="value">{{ currentOrder.roomTypeName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房价:</span>
                <span class="value">¥{{ currentOrder.roomPrice?.toFixed(2) }}</span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">入住日期:</span>
                <span class="value">{{ currentOrder.checkInDate }}</span>
              </div>
              <div class="detail-item">
                <span class="label">离店日期:</span>
                <span class="value">{{ currentOrder.checkOutDate }}</span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">住宿天数:</span>
                <span class="value">{{ currentOrder.days }}天</span>
              </div>
              <div class="detail-item">
                <span class="label">总金额:</span>
                <span class="value">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>客人信息</h3>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">姓名:</span>
                <span class="value">{{ currentOrder.guestName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">电话:</span>
                <span class="value">{{ currentOrder.guestPhone }}</span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">证件号:</span>
                <span class="value">{{ currentOrder.guestIdCard }}</span>
              </div>
              <div class="detail-item">
                <span class="label">用户:</span>
                <span class="value">{{ currentOrder.username }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>备注信息</h3>
            <div class="detail-row">
              <div class="detail-item">
                <span class="label">客人备注:</span>
                <span class="value">{{ currentOrder.remark || '无' }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section" v-if="currentOrder.status === 1 || currentOrder.status === 2">
            <div class="action-buttons">
              <el-button type="primary" @click="openEditDialog(currentOrder)">修改订单</el-button>
              <el-button 
                type="primary" 
                v-if="currentOrder.status === 2"
                @click="checkIn(currentOrder)"
              >
                办理入住
              </el-button>
              <el-button 
                type="danger" 
                @click="handleCancelOrder(currentOrder)"
              >
                取消订单
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </template>
    
    <!-- 订单列表页面 -->
    <template v-else>
      <div class="page-header">
        <h2>订单管理</h2>
        <div class="header-actions">
          <!-- 移除新建预订按钮 -->
        </div>
      </div>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable></el-input>
          </el-form-item>
          <el-form-item label="客户姓名">
            <el-input v-model="searchForm.guestName" placeholder="请输入客户姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="searchForm.guestPhone" placeholder="请输入联系电话" clearable></el-input>
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="searchForm.guestIdCard" placeholder="请输入身份证号" clearable></el-input>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select 
              v-model="searchForm.status" 
              placeholder="请选择状态" 
              clearable 
              style="width: 160px;" 
              popper-class="status-select-dropdown"
              placement="bottom-start"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="入住日期">
            <el-date-picker 
              v-model="searchForm.checkInDate" 
              type="date" 
              placeholder="选择入住日期" 
              clearable
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 订单列表 -->
      <div v-loading="loading">
        <el-table :data="orderList" style="width: 100%" border stripe>
          <el-table-column
            v-for="column in tableColumns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
          >
            <!-- 订单状态列的自定义渲染 -->
            <template v-if="column.prop === 'status'" #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
            
            <!-- 金额列的自定义渲染 -->
            <template v-if="column.prop === 'totalAmount'" #default="{ row }">
              ¥{{ row.totalAmount.toFixed(2) }}
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" fixed="right" width="250">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="viewOrderDetail(row)"
              >
                详情
              </el-button>
              
              <el-button
                size="small"
                type="primary"
                v-if="row.status === 1 || row.status === 2"
                @click="openEditDialog(row)"
              >
                修改
              </el-button>
              
              <el-button
                size="small"
                type="primary"
                v-if="row.status === 2"
                @click="checkIn(row)"
              >
                办理入住
              </el-button>
              
              <el-button
                size="small"
                type="danger"
                v-if="row.status === 1 || row.status === 2"
                @click="handleCancelOrder(row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </template>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="orderDetailDialogVisible"
      title="订单详情"
      width="650px"
    >
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">订单号:</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态:</span>
              <span class="value">
                <el-tag :type="getStatusTagType(currentOrder.status)">
                  {{ getStatusText(currentOrder.status) }}
                </el-tag>
              </span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">房型:</span>
              <span class="value">{{ currentOrder.roomTypeName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">房价:</span>
              <span class="value">¥{{ currentOrder.roomPrice?.toFixed(2) }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">入住日期:</span>
              <span class="value">{{ currentOrder.checkInDate }}</span>
            </div>
            <div class="detail-item">
              <span class="label">离店日期:</span>
              <span class="value">{{ currentOrder.checkOutDate }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">住宿天数:</span>
              <span class="value">{{ currentOrder.days }}天</span>
            </div>
            <div class="detail-item">
              <span class="label">总金额:</span>
              <span class="value">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>客人信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">姓名:</span>
              <span class="value">{{ currentOrder.guestName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">电话:</span>
              <span class="value">{{ currentOrder.guestPhone }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">证件号:</span>
              <span class="value">{{ currentOrder.guestIdCard }}</span>
            </div>
            <div class="detail-item">
              <span class="label">用户:</span>
              <span class="value">{{ currentOrder.username }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>支付信息</h3>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ currentOrder.paymentMethod || '未支付' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">支付时间:</span>
              <span class="value">{{ currentOrder.paymentTime || '未支付' }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item">
              <span class="label">交易号:</span>
              <span class="value">{{ currentOrder.paymentNo || '无' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">下单时间:</span>
              <span class="value">{{ currentOrder.createTime }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="orderDetailDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            v-if="currentOrder && (currentOrder.status === 2)"
            @click="checkIn(currentOrder)"
          >
            办理入住
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 编辑订单对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改预订信息"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="editForm.roomTypeId" placeholder="请选择房型">
            <el-option
              v-for="item in roomTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="editForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker
            v-model="editForm.checkOutDate"
            type="date"
            placeholder="选择离店日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="editForm.guestName" placeholder="请输入入住人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="guestPhone">
          <el-input v-model="editForm.guestPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="guestIdCard">
          <el-input v-model="editForm.guestIdCard" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确认修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style>
/* 全局样式，不使用scoped */
:deep(.status-select-dropdown) {
  min-width: 160px !important;
  margin-top: 5px !important;
}
</style>

<style scoped>
.staff-order-container {
  width: 100%;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #f0f2f5;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  position: relative;
  display: inline-block;
  padding-bottom: 10px;
}

.page-header h2::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #409EFF;
  border-radius: 2px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail-card {
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.order-detail .detail-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.order-detail .detail-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #409EFF;
  font-weight: 600;
}

.order-detail .detail-row {
  display: flex;
  margin-bottom: 12px;
}

.order-detail .detail-item {
  flex: 1;
  min-width: 0;
}

.order-detail .label {
  color: #606266;
  margin-right: 8px;
  font-size: 14px;
}

.order-detail .value {
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .order-detail .detail-row {
    flex-direction: column;
  }
  
  .order-detail .detail-item {
    margin-bottom: 8px;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
  }
}
</style> 