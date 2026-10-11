/**
 * 房型相关API的模拟数据
 */

// 房型列表模拟数据
export const mockRoomTypes = [
  {
    id: 1,
    typeName: '标准单人间',
    mainImage: 'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 258,
    discount: 0.8,
    area: 18,
    bedType: '单人床',
    maxGuests: 1,
    description: '舒适的单人间，配备基本设施，适合商务出行或单人旅游。',
    facilities: 'WiFi,电视,空调',
    floor: '2-6',
    totalRooms: 20,
    weight: 100,
    status: 1,
    images: [
      {
        id: 1,
        roomTypeId: 1,
        imageUrl: 'https://img0.baidu.com/it/u=2832536829,4090354037&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        isMain: 1,
        sortOrder: 1
      }
    ],
    reviews: [
      {
        id: 11,
        content: '房间干净整洁，服务也很好',
        score: 5,
        createTime: '2025-04-01',
        userId: 1,
        userAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        reviewImages: [],
        reply: '感谢您的好评，我们会继续努力提供更好的服务。',
        replyTime: '2025-04-02',
        username: '太极',
        isAnonymous: 0
      },
      {
        id: 12,
        content: '性价比很高，地理位置也好',
        score: 4,
        createTime: '2025-03-20',
        userId: 2,
        userAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        reviewImages: [],
        reply: null,
        replyTime: null,
        username: '匿名用户',
        isAnonymous: 1
      }
    ]
  },
  {
    id: 2,
    typeName: '标准双床房',
    mainImage: 'https://img1.baidu.com/it/u=1187860984,2261105129&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 328,
    discount: 0.8,
    area: 25,
    bedType: '两张单人床',
    maxGuests: 2,
    description: '实用的双床房，配有两张单人床，简约舒适，适合商务或结伴出行。',
    facilities: 'WiFi,电视,空调,电热水壶',
    floor: '2-8',
    totalRooms: 30,
    weight: 90,
    status: 1,
    images: [
      {
        id: 2,
        roomTypeId: 2,
        imageUrl: 'https://img1.baidu.com/it/u=1187860984,2261105129&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        isMain: 1,
        sortOrder: 1
      }
    ],
    reviews: [
      {
        id: 21,
        content: '双床很实用，适合两人出行',
        score: 5,
        createTime: '2025-04-05',
        userId: 1,
        userAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        reviewImages: [],
        reply: '感谢您的评价，欢迎再次入住。',
        replyTime: '2025-04-06',
        username: '太极',
        isAnonymous: 0
      }
    ]
  },
  {
    id: 3,
    typeName: '豪华大床房',
    mainImage: 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 398,
    discount: 0.9,
    area: 30,
    bedType: '特大号床',
    maxGuests: 2,
    description: '宽敞明亮的客房配备特大号床，提供舒适的睡眠体验，配有现代化设施。',
    facilities: 'WiFi,电视,空调,电热水壶,冰箱,早餐',
    floor: '5-12',
    totalRooms: 25,
    weight: 120,
    status: 1,
    images: [
      {
        id: 3,
        roomTypeId: 3,
        imageUrl: 'https://img0.baidu.com/it/u=2141061893,3465688082&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        isMain: 1,
        sortOrder: 1
      }
    ],
    reviews: [
      {
        id: 31,
        content: '床很大很舒服，早餐也很丰盛',
        score: 5,
        createTime: '2025-04-10',
        userId: 2,
        userAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        reviewImages: [],
        reply: null,
        replyTime: null,
        username: '游客',
        isAnonymous: 0
      }
    ]
  },
  {
    id: 4,
    typeName: '行政套房',
    mainImage: 'https://img1.baidu.com/it/u=1979540962,1457794044&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 598,
    discount: 0.9,
    area: 45,
    bedType: '特大号床',
    maxGuests: 2,
    description: '豪华行政套房，配有独立起居室和卧室，享有城市美景，提供高端服务。',
    facilities: 'WiFi,电视,空调,冰箱,浴缸,吹风机,minibar,书桌,保险箱,熨斗',
    floor: '10-15',
    totalRooms: 15,
    weight: 150,
    status: 1,
    images: [
      {
        id: 4,
        roomTypeId: 4,
        imageUrl: 'https://img1.baidu.com/it/u=1979540962,1457794044&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        isMain: 1,
        sortOrder: 1
      }
    ],
    reviews: []
  },
  {
    id: 5,
    typeName: '家庭房',
    mainImage: 'https://img2.baidu.com/it/u=3482414296,1599253536&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
    price: 498,
    discount: 0.8,
    area: 40,
    bedType: '一张大床和一张单人床',
    maxGuests: 3,
    description: '适合家庭入住的亲子房，配有1张大床和1张儿童床，有儿童游乐区和舒适的休息空间。',
    facilities: 'WiFi,电视,空调,冰箱,淋浴,吹风机',
    floor: '3-6',
    totalRooms: 10,
    weight: 110,
    status: 1,
    images: [
      {
        id: 5,
        roomTypeId: 5,
        imageUrl: 'https://img2.baidu.com/it/u=3482414296,1599253536&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333',
        isMain: 1,
        sortOrder: 1
      }
    ],
    reviews: []
  }
]

/**
 * 根据筛选条件获取房型列表
 * @param {Object} params - 筛选参数
 * @returns {Object} - 模拟的API响应
 */
export function getMockRoomTypesByFilter(params) {
  let filteredData = [...mockRoomTypes]
  
  // 应用房型ID筛选
  if (params.roomTypeId) {
    filteredData = filteredData.filter(room => room.id === parseInt(params.roomTypeId))
  }
  
  // 应用价格筛选
  if (params.minPrice) {
    filteredData = filteredData.filter(room => room.price >= params.minPrice)
  }
  
  if (params.maxPrice) {
    filteredData = filteredData.filter(room => room.price <= params.maxPrice)
  }
  
  // 应用人数筛选
  if (params.maxGuests) {
    filteredData = filteredData.filter(room => room.maxGuests >= params.maxGuests)
  }
  
  // 只返回启用状态的房型
  filteredData = filteredData.filter(room => room.status === 1)
  
  // 模拟分页
  const pageNum = params.pageNum || 1
  const pageSize = params.pageSize || 10
  const start = (pageNum - 1) * pageSize
  const end = start + pageSize
  const pageData = filteredData.slice(start, end)
  
  return {
    code: 200,
    message: '获取房型列表成功',
    data: {
      total: filteredData.length,
      list: pageData.map(room => ({
        id: room.id,
        typeName: room.typeName,
        price: room.price,
        area: room.area,
        bedType: room.bedType,
        maxGuests: room.maxGuests,
        description: room.description,
        facilities: room.facilities,
        weight: room.weight,
        status: room.status,
        mainImage: room.mainImage
      }))
    }
  }
}

/**
 * 获取房型详情
 * @param {number} id - 房型ID
 * @returns {Object} - 模拟的API响应
 */
export function getMockRoomTypeDetail(id) {
  const parsedId = parseInt(id)
  const roomType = mockRoomTypes.find(r => r.id === parsedId)
  
  if (!roomType) {
    return {
      code: 404,
      message: '房型不存在',
      data: null
    }
  }
  
  return {
    code: 200,
    message: '获取房型详情成功',
    data: {
      id: roomType.id,
      typeName: roomType.typeName,
      price: roomType.price,
      area: roomType.area,
      bedType: roomType.bedType,
      maxGuests: roomType.maxGuests,
      description: roomType.description,
      facilities: roomType.facilities,
      weight: roomType.weight,
      status: roomType.status,
      images: roomType.images,
      reviews: roomType.reviews
    }
  }
}

/**
 * 获取热门房型列表
 * @param {number} limit - 限制数量
 * @returns {Object} - 模拟的API响应
 */
export function getMockHotRoomTypes(limit = 6) {
  // 根据权重排序后取前N个作为热门房型
  const hotRoomTypes = [...mockRoomTypes]
    .filter(room => room.status === 1) // 只返回启用状态的房型
    .sort((a, b) => b.weight - a.weight)
    .slice(0, limit)
    .map(room => ({
      id: room.id,
      typeName: room.typeName,
      price: room.price,
      area: room.area,
      bedType: room.bedType,
      maxGuests: room.maxGuests,
      description: room.description,
      mainImage: room.mainImage
    }))
  
  return {
    code: 200,
    message: '获取热门房型成功',
    data: hotRoomTypes
  }
}

export default {
  mockRoomTypes,
  getMockRoomTypesByFilter,
  getMockRoomTypeDetail,
  getMockHotRoomTypes
} 