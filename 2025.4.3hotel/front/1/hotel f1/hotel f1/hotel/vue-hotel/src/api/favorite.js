import request from '@/utils/request'

/**
 * 添加收藏
 * @param {Object} data - 收藏信息
 * @returns {Promise}
 */
export function addFavorite(data) {
  return request({
    url: '/api/user/favorite/add',
    method: 'post',
    data
  })
}

/**
 * 取消收藏
 * @param {Object} data - 取消收藏信息
 * @returns {Promise}
 */
export function cancelFavorite(data) {
  return request({
    url: '/api/user/favorite/cancel',
    method: 'post',
    data
  })
}

/**
 * 获取收藏列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getFavoriteList(params) {
  return request({
    url: '/api/user/favorite/list',
    method: 'get',
    params
  })
}

export default {
  addFavorite,
  cancelFavorite,
  getFavoriteList
} 