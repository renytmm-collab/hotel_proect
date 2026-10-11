import request from '@/utils/request'

/**
 * 员工登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function staffLogin(data) {
  console.log('员工登录请求参数:', data);
  
  return request({
    url: '/api/admin/login',
    method: 'post',
    data
  }).then(response => {
    // 正常返回响应
    return response;
  }).catch(error => {
    // 如果是后端返回的错误（如密码错误），重新抛出包含错误信息的错误
    console.error('员工登录请求错误:', error);
    throw error;
  });
}

/**
 * 获取员工信息
 * @returns {Promise}
 */
export function getStaffInfo() {
  return request({
    url: '/api/admin/info',
    method: 'get'
  })
}

/**
 * 更新员工个人信息
 * @param {Object} data - 员工信息
 * @returns {Promise}
 */
export function updateStaffInfo(data) {
  return request({
    url: '/api/admin/updateInfo',
    method: 'put',
    data
  })
}

/**
 * 修改员工密码
 * @param {Object} data - 密码信息
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/api/admin/password',
    method: 'put',
    data
  })
}

/**
 * 修改员工密码（别名）
 * @param {Object} data - 密码信息
 * @returns {Promise}
 */
export function updateStaffPassword(data) {
  return changePassword(data);
}

/**
 * 员工退出登录
 * @returns {Promise}
 */
export function staffLogout() {
  return request({
    url: '/api/admin/logout',
    method: 'post'
  })
}

/**
 * 获取员工考勤信息
 * @returns {Promise}
 */
export function getStaffAttendance() {
  return request({
    url: '/api/employee/attendance/summary',
    method: 'get'
  })
}

/**
 * 获取客人列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getGuestList(params) {
  return request({
    url: '/api/employee/reception/guest/stayRecords',
    method: 'get',
    params
  })
}

/**
 * 获取客人详情
 * @param {number} id - 客人ID
 * @returns {Promise}
 */
export function getGuestDetail(id) {
  return request({
    url: '/api/employee/reception/guest/detail/${id}',
    method: 'get'
  })
}

/**
 * 上传员工头像
 * @param {FormData} data - 包含头像文件的FormData对象
 * @returns {Promise}
 */
export function uploadStaffAvatar(data) {
  return request({
    url: '/api/admin/common/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  staffLogin,
  getStaffInfo,
  updateStaffInfo,
  changePassword,
  updateStaffPassword,
  staffLogout,
  getStaffAttendance,
  getGuestList,
  getGuestDetail,
  uploadStaffAvatar
} 