<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomList, addRoom, updateRoomStatus, batchAddRooms } from '@/api/room'
import { getRoomTypeList } from '@/api/roomType'

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  roomTypeId: '',
  floor: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 房间表单
const roomForm = reactive({
  roomNumber: '',
  roomTypeId: '',
  floor: ''
})

// 批量添加表单
const batchForm = reactive({
  roomTypeId: '',
  floor: '',
  startNumber: '',
  count: 10
})

// 状态修改表单
const statusForm = reactive({
  id: '',
  status: 0
})

// 房间列表
const roomList = ref([])
const total = ref(0)

// 房型列表
const roomTypeList = ref([])

// 对话框控制
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const statusDialogVisible = ref(false)

// 当前选中的房间
const currentRoom = ref(null)

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 获取房间列表
const fetchRoomList = async () => {
  loading.value = true
  try {
    const res = await getRoomList(searchForm)
    if (res.code === 200) {
      roomList.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取房间列表失败')
    }
  } catch (error) {
    console.error('获取房间列表错误:', error)
    ElMessage.error('获取房间列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 获取房型列表
const fetchRoomTypeList = async () => {
  try {
    const res = await getRoomTypeList({
      pageNum: 1,
      pageSize: 100,
      status: 1 // 只获取启用的房型
    })
    if (res.code === 200) {
      roomTypeList.value = res.data.list
    } else {
      ElMessage.error(res.message || '获取房型列表失败')
    }
  } catch (error) {
    console.error('获取房型列表错误:', error)
    ElMessage.error('获取房型列表失败，请稍后再试')
  }
}

// 处理搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchRoomList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.roomNumber = ''
  searchForm.roomTypeId = ''
  searchForm.floor = ''
  searchForm.status = ''
  searchForm.pageNum = 1
  fetchRoomList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchRoomList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  searchForm.pageSize = val
  searchForm.pageNum = 1
  fetchRoomList()
}

// 打开添加对话框
const openAddDialog = () => {
  roomForm.roomNumber = ''
  roomForm.roomTypeId = ''
  roomForm.floor = ''
  dialogVisible.value = true
}

// 打开批量添加对话框
const openBatchDialog = () => {
  batchForm.roomTypeId = ''
  batchForm.floor = ''
  batchForm.startNumber = ''
  batchForm.count = 10
  batchDialogVisible.value = true
}

// 打开状态修改对话框
const openStatusDialog = (row) => {
  currentRoom.value = row
  statusForm.id = row.id
  statusForm.status = row.status
  statusDialogVisible.value = true
}

// 提交房间表单
const submitRoomForm = async () => {
  if (!roomForm.roomNumber) {
    ElMessage.warning('请输入房间号')
    return
  }
  if (!roomForm.roomTypeId) {
    ElMessage.warning('请选择房型')
    return
  }
  if (!roomForm.floor) {
    ElMessage.warning('请输入楼层')
    return
  }
  
  submitting.value = true
  try {
    const res = await addRoom(roomForm)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      fetchRoomList()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    console.error('添加房间错误:', error)
    ElMessage.error('添加失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 提交批量添加表单
const submitBatchForm = async () => {
  if (!batchForm.roomTypeId) {
    ElMessage.warning('请选择房型')
    return
  }
  if (!batchForm.floor) {
    ElMessage.warning('请输入楼层')
    return
  }
  if (!batchForm.startNumber) {
    ElMessage.warning('请输入起始房间号')
    return
  }
  if (!batchForm.count || batchForm.count <= 0) {
    ElMessage.warning('请输入有效的添加数量')
    return
  }
  
  submitting.value = true
  try {
    const res = await batchAddRooms(batchForm)
    if (res.code === 200) {
      ElMessage.success('批量添加成功')
      batchDialogVisible.value = false
      fetchRoomList()
    } else {
      ElMessage.error(res.message || '批量添加失败')
    }
  } catch (error) {
    console.error('批量添加房间错误:', error)
    ElMessage.error('批量添加失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 提交状态修改
const submitStatusForm = async () => {
  submitting.value = true
  try {
    const res = await updateRoomStatus(statusForm)
    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      
      // 更新本地列表中的状态
      const index = roomList.value.findIndex(room => room.id === statusForm.id)
      if (index !== -1) {
        roomList.value[index].status = statusForm.status
      }
    } else {
      ElMessage.error(res.message || '状态修改失败')
    }
  } catch (error) {
    console.error('修改房间状态错误:', error)
    ElMessage.error('状态修改失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 获取房型名称
const getRoomTypeName = (roomTypeId) => {
  const roomType = roomTypeList.value.find(item => item.id === roomTypeId)
  return roomType ? roomType.typeName : ''
}

// 格式化状态
const statusText = (status) => {
  switch (Number(status)) {
    case 0: return '正常'
    case 1: return '已入住'
    case 2: return '清洁中'
    case 3: return '维修中'
    case 4: return '下架'
    default: return '未知'
  }
}

// 获取状态标签类型
const statusType = (status) => {
  switch (Number(status)) {
    case 0: return 'success'
    case 1: return 'primary'
    case 2: return 'warning'
    case 3: return 'danger'
    case 4: return 'info'
    default: return ''
  }
}

// 组件加载时获取数据
onMounted(() => {
  fetchRoomList()
  fetchRoomTypeList()
})
</script>

<template>
  <div class="room-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">房间管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="searchForm.roomTypeId" placeholder="请选择房型" clearable>
            <el-option
              v-for="item in roomTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="searchForm.floor" placeholder="请输入楼层" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option :value="0" label="正常" />
            <el-option :value="1" label="已入住" />
            <el-option :value="2" label="清洁中" />
            <el-option :value="3" label="维修中" />
            <el-option :value="4" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAddDialog">新增房间</el-button>
          <el-button type="warning" @click="openBatchDialog">批量添加</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="roomList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roomNumber" label="房间号" width="120" />
        <el-table-column label="房型" width="150">
          <template #default="scope">
            {{ getRoomTypeName(scope.row.roomTypeId) || scope.row.roomTypeName }}
          </template>
        </el-table-column>
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openStatusDialog(scope.row)"
            >
              修改状态
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加房间对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加房间"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="roomForm"
        label-width="100px"
      >
        <el-form-item label="房间号" required>
          <el-input 
            v-model="roomForm.roomNumber" 
            placeholder="请输入房间号"
          />
        </el-form-item>
        <el-form-item label="房型" required>
          <el-select 
            v-model="roomForm.roomTypeId" 
            placeholder="请选择房型"
            style="width: 100%"
          >
            <el-option
              v-for="item in roomTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" required>
          <el-input 
            v-model="roomForm.floor" 
            placeholder="请输入楼层"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitRoomForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量添加对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量添加房间"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="batchForm"
        label-width="120px"
      >
        <el-form-item label="房型" required>
          <el-select 
            v-model="batchForm.roomTypeId" 
            placeholder="请选择房型"
            style="width: 100%"
          >
            <el-option
              v-for="item in roomTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" required>
          <el-input 
            v-model="batchForm.floor" 
            placeholder="请输入楼层"
          />
        </el-form-item>
        <el-form-item label="起始房间号" required>
          <el-input 
            v-model="batchForm.startNumber" 
            placeholder="例如：101"
          />
        </el-form-item>
        <el-form-item label="添加数量" required>
          <el-input-number 
            v-model="batchForm.count" 
            :min="1" 
            :max="100" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitBatchForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改房间状态"
      width="400px"
      destroy-on-close
    >
      <div v-if="currentRoom" class="status-dialog-content">
        <p>房间号：<strong>{{ currentRoom.roomNumber }}</strong></p>
        <p>当前状态：<el-tag :type="statusType(currentRoom.status)">{{ statusText(currentRoom.status) }}</el-tag></p>
        
        <el-form
          :model="statusForm"
          label-width="100px"
        >
          <el-form-item label="新状态" required>
            <el-select 
              v-model="statusForm.status" 
              placeholder="请选择状态"
              style="width: 100%"
            >
              <el-option :value="0" label="正常" />
              <el-option :value="2" label="清洁中" />
              <el-option :value="3" label="维修中" />
              <el-option :value="4" label="下架" />
              <el-option :value="1" label="已入住" disabled />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitStatusForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.room-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.status-dialog-content {
  padding: 10px 0;
}

.status-dialog-content p {
  margin: 10px 0;
}
</style> 