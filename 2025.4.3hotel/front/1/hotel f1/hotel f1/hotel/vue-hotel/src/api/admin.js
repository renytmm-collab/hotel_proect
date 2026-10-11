import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/admin/login',
    method: 'post',
    data
  })
}

/**
 * 获取员工列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getEmployeeList(params) {
  return request({
    url: '/api/admin/employee/list',
    method: 'get',
    params
  })
}

/**
 * 添加员工
 * @param {Object} data - 员工信息
 * @returns {Promise}
 */
export function addEmployee(data) {
  return request({
    url: '/api/admin/employee/add',
    method: 'post',
    data
  })
}

/**
 * 修改员工信息
 * @param {Object} data - 员工信息
 * @returns {Promise}
 */
export function updateEmployee(data) {
  return request({
    url: '/api/admin/employee/update',
    method: 'put',
    data
  })
}

/**
 * 重置员工密码
 * @param {Object} data - 重置密码信息
 * @returns {Promise}
 */
export function resetPassword(data) {
  return request({
    url: '/api/admin/employee/resetPassword',
    method: 'post',
    data
  })
}

/**
 * 获取员工详细信息
 * @param {number} id - 员工ID
 * @returns {Promise}
 */
export function getEmployeeInfo(id) {
  return request({
    url: `/api/admin/employee/info/${id}`,
    method: 'get'
  })
}

/**
 * 获取角色列表
 * @returns {Promise}
 */
export function getRoleList() {
  return request({
    url: '/api/admin/role/list',
    method: 'get'
  })
}

/**
 * 添加角色
 * @param {Object} data - 角色信息
 * @returns {Promise}
 */
export function addRole(data) {
  return request({
    url: '/api/admin/role/add',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 * @param {Object} data - 角色信息
 * @returns {Promise}
 */
export function updateRole(data) {
  return request({
    url: '/api/admin/role/update',
    method: 'put',
    data
  })
}

/**
 * 获取管理员信息
 * @returns {Promise}
 */
export function getAdminInfo() {
  return request({
    url: '/api/admin/info',
    method: 'get'
  })
}

/**
 * 修改个人信息
 * @param {Object} data - 个人信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  return request({
    url: '/api/admin/updateInfo',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/api/admin/password',
    method: 'put',
    data
  })
}

/**
 * 退出登录
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/admin/logout',
    method: 'post'
  })
}

/**
 * 获取员工考勤记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getAttendanceList(params) {
  return request({
    url: '/api/admin/attendance/list',
    method: 'get',
    params
  })
}

/**
 * 修改考勤状态
 * @param {Object} data - 考勤状态信息
 * @returns {Promise}
 */
export function updateAttendanceStatus(data) {
  return request({
    url: '/api/admin/attendance/updateStatus',
    method: 'put',
    data
  })
}

/**
 * 获取绩效评估列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getPerformanceList(params) {
  return request({
    url: '/api/admin/performance/list',
    method: 'get',
    params
  })
}

/**
 * 添加绩效评估
 * @param {Object} data - 绩效评估信息
 * @returns {Promise}
 */
export function addPerformance(data) {
  return request({
    url: '/api/admin/performance/add',
    method: 'post',
    data
  })
}

/**
 * 修改绩效评估
 * @param {Object} data - 绩效评估信息
 * @returns {Promise}
 */
export function updatePerformance(data) {
  return request({
    url: '/api/admin/performance/update',
    method: 'put',
    data
  })
}

/**
 * 获取收入记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getIncomeList(params) {
  return request({
    url: '/api/admin/income/list',
    method: 'get',
    params
  })
}

/**
 * 获取支出记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getExpenseList(params) {
  return request({
    url: '/api/admin/expense/list',
    method: 'get',
    params
  })
}

/**
 * 添加支出记录
 * @param {Object} data - 支出记录信息
 * @returns {Promise}
 */
export function addExpense(data) {
  return request({
    url: '/api/admin/expense/add',
    method: 'post',
    data
  })
}

/**
 * 修改支出记录
 * @param {Object} data - 支出记录信息
 * @returns {Promise}
 */
export function updateExpense(data) {
  return request({
    url: '/api/admin/expense/update',
    method: 'put',
    data
  })
}

/**
 * 客房销售统计
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomSalesStats(params) {
  return request({
    url: '/api/admin/stats/roomSales',
    method: 'get',
    params
  })
}

/**
 * 收入统计
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getIncomeStats(params) {
  return request({
    url: '/api/admin/stats/income',
    method: 'get',
    params
  })
}

/**
 * 支出统计
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getExpenseStats(params) {
  return request({
    url: '/api/admin/stats/expense',
    method: 'get',
    params
  })
}

/**
 * 利润统计
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getProfitStats(params) {
  return request({
    url: '/api/admin/stats/profit',
    method: 'get',
    params
  })
}

/**
 * 入住率统计
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getOccupancyStats(params) {
  return request({
    url: '/api/admin/stats/occupancy',
    method: 'get',
    params
  })
}

/**
 * 上传文件（头像/房型图片）
 * @param {FormData} data - 文件数据
 * @returns {Promise}
 */
export function uploadFile(data) {
  return request({
    url: '/api/admin/common/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取房型名列表(查询筛选的下拉框选择用)
 * @returns {Promise}
 */
export function getRoomTypeOptions() {
  return request({
    url: '/api/admin/common/getype',
    method: 'get'
  })
}

/**
 * 获取角色名列表(修改员工角色的下拉框选择用)
 * @returns {Promise}
 */
export function getRoleOptions() {
  return request({
    url: '/api/admin/common/getrole',
    method: 'get'
  })
}

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/api/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserStatus(data) {
  return request({
    url: '/api/admin/user/update',
    method: 'put',
    data
  })
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function getUserInfo(id) {
  return request({
    url: `/api/admin/user/info/${id}`,
    method: 'get'
  })
}

export default {
  login,
  getEmployeeList,
  addEmployee,
  updateEmployee,
  resetPassword,
  getEmployeeInfo,
  getRoleList,
  addRole,
  updateRole,
  updateUserInfo,
  updatePassword,
  getAdminInfo,
  logout,
  getAttendanceList,
  updateAttendanceStatus,
  getPerformanceList,
  addPerformance,
  updatePerformance,
  getIncomeList,
  getExpenseList,
  addExpense,
  updateExpense,
  getRoomSalesStats,
  getIncomeStats,
  getExpenseStats,
  getProfitStats,
  getOccupancyStats,
  uploadFile,
  getRoomTypeOptions,
  getRoleOptions,
  getUserList,
  updateUserStatus,
  getUserInfo
} 