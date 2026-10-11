import request from '@/utils/request'

/**
 * 提交评价
 * @param {Object} data - 评价信息
 * @returns {Promise}
 */
export function submitReview(data) {
  return request({
    url: '/api/user/review/submit',
    method: 'post',
    data
  })
}

/**
 * 获取我的评价列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getReviewList(params) {
  return request({
    url: '/api/user/review/list',
    method: 'get',
    params
  })
}

/**
 * 删除评价
 * @param {number} id - 评价ID
 * @returns {Promise}
 */
export function deleteReview(id) {
  return request({
    url: `/api/user/review/${id}`,
    method: 'delete'
  })
}

/**
 * 获取房型评价列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRoomTypeReviews(params) {
  return request({
    url: '/api/room/reviews',
    method: 'get',
    params
  })
}

export default {
  submitReview,
  getReviewList,
  deleteReview,
  getRoomTypeReviews
} 