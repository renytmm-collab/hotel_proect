import request from '@/utils/request'

/**
 * 员工打卡签到
 * @returns {Promise}
 */
export function checkIn() {
  return request({
    url: '/api/employee/checkIn',
    method: 'post'
  })
}

/**
 * 员工打卡签退
 * @returns {Promise}
 */
export function checkOut() {
  return request({
    url: '/api/employee/checkOut',
    method: 'post'
  })
}

/**
 * 查询个人考勤记录
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getAttendanceList(params) {
  return request({
    url: '/api/employee/attendance/list',
    method: 'get',
    params
  })
}

/**
 * 查询当日签到状态
 * @returns {Promise}
 */
export function getAttendanceStatus() {
  return request({
    url: '/api/employee/attendance/status',
    method: 'get'
  })
}

export default {
  checkIn,
  checkOut,
  getAttendanceList,
  getAttendanceStatus
} 