<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHotRoomTypes } from '@/api/roomType'

const router = useRouter()
const bannerHeight = ref('500px')
const hotRooms = ref([])
const loading = ref(false)

// 获取热门房型数据
const fetchHotRoomTypes = async () => {
  loading.value = true
  try {
    const res = await getHotRoomTypes(4) // 获取4个热门房型
    if (res.code === 200 && res.data) {
      hotRooms.value = res.data.map(item => ({
        id: item.id,
        name: item.typeName,
        image: item.mainImage,
        price: item.price,
        description: item.description || '舒适的住宿环境，配有现代化设施',
        bedType: item.bedType,
        maxGuests: item.maxGuests,
        area: item.area
      }))
    } else {
      throw new Error(res.msg || '获取热门房型失败')
    }
  } catch (error) {
    console.error('获取热门房型失败:', error)
    ElMessage.warning('获取热门房型数据失败')
    // 使用示例数据作为备用
    hotRooms.value = [
      {
        id: 1,
        name: '豪华大床房',
        image: 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        price: 398,
        description: '宽敞明亮的客房配备特大号床，提供舒适的睡眠体验，配有现代化设施。',
        bedType: '特大号床',
        maxGuests: 2,
        area: 30
      },
      {
        id: 2,
        name: '行政套房',
        image: 'https://img1.baidu.com/it/u=1979540962,1457794044&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        price: 598,
        description: '高端豪华套房，独立客厅和卧室，享受更多空间和私密性，配备顶级设施。',
        bedType: '特大号床',
        maxGuests: 2,
        area: 45
      },
      {
        id: 3,
        name: '家庭房',
        image: 'https://img2.baidu.com/it/u=3482414296,1599253536&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        price: 498,
        description: '宽敞的房间配备双床，适合家庭入住，配备儿童设施，温馨舒适。',
        bedType: '一张大床和一张单人床',
        maxGuests: 3,
        area: 40
      },
      {
        id: 4,
        name: '标准双床房',
        image: 'https://img1.baidu.com/it/u=1187860984,2261105129&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        price: 328,
        description: '实用的双床房，配有两张单人床，简约舒适，适合商务或结伴出行。',
        bedType: '两张单人床',
        maxGuests: 2,
        area: 25
      }
    ]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotRoomTypes()
})

const viewRoomDetail = (id) => {
  router.push(`/room/detail/${id}`)
}
</script>

<template>
  <div class="home-container">
    <!-- 横幅 -->
    <div class="banner" :style="{ height: bannerHeight }">
      <div class="banner-content">
        <h1>欢迎来到我们的酒店</h1>
        <p>舒适的环境，贴心的服务，为您提供难忘的住宿体验</p>
        <el-button type="primary" size="large" @click="router.push('/room/list')">立即预订</el-button>
      </div>
    </div>

    <!-- 热门房型 -->
    <div class="hot-rooms section">
      <div class="section-title">
        <h2>热门房型</h2>
        <el-button text @click="router.push('/room/list')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="6" v-for="room in hotRooms" :key="room.id">
          <el-card class="room-card" shadow="hover" @click="viewRoomDetail(room.id)">
            <img :src="room.image" class="room-image" alt="房间图片" />
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <div class="room-tags">
                <el-tag size="small" type="info">{{ room.bedType }}</el-tag>
                <el-tag size="small" type="info">{{ room.maxGuests }}人</el-tag>
              </div>
              <p class="room-description">{{ room.description }}</p>
              <div class="room-price">
                <span class="price">¥{{ room.price }}</span>
                <span class="unit">/晚</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 酒店设施 -->
    <div class="facilities section">
      <h2>酒店设施</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="facility-item">
            <el-icon><Refrigerator /></el-icon>
            <h3>餐厅</h3>
            <p>提供各式美食，早餐、午餐和晚餐全天候服务</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="facility-item">
            <el-icon><Sunny /></el-icon>
            <h3>游泳池</h3>
            <p>室内恒温游泳池，全年开放，提供舒适的游泳环境</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="facility-item">
            <el-icon><Service /></el-icon>
            <h3>健身中心</h3>
            <p>配备现代化健身设备，24小时开放，满足您的健身需求</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 关于我们 -->
    <div class="about section">
      <h2>关于我们</h2>
      <p>我们的酒店位于市中心，交通便利，环境优美。拥有多种类型的客房，满足不同客人的需求。我们的服务团队专业热情，致力于为每位客人提供最优质的服务。无论是商务出行还是休闲旅游，我们都将为您带来舒适难忘的住宿体验。</p>
    </div>

    <!-- 联系我们 -->
    <div class="contact section">
      <h2>联系我们</h2>
      <p><strong>地址：</strong>北京市朝阳区建国路88号</p>
      <p><strong>电话：</strong>010-12345678</p>
      <p><strong>电子邮件：</strong>contact@hotel.com</p>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 100%;
}

.banner {
  background-image: url('https://img0.baidu.com/it/u=2834113278,3783056987&fm=253&fmt=auto&app=138&f=JPEG?w=1180&h=492');
  background-size: cover;
  background-position: center;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 40px;
}

.banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
}

.banner-content {
  position: relative;
  z-index: 1;
  color: white;
  padding: 80px 40px;
  text-align: center;
}

.banner-content h1 {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.banner-content p {
  font-size: 1.2rem;
  margin-bottom: 24px;
}

.section {
  margin-bottom: 60px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section h2 {
  font-size: 1.8rem;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.section h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.room-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  height: 100%;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.room-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px 4px 0 0;
}

.room-info {
  padding: 15px;
}

.room-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
}

.room-tags {
  margin-bottom: 10px;
}

.room-tags .el-tag {
  margin-right: 5px;
}

.room-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 15px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.room-price {
  display: flex;
  align-items: baseline;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.unit {
  color: #909399;
  margin-left: 3px;
}

.facility-item {
  text-align: center;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s;
}

.facility-item:hover {
  background-color: #ecf5ff;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.05);
}

.facility-item .el-icon {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 10px;
}

.facility-item h3 {
  margin: 10px 0;
  font-size: 18px;
}

.facility-item p {
  color: #606266;
  font-size: 14px;
}

.about p, .contact p {
  line-height: 1.6;
  color: #606266;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .banner-content h1 {
    font-size: 2rem;
  }
  
  .banner-content p {
    font-size: 1rem;
  }
  
  .section h2 {
    font-size: 1.5rem;
  }
}
</style> 