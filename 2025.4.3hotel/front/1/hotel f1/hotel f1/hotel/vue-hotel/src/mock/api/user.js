/**
 * 用户相关API的模拟数据
 */

// 模拟用户数据
export const mockUsers = [
  {
    id: 1,
    username: '太极',
    nickname: '太极达人',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    phone: '13800138001',
    password: '123456',
    status: 1,
    createTime: '2023-05-01 12:00:00',
    realName: '张三丰',
    idCard: '110101199001011234'
  },
  {
    id: 2,
    username: '游客',
    nickname: '快乐游客',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    phone: '13900139001',
    password: '123456',
    status: 1,
    createTime: '2023-05-02 12:00:00',
    realName: '',
    idCard: ''
  }
]

// 模拟令牌，实际应用中应该使用 JWT 等
const generateToken = (userId) => {
  return `mock_token_${userId}_${Date.now()}`
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @returns {Object} - 模拟的API响应
 */
export function mockRegister(data) {
  const { username, phone, password } = data
  
  // 检查手机号是否已被注册
  if (mockUsers.find(user => user.phone === phone)) {
    return {
      code: 400,
      message: '该手机号已被注册',
      data: null
    }
  }
  
  // 创建新用户
  const newUser = {
    id: mockUsers.length + 1,
    username,
    nickname: username,
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    phone,
    password,
    status: 1,
    createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    realName: '',
    idCard: ''
  }
  
  mockUsers.push(newUser)
  
  return {
    code: 200,
    message: '注册成功',
    data: null
  }
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @returns {Object} - 模拟的API响应
 */
export function mockLogin(data) {
  const { phone, password } = data
  
  // 查找用户
  const user = mockUsers.find(user => user.phone === phone)
  
  if (!user) {
    return {
      code: 400,
      message: '用户不存在',
      data: null
    }
  }
  
  if (user.password !== password) {
    return {
      code: 400,
      message: '密码错误',
      data: null
    }
  }
  
  // 生成令牌
  const token = generateToken(user.id)
  
  return {
    code: 200,
    message: '登录成功',
    data: {
      token,
      id: user.id,
      status: user.status,
      username: user.username,
      avatar: user.avatar,
      phone: user.phone
    }
  }
}

/**
 * 获取用户信息
 * @param {number} userId - 用户ID
 * @returns {Object} - 模拟的API响应
 */
export function mockGetUserInfo(userId) {
  // 在实际实现中，userId 应该从令牌中解析获取
  const user = mockUsers.find(user => user.id === userId)
  
  if (!user) {
    return {
      code: 401,
      message: '未授权',
      data: null
    }
  }
  
  return {
    code: 200,
    message: '获取用户信息成功',
    data: {
      id: user.id,
      username: user.username,
      nickname: user.nickname,
      avatar: user.avatar,
      phone: user.phone,
      status: user.status,
      createTime: user.createTime
    }
  }
}

/**
 * 修改用户信息
 * @param {number} userId - 用户ID
 * @param {Object} data - 用户信息
 * @returns {Object} - 模拟的API响应
 */
export function mockUpdateUserInfo(userId, data) {
  // 在实际实现中，userId 应该从令牌中解析获取
  const userIndex = mockUsers.findIndex(user => user.id === userId)
  
  if (userIndex === -1) {
    return {
      code: 401,
      message: '未授权',
      data: null
    }
  }
  
  // 更新用户信息
  const { username, avatar, phone, realName } = data
  
  if (username) mockUsers[userIndex].username = username
  if (avatar) mockUsers[userIndex].avatar = avatar
  if (phone) mockUsers[userIndex].phone = phone
  if (realName) mockUsers[userIndex].realName = realName
  
  return {
    code: 200,
    message: '更新用户信息成功',
    data: null
  }
}

/**
 * 修改密码
 * @param {number} userId - 用户ID
 * @param {Object} data - 密码信息
 * @returns {Object} - 模拟的API响应
 */
export function mockUpdatePassword(userId, data) {
  // 在实际实现中，userId 应该从令牌中解析获取
  const userIndex = mockUsers.findIndex(user => user.id === userId)
  
  if (userIndex === -1) {
    return {
      code: 401,
      message: '未授权',
      data: null
    }
  }
  
  const { oldPassword, newPassword } = data
  
  if (mockUsers[userIndex].password !== oldPassword) {
    return {
      code: 400,
      message: '原密码错误',
      data: null
    }
  }
  
  // 更新密码
  mockUsers[userIndex].password = newPassword
  
  return {
    code: 200,
    message: '修改密码成功',
    data: null
  }
}

/**
 * 实名认证
 * @param {number} userId - 用户ID
 * @param {Object} data - 实名信息
 * @returns {Object} - 模拟的API响应
 */
export function mockRealNameAuth(userId, data) {
  // 在实际实现中，userId 应该从令牌中解析获取
  const userIndex = mockUsers.findIndex(user => user.id === userId)
  
  if (userIndex === -1) {
    return {
      code: 401,
      message: '未授权',
      data: null
    }
  }
  
  const { realName, idCard } = data
  
  // 验证身份证号（简单验证）
  if (!idCard || idCard.length !== 18) {
    return {
      code: 400,
      message: '身份证号格式错误',
      data: null
    }
  }
  
  // 更新实名信息
  mockUsers[userIndex].realName = realName
  mockUsers[userIndex].idCard = idCard
  
  return {
    code: 200,
    message: '实名认证成功',
    data: null
  }
}

export default {
  mockUsers,
  mockRegister,
  mockLogin,
  mockGetUserInfo,
  mockUpdateUserInfo,
  mockUpdatePassword,
  mockRealNameAuth
} 