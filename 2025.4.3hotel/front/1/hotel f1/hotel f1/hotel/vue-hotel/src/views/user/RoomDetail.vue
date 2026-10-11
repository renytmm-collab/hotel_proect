<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomTypeDetail } from '@/api/roomType'
import { addFavorite, cancelFavorite } from '@/api/favorite'
import { getRoomTypeReviews } from '@/api/review'
import { getOrderListByRoomType, createOrder } from '@/api/order'
import { getUserInfo } from '@/api/user'
import { Plus, Star, Calendar, Message, ArrowLeft, StarFilled, Picture, User, HomeFilled, Sunrise, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const room = ref(null)
const activeImage = ref(0)
const isFavorite = ref(false)
const favoriteLoading = ref(false)

// 评论分页参数
const reviewPage = ref(1)
const reviewPageSize = ref(5)
const hasMoreReviews = ref(true)
const loadingMoreReviews = ref(false)

// 预订相关状态变量
const dateRange = ref([])
const guests = ref(1)
const bookLoading = ref(false)
const userInfo = ref(null)
const userLoading = ref(false)

// 预订表单
const bookingForm = ref({
  guestName: '',
  guestPhone: '',
  guestIdCard: '',
  isAuthenticated: false
})

// 默认图片URL和基础URL
const DEFAULT_ROOM_IMAGE = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// 监听收藏状态变化
watch(() => isFavorite.value, (newValue) => {
  console.log('Favorite state changed:', newValue)
})

/**
 * 处理图片URL
 * @param {string} url 图片URL
 * @returns {string} 处理后的URL
 */
const processImageUrl = (url) => {
  if (!url) return '';
  
  // 记录处理前的URL
  console.log('处理前的URL:', url);
  
  // 已经是完整URL的情况
  if (url.startsWith('http')) {
    console.log('返回完整URL:', url);
    return url;
  }

  // 处理相对路径
  let baseApiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
  
  // 确保baseApiUrl不以/结尾
  if (baseApiUrl.endsWith('/')) {
    baseApiUrl = baseApiUrl.slice(0, -1);
  }
  
  // 确保url以/开头
  const normalizedUrl = url.startsWith('/') ? url : `/${url}`;
  
  // 拼接完整URL
  const fullUrl = baseApiUrl + normalizedUrl;
  console.log('构建完整URL:', fullUrl);
  return fullUrl;
};

/**
 * 处理图片加载错误
 * @param {Event} e 事件对象
 */
const handleImageError = (e) => {
  e.target.src = '';
  e.target.onerror = null;
  e.target.style.display = 'none';
};

/**
 * 更改当前显示的图片
 * @param {number} index 图片索引
 */
const changeImage = (index) => {
  activeImage.value = index;
};

// 获取房型详情
const fetchRoomTypeDetail = async (id) => {
  loading.value = true
  try {
    const res = await getRoomTypeDetail(id)
    
    console.log('获取房型详情响应:', res);
    
    if (res.code === 200 && res.data) {
      // 处理响应数据
      const roomData = res.data
      console.log('房型数据:', roomData);
      console.log('评论数据:', roomData.reviews);
      
      if (roomData.reviews && roomData.reviews.length > 0) {
        console.log('第一条评论的图片:', roomData.reviews[0].reviewImages);
        console.log('第一条评论的用户头像:', roomData.reviews[0].userAvatar);
      }
      
      room.value = {
        id: roomData.id,
        name: roomData.typeName,
        images: roomData.images?.map(img => processImageUrl(img.imageUrl))?.filter(url => url) || [],
        price: roomData.price,
        discount: 1, // 默认无折扣
        size: `${roomData.area}平方米`,
        bedType: roomData.bedType,
        maxGuests: roomData.maxGuests,
        description: roomData.description,
        facilities: roomData.facilities?.split(',') || [],
        reviews: processReviews(roomData.reviews || []),
        availableCount: 5, // 假设有5间可用，实际项目中可能需要通过其他API获取
        isFavorite: roomData.isFavorite // 是否已收藏
      }
      
      // 更新收藏状态
      isFavorite.value = room.value.isFavorite
    } else {
      ElMessage.error('获取房型详情失败')
      router.push('/room/list')
    }
  } catch (error) {
    console.error('获取房型详情失败:', error)
    ElMessage.warning('连接后端服务失败，使用本地模拟数据展示')
    // 使用示例数据作为备用
    room.value = getBackupRoomData(id)
  } finally {
    loading.value = false
  }
}

// 处理评论数据
const processReviews = (reviews) => {
  console.log('原始评论数据:', reviews);
  
  return reviews.map(review => {
    // 处理评论图片，确保有正确的完整URL
    let processedImages = [];
    if (review.reviewImages && Array.isArray(review.reviewImages)) {
      console.log(`评论ID ${review.id} 的原始图片数据:`, review.reviewImages);
      
      processedImages = review.reviewImages.map(img => {
        // 处理不同格式的图片数据
        let imagePath = '';
        if (typeof img === 'object') {
          imagePath = img.imageUrl || img.url || '';
        } else if (typeof img === 'string') {
          // 如果是字符串，直接使用
          imagePath = img;
        }
        
        // 如果是纯数字或其他不合法路径，跳过
        if (/^\d+$/.test(imagePath)) {
          console.log('跳过纯数字的图片路径:', imagePath);
          return '';
        }
        
        console.log('处理后的图片路径:', imagePath);
        return imagePath; // 返回原始路径，在模板中统一处理
      }).filter(Boolean); // 过滤掉空值
      
      console.log(`评论ID ${review.id} 的处理后图片数组:`, processedImages);
    }
    
    // 处理用户头像
    let userAvatar = review.userAvatar;
    if (userAvatar === null || userAvatar === undefined) {
      userAvatar = ''; // 确保为空字符串而非null
    }
    
    return {
      id: review.id,
      content: review.content,
      score: review.score,
      createTime: review.createTime,
      username: review.username,
      userAvatar: userAvatar,
      isAnonymous: review.isAnonymous === 1 || review.isAnonymous === true,
      reviewImages: processedImages,
      reply: review.reply,
      replyTime: review.replyTime
    }
  })
}

// 备用数据
const getBackupRoomData = (id) => {
  const mockRooms = [
    {
      id: 1,
      name: '标准单人间',
      images: [
        'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        'https://img1.baidu.com/it/u=147501608,1562263135&fm=253&fmt=auto&app=120&f=JPEG?w=1140&h=641',
        'https://img0.baidu.com/it/u=3417945977,3650199198&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333'
      ],
      price: 258,
      discount: 1,
      size: '18平方米',
      bedType: '单人床',
      maxGuests: 1,
      description: '舒适的单人间，配备基本设施，适合商务出行或单人旅游。房间内配有舒适单人床、写字台、空调、液晶电视、独立卫浴等基本设施，满足您的商务或旅游需求。',
      facilities: ['空调', '液晶电视', '独立卫浴', '免费WiFi', '写字台', '电热水壶', '吹风机'],
      availableCount: 5,
      reviews: [
        {
          id: 1,
          content: '干净整洁，性价比高',
          score: 4,
          createTime: '2024-04-01',
          username: '匿名用户',
          isAnonymous: 1
        }
      ]
    },
    {
      id: 2,
      name: '豪华大床房',
      images: [
        'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        'https://img0.baidu.com/it/u=3106197342,3338660926&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        'https://img2.baidu.com/it/u=3462337972,288786938&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333'
      ],
      price: 398,
      discount: 1,
      size: '30平方米',
      bedType: '特大号床',
      maxGuests: 2,
      description: '宽敞明亮的客房配备特大号床，提供舒适的睡眠体验，配有现代化设施。房间装修典雅，配备高品质床上用品、豪华卫浴设施、宽带上网等服务，让您享受高品质的住宿体验。',
      facilities: ['空调', '液晶电视', '豪华卫浴', '免费WiFi', '写字台', '迷你吧', '电热水壶', '吹风机', '保险箱', '免费早餐'],
      availableCount: 10,
      reviews: [
        {
          id: 2,
          content: '环境很好，服务一流',
          score: 5,
          createTime: '2024-03-25',
          username: '张先生',
          isAnonymous: 0
        },
        {
          id: 3,
          content: '床很舒服，但隔音一般',
          score: 4,
          createTime: '2024-03-20',
          username: '匿名用户',
          isAnonymous: 1
        }
      ]
    }
  ]
  
  const parsedId = parseInt(id)
  return mockRooms.find(r => r.id === parsedId) || mockRooms[0]
}

// 获取用户信息
const fetchUserInfo = async () => {
  userLoading.value = true
  try {
    const res = await getUserInfo()
    console.log('获取用户信息响应:', res)
    
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      
      // 如果用户已实名认证，获取认证信息
      if (res.data.realName && res.data.idCard) {
        bookingForm.value.guestName = res.data.realName
        bookingForm.value.guestIdCard = res.data.idCard
        bookingForm.value.isAuthenticated = true
      } else {
        // 用户未实名认证，清空表单字段
        bookingForm.value.guestName = ''
        bookingForm.value.guestIdCard = ''
        bookingForm.value.isAuthenticated = false
      }
      
      // 设置联系电话
      if (res.data.phone) {
        bookingForm.value.guestPhone = res.data.phone
      }
      
      console.log('更新预订表单:', bookingForm.value)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    userLoading.value = false
  }
}

// 预订房间
const bookRoom = async () => {
  if (!room.value) {
    ElMessage.error('房间信息加载失败，请刷新重试')
    return
  }

  if (!dateRange.value || dateRange.value.length < 2) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  
  // 验证预订人信息
  if (!bookingForm.value.guestName) {
    ElMessage.warning('请填写预订人真实姓名')
    return
  }
  
  if (!bookingForm.value.guestPhone) {
    ElMessage.warning('请填写预订人联系电话')
    return
  }
  
  if (!bookingForm.value.guestIdCard) {
    ElMessage.warning('请填写预订人身份证号')
    return
  }

  try {
    bookLoading.value = true
    
    // 将日期对象转换为 YYYY-MM-DD 格式字符串
    const formatDate = (date) => {
      if (!date) return '';
      if (typeof date === 'string') {
        // 如果已经是字符串，尝试转换为日期再格式化
        date = new Date(date);
      }
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };
    
    const startDate = formatDate(dateRange.value[0]);
    const endDate = formatDate(dateRange.value[1]);
    const totalAmount = calculateTotalPrice()

    // 构建预订请求
    const bookingData = {
      roomTypeId: room.value.id,
      checkInDate: startDate,
      checkOutDate: endDate,
      guestName: bookingForm.value.guestName,
      guestPhone: bookingForm.value.guestPhone,
      guestIdCard: bookingForm.value.guestIdCard
    }

    console.log('发送预订请求:', bookingData)

    // 使用请求库而不是原生fetch，以确保一致的错误处理
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录再进行预订')
      router.push('/user/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
      return
    }

    // 使用已定义的API函数而不是直接使用fetch
    const res = await createOrder(bookingData)
    
    if (res.code === 200) {
      ElMessage.success('预订成功！')
      // 跳转到订单页面或支付页面
      router.push(`/order/list`)
    } else {
      throw new Error(res.message || '预订失败')
    }
  } catch (error) {
    console.error('预订失败:', error)
    ElMessage.error(error.message || '预订失败，请稍后重试')
  } finally {
    bookLoading.value = false
  }
}

const goBack = () => {
  router.push('/room/list')
}

// 添加收藏
const toggleFavorite = async () => {
  if (!room.value) return
  
  favoriteLoading.value = true
  try {
    const data = {
      roomTypeId: room.value.id
    }
    
    let res
    if (isFavorite.value) {
      // 取消收藏
      res = await cancelFavorite(data)
    } else {
      // 添加收藏
      res = await addFavorite(data)
    }
    
    if (res.code === 200) {
      isFavorite.value = !isFavorite.value
      ElMessage.success(isFavorite.value ? '已添加收藏' : '已取消收藏')
    } else {
      ElMessage.error(res.message || (isFavorite.value ? '取消收藏失败' : '添加收藏失败'))
    }
  } catch (error) {
    console.error('收藏操作错误:', error)
    ElMessage.error(isFavorite.value ? '取消收藏失败' : '添加收藏失败')
  } finally {
    favoriteLoading.value = false
  }
}

// 加载更多评论
const loadMoreReviews = async () => {
  if (!room.value || !hasMoreReviews.value || loadingMoreReviews.value) return
  
  loadingMoreReviews.value = true
  try {
    reviewPage.value += 1
    const params = {
      roomTypeId: room.value.id,
      pageNum: reviewPage.value,
      pageSize: reviewPageSize.value
    }
    
    const res = await getRoomTypeReviews(params)
    
    if (res.code === 200 && res.data) {
      if (res.data.list && res.data.list.length > 0) {
        room.value.reviews = [
          ...room.value.reviews,
          ...processReviews(res.data.list)
        ]
      }
      
      // 判断是否还有更多评论
      hasMoreReviews.value = res.data.list.length === reviewPageSize.value
    } else {
      ElMessage.error(res.message || '获取更多评价失败')
    }
  } catch (error) {
    console.error('获取更多评价失败:', error)
    ElMessage.error('获取更多评价失败，请稍后再试')
  } finally {
    loadingMoreReviews.value = false
  }
}

const goToAddReview = async () => {
  try {
    // 检查用户是否有已完成该房型的订单
    const res = await getOrderListByRoomType({ 
      roomTypeId: room.value.id,
      status: 4 // 已完成状态
    })
    
    if (res.code === 200 && res.data && res.data.list && res.data.list.length > 0) {
      // 用户有已完成的订单，可以评价
      const orders = res.data.list
      
      if (orders.length === 1) {
        // 只有一个订单，直接跳转评价页面
        router.push(`/review/create/${orders[0].id}`)
      } else {
        // 多个订单，让用户选择
        ElMessageBox.confirm(
          '您有多个已完成的订单，请到"我的订单"页面选择要评价的订单',
          '提示',
          {
            confirmButtonText: '去我的订单',
            cancelButtonText: '取消',
            type: 'info'
          }
        ).then(() => {
          router.push('/order/list')
        }).catch(() => {})
      }
    } else {
      // 用户没有已完成的订单
      ElMessage.warning('您需要先完成该房型的订单才能提交评价')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单信息失败，请稍后再试')
  }
}

// 更新收藏状态
const updateFavorite = () => {
  isFavorite.value = !isFavorite.value
  localStorage.setItem('favorite', JSON.stringify(isFavorite.value))
}

// 日期禁用函数
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 计算总价
const calculateTotalPrice = () => {
  if (!dateRange.value || dateRange.value.length < 2 || !room.value) return 0
  
  const start = new Date(dateRange.value[0])
  const end = new Date(dateRange.value[1])
  const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return Math.round(room.value.price * (room.value.discount || 1) * days)
}

// 重置或重新填充预订表单
const resetBookingForm = (fillFromUser = true) => {
  if (fillFromUser && userInfo.value) {
    // 从用户信息中填充
    bookingForm.value.guestName = userInfo.value.realName || ''
    bookingForm.value.guestIdCard = userInfo.value.idCard || ''
    bookingForm.value.guestPhone = userInfo.value.phone || ''
    bookingForm.value.isAuthenticated = !!(userInfo.value.realName && userInfo.value.idCard)
  } else {
    // 清空表单
    bookingForm.value.guestName = ''
    bookingForm.value.guestIdCard = ''
    bookingForm.value.guestPhone = userInfo.value?.phone || ''
    bookingForm.value.isAuthenticated = false
  }
}

// 图片预览
const previewImage = (image) => {
  if (!image) return
  
  // 使用Element Plus的图片预览组件而不是打开新窗口
  // 什么都不做，因为el-image组件自带预览功能
  console.log('预览图片:', processImageUrl(image));
}

// 修改changeActiveImage函数
const changeActiveImage = (index) => {
  if (room.value.images && room.value.images.length > 0) {
    activeImage.value = index;
  }
};

onMounted(() => {
  const id = route.params.id
  fetchRoomTypeDetail(id)
  fetchUserInfo()
})
</script>

<template>
  <div class="room-detail-page">
    <el-result v-if="loading" icon="Loading" title="正在加载房间信息..." :icon-color="'#409EFF'" />
    <el-result v-else-if="!room" icon="Error" title="房间信息不存在" sub-title="无法找到该房间信息，请返回房间列表" :icon-color="'#F56C6C'">
      <template #extra>
        <el-button type="primary" @click="router.push('/room/list')">返回房间列表</el-button>
      </template>
    </el-result>
    <div v-else class="room-detail-container">
      <!-- 返回按钮 -->
      <div class="back-button-container">
        <el-button @click="goBack" icon="ArrowLeft" plain>返回列表</el-button>
      </div>

      <!-- 房间标题 -->
      <div class="room-title-wrapper">
        <h1 class="room-title">{{ room.name || '加载中...' }}</h1>
        <el-button 
          class="favorite-btn-sm" 
          :type="isFavorite ? 'danger' : 'default'" 
          @click="toggleFavorite" 
          :icon="isFavorite ? 'StarFilled' : 'Star'"
          circle 
          :loading="favoriteLoading"
        />
      </div>

      <!-- 图片展示区域 -->
      <div class="gallery-container">
        <div class="main-image-wrapper">
          <div v-if="!room.images || room.images.length === 0 || !processImageUrl(room.images[activeImage])" class="empty-image">
            <el-icon :size="48"><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
          <img
            v-else
            :src="processImageUrl(room.images[activeImage])"
            :alt="room.name"
            class="main-image"
            @error="handleImageError"
          />
        </div>
        <div v-if="room.images && room.images.length > 0" class="thumbnail-gallery">
          <div
            v-for="(image, index) in room.images"
            :key="index"
            class="thumbnail-item"
            :class="{ active: index === activeImage }"
            @click="changeActiveImage(index)"
          >
            <div v-if="!processImageUrl(image)" class="thumbnail-placeholder">
              <el-icon :size="24"><Picture /></el-icon>
            </div>
            <img 
              v-else
              :src="processImageUrl(image)" 
              :alt="`缩略图 ${index + 1}`" 
              @error="handleImageError" 
            />
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-container">
        <!-- 左侧：房间详情 -->
        <div class="room-details">
          <div class="price-label">
            <span class="price">¥{{ Math.round(room.price * room.discount) }}</span>
            <span class="per-night">/ 晚</span>
          </div>

          <div class="feature-highlights">
            <div class="feature-item">
              <el-icon><User /></el-icon>
              <span>可住 {{ room.maxGuests || '--' }} 人</span>
            </div>
            <div class="feature-item">
              <el-icon><HomeFilled /></el-icon>
              <span>{{ room.size || '--' }}</span>
            </div>
            <div class="feature-item">
              <el-icon><Sunrise /></el-icon>
              <span>{{ room.bedType || '--' }}</span>
            </div>
          </div>

          <div class="description-box">
            <h3 class="section-title">房间描述</h3>
            <p class="description-content">{{ room.description || '暂无描述' }}</p>
          </div>

          <div class="amenities-box">
            <h3 class="section-title">设施与服务</h3>
            <div class="amenities-grid">
              <div v-for="(amenity, index) in room.facilities" :key="index" class="amenity-item">
                <el-icon><Check /></el-icon>
                <span>{{ amenity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：预订面板 -->
        <div class="booking-panel">
          <div class="booking-panel-header">
            <h3>预订信息</h3>
          </div>
          
          <div class="booking-price">
            <span class="rate-label">房间价格</span>
            <div class="rate-value">
              ¥{{ room.price }}<span class="unit">/晚</span>
            </div>
          </div>
          
          <div class="booking-dates">
            <span class="date-label">选择日期</span>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              start-placeholder="入住日期"
              end-placeholder="退房日期"
              :disabled-date="disabledDate"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </div>
          
          <div class="booking-guests">
            <span class="guest-label">入住人数</span>
            <el-input-number 
              v-model="guests" 
              :min="1" 
              :max="room.maxGuests || 4" 
              style="width: 100%"
            />
          </div>
          
          <!-- 预订人信息 -->
          <div class="guest-info-form">
            <span class="guest-info-label">预订人信息</span>
            
            <div v-if="userLoading" class="form-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else>
              <div class="form-item">
                <span class="form-label">真实姓名</span>
                <el-input 
                  v-model="bookingForm.guestName" 
                  placeholder="请输入预订人真实姓名" 
                />
              </div>
              
              <div class="form-item">
                <span class="form-label">联系电话</span>
                <el-input 
                  v-model="bookingForm.guestPhone" 
                  placeholder="请输入预订人联系电话"
                />
              </div>
              
              <div class="form-item">
                <span class="form-label">身份证号</span>
                <el-input 
                  v-model="bookingForm.guestIdCard" 
                  placeholder="请输入预订人身份证号" 
                />
              </div>
              
              <div v-if="bookingForm.isAuthenticated" class="auth-tip">
                <el-alert type="info" :closable="false">
                  已从您的个人信息自动填入，如需修改请直接编辑
                </el-alert>
              </div>
              
              <div class="form-actions">
                <el-button size="small" @click="resetBookingForm(true)" type="info" plain>
                  重新填入个人信息
                </el-button>
                <el-button size="small" @click="resetBookingForm(false)" type="danger" plain>
                  清空表单
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="panel-divider"></div>
          
          <div class="booking-total">
            <span class="total-label">总价</span>
            <div class="total-price">
              ¥{{ calculateTotalPrice() }}
            </div>
          </div>
          
          <el-button 
            type="primary" 
            class="book-now-btn"
            @click="bookRoom" 
            :loading="bookLoading"
            :disabled="bookLoading"
          >
            立即预订
          </el-button>
          
          <el-button 
            :type="isFavorite ? 'danger' : 'default'" 
            class="add-to-favorite-btn"
            @click="toggleFavorite" 
            :loading="favoriteLoading"
            :disabled="favoriteLoading"
          >
            <el-icon class="favorite-icon">
              <StarFilled v-if="isFavorite" />
              <Star v-else />
            </el-icon>
            {{ isFavorite ? '取消收藏' : '添加收藏' }}
          </el-button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="reviews-container">
        <h2 class="reviews-title">住客评价</h2>
        <div v-if="room.reviews && room.reviews.length > 0" class="reviews-list">
          <div v-for="(review, index) in room.reviews" :key="index" class="review-card">
            <div class="review-header">
              <div class="reviewer-avatar">
                <template v-if="review.userAvatar">
                  <el-image 
                    :src="processImageUrl(review.userAvatar)" 
                    fit="cover"
                    class="avatar-image"
                  >
                    <template #error>
                      <div class="avatar-placeholder">
                        <el-icon :size="24"><User /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </template>
                <div v-else class="avatar-placeholder">
                  <el-icon :size="24"><User /></el-icon>
                </div>
              </div>
              <div class="reviewer-info">
                <h4 class="reviewer-name">{{ review.isAnonymous ? '匿名用户' : review.username }}</h4>
                <span class="review-date">{{ review.createTime }}</span>
              </div>
              <div class="review-score">
                <el-rate
                  v-model="review.score"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </div>
            <div class="review-body">
              <p class="review-text">{{ review.content }}</p>
              <div v-if="review.reviewImages && review.reviewImages.length > 0" class="review-gallery">
                <div 
                  v-for="(image, imgIndex) in review.reviewImages" 
                  :key="imgIndex" 
                  class="review-image-container"
                  @click="previewImage(image)"
                >
                  <el-image 
                    :src="processImageUrl(image)" 
                    fit="cover"
                    :preview-src-list="[processImageUrl(image)]"
                    class="review-image"
                    :initial-index="imgIndex"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>加载失败</span>
                      </div>
                    </template>
                  </el-image>
                </div>
              </div>
              
              <!-- 酒店回复部分 -->
              <div v-if="review.reply" class="hotel-reply">
                <div class="reply-header">
                  <el-icon><Message /></el-icon>
                  <span>酒店回复</span>
                </div>
                <p class="reply-content">{{ review.reply }}</p>
                <div v-if="review.replyTime" class="reply-time">{{ review.replyTime }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-reviews">
          <el-empty description="暂无评价" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.room-detail-page {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 60px;
}

.room-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}

.room-title-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.room-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.favorite-btn-sm {
  font-size: 18px;
}

.gallery-container {
  margin-bottom: 30px;
}

.main-image-wrapper {
  width: 100%;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.02);
}

.empty-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  background-color: #f8f9fa;
}

.empty-image span {
  margin-top: 16px;
  font-size: 16px;
}

.thumbnail-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.thumbnail-item {
  width: 100px;
  height: 75px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.thumbnail-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-item.active {
  border: 3px solid var(--el-color-primary);
  transform: scale(1.05);
}

.thumbnail-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  background-color: #f5f7fa;
}

.content-container {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
}

.room-details {
  flex: 2;
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.price-label {
  margin-bottom: 24px;
}

.price {
  font-size: 32px;
  font-weight: 700;
  color: #409EFF;
}

.per-night {
  font-size: 18px;
  color: #606266;
  margin-left: 4px;
}

.feature-highlights {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #606266;
}

.feature-item .el-icon {
  font-size: 20px;
  color: #409EFF;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  margin-top: 30px;
}

.description-content {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 30px;
}

.amenities-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.amenity-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #606266;
}

.amenity-item .el-icon {
  color: #67C23A;
}

.booking-panel {
  flex: 1;
  background-color: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  height: fit-content;
}

.booking-panel-header h3 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  text-align: center;
}

.panel-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 20px 0;
}

.booking-price, .booking-dates, .booking-guests, .booking-total {
  margin-bottom: 20px;
}

.rate-label, .date-label, .guest-label, .total-label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
}

.rate-value {
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
}

.unit {
  font-size: 16px;
  color: #909399;
  margin-left: 4px;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #F56C6C;
}

.book-now-btn {
  width: 100%;
  margin-bottom: 15px;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
}

.add-to-favorite-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.favorite-icon {
  font-size: 18px;
}

.reviews-container {
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.reviews-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  position: relative;
  padding-bottom: 10px;
}

.reviews-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background-color: #409EFF;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-card {
  padding: 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.review-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.reviewer-avatar {
  margin-right: 16px;
}

.avatar-image, .avatar-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-placeholder {
  background-color: #e6f1fc;
  color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reviewer-info {
  flex: 1;
}

.reviewer-name {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.review-date {
  font-size: 14px;
  color: #909399;
}

.review-body {
  margin-left: 66px;
}

.review-text {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 16px;
}

.review-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.review-image-container {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.review-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.hotel-reply {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
  margin-bottom: 16px;
  border-left: 3px solid #409EFF;
}

.reply-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 10px;
}

.reply-header .el-icon {
  margin-right: 8px;
}

.reply-content {
  font-size: 15px;
  line-height: 1.6;
  color: #606266;
  margin: 0 0 10px 0;
}

.reply-time {
  font-size: 13px;
  color: #909399;
  text-align: right;
}

.no-reviews {
  padding: 40px;
  text-align: center;
}

.guest-info-form {
  margin-bottom: 20px;
}

.guest-info-label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 15px;
}

.form-item {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.form-loading {
  padding: 10px 0;
}

.auth-tip {
  margin-top: 10px;
}

.form-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.form-actions .el-button {
  flex: 1;
  font-size: 12px;
}

@media (max-width: 992px) {
  .content-container {
    flex-direction: column;
  }
  
  .main-image-wrapper {
    height: 400px;
  }
  
  .amenities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .booking-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .room-title {
    font-size: 28px;
  }
  
  .main-image-wrapper {
    height: 320px;
  }
  
  .thumbnail-item {
    width: 80px;
    height: 60px;
  }
  
  .amenities-grid {
    grid-template-columns: 1fr;
  }
  
  .review-body {
    padding-left: 0;
  }
  
  .review-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .reviewer-avatar {
    margin-bottom: 12px;
  }
  
  .review-score {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style> 