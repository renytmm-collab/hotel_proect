import request from '@/utils/request'

/**
 * 获取房间列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomList(params) {
  return request({
    url: '/api/admin/room/list',
    method: 'get',
    params
  })
}

/**
 * 添加房间
 * @param {Object} data - 房间信息
 * @returns {Promise}
 */
export function addRoom(data) {
  return request({
    url: '/api/admin/room/add',
    method: 'post',
    data
  })
}

/**
 * 修改房间状态
 * @param {Object} data - 房间状态信息
 * @returns {Promise}
 */
export function updateRoomStatus(data) {
  return request({
    url: '/api/admin/room/update',
    method: 'put',
    data
  })
}

/**
 * 批量添加房间
 * @param {Object} data - 批量添加信息
 * @returns {Promise}
 */
export function batchAddRooms(data) {
  return request({
    url: '/api/admin/room/batchAdd',
    method: 'post',
    data
  })
}

export default {
  getRoomList,
  addRoom,
  updateRoomStatus,
  batchAddRooms
} 