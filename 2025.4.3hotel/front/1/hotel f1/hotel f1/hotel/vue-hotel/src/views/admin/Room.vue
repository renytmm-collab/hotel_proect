<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoomList, addRoom, updateRoomStatus, batchAddRooms } from '@/api/room'
import { getRoomTypeNames } from '@/api/roomType'

// 搜索表单
const searchForm = ref({
  roomNumber: '',
  roomTypeId: '',
  floor: '',
  status: ''
})

// 房间表单
const roomForm = ref({
  id: null,
  roomNumber: '',
  roomTypeId: '',
  floor: '',
  status: 1
})

// 批量添加表单
const batchForm = ref({
  roomTypeId: '',
  floor: '',
  startNumber: '',
  count: 1
})

// 表格数据
const roomList = ref([])
const roomTypes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗控制
const showDialog = ref(false)
const showBatchDialog = ref(false)
const isEdit = ref(false)
const selectedRoom = ref({})

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑房间' : '添加房间'
})

// 状态文本转换
const statusText = (status) => {
  const statusMap = {
    1: '空闲',
    2: '已入住',
    3: '清洁中',
    4: '维修中',
    5: '下架'
  }
  return statusMap[status] || status
}

// 获取房间类型数据
const fetchRoomTypes = async () => {
  try {
    const res = await getRoomTypeNames()
    if (res.code === 200) {
      roomTypes.value = res.data
    }
  } catch (error) {
    console.error('获取房间类型失败:', error)
    ElMessage.error('获取房间类型失败')
  }
}

// 获取房间列表
const fetchRooms = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm.value
    }
    const res = await getRoomList(params)
    if (res.code === 200) {
      roomList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取房间列表失败')
    }
  } catch (error) {
    console.error('获取房间列表失败:', error)
    ElMessage.error('获取房间列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索房间
const searchRooms = () => {
  currentPage.value = 1
  fetchRooms()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    roomNumber: '',
    roomTypeId: '',
    floor: '',
    status: ''
  }
  currentPage.value = 1
  fetchRooms()
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  fetchRooms()
}

// 打开添加弹窗
const openAddDialog = () => {
  isEdit.value = false
  roomForm.value = {
    id: null,
    roomNumber: '',
    roomTypeId: roomTypes.value && roomTypes.value.length > 0 ? roomTypes.value[0].id : '',
    floor: '',
    status: 1
  }
  showDialog.value = true
}

// 打开批量添加弹窗
const openBatchDialog = () => {
  batchForm.value = {
    roomTypeId: roomTypes.value && roomTypes.value.length > 0 ? roomTypes.value[0].id : '',
    floor: '',
    startNumber: '',
    count: 1
  }
  showBatchDialog.value = true
}

// 修改房间状态
const updateStatus = async (room, newStatus) => {
  try {
    const res = await updateRoomStatus({
      id: room.id,
      status: newStatus
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      fetchRooms()
    } else {
      ElMessage.error(res.message || '状态更新失败')
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 提交房间表单
const submitRoomForm = async () => {
  try {
    const res = await addRoom(roomForm.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showDialog.value = false
      fetchRooms()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    console.error('添加房间失败:', error)
    ElMessage.error('添加房间失败')
  }
}

// 提交批量添加表单
const submitBatchForm = async () => {
  try {
    const res = await batchAddRooms(batchForm.value)
    if (res.code === 200) {
      ElMessage.success('批量添加成功')
      showBatchDialog.value = false
      fetchRooms()
    } else {
      ElMessage.error(res.message || '批量添加失败')
    }
  } catch (error) {
    console.error('批量添加失败:', error)
    ElMessage.error('批量添加失败')
  }
}

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}

// 生命周期钩子
onMounted(() => {
  fetchRoomTypes()
  fetchRooms()
})
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">房间管理</h1>
    <div class="breadcrumb">
      <span @click="goToHome">首页</span> / <span>房间管理</span>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="bg-white p-4 mb-4 rounded shadow">
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">房间号</label>
          <input v-model="searchForm.roomNumber" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入房间号" />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">房间类型</label>
          <select v-model="searchForm.roomTypeId" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option v-for="type in roomTypes" :key="type.id" :value="type.id">
              {{ type.typeName }}
            </option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">楼层</label>
          <input v-model="searchForm.floor" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入楼层" />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">状态</label>
          <select v-model="searchForm.status" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option value="1">空闲</option>
            <option value="2">已入住</option>
            <option value="3">清洁中</option>
            <option value="4">维修中</option>
            <option value="5">下架</option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button @click="searchRooms" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            搜索
          </button>
          <button @click="resetSearch" class="ml-2 px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
            重置
          </button>
          <button @click="openAddDialog" class="ml-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">
            添加房间
          </button>
          <button @click="openBatchDialog" class="ml-2 px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700">
            批量添加
          </button>
        </div>
      </div>
    </div>

    <!-- 房间表格 -->
    <div class="bg-white p-4 rounded shadow overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              房间号
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              房间类型
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              楼层
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              状态
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              创建时间
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              操作
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="room in roomList" :key="room.id">
            <td class="px-6 py-4 whitespace-nowrap">{{ room.roomNumber }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ room.roomTypeName }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ room.floor }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="{
                'px-2 py-1 text-xs rounded-full': true,
                'bg-green-100 text-green-800': Number(room.status) === 1,
                'bg-red-100 text-red-800': Number(room.status) === 2,
                'bg-yellow-100 text-yellow-800': Number(room.status) === 3,
                'bg-gray-100 text-gray-800': Number(room.status) === 4,
                'bg-gray-300 text-gray-800': Number(room.status) === 5
              }">
                {{ statusText(room.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">{{ room.createTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <select v-model="room.status" @change="updateStatus(room, $event.target.value)"
                class="px-2 py-1 border border-gray-300 rounded-md">
                <option value="1">空闲</option>
                <option value="2">已入住</option>
                <option value="3">清洁中</option>
                <option value="4">维修中</option>
                <option value="5">下架</option>
              </select>
            </td>
          </tr>
          <tr v-if="!roomList || roomList.length === 0">
            <td colspan="6" class="px-6 py-4 text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="flex flex-col items-center mt-4">
        <div class="mb-2">共 {{ total }} 条</div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchRooms"
          @current-change="fetchRooms"
        />
      </div>
    </div>

    <!-- 添加房间弹窗 -->
    <div v-if="showDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">{{ dialogTitle }}</h2>
        <form @submit.prevent="submitRoomForm">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">房间号 <span class="text-red-500">*</span></label>
            <input v-model="roomForm.roomNumber" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
              required />
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">房间类型 <span class="text-red-500">*</span></label>
            <select v-model="roomForm.roomTypeId" class="w-full px-3 py-2 border border-gray-300 rounded-md" required>
              <option v-for="type in roomTypes" :key="type.id" :value="type.id">
                {{ type.typeName }}
              </option>
            </select>
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">楼层 <span class="text-red-500">*</span></label>
            <input v-model="roomForm.floor" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
              required />
          </div>
          <div class="flex justify-end space-x-2">
            <button type="button" @click="showDialog = false"
              class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
              取消
            </button>
            <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
              确认
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 批量添加弹窗 -->
    <div v-if="showBatchDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">批量添加房间</h2>
        <form @submit.prevent="submitBatchForm">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">房间类型 <span class="text-red-500">*</span></label>
            <select v-model="batchForm.roomTypeId" class="w-full px-3 py-2 border border-gray-300 rounded-md" required>
              <option v-for="type in roomTypes" :key="type.id" :value="type.id">
                {{ type.typeName }}
              </option>
            </select>
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">楼层 <span class="text-red-500">*</span></label>
            <input v-model="batchForm.floor" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
              required />
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">起始房间号 <span class="text-red-500">*</span></label>
            <input v-model="batchForm.startNumber" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
              required />
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">添加数量 <span class="text-red-500">*</span></label>
            <input v-model.number="batchForm.count" type="number" min="1" class="w-full px-3 py-2 border border-gray-300 rounded-md"
              required />
          </div>
          <div class="flex justify-end space-x-2">
            <button type="button" @click="showBatchDialog = false"
              class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
              取消
            </button>
            <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
              确认
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.breadcrumb {
  font-size: 14px;
  color: #666;
}

.breadcrumb span {
  cursor: pointer;
}

.breadcrumb span:hover {
  color: #409EFF;
}

.el-pagination {
  justify-content: center;
}
</style>