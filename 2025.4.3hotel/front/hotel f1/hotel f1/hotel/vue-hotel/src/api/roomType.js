import request from '@/utils/request'
import { getMockRoomTypesByFilter, getMockRoomTypeDetail, getMockHotRoomTypes } from '@/mock/api/roomType'

// 是否使用模拟数据
const useMock = true

/**
 * 获取房型名列表（用于查询筛选的下拉框）
 * @returns {Promise} 返回格式为 { code: 200, message: null, data: { id: '1', typeName: '' } }
 */
export function getRoomTypeNames() {
  return request({
    url: '/api/admin/common/getype',
    method: 'get'
  })
}

/**
 * 获取房型列表（管理员使用）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomTypeList(params) {
  return request({
    url: '/api/admin/roomType/list',
    method: 'get',
    params
  })
}

/**
 * 添加房型
 * @param {Object} data - 房型信息
 * @returns {Promise}
 */
export function addRoomType(data) {
  return request({
    url: '/api/admin/roomType/add',
    method: 'post',
    data
  })
}

/**
 * 修改房型
 * @param {Object} data - 房型信息
 * @returns {Promise}
 */
export function updateRoomType(data) {
  return request({
    url: '/api/admin/roomType/update',
    method: 'put',
    data
  })
}

/**
 * 上传房型图片
 * @param {Object} data - 图片信息
 * @returns {Promise}
 */
export function uploadRoomTypeImage(data) {
  return request({
    url: '/api/admin/roomType/uploadImage',
    method: 'post',
    data
  })
}

/**
 * 获取房型图片列表
 * @param {number} roomTypeId - 房型ID
 * @returns {Promise}
 */
export function getRoomTypeImages(roomTypeId) {
  return request({
    url: `/api/admin/roomType/images/${roomTypeId}`,
    method: 'get'
  })
}

/**
 * 删除房型图片
 * @param {number} id - 图片ID
 * @returns {Promise}
 */
export function deleteRoomTypeImage(id) {
  return request({
    url: `/api/admin/roomType/deleteImage/${id}`,
    method: 'delete'
  })
}

/**
 * 获取房型详细信息（管理员使用）
 * @param {number} id - 房型ID
 * @returns {Promise}
 */
export function getRoomTypeInfo(id) {
  return request({
    url: `/api/admin/roomType/info/${id}`,
    method: 'get'
  })
}

/**
 * 获取热门房型列表（前台用户使用）
 * @param {number} limit - 限制数量
 * @returns {Promise}
 */
export function getHotRoomTypes(limit = 6) {
  // 使用真实API
  return request({
    url: '/api/user/roomType/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取房型详细信息（前台用户使用）
 * @param {number} id - 房型ID
 * @returns {Promise}
 */
export function getRoomTypeDetail(id) {
  // 使用真实API
  return request({
    url: `/api/user/roomType/${id}`,
    method: 'get'
  })
}

/**
 * 获取房型列表（前台用户使用）
 * @param {Object} params - 筛选参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} [params.roomTypeId] - 房型ID(可选)
 * @param {number} [params.minPrice] - 最低价格(可选)
 * @param {number} [params.maxPrice] - 最高价格(可选)
 * @param {number} [params.maxGuests] - 最大入住人数(可选)
 * @returns {Promise} 返回格式为 { code: 200, message: null, data: { total: 数量, list: [...房型数据] } }
 * 或 { code: 200, msg: null, data: { total: 数量, records: [...房型数据] } }
 */
export function getUserRoomTypeList(params) {
  return request({
    url: '/api/user/roomType/list',
    method: 'get',
    params
  })
}

/**
 * 根据筛选条件获取房型列表（前台用户使用 - 兼容旧接口）
 * @param {Object} params - 筛选参数
 * @returns {Promise}
 */
export function getRoomTypesByFilter(params) {
  return getUserRoomTypeList({
    pageNum: params.page || 1,
    pageSize: params.size || 10,
    minPrice: params.priceRange?.[0],
    maxPrice: params.priceRange?.[1],
    maxGuests: params.maxGuests,
    roomTypeId: params.roomTypeId
  })
}

export default {
  getRoomTypeList,
  addRoomType,
  updateRoomType,
  uploadRoomTypeImage,
  getRoomTypeImages,
  deleteRoomTypeImage,
  getRoomTypeInfo,
  getHotRoomTypes,
  getRoomTypeDetail,
  getRoomTypesByFilter,
  getUserRoomTypeList,
  getRoomTypeNames
} 