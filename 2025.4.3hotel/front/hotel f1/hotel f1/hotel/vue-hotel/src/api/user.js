import request from '@/utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function login(data) {
  // 调试信息
  console.log('登录请求参数:', data);
  
  return request({
    url: '/api/user/login',
    method: 'post',
    data: {
      phone: data.phone,
      password: data.password
    }
  }).then(response => {
    // 正常返回响应
    return response;
  }).catch(error => {
    // 如果是后端返回的错误（如密码错误），重新抛出包含错误信息的错误
    console.error('登录请求错误:', error);
    throw error;
  });
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

/**
 * 修改用户信息
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  // 确保sex是数值类型
  const userData = {...data}
  if (userData.sex !== undefined) {
    userData.sex = Number(userData.sex)
  }
  
  console.log('API发送更新用户数据:', userData)
  
  return request({
    url: '/api/user/update',
    method: 'put',
    data: userData
  })
}

/**
 * 修改密码
 * @param {Object} data - 包含旧密码和新密码
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data
  })
}

/**
 * 实名认证
 * @param {Object} data - 实名信息
 * @returns {Promise}
 */
export function realNameAuth(data) {
  // 确保使用接口所需的字段名
  const submitData = {
    realName: data.realName,
    idCard: data.idCard
  };
  
  return request({
    url: '/api/user/real',
    method: 'post',
    data: submitData
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post'
  })
}

/**
 * 上传用户头像
 * @param {FormData} data - 包含头像文件的FormData对象
 * @returns {Promise}
 */
export function uploadAvatar(file) {
  // 创建FormData对象
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/api/admin/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取房型列表(未下架)
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomTypeList(params) {
  return request({
    url: '/api/user/roomType/list',
    method: 'get',
    params
  })
}

export default {
  register,
  login,
  getUserInfo,
  updateUserInfo,
  updatePassword,
  realNameAuth,
  logout,
  uploadAvatar,
  getRoomTypeList
} 