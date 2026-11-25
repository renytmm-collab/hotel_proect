<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Refrigerator, Sunny, Service, Location, Phone, Clock, Van, Connection, Suitcase } from '@element-plus/icons-vue'
import { getHotRoomTypes } from '@/api/roomType'

const router = useRouter()
const hotRooms = ref([])
const loading = ref(false)

const hotelStats = [
  { num: '500m', label: '距地铁1号线人民广场站' },
  { num: '24h', label: '全天候前台与行李寄存' },
  { num: '4.9分', label: '住客综合口碑评分' },
  { num: '免费', label: '专属停车场与高速 WiFi' }
]

const facilities = [
  { icon: Refrigerator, title: '自助餐厅', desc: '每日 07:00–10:00 供应中西式自助早餐，豪华房及以上含双早' },
  { icon: Sunny, title: '恒温泳池', desc: '室内四季恒温泳池与休闲区，住客凭房卡免费畅游' },
  { icon: Service, title: '24h 健身中心', desc: '配备全套有氧及力量器械，随时保持商旅好状态' },
  { icon: Van, title: '免费停车场', desc: '酒店专属地下车位，住店客人免费停放，先到先得' },
  { icon: Connection, title: '全馆高速 WiFi', desc: '独立千兆光纤覆盖每间客房与公区，办公会议流畅稳定' },
  { icon: Suitcase, title: '行李寄存与礼宾', desc: '24 小时免费寄存行李，支持电子/纸质发票快速开具' }
]

const fetchHotRoomTypes = async () => {
  loading.value = true
  try {
    const res = await getHotRoomTypes(4)
    if (res.code === 200 && res.data && res.data.length > 0) {
      hotRooms.value = res.data.map(item => ({
        id: item.id,
        name: item.typeName,
        image: item.mainImage || '/upload/5a46ac76-74cc-4367-8bf5-12ed22b56361.jpg',
        price: item.price,
        description: item.description || '舒适雅致的住宿环境，配备现代化客房设施。',
        bedType: item.bedType,
        maxGuests: item.maxGuests,
        area: item.area
      }))
    } else {
      throw new Error(res.msg || '获取热门房型失败')
    }
  } catch (error) {
    console.error('获取热门房型失败:', error)
    ElMessage.warning('热门房型加载异常，已展示精选推荐')
    hotRooms.value = [
      { id: 4, name: '行政套房', image: '/upload/64816e69-9816-43d2-9a83-3252ed0aedec.jpg', price: 598, description: '独立会客厅与主卧分区设计，高楼层全景落地窗。', bedType: '2.0m 尊享大床', maxGuests: 2, area: 52 },
      { id: 5, name: '家庭房', image: '/upload/5b32b1a7-2dfe-4ad3-8593-6533039a59d4.jpg', price: 498, description: '专为一家三口打造的温馨亲子空间，配儿童洗漱套装。', bedType: '大床 + 儿童床', maxGuests: 3, area: 42 },
      { id: 3, name: '豪华大床房', image: '/upload/5a46ac76-74cc-4367-8bf5-12ed22b56361.jpg', price: 398, description: '宽敞明亮的城市景大床房，精选乳胶床垫含双早。', bedType: '1.8m 特大床', maxGuests: 2, area: 34 },
      { id: 2, name: '标准双床房', image: '/upload/39748644-0495-4455-8925-4bdb29134951.jpg', price: 328, description: '通透实用的经典双床布局，适合同事同行或好友出游。', bedType: '两张 1.2m 床', maxGuests: 2, area: 28 }
    ]
  } finally {
    loading.value = false
  }
}

const openAiAssistant = () => {
  const fab = document.querySelector('.cw-fab')
  if (fab) fab.click()
}

const viewRoomDetail = (id) => {
  router.push(`/room/detail/${id}`)
}

onMounted(fetchHotRoomTypes)
</script>

<template>
  <div class="home-container">
    <!-- 顶部品质横幅 -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <span class="hero-badge">逸云精品酒店 · 四星级城市商务优选</span>
        <h1>城市中心，静享从容居停</h1>
        <p>距地铁 1 号线人民广场站仅 500 米 · 实时房态在线预订 · 7×24 小时 AI 智能客服贴心答疑</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="cta-primary" @click="router.push('/room/list')">
            立即预订客房
          </el-button>
          <el-button size="large" round class="cta-ghost" @click="openAiAssistant">
            咨询智能客服
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-pill" v-for="s in hotelStats" :key="s.label">
            <strong>{{ s.num }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门房型 -->
    <section class="section">
      <div class="section-header">
        <div>
          <h2>精选热门房型</h2>
          <p class="section-sub">实时同步酒店房态与最新房价，支持在线预订与到店支付</p>
        </div>
        <el-button text type="primary" @click="router.push('/room/list')">
          查看全部房型 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="6" v-for="room in hotRooms" :key="room.id">
          <div class="room-card" @click="viewRoomDetail(room.id)">
            <div class="room-img-wrap">
              <img :src="room.image" class="room-image" :alt="room.name" />
              <span class="area-badge" v-if="room.area">{{ room.area }}㎡</span>
            </div>
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <div class="room-tags">
                <span class="tag">{{ room.bedType }}</span>
                <span class="tag">可住 {{ room.maxGuests }} 人</span>
              </div>
              <p class="room-description">{{ room.description }}</p>
              <div class="room-footer">
                <div class="room-price">
                  <span class="price">¥{{ room.price }}</span>
                  <span class="unit">/晚</span>
                </div>
                <span class="book-link">查看详情 →</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 酒店设施与服务 -->
    <section class="section">
      <div class="section-header">
        <div>
          <h2>设施与贴心服务</h2>
          <p class="section-sub">入住时间 14:00 后 · 退房时间次日 12:00 前 · 前台 24 小时服务热线 8888</p>
        </div>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="item in facilities" :key="item.title">
          <div class="facility-item">
            <div class="facility-icon">
              <el-icon :size="26"><component :is="item.icon" /></el-icon>
            </div>
            <div class="facility-text">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 关于逸云 & 联系我们 -->
    <section class="section info-grid">
      <div class="info-card">
        <h3>关于逸云酒店</h3>
        <p>
          逸云酒店是一家按四星级标准打造的城市商务精品酒店，位于市中心人民广场商圈，交通便捷。
          酒店拥有标准单人间、标准双床房、豪华大床房、家庭房与行政套房五大房型，
          融合数字化运营与 AI 智能客服，为商旅与亲子家庭宾客提供省心、舒适的入住体验。
        </p>
      </div>
      <div class="info-card contact-card">
        <h3>联系与交通</h3>
        <div class="contact-row"><el-icon><Location /></el-icon><span>市中心人民广场商圈（距地铁 1 号线约 500 米，步行 8 分钟）</span></div>
        <div class="contact-row"><el-icon><Phone /></el-icon><span>24 小时前台服务热线：<strong>8888</strong> / 010-88886666</span></div>
        <div class="contact-row"><el-icon><Clock /></el-icon><span>入住 14:00 后 · 退房 12:00 前 · 早餐 07:00–10:00</span></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1240px;
  margin: 0 auto;
  padding-bottom: 32px;
}

.hero {
  position: relative;
  min-height: 430px;
  border-radius: 18px;
  overflow: hidden;
  background-image: url('/upload/64816e69-9816-43d2-9a83-3252ed0aedec.jpg');
  background-size: cover;
  background-position: center;
  margin-bottom: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.82) 0%, rgba(15, 118, 110, 0.56) 60%, rgba(15, 23, 42, 0.72) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
  padding: 48px 28px;
  max-width: 820px;
}

.hero-badge {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.28);
  font-size: 12.5px;
  letter-spacing: 1px;
  margin-bottom: 16px;
}

.hero-content h1 {
  font-size: 40px;
  font-weight: 700;
  margin: 0 0 14px;
  letter-spacing: 2px;
}

.hero-content p {
  font-size: 15.5px;
  color: rgba(241, 245, 249, 0.92);
  margin: 0 0 28px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-bottom: 34px;
}

.cta-primary {
  padding: 0 28px;
  font-weight: 600;
}

.cta-ghost {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.45);
  color: #ffffff;
  padding: 0 24px;
}

.cta-ghost:hover {
  background: rgba(255, 255, 255, 0.24);
  color: #ffffff;
  border-color: #ffffff;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  padding: 14px 16px;
}

.stat-pill strong {
  display: block;
  font-size: 18px;
  color: #5eead4;
}

.stat-pill span {
  font-size: 12px;
  color: #cbd5e1;
}

.section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 22px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px;
}

.section-sub {
  margin: 0;
  font-size: 13.5px;
  color: #64748b;
}

.room-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: transform 0.24s ease, box-shadow 0.24s ease;
  margin-bottom: 20px;
}

.room-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.10);
}

.room-img-wrap {
  position: relative;
  height: 185px;
  overflow: hidden;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.room-card:hover .room-image {
  transform: scale(1.05);
}

.area-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
}

.room-info {
  padding: 16px;
}

.room-info h3 {
  margin: 0 0 8px;
  font-size: 17px;
  color: #0f172a;
}

.room-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.tag {
  font-size: 11.5px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #475569;
}

.room-description {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
  height: 39px;
  margin: 0 0 14px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.room-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.price {
  color: #0f766e;
  font-size: 21px;
  font-weight: 700;
}

.unit {
  color: #94a3b8;
  font-size: 12px;
  margin-left: 2px;
}

.book-link {
  font-size: 12.5px;
  color: #2563eb;
  font-weight: 500;
}

.facility-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  transition: all 0.22s;
}

.facility-item:hover {
  border-color: #99f6e4;
  box-shadow: 0 10px 20px rgba(15, 118, 110, 0.08);
}

.facility-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f0fdfa;
  color: #0f766e;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.facility-text h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #0f172a;
}

.facility-text p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #64748b;
}

.info-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
}

.info-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 24px 26px;
}

.info-card h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #0f172a;
}

.info-card p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.75;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 13.5px;
  margin-bottom: 10px;
}

.contact-row .el-icon {
  color: #0f766e;
  font-size: 16px;
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 26px;
  }
  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
