import request from '@/utils/request'

/**
 * 查询预订订单列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getOrderList(params) {
  return request({
    url: '/api/employee/reception/order/list',
    method: 'get',
    params
  })
}

/**
 * 获取预订订单详情
 * @param {number} id - 订单ID
 * @returns {Promise}
 */
export function getOrderDetail(id) {
  return request({
    url: `/api/employee/reception/order/${id}`,
    method: 'get'
  })
}

/**
 * 修改预订信息
 * @param {Object} data - 修改的预订信息
 * @returns {Promise}
 */
export function updateOrder(data) {
  return request({
    url: '/api/employee/reception/order/update',
    method: 'put',
    data
  })
}

/**
 * 取消预订
 * @param {Object} data - 取消预订信息
 * @returns {Promise}
 */
export function cancelOrder(data) {
  return request({
    url: '/api/employee/reception/order/cancel',
    method: 'post',
    data
  })
}

/**
 * 办理入住
 * @param {Object} data - 入住信息
 * @returns {Promise}
 */
export function checkIn(data) {
  return request({
    url: '/api/employee/reception/checkIn',
    method: 'post',
    data
  })
}

/**
 * 查询入住记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getCheckInList(params) {
  return request({
    url: '/api/employee/reception/checkIn/list',
    method: 'get',
    params
  })
}

/**
 * 获取入住详情
 * @param {number} id - 入住记录ID
 * @returns {Promise}
 */
export function getCheckInDetail(id) {
  return request({
    url: `/api/employee/reception/checkIn/${id}`,
    method: 'get'
  })
}

/**
 * 办理退房
 * @param {Object} data - 退房结算信息
 * @returns {Promise}
 */
export function handleCheckOut(data) {
  return request({
    url: '/api/employee/reception/checkOut/handle',
    method: 'post',
    data
  })
}

/**
 * 查询结算记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getCheckOutList(params) {
  return request({
    url: '/api/employee/reception/checkOut/list',
    method: 'get',
    params
  })
}

/**
 * 获取结算详情
 * @param {number} id - 结算记录ID
 * @returns {Promise}
 */
export function getCheckOutDetail(id) {
  return request({
    url: `/api/employee/reception/checkOut/${id}`,
    method: 'get'
  })
}

/**
 * 获取房态列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomStatusList(params) {
  return request({
    url: '/api/employee/reception/room/status',
    method: 'get',
    params
  })
}

/**
 * 更新房间状态
 * @param {Object} data - 更新参数
 * @returns {Promise}
 */
export function updateRoomStatus(data) {
  return request({
    url: '/api/employee/reception/room/updateStatus',
    method: 'put',
    data
  })
}

/**
 * 获取房间详细状态
 * @param {number} id - 房间ID
 * @returns {Promise}
 */
export function getRoomDetailStatus(id) {
  return request({
    url: `/api/employee/reception/room/detailStatus/${id}`,
    method: 'get'
  })
}

/**
 * 查询客人入住记录
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getGuestStayRecords(params) {
  return request({
    url: '/api/employee/reception/guest/stayRecords',
    method: 'get',
    params
  })
}

/**
 * 回复客人评论
 * @param {Object} data - 回复信息
 * @returns {Promise}
 */
export function replyGuestReview(data) {
  return request({
    url: '/api/employee/reception/guest/reply',
    method: 'put',
    data
  })
}

/**
 * 获取评价列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getReviewList(params) {
  return request({
    url: '/api/employee/reception/guest/reply/list',
    method: 'get',
    params
  })
}

/**
 * 获取所有房型列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomTypeList(params) {
  return request({
    url: '/api/employee/reception/roomType/list',
    method: 'get',
    params
  })
}

/**
 * 获取房型名列表(查询筛选的下拉框选择用)
 * @returns {Promise}
 */
export function getRoomTypeNames() {
  return request({
    url: '/api/admin/common/getype',
    method: 'get'
  })
}

export default {
  getOrderList,
  getOrderDetail,
  updateOrder,
  cancelOrder,
  checkIn,
  getCheckInList,
  getCheckInDetail,
  handleCheckOut,
  getCheckOutList,
  getCheckOutDetail,
  getRoomStatusList,
  updateRoomStatus,
  getRoomDetailStatus,
  getGuestStayRecords,
  replyGuestReview,
  getReviewList,
  getRoomTypeList,
  getRoomTypeNames
} 