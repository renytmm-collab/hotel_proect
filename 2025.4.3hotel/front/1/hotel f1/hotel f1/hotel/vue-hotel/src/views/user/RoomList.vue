<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserRoomTypeList, getRoomTypeNames } from '@/api/roomType'

const router = useRouter()
const loading = ref(false)
const rooms = ref([])
const roomTypeOptions = ref([
  { label: '全部', value: '' }
])
const selectedRoomType = ref('')
const priceRange = ref([0, 2000])
const maxGuestsOptions = ref([
  { label: '不限', value: '' },
  { label: '1人', value: 1 },
  { label: '2人', value: 2 },
  { label: '3人', value: 3 },
  { label: '4人', value: 4 }
])
const selectedMaxGuests = ref('')
const selectedRoomTypeId = ref(null)

// 过滤条件
const filter = reactive({
  minPrice: 0,
  maxPrice: 2000,
  maxGuests: '',
  roomTypeId: null
})

// 分页相关
const pagination = reactive({
  pageNum: 1,
  pageSize: 5,  // 修改默认每页显示5条
  total: 0
})

// 模拟房型数据（作为备用数据）
const mockRoomTypes = [
  {
    id: 1,
    name: '标准单人间',
    image: 'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 258,
    discount: 0.8,
    size: '18平方米',
    bedType: '单人床',
    capacity: 1,
    maxGuests: 1,
    description: '舒适的单人间，配备基本设施，适合商务出行或单人旅游。',
    facilities: ['WiFi', '电视', '空调'],
    area: 18,
    floor: '2-6',
    totalRooms: 20,
    breakfast: false,
    window: true,
    wifi: true
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
    maxGuests: 2,
    description: '实用的双床房，配有两张单人床，简约舒适，适合商务或结伴出行。',
    facilities: ['WiFi', '电视', '空调', '电热水壶'],
    area: 25,
    floor: '2-8',
    totalRooms: 30,
    breakfast: false,
    window: true,
    wifi: true
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
    maxGuests: 2,
    description: '宽敞明亮的客房配备特大号床，提供舒适的睡眠体验，配有现代化设施。',
    facilities: ['WiFi', '电视', '空调', '电热水壶', '冰箱', '早餐'],
    area: 30,
    floor: '5-12',
    totalRooms: 25,
    breakfast: true,
    window: true,
    wifi: true
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
    maxGuests: 2,
    description: '豪华行政套房，配有独立起居室和卧室，享有城市美景，提供高端服务。',
    facilities: ['WiFi', '电视', '空调', '冰箱', '浴缸', '吹风机', 'minibar', '书桌', '保险箱', '熨斗'],
    area: 45,
    floor: '10-15',
    totalRooms: 15,
    breakfast: true,
    window: true,
    wifi: true
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
    maxGuests: 3,
    description: '适合家庭入住的亲子房，配有1张大床和1张儿童床，有儿童游乐区和舒适的休息空间。',
    facilities: ['WiFi', '电视', '空调', '冰箱', '淋浴', '吹风机'],
    area: 40,
    floor: '3-6',
    totalRooms: 10,
    breakfast: true,
    window: true,
    wifi: true
  }
]

// 获取房型列表
const fetchRoomTypes = async () => {
  loading.value = true
  try {
    const res = await getUserRoomTypeList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      minPrice: filter.minPrice,
      maxPrice: filter.maxPrice,
      maxGuests: filter.maxGuests,
      roomTypeId: filter.roomTypeId
    })
    
    if (res.code === 200) {
      // 后端实际返回数据结构: {code:200, msg:null, data:{total:7, records:[...]}}
      // 处理两种可能的后端返回格式
      if (res.data) {
        let roomList = []
        let totalCount = 0
        
        // 情况1：数据在 res.data.list 中
        if (res.data.list && Array.isArray(res.data.list)) {
          roomList = res.data.list
          totalCount = res.data.total || roomList.length
        } 
        // 情况2：数据在 res.data.records 中
        else if (res.data.records && Array.isArray(res.data.records)) {
          roomList = res.data.records
          totalCount = res.data.total || roomList.length
        }
        // 情况3：数据直接在 res.data 中且为数组
        else if (Array.isArray(res.data)) {
          roomList = res.data
          totalCount = roomList.length
        }
        // 情况4：没有找到有效数据
        else {
          roomList = []
          totalCount = 0
        }
        
        if (roomList.length > 0) {
          rooms.value = roomList.map(item => {
            const roomData = {
              id: item.id,
              name: item.typeName,  // 接口返回的是typeName
              image: item.mainImage || null, // 接口返回的是mainImage
              price: item.price || 0,
              description: item.description || '舒适的住宿环境，配有现代化设施',
              bedType: item.bedType || '标准床',
              maxGuests: item.maxGuests || 2,
              facilities: item.facilities ? (typeof item.facilities === 'string' ? item.facilities.split(',') : item.facilities) : [],
              area: item.area || 25,
              status: item.status,
              weight: item.weight
            }
            return roomData
          })
          pagination.total = totalCount
        } else {
          rooms.value = []
          pagination.total = 0
        }
      } else {
        rooms.value = []
        pagination.total = 0
        console.warn('返回的数据结构中不包含data字段:', res)
      }
    } else {
      throw new Error(res.message || res.msg || '获取房型列表失败')
    }
  } catch (error) {
    console.error('获取房型列表失败:', error)
    ElMessage.warning('获取房型列表失败，使用本地数据显示')
    
    // 使用模拟数据
    useMockData()
  } finally {
    loading.value = false
  }
}

// 使用模拟数据
const useMockData = () => {
  // 根据筛选条件过滤模拟数据
  let filteredData = [...mockRoomTypes]
  
  // 应用价格筛选
  if (filter.minPrice || filter.maxPrice) {
    filteredData = filteredData.filter(room => 
      room.price >= filter.minPrice && room.price <= filter.maxPrice
    )
  }
  
  // 应用房型ID筛选
  if (filter.roomTypeId) {
    filteredData = filteredData.filter(room => room.id === filter.roomTypeId)
  }
  
  // 应用人数筛选
  if (filter.maxGuests) {
    filteredData = filteredData.filter(room => room.maxGuests >= filter.maxGuests)
  }
  
  // 更新数据和分页信息
  rooms.value = filteredData
  pagination.total = filteredData.length
}

// 过滤房型（本地筛选，对于搜索关键词）
const filteredRooms = computed(() => {
  return rooms.value
})

// 监听过滤器或分页变化
watch([filter, () => pagination.pageNum, () => pagination.pageSize], () => {
  fetchRoomTypes()
})

// 应用过滤条件
const applyFilters = () => {
  filter.minPrice = priceRange.value[0]
  filter.maxPrice = priceRange.value[1]
  filter.maxGuests = selectedMaxGuests.value
  filter.roomTypeId = selectedRoomTypeId.value
  pagination.pageNum = 1
  fetchRoomTypes()
}

// 重置过滤条件
const resetFilters = () => {
  priceRange.value = [0, 2000]
  selectedRoomType.value = ''
  selectedMaxGuests.value = ''
  selectedRoomTypeId.value = null
  filter.minPrice = 0
  filter.maxPrice = 2000
  filter.maxGuests = ''
  filter.roomTypeId = null
  pagination.pageNum = 1
  fetchRoomTypes()
}

// 查看房型详情
const viewRoomDetail = (id) => {
  router.push(`/room/detail/${id}`)
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
}

// 处理每页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
}

// 处理图片加载错误
const handleImageError = (event) => {
  // 直接清除图片源
  event.target.src = ''
  // 清除错误处理器
  event.target.onerror = null
}

// 获取房型名称列表
const fetchRoomTypeNames = async () => {
  try {
    const res = await getRoomTypeNames()
    if (res.code === 200 && res.data) {
      // 确保返回的是数组
      const typeNames = Array.isArray(res.data) ? res.data : [res.data]
      
      // 添加所有房型到下拉框，保持全部选项在第一位
      const options = [{ label: '全部', value: '' }]
      
      typeNames.forEach(type => {
        options.push({
          label: type.typeName,
          value: type.typeName,
          id: type.id
        })
      })
      
      roomTypeOptions.value = options
    } else {
      console.warn('获取房型名列表失败:', res)
    }
  } catch (error) {
    console.error('获取房型名列表失败:', error)
    // 失败时保持默认选项
  }
}

// 处理房型选择
watch(selectedRoomType, (newVal) => {
  // 查找选中房型的ID
  const selectedOption = roomTypeOptions.value.find(opt => opt.value === newVal)
  selectedRoomTypeId.value = selectedOption?.id || null
})

onMounted(() => {
  fetchRoomTypes()
  fetchRoomTypeNames()
})
</script>

<template>
  <div class="room-list-container">
    <h2 class="page-title">房型列表</h2>

    <!-- 筛选器 -->
    <div class="filter-container">
      <el-card shadow="hover">
        <div class="filter-content">
          <el-select
            v-model="selectedRoomType"
            placeholder="房型"
            class="filter-item"
            clearable
          >
            <el-option
              v-for="item in roomTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div class="filter-item">
            <span class="filter-label">价格范围：</span>
            <el-slider
              v-model="priceRange"
              range
              :min="0"
              :max="2000"
              :step="10"
              show-stops
            />
          </div>
          <el-select
            v-model="selectedMaxGuests"
            placeholder="入住人数"
            class="filter-item"
            clearable
          >
            <el-option
              v-for="item in maxGuestsOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div class="filter-actions">
            <el-button type="primary" @click="applyFilters">筛选</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 房型列表 -->
    <div class="room-list" v-loading="loading">
      <template v-if="filteredRooms.length > 0">
        <el-card
          v-for="room in filteredRooms"
          :key="room.id"
          class="room-card"
          shadow="hover"
          @click="viewRoomDetail(room.id)"
        >
          <div class="room-image">
            <template v-if="room.image">
              <img 
                :src="room.image" 
                :alt="room.name" 
                @error="handleImageError" 
              />
            </template>
            <div v-else class="image-placeholder">
              <el-icon><Picture /></el-icon>
              <div>暂无图片</div>
            </div>
            <div class="room-price-tag">
              <span class="price">¥{{ room.price }}</span>
              <span class="unit">/晚</span>
            </div>
          </div>
          <div class="room-details">
            <div class="room-header">
              <h3>{{ room.name }}</h3>
              <div class="room-tags">
                <el-tag size="small" type="success" v-if="room.area">{{ room.area }}m²</el-tag>
                <el-tag size="small" type="info" v-if="room.bedType">{{ room.bedType }}</el-tag>
                <el-tag size="small" type="warning" v-if="room.maxGuests">最多{{ room.maxGuests }}人</el-tag>
              </div>
            </div>
            <div class="room-description">
              {{ room.description }}
            </div>
            <div class="room-facilities" v-if="room.facilities && room.facilities.length > 0">
              <span class="facility-label">设施：</span>
              <div class="facility-tags">
                <el-tag
                  v-for="(facility, index) in room.facilities.slice(0, 4)"
                  :key="index"
                  size="small"
                  class="facility-tag"
                  effect="plain"
                >
                  {{ facility }}
                </el-tag>
                <el-tag size="small" v-if="room.facilities.length > 4" type="info">
                  +{{ room.facilities.length - 4 }}
                </el-tag>
              </div>
            </div>
            <div class="booking-action">
              <el-button type="primary" size="small">立即预订</el-button>
              <el-button size="small">查看详情</el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
      
      <el-empty v-else description="没有找到符合条件的房型" />
    </div>
  </div>
</template>

<style scoped>
.room-list-container {
  width: 100%;
  max-width: 100%;
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

.filter-container {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-start;
  justify-content: space-between;
}

.filter-item {
  flex: 1;
  min-width: 200px;
  max-width: 300px;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.room-card {
  display: flex;
  margin-bottom: 20px;
  overflow: hidden;
  border-radius: 10px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.room-image {
  width: 100%;
  height: 200px;
  position: relative;
  overflow: hidden;
  background-color: #f5f7fa;
}

.room-image img {
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

.room-price-tag {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 10px;
  border-radius: 20px;
  font-weight: bold;
}

.room-details {
  flex: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.room-header h3 {
  margin: 0;
  font-size: 1.4rem;
  color: #303133;
}

.room-tags {
  display: flex;
  gap: 8px;
}

.room-description {
  margin: 10px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  display: box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  box-orient: vertical;
  max-height: 4.5em; /* 3 lines × 1.5 line-height */
}

.room-facilities {
  margin: 15px 0;
  display: flex;
  align-items: center;
}

.facility-label {
  color: #909399;
  margin-right: 10px;
}

.facility-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.facility-tag {
  margin: 0 !important;
}

.booking-action {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>