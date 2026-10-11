import request from '@/utils/request'

/**
 * 创建预订订单
 * @param {Object} data - 订单信息
 * @returns {Promise}
 */
export function createOrder(data) {
  return request({
    url: '/api/user/order/create',
    method: 'post',
    data
  })
}

/**
 * 支付订单
 * @param {Object} data - 支付信息
 * @returns {Promise}
 */
export function payOrder(data) {
  return request({
    url: '/api/user/order/pay',
    method: 'post',
    data
  })
}

/**
 * 取消订单
 * @param {Object} data - 取消订单信息
 * @returns {Promise}
 */
export function cancelOrder(data) {
  return request({
    url: '/api/user/order/cancel',
    method: 'post',
    data
  })
}

/**
 * 获取订单列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getOrderList(params) {
  return request({
    url: '/api/user/order/list',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 * @returns {Promise}
 */
export function getOrderDetail(id) {
  console.log(`请求订单详情API，订单ID: ${id}`);
  
  return request({
    url: `/api/user/order/${id}`,
    method: 'get'
  })
}

/**
 * 更新预订订单信息
 * @param {Object} data - 更新后的订单信息
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
 * 获取指定房型的订单列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getOrderListByRoomType(params) {
  return request({
    url: '/api/user/order/room-type',
    method: 'get',
    params
  })
}

export default {
  createOrder,
  payOrder,
  cancelOrder,
  getOrderList,
  getOrderDetail,
  updateOrder,
  getOrderListByRoomType
} 